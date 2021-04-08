/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
package org.jkiss.dbeaver.model.impl.jdbc.struct;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.data.DBDLabelValuePair;
import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.model.impl.DBDummyNumberTransformer;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.meta.IPropertyValueListProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.sql.SQLExpressionFormatter;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;
import org.jkiss.dbeaver.model.virtual.DBVUtils;
import org.jkiss.utils.CommonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * JDBC abstract table column
 */
public abstract class JDBCTableColumn<TABLE_TYPE extends DBSEntity> extends JDBCAttribute implements DBSTableColumn, DBSEntityAttribute, DBSAttributeEnumerable, DBPSaveableObject {

    private static final Log log = Log.getLog(JDBCTableColumn.class);

    private final TABLE_TYPE table;
    private boolean persisted;
    private String defaultValue;

    protected JDBCTableColumn(TABLE_TYPE table, boolean persisted)
    {
        this.table = table;
        this.persisted = persisted;
    }

    protected JDBCTableColumn(
            TABLE_TYPE table,
            boolean persisted,
            String name,
            String typeName,
            int valueType,
            int ordinalPosition,
            long maxLength,
            Integer scale,
            Integer precision,
            boolean required,
            boolean autoGenerated,
            String defaultValue)
    {
        super(name, typeName, valueType, ordinalPosition, maxLength, scale, precision, required, autoGenerated);
        this.defaultValue = defaultValue;
        this.table = table;
        this.persisted = persisted;
    }

    protected JDBCTableColumn(
        TABLE_TYPE table,
        DBSEntityAttribute source,
        boolean persisted)
    {
        super(source);
        this.table = table;
        this.persisted = persisted;
        this.defaultValue = source.getDefaultValue();
    }

    public TABLE_TYPE getTable()
    {
        return table;
    }

    @NotNull
    @Override
    public TABLE_TYPE getParentObject()
    {
        return getTable();
    }

    @NotNull
    @Property(viewable = true, editable = true, valueTransformer = DBObjectNameCaseTransformer.class, order = 10)
    @Override
    public String getName()
    {
        return super.getName();
    }

    @Property(viewable = true, editable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

    @Override
    public void setTypeName(String typeName) {
        super.setTypeName(typeName);
        final DBPDataTypeProvider dataTypeProvider = DBUtils.getParentOfType(DBPDataTypeProvider.class, this);
        if (dataTypeProvider != null) {
            DBSDataType dataType = dataTypeProvider.getLocalDataType(typeName);
            if (dataType != null) {
                this.valueType = dataType.getTypeID();
                if (this instanceof DBSTypedObjectExt4) {
                    try {
                        ((DBSTypedObjectExt4) this).setDataType(dataType);
                    } catch (Throwable e) {
                        log.debug(e);
                    }
                }
            } else {
                this.valueType = -1;
            }
        }
    }

    @Property(viewable = true, editable = true, order = 40, valueRenderer = DBDummyNumberTransformer.class)
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    @Property(viewable = true, editable = true, order = 50)
    @Override
    public boolean isRequired()
    {
        return super.isRequired();
    }

    @Property(viewable = true, editable = true, order = 70)
    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean isPersisted()
    {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted)
    {
        this.persisted = persisted;
    }

    @NotNull
    @Override
    public List<DBDLabelValuePair> getValueEnumeration(
        @NotNull DBCSession session,
        @Nullable Object valuePattern,
        int maxResults,
        boolean calcCount,
        boolean formatValues,
        boolean caseInsensitiveSearch) throws DBException
    {
        final String identifier = DBUtils.getQuotedIdentifier(this);
        DBDValueHandler valueHandler = DBUtils.findValueHandler(session, this);
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if (!calcCount) {
            query.append("DISTINCT ");
        }
        query.append(identifier);
        if (calcCount) {
            query.append(", count(*)");
        } else {
            query.append(", NULL");
        }
        // Do not use description columns because they duplicate distinct value
//        String descColumns = DBVUtils.getDictionaryDescriptionColumns(session.getProgressMonitor(), this);
//        if (descColumns != null) {
//            query.append(", ").append(descColumns);
//        }
        query.append("\nFROM ").append(DBUtils.getObjectFullName(getTable(), DBPEvaluationContext.DML));
        if (valuePattern instanceof String) {
            query.append("\nWHERE ");
            if (getDataKind() == DBPDataKind.STRING) {
                final SQLDialect dialect = getDataSource().getSQLDialect();
                final SQLExpressionFormatter caseInsensitiveFormatter = caseInsensitiveSearch
                    ? dialect.getCaseInsensitiveExpressionFormatter(DBCLogicalOperator.LIKE)
                    : null;
                if (caseInsensitiveSearch && caseInsensitiveFormatter != null) {
                    query.append(caseInsensitiveFormatter.format(identifier, "?"));
                } else {
                    query.append(identifier).append(" LIKE ?");
                }

            } else {
                query.append(identifier).append(" = ?");
            }
        }
        if (calcCount) {
            query.append("\nGROUP BY ").append(identifier);
            query.append("\nORDER BY 2 DESC");
        } else {
            query.append("\nORDER BY 1");
        }

        try (DBCStatement dbStat = session.prepareStatement(DBCStatementType.QUERY, query.toString(), false, false, false)) {
            if (valuePattern instanceof String) {
                if (getDataKind() == DBPDataKind.STRING) {
                    valueHandler.bindValueObject(session, dbStat, this, 0, "%" + valuePattern + "%");
                } else {
                    valueHandler.bindValueObject(session, dbStat, this, 0, valuePattern);
                }
            }
            dbStat.setLimit(0, maxResults);
            if (dbStat.executeStatement()) {
                try (DBCResultSet dbResult = dbStat.openResultSet()) {
                    return DBVUtils.readDictionaryRows(session, this, valueHandler, dbResult, formatValues, calcCount);
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    public static class ColumnTypeNameListProvider implements IPropertyValueListProvider<JDBCTableColumn<?>> {

        @Override
        public boolean allowCustomValue()
        {
            return true;
        }

        @Override
        public Object[] getPossibleValues(JDBCTableColumn<?> column)
        {
            Set<String> typeNames = new TreeSet<>();
            if (column.getDataSource() instanceof DBPDataTypeProvider) {
                for (DBSDataType type : ((DBPDataTypeProvider) column.getDataSource()).getLocalDataTypes()) {
                    if (type.getDataKind() != DBPDataKind.UNKNOWN && !CommonUtils.isEmpty(type.getName()) && Character.isLetter(type.getName().charAt(0))) {
                        typeNames.add(type.getName());
                    }
                }
            }
            return typeNames.toArray(new String[0]);
        }
    }

}
