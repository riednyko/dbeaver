/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.edit;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.ext.db2.DB2Messages;
import org.jkiss.dbeaver.ext.db2.model.DB2Table;
import org.jkiss.dbeaver.ext.db2.model.DB2TableColumn;
import org.jkiss.dbeaver.ext.db2.model.DB2TableForeignKey;
import org.jkiss.dbeaver.ext.db2.model.DB2TableKeyColumn;
import org.jkiss.dbeaver.ext.db2.model.DB2TableUniqueKey;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.JDBCForeignKeyManager;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.ui.dialogs.struct.EditForeignKeyDialog;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DB2 Foreign key Manager
 * 
 * @author Denis Forveille
 */
public class DB2ForeignKeyManager extends JDBCForeignKeyManager<DB2TableForeignKey, DB2Table> {

    private static final String SQL_DROP_FK = "ALTER TABLE %s DROP FOREIGN KEY %s";

    private static final String CONS_FK_NAME = "%s_%s_FK";

    private static final DBSForeignKeyModifyRule[] FK_RULES = { DBSForeignKeyModifyRule.NO_ACTION, DBSForeignKeyModifyRule.CASCADE,
        DBSForeignKeyModifyRule.RESTRICT, DBSForeignKeyModifyRule.SET_NULL, DBSForeignKeyModifyRule.SET_DEFAULT };

    // -----------------
    // Business Contract
    // -----------------

    @Override
    public DBSObjectCache<? extends DBSObject, DB2TableForeignKey> getObjectsCache(DB2TableForeignKey object)
    {
        return object.getParentObject().getSchema().getAssociationCache();
    }

    // ------
    // Create
    // ------
    @Override
    public DB2TableForeignKey createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, DB2Table db2Table,
        Object from)
    {
        EditForeignKeyDialog editDialog =
            new EditForeignKeyDialog(workbenchWindow.getShell(), DB2Messages.edit_db2_foreign_key_manager_dialog_title, db2Table,
                FK_RULES);
        if (editDialog.open() != IDialogConstants.OK_ID) {
            return null;
        }

        DBSForeignKeyModifyRule deleteRule = editDialog.getOnDeleteRule();
        DBSForeignKeyModifyRule updateRule = editDialog.getOnUpdateRule();
        DB2TableUniqueKey ukConstraint = (DB2TableUniqueKey) editDialog.getUniqueConstraint();

        String tableName = CommonUtils.escapeIdentifier(db2Table.getName());
        String targetTableName = CommonUtils.escapeIdentifier(editDialog.getUniqueConstraint().getParentObject().getName());

        DB2TableForeignKey foreignKey = new DB2TableForeignKey(db2Table, ukConstraint, deleteRule, updateRule);

        String fkBaseName = String.format(CONS_FK_NAME, tableName, targetTableName);
        String fkName = DBObjectNameCaseTransformer.transformName(foreignKey, fkBaseName);

        foreignKey.setName(fkName);

        List<DB2TableKeyColumn> columns = new ArrayList<DB2TableKeyColumn>(editDialog.getColumns().size());
        DB2TableKeyColumn column;
        int colIndex = 1;
        for (EditForeignKeyDialog.FKColumnInfo tableColumn : editDialog.getColumns()) {
            column = new DB2TableKeyColumn(foreignKey, (DB2TableColumn) tableColumn.getOwnColumn(), colIndex++);
            columns.add(column);
        }

        foreignKey.setColumns(columns);

        return foreignKey;
    }

    // ------
    // Drop
    // ------
    @Override
    public String getDropForeignKeyPattern(DB2TableForeignKey foreignKey)
    {
        String tableName = foreignKey.getTable().getFullQualifiedName();
        return String.format(SQL_DROP_FK, tableName, foreignKey.getName());
    }

}
