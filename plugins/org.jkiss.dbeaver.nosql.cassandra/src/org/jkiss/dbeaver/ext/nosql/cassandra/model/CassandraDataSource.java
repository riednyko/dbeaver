/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.ext.nosql.cassandra.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabaseTermProvider;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCConnector;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCBasicDataTypeCache;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CassandraDataSource
 */
public class CassandraDataSource extends JDBCDataSource
    implements DBPDataSource, JDBCConnector, DBSObjectSelector, IDatabaseTermProvider, IAdaptable
{
    static final Log log = LogFactory.getLog(CassandraDataSource.class);

    private final JDBCBasicDataTypeCache dataTypeCache;
    private List<CassandraKeyspace> keyspaces;
    private String selectedKeyspace;

    public CassandraDataSource(DBSDataSourceContainer container)
        throws DBException
    {
        super(container);
        this.dataTypeCache = new JDBCBasicDataTypeCache(container);
    }

    public Collection<CassandraKeyspace> getKeyspaces()
    {
        return keyspaces;
    }

    public CassandraKeyspace getSchema(String name)
    {
        return DBUtils.findObject(getKeyspaces(), name);
    }

    @Override
    public CassandraDataSource getDataSource() {
        return this;
    }

    @Override
    public void initialize(DBRProgressMonitor monitor)
        throws DBException
    {
        super.initialize(monitor);
        try {
            dataTypeCache.getObjects(monitor, this);
        } catch (DBException e) {
            log.warn("Can't fetch data types", e);
        }
        JDBCExecutionContext context = openContext(monitor, DBCExecutionPurpose.META, "Read cassandra metadata");
        try {
            // Read metadata
            JDBCDatabaseMetaData metaData = context.getMetaData();
            // Catalogs not supported - try to read root keyspaces
            monitor.subTask("Extract keyspaces");
            monitor.worked(1);
            List<CassandraKeyspace> tmpSchemas = loadKeyspaces(context);
            if (tmpSchemas != null) {
                this.keyspaces = tmpSchemas;
            }
            // Get selected entity (catalog or schema)
            selectedKeyspace = context.getSchema();

        } catch (SQLException ex) {
            throw new DBException("Error reading metadata", ex);
        }
        finally {
            context.close();
        }
    }

    List<CassandraKeyspace> loadKeyspaces(JDBCExecutionContext context)
        throws DBException
    {
        try {
            DBSObjectFilter ksFilters = getContainer().getObjectFilter(CassandraKeyspace.class, null);

            List<CassandraKeyspace> tmpKeyspaces = new ArrayList<CassandraKeyspace>();
            JDBCResultSet dbResult;
            try {
                dbResult = context.getMetaData().getSchemas(
                    null,
                    ksFilters != null && ksFilters.hasSingleMask() ? ksFilters.getSingleMask() : null);
            } catch (Throwable e) {
                // This method not supported (may be old driver version)
                // Use general schema reading method
                dbResult = context.getMetaData().getSchemas();
            }

            try {
                while (dbResult.next()) {
                    if (context.getProgressMonitor().isCanceled()) {
                        break;
                    }
                    String ksName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TABLE_SCHEM);
                    if (CommonUtils.isEmpty(ksName)) {
                        continue;
                    }
                    if (ksFilters != null && !ksFilters.matches(ksName)) {
                        // Doesn't match filter
                        continue;
                    }
                    context.getProgressMonitor().subTask("Keyspace " + ksName);

                    CassandraKeyspace keyspace = new CassandraKeyspace(this, ksName, dbResult);
                    tmpKeyspaces.add(keyspace);
                }
            } finally {
                dbResult.close();
            }
            return tmpKeyspaces;
        } catch (Exception ex) {
            log.error("Could not read keyspace list", ex);
            return null;
        }
    }

    @Override
    public boolean refreshObject(DBRProgressMonitor monitor)
        throws DBException
    {
        super.refreshObject(monitor);

        this.keyspaces = null;

        this.initialize(monitor);

        return true;
    }

    CassandraColumnFamily findTable(DBRProgressMonitor monitor, String schemaName, String tableName)
        throws DBException
    {
        if (!CommonUtils.isEmpty(schemaName)) {
            CassandraKeyspace container = this.getSchema(schemaName);
            if (container == null) {
                log.error("Schema '" + schemaName + "' not found");
            } else {
                return container.getChild(monitor, tableName);
            }
        }
        return null;
    }

    @Override
    public Collection<? extends DBSObject> getChildren(DBRProgressMonitor monitor)
        throws DBException
    {
        return getKeyspaces();
    }

    @Override
    public DBSObject getChild(DBRProgressMonitor monitor, String childName)
        throws DBException
    {
        return getSchema(childName);
    }

    @Override
    public Class<? extends DBSObject> getChildType(DBRProgressMonitor monitor)
        throws DBException
    {
        return CassandraKeyspace.class;
    }

    @Override
    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException {
        for (CassandraKeyspace schema : keyspaces) {
            schema.cacheStructure(monitor, scope);
        }
    }

    public boolean isChild(DBSObject object)
        throws DBException
    {
        return !CommonUtils.isEmpty(keyspaces) &&
            object instanceof CassandraKeyspace &&
            keyspaces.contains(CassandraKeyspace.class.cast(object));
    }

    @Override
    public boolean supportsObjectSelect()
    {
        return true;
    }

    @Override
    public DBSObject getSelectedObject()
    {
        return getSchema(selectedKeyspace);
    }

    @Override
    public void selectObject(DBRProgressMonitor monitor, DBSObject object)
        throws DBException
    {
        final DBSObject oldSelectedEntity = getSelectedObject();
        if (object == oldSelectedEntity) {
            return;
        }
        if (!isChild(object)) {
            throw new DBException("Bad child object specified as active: " + object);
        }

        setActiveEntityName(monitor, object);

        if (oldSelectedEntity != null) {
            DBUtils.fireObjectSelect(oldSelectedEntity, false);
        }
        DBUtils.fireObjectSelect(object, true);
    }

    void setActiveEntityName(DBRProgressMonitor monitor, DBSObject entity) throws DBException
    {
        if (entity instanceof CassandraKeyspace) {
            JDBCExecutionContext context = openContext(monitor, DBCExecutionPurpose.UTIL, "Set active catalog");
            try {
                context.setSchema(entity.getName());
                selectedKeyspace = entity.getName();
            } catch (SQLException e) {
                throw new DBException(e);
            }
            finally {
                context.close();
            }
        }
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        if (adapter == DBSStructureAssistant.class) {
            return new CassandraStructureAssistant(this);
        } else {
            return null;
        }
    }

    @Override
    public String getObjectTypeTerm(String path, String objectType, boolean multiple)
    {
        String term = null;
        if ("cluster".equals(objectType)) {
            term = "Cluster";
        } else if ("keypace".equals(objectType)) {
            term = "Keyspace";
        }
        if (term != null && multiple) {
            term += "s";
        }
        return term;
    }

    @Override
    public Collection<? extends DBSDataType> getDataTypes()
    {
        return dataTypeCache.getCachedObjects();
    }

    @Override
    public DBSDataType getDataType(String typeName)
    {
        return dataTypeCache.getCachedObject(typeName);
    }

}
