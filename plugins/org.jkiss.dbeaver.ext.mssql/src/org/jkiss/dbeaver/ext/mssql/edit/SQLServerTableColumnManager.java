/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.mssql.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mssql.SQLServerUtils;
import org.jkiss.dbeaver.ext.mssql.model.*;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableColumnManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * SQLServer table column manager
 */
public class SQLServerTableColumnManager extends SQLTableColumnManager<SQLServerTableColumn, SQLServerTableBase> implements DBEObjectRenamer<SQLServerTableColumn> {

    protected final ColumnModifier<SQLServerTableColumn> IdentityModifier = (monitor, column, sql, command) -> {
        if (column.isAutoGenerated()) {
            try {
                SQLServerTableColumn.IdentityInfo identityInfo = column.getIdentityInfo(monitor);
                long incrementValue = identityInfo.getIncrementValue();
                if (incrementValue <= 0) incrementValue = 1;
                sql.append(" IDENTITY(").append(identityInfo.getSeedValue()).append(",").append(incrementValue).append(")");
            } catch (DBCException e) {
                log.error("Error reading identity information", e);
            }
        }
    };

    protected final ColumnModifier<SQLServerTableColumn> CollateModifier = (monitor, column, sql, command) -> {
        String collationName = column.getCollationName();
        if (!CommonUtils.isEmpty(collationName)) {
            sql.append(" COLLATE ").append(collationName);
        }
    };

    protected final ColumnModifier<SQLServerTableColumn> SQLServerDefaultModifier = (monitor, column, sql, command) -> {
        if (!column.isPersisted()) {
            DefaultModifier.appendModifier(monitor, column, sql, command);
        }
    };

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, SQLServerTableColumn> getObjectsCache(SQLServerTableColumn object)
    {
        return object.getParentObject().getContainer().getTableCache().getChildrenCache(object.getParentObject());
    }

    protected ColumnModifier[] getSupportedModifiers(SQLServerTableColumn column, Map<String, Object> options)
    {
        return new ColumnModifier[] {DataTypeModifier, IdentityModifier, CollateModifier, SQLServerDefaultModifier, NullNotNullModifier};
    }

    @Override
    protected SQLServerTableColumn createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options)
    {
        SQLServerTable table = (SQLServerTable) container;

        DBSDataType columnType = findBestDataType(table.getDataSource(), "varchar"); //$NON-NLS-1$

        final SQLServerTableColumn column = new SQLServerTableColumn(table);
        column.setName(getNewColumnName(monitor, context, table));
        column.setDataType((SQLServerDataType) columnType);
        column.setTypeName(columnType == null ? "varchar" : columnType.getName()); //$NON-NLS-1$
        column.setMaxLength(columnType != null && columnType.getDataKind() == DBPDataKind.STRING ? 100 : 0);
        column.setValueType(columnType == null ? Types.VARCHAR : columnType.getTypeID());
        column.setOrdinalPosition(-1);
        return column;
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options)
    {
        final SQLServerTableColumn column = command.getObject();
        int totalProps = command.getProperties().size();
        boolean hasComment = command.getProperty(DBConstants.PROP_ID_DESCRIPTION) != null;
        if (hasComment) totalProps--;
        if (column.isPersisted() && command.hasProperty("defaultValue")) {
            totalProps--;

            // [Re]create default constraint. Classic MS-style pain in the ass
            String oldConstraintName = null;
            try (JDBCSession session = DBUtils.openMetaSession(monitor, column, "Read default constraint")) {
                oldConstraintName = JDBCUtils.queryString(session, "SELECT name FROM " +
                    SQLServerUtils.getSystemTableName(column.getTable().getDatabase(), "DEFAULT_CONSTRAINTS") +
                    " WHERE PARENT_OBJECT_ID = ? AND PARENT_COLUMN_ID = ?", column.getTable().getObjectId(), column.getObjectId());
            } catch (SQLException e) {
                log.error(e);
            }

            if (oldConstraintName != null) {
                actionList.add(new SQLDatabasePersistAction("Drop default constraint",
                "ALTER TABLE " + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + " DROP CONSTRAINT " + DBUtils.getQuotedIdentifier(column.getDataSource(), oldConstraintName)  //$NON-NLS-1$
                    ));
            }

            String defaultValue = column.getDefaultValue();
            if (!CommonUtils.isEmpty(defaultValue)) {
                StringBuilder sql = new StringBuilder();
                sql.append("ALTER TABLE ").append(column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL)).append(" ADD ");
                DefaultModifier.appendModifier(monitor, column, sql, command);
                sql.append(" FOR ").append(DBUtils.getQuotedIdentifier(column));
                actionList.add(new SQLDatabasePersistAction("Alter default value", sql.toString())); //$NON-NLS-1$
            }
        }
        if (hasComment) {
            boolean isUpdate = SQLServerUtils.isCommentSet(
                monitor,
                column.getTable().getDatabase(),
                SQLServerObjectClass.OBJECT_OR_COLUMN,
                column.getTable().getObjectId(),
                column.getObjectId());
            actionList.add(
                new SQLDatabasePersistAction(
                    "Add column comment",
                    "EXEC " + SQLServerUtils.getSystemTableName(column.getTable().getDatabase(), isUpdate ? "sp_updateextendedproperty" : "sp_addextendedproperty") +
                        " 'MS_Description', " + SQLUtils.quoteString(command.getObject(), command.getObject().getDescription()) + "," +
                        " 'user', '" + column.getTable().getSchema().getName() + "'," +
                        " 'table', '" + column.getTable().getName() + "'," +
                        " 'column', '" + column.getName() + "'"));
        }
        if (totalProps > 0) {
            actionList.add(new SQLDatabasePersistAction(
                "Modify column",
                "ALTER TABLE " + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + //$NON-NLS-1$
                    " ALTER COLUMN " + getNestedDeclaration(monitor, column.getTable(), command, options))); //$NON-NLS-1$
        }
    }

    @Override
    public void renameObject(DBECommandContext commandContext, SQLServerTableColumn object, String newName) throws DBException {
        processObjectRename(commandContext, object, newName);
    }

    @Override
    protected void addObjectRenameActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectRenameCommand command, Map<String, Object> options)
    {
        final SQLServerTableColumn column = command.getObject();

        actions.add(
            new SQLDatabasePersistAction(
                "Rename column",
                    "EXEC " + SQLServerUtils.getSystemTableName(column.getTable().getDatabase(), "sp_rename") +
                    " '" + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DML) + "." + DBUtils.getQuotedIdentifier(column.getDataSource(), command.getOldName()) +
                    "' , '" + DBUtils.getQuotedIdentifier(column.getDataSource(), command.getNewName()) + "', 'COLUMN'")
        );
    }

}
