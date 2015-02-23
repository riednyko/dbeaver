/*
 * Copyright (C) 2010-2015 Serge Rieder
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
package org.jkiss.dbeaver.ext.mssql.model.plan;

import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * MSSQL execution plan node
 */
public class MSSQLPlanNode implements DBCPlanNode {

    private long id;
    private String selectType;
    private String table;
    private String type;
    private String possibleKeys;
    private String key;
    private String keyLength;
    private String ref;
    private long rowCount;
    private double filtered;
    private String extra;

    private MSSQLPlanNode parent;
    private List<MSSQLPlanNode> nested;

    public MSSQLPlanNode(MSSQLPlanNode parent, ResultSet dbResult) throws SQLException
    {
        this.parent = parent;
        this.id = JDBCUtils.safeGetLong(dbResult, "id");
        this.selectType = JDBCUtils.safeGetString(dbResult, "select_type");
        this.table = JDBCUtils.safeGetString(dbResult, "table");
        this.type = JDBCUtils.safeGetString(dbResult, "type");
        this.possibleKeys = JDBCUtils.safeGetString(dbResult, "possible_keys");
        this.key = JDBCUtils.safeGetString(dbResult, "key");
        this.keyLength = JDBCUtils.safeGetString(dbResult, "key_len");
        this.ref = JDBCUtils.safeGetString(dbResult, "ref");
        this.rowCount = JDBCUtils.safeGetLong(dbResult, "rows");
        this.filtered = JDBCUtils.safeGetDouble(dbResult, "filtered");
        this.extra = JDBCUtils.safeGetString(dbResult, "extra");
    }

    @Override
    public DBCPlanNode getParent()
    {
        return parent;
    }

    @Override
    public List<MSSQLPlanNode> getNested()
    {
        return nested;
    }

    @Property(order = 0, viewable = true)
    public long getId()
    {
        return id;
    }

    @Property(order = 1, viewable = true)
    public String getSelectType()
    {
        return selectType;
    }

    @Property(order = 2, viewable = true)
    public String getTable()
    {
        return table;
    }

    @Property(order = 3, viewable = true)
    public String getType()
    {
        return type;
    }

    @Property(order = 4, viewable = true)
    public String getPossibleKeys()
    {
        return possibleKeys;
    }

    @Property(order = 5, viewable = true)
    public String getKey()
    {
        return key;
    }

    @Property(order = 6, viewable = true)
    public String getKeyLength()
    {
        return keyLength;
    }

    @Property(order = 7, viewable = true)
    public String getRef()
    {
        return ref;
    }

    @Property(order = 8, viewable = true)
    public long getRowCount()
    {
        return rowCount;
    }

    @Property(order = 9, viewable = true)
    public double getFiltered()
    {
        return filtered;
    }

    @Property(order = 10, viewable = true)
    public String getExtra()
    {
        return extra;
    }

    @Override
    public String toString() {
        return table + " " + type + " " + key;
    }
}
