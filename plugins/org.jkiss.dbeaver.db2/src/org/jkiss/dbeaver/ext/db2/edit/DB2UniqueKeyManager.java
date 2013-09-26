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
import org.jkiss.dbeaver.ext.db2.model.DB2TableKeyColumn;
import org.jkiss.dbeaver.ext.db2.model.DB2TableUniqueKey;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.JDBCConstraintManager;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.dialogs.struct.EditConstraintDialog;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DB2 Unique Keys Manager
 * 
 * @author Denis Forveille
 */
public class DB2UniqueKeyManager extends JDBCConstraintManager<DB2TableUniqueKey, DB2Table> {

    private static final String SQL_DROP_PK = "ALTER TABLE %s DROP PRIMARY_KEY ";
    private static final String SQL_DROP_UK = "ALTER TABLE %s DROP UNIQUE %s";

    private static final String CONS_PK_SUF = "_PK";
    private static final String CONS_UK_SUF = "_UK";

    private static final DBSEntityConstraintType[] CONS_TYPES = { DBSEntityConstraintType.PRIMARY_KEY,
        DBSEntityConstraintType.UNIQUE_KEY };

    // -----------------
    // Business Contract
    // -----------------

    @Override
    public DBSObjectCache<? extends DBSObject, DB2TableUniqueKey> getObjectsCache(DB2TableUniqueKey object)
    {
        return object.getParentObject().getSchema().getConstraintCache();
    }

    // ------
    // Create
    // ------

    @Override
    public DB2TableUniqueKey createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, DB2Table db2Table,
        Object from)
    {
        EditConstraintDialog editDialog =
            new EditConstraintDialog(workbenchWindow.getShell(), DB2Messages.edit_db2_constraint_manager_dialog_title, db2Table,
                CONS_TYPES);
        if (editDialog.open() != IDialogConstants.OK_ID) {
            return null;
        }

        String suffix;
        DBSEntityConstraintType type = editDialog.getConstraintType();
        if (type.equals(DBSEntityConstraintType.PRIMARY_KEY)) {
            suffix = CONS_PK_SUF;
        } else {
            suffix = CONS_UK_SUF;
        }

        DB2TableUniqueKey constraint = new DB2TableUniqueKey(db2Table, editDialog.getConstraintType());

        String constraintName =
            DBObjectNameCaseTransformer.transformName(constraint, CommonUtils.escapeIdentifier(db2Table.getName()) + suffix);
        constraint.setName(constraintName);

        List<DB2TableKeyColumn> columns = new ArrayList<DB2TableKeyColumn>(editDialog.getSelectedColumns().size());
        DB2TableKeyColumn column;
        int colIndex = 1;
        for (DBSEntityAttribute tableColumn : editDialog.getSelectedColumns()) {
            column = new DB2TableKeyColumn(constraint, (DB2TableColumn) tableColumn, colIndex++);
            columns.add(column);
        }
        constraint.setColumns(columns);

        return constraint;
    }

    // ------
    // DROP
    // ------

    @Override
    public String getDropConstraintPattern(DB2TableUniqueKey constraint)
    {
        String tablename = constraint.getTable().getFullQualifiedName();
        if (constraint.getConstraintType().equals(DBSEntityConstraintType.PRIMARY_KEY)) {
            return String.format(SQL_DROP_PK, tablename);
        } else {
            return String.format(SQL_DROP_UK, tablename, constraint.getName());
        }
    }
}
