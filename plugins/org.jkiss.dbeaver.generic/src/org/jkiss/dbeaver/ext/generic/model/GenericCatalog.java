/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.generic.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.GenericConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectSelector;
import org.jkiss.dbeaver.model.struct.rdb.DBSCatalog;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;
import java.util.List;

/**
 * GenericCatalog
 */
public class GenericCatalog extends GenericObjectContainer implements DBSCatalog, DBSObjectSelector
{
    private String catalogName;
    private List<GenericSchema> schemas;
    private boolean isInitialized = false;

    public GenericCatalog(GenericDataSource dataSource, String catalogName)
    {
        super(dataSource);
        this.catalogName = catalogName;
    }

    @Override
    public GenericCatalog getCatalog()
    {
        return this;
    }

    @Override
    public GenericSchema getSchema()
    {
        return null;
    }

    @Override
    public GenericCatalog getObject()
    {
        return this;
    }

    public Collection<GenericSchema> getSchemas(DBRProgressMonitor monitor)
        throws DBException
    {
        if (schemas == null && !isInitialized) {
            JDBCSession session = this.getDataSource().openSession(monitor, DBCExecutionPurpose.META, "Load catalog schemas");
            try {
                this.schemas = this.getDataSource().loadSchemas(session, this);
                this.isInitialized = true;
            }
            finally {
                session.close();
            }
        }
        return schemas;
    }

    public GenericSchema getSchema(DBRProgressMonitor monitor, String name)
        throws DBException
    {
        return DBUtils.findObject(getSchemas(monitor), name);
    }

    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return catalogName;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public DBSObject getParentObject()
    {
        return getDataSource().getContainer();
    }

    @Override
    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException
    {
        if (CommonUtils.isEmpty(getSchemas(monitor))) {
            // Cache tables only if we don't have schemas
            super.cacheStructure(monitor, scope);
        }
    }

    @Override
    public Collection<? extends DBSObject> getChildren(DBRProgressMonitor monitor)
        throws DBException
    {
        if (!CommonUtils.isEmpty(getSchemas(monitor))) {
            return getSchemas(monitor);
        } else {
            return getTables(monitor);
        }
    }

    @Override
    public DBSObject getChild(DBRProgressMonitor monitor, String childName)
        throws DBException
    {
        if (!CommonUtils.isEmpty(getSchemas(monitor))) {
            return getSchema(monitor, childName);
        } else {
            return super.getChild(monitor, childName);
        }
    }

    @Override
    public Class<? extends DBSObject> getChildType(DBRProgressMonitor monitor)
        throws DBException
    {
        if (!CommonUtils.isEmpty(getSchemas(monitor))) {
            return GenericSchema.class;
        } else {
            return GenericTable.class;
        }
    }

    @Override
    public boolean refreshObject(DBRProgressMonitor monitor) throws DBException {
        super.refreshObject(monitor);
        this.schemas = null;
        this.isInitialized = false;
        return true;
    }

    @Override
    public boolean supportsObjectSelect()
    {
        return GenericConstants.ENTITY_TYPE_SCHEMA.equals(getDataSource().getSelectedEntityType()) &&
            !CommonUtils.isEmpty(schemas);
    }

    @Override
    public GenericSchema getSelectedObject()
    {
        return DBUtils.findObject(schemas, getDataSource().getSelectedEntityName());
    }

    @Override
    public void selectObject(DBRProgressMonitor monitor, DBSObject object) throws DBException
    {
        final GenericSchema oldSelectedEntity = getSelectedObject();
        // Check removed because we can select the same object on invalidate
//        if (object == oldSelectedEntity) {
//            return;
//        }
        if (!(object instanceof GenericSchema)) {
            throw new DBException("Bad child type: " + object);
        }
        if (!schemas.contains(GenericSchema.class.cast(object))) {
            throw new DBException("Wrong child object specified as active: " + object);
        }

        getDataSource().setActiveEntityName(monitor, object);

        if (oldSelectedEntity != null) {
            DBUtils.fireObjectSelect(oldSelectedEntity, false);
        }
        DBUtils.fireObjectSelect(object, true);
    }
}
