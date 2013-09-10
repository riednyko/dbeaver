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
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableForeignKey;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKey;
import org.jkiss.dbeaver.ui.properties.IPropertyValueListProvider;

import java.sql.ResultSet;

/**
 * OracleTableForeignKey
 */
public class OracleTableForeignKey extends OracleTableConstraintBase implements DBSTableForeignKey
{
    private OracleTableConstraint referencedKey;
    private DBSForeignKeyModifyRule deleteRule;

    public OracleTableForeignKey(
        OracleTableBase oracleTable,
        String name,
        OracleObjectStatus status,
        OracleTableConstraint referencedKey,
        DBSForeignKeyModifyRule deleteRule)
    {
        super(oracleTable, name, DBSEntityConstraintType.FOREIGN_KEY, null, status);
        this.referencedKey = referencedKey;
        this.deleteRule = deleteRule;
    }

    public OracleTableForeignKey(
        DBRProgressMonitor monitor,
        OracleTable table,
        ResultSet dbResult)
        throws DBException
    {
        super(
            table,
            JDBCUtils.safeGetString(dbResult, "CONSTRAINT_NAME"),
            null,
            DBSEntityConstraintType.FOREIGN_KEY,
            true);

        String refName = JDBCUtils.safeGetString(dbResult, "R_CONSTRAINT_NAME");
        String refOwnerName = JDBCUtils.safeGetString(dbResult, "R_OWNER");
        String refTableName = JDBCUtils.safeGetString(dbResult, "R_TABLE_NAME");
        OracleTableBase refTable = OracleTableBase.findTable(
            monitor,
            table.getDataSource(),
            refOwnerName,
            refTableName);
        if (refTable == null) {
            log.warn("Referenced table '" + DBUtils.getSimpleQualifiedName(refOwnerName, refTableName) + "' not found");
        } else {
            referencedKey = refTable.getConstraint(monitor, refName);
            if (referencedKey == null) {
                log.warn("Referenced constraint '" + refName + "' not found in table '" + refTable.getFullQualifiedName() + "'");
            }
        }

        String deleteRuleName = JDBCUtils.safeGetString(dbResult, "DELETE_RULE");
        this.deleteRule = "CASCADE".equals(deleteRuleName) ? DBSForeignKeyModifyRule.CASCADE : DBSForeignKeyModifyRule.NO_ACTION;
    }

    @Property(viewable = true, order = 3)
    public OracleTableBase getReferencedTable()
    {
        return referencedKey.getTable();
    }

    @Override
    @Property(id = "reference", viewable = true, order = 4)
    public OracleTableConstraint getReferencedConstraint()
    {
        return referencedKey;
    }

    @Override
    @Property(viewable = true, editable = true, listProvider = ConstraintModifyRuleListProvider.class, order = 5)
    public DBSForeignKeyModifyRule getDeleteRule()
    {
        return deleteRule;
    }

    // Update rule is not supported by Oracle
    @Override
    public DBSForeignKeyModifyRule getUpdateRule()
    {
        return DBSForeignKeyModifyRule.NO_ACTION;
    }

    @Override
    public OracleTableBase getAssociatedEntity()
    {
        return getReferencedTable();
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            getTable(),
            this);
    }

    public static class ConstraintModifyRuleListProvider implements IPropertyValueListProvider<JDBCTableForeignKey> {

        @Override
        public boolean allowCustomValue()
        {
            return false;
        }

        @Override
        public Object[] getPossibleValues(JDBCTableForeignKey foreignKey)
        {
            return new DBSForeignKeyModifyRule[] {
                DBSForeignKeyModifyRule.NO_ACTION,
                DBSForeignKeyModifyRule.CASCADE,
                DBSForeignKeyModifyRule.RESTRICT,
                DBSForeignKeyModifyRule.SET_NULL,
                DBSForeignKeyModifyRule.SET_DEFAULT };
        }
    }
}
