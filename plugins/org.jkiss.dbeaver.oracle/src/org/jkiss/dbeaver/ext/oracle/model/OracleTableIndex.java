/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableIndex;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.LazyProperty;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObjectLazy;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * OracleTableIndex
 */
public class OracleTableIndex extends JDBCTableIndex<OracleSchema, OracleTablePhysical> implements DBSObjectLazy
{

    private Object tablespace;
    private boolean nonUnique;
    private List<OracleTableIndexColumn> columns;

    public OracleTableIndex(
        OracleSchema schema,
        OracleTablePhysical table,
        String indexName,
        ResultSet dbResult)
    {
        super(schema, table, indexName, null, true);
        String indexTypeName = JDBCUtils.safeGetString(dbResult, "INDEX_TYPE");
        this.nonUnique = !"UNIQUE".equals(JDBCUtils.safeGetString(dbResult, "UNIQUENESS"));
        if (OracleConstants.INDEX_TYPE_NORMAL.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_NORMAL;
        } else if (OracleConstants.INDEX_TYPE_BITMAP.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_BITMAP;
        } else if (OracleConstants.INDEX_TYPE_FUNCTION_BASED_NORMAL.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_FUNCTION_BASED_NORMAL;
        } else if (OracleConstants.INDEX_TYPE_FUNCTION_BASED_BITMAP.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_FUNCTION_BASED_BITMAP;
        } else if (OracleConstants.INDEX_TYPE_DOMAIN.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_DOMAIN;
        } else {
            indexType = DBSIndexType.OTHER;
        }
        this.tablespace = JDBCUtils.safeGetString(dbResult, "TABLESPACE_NAME");
    }

    public OracleTableIndex(OracleSchema schema, OracleTablePhysical parent, String name, boolean unique, DBSIndexType indexType)
    {
        super(schema, parent, name, indexType, false);
        this.nonUnique = !unique;

    }

    @NotNull
    @Override
    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Override
    @Property(viewable = true, order = 5)
    public boolean isUnique()
    {
        return !nonUnique;
    }

    @Override
    public Object getLazyReference(Object propertyId)
    {
        return tablespace;
    }

    @Property(viewable = true, order = 10)
    @LazyProperty(cacheValidator = OracleTablespace.TablespaceReferenceValidator.class)
    public Object getTablespace(DBRProgressMonitor monitor) throws DBException
    {
        return OracleTablespace.resolveTablespaceReference(monitor, this, null);
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public Collection<OracleTableIndexColumn> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }

    @Association
    public OracleTableIndexColumn getColumn(String columnName)
    {
        return DBUtils.findObject(columns, columnName);
    }

    void setColumns(List<OracleTableIndexColumn> columns)
    {
        this.columns = columns;
    }

    public void addColumn(OracleTableIndexColumn column)
    {
        if (columns == null) {
            columns = new ArrayList<OracleTableIndexColumn>();
        }
        columns.add(column);
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            this);
    }

    @Override
    public String toString()
    {
        return getFullQualifiedName();
    }
}
