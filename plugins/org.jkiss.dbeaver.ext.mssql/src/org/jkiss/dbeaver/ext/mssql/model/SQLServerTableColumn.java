/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ext.mssql.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPOrderedObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;

import java.sql.ResultSet;

/**
 * SQLServerTableColumn
 */
public class SQLServerTableColumn extends JDBCTableColumn<SQLServerTableBase> implements DBSTableColumn, DBPNamedObject2, DBPOrderedObject
{
    private static final Log log = Log.getLog(SQLServerTableColumn.class);

    private String comment;
    private String fullTypeName;

    public SQLServerTableColumn(SQLServerTableBase table)
    {
        super(table, false);
    }

    public SQLServerTableColumn(
        SQLServerTableBase table,
        ResultSet dbResult)
        throws DBException
    {
        super(table, true);
        loadInfo(dbResult);
    }

    // Copy constructor
    public SQLServerTableColumn(
        DBRProgressMonitor monitor, SQLServerTableBase table,
        DBSEntityAttribute source)
        throws DBException
    {
        super(table, source, false);
        this.comment = source.getDescription();
        if (source instanceof SQLServerTableColumn) {
            SQLServerTableColumn mySource = (SQLServerTableColumn)source;
            // Copy
        }
    }

    private void loadInfo(ResultSet dbResult)
        throws DBException
    {
        setName(JDBCUtils.safeGetString(dbResult, "name"));
        setOrdinalPosition(JDBCUtils.safeGetInt(dbResult, "column_id"));
        String typeName = "?";//JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_DATA_TYPE);
        setTypeName(typeName);
        //setValueType(SQLServerUtils.typeNameToValueType(typeName));
        DBSDataType dataType = getDataSource().getLocalDataType(typeName);
        //this.comment = JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_COLUMN_COMMENT);
        setRequired(JDBCUtils.safeGetInt(dbResult, "is_nullable") != 0);
        setScale(JDBCUtils.safeGetInteger(dbResult, "scale"));
        setPrecision(JDBCUtils.safeGetInteger(dbResult, "precision"));
/*
        String defaultValue = JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_COLUMN_DEFAULT);
        if (defaultValue != null) {
            switch (getDataKind()) {
                case STRING:
                    // Escape if it is not NULL (#1913)
                    // Although I didn't reproduce that locally - perhaps depends on server config.
                    if (!SQLConstants.NULL_VALUE.equals(defaultValue) && !SQLUtils.isStringQuoted(defaultValue)) {
                        defaultValue = SQLUtils.quoteString(getDataSource(), defaultValue);
                    }
                    break;
                case DATETIME:
                    if (!defaultValue.isEmpty() && Character.isDigit(defaultValue.charAt(0))) {
                        defaultValue = "'" + defaultValue + "'";
                    }
                    break;

            }
            setDefaultValue(defaultValue);
        }
        this.collation = getDataSource().getCollation(JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_COLLATION_NAME));

        this.extraInfo = JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_COLUMN_EXTRA);
        this.autoGenerated = extraInfo != null && extraInfo.contains(SQLServerConstants.EXTRA_AUTO_INCREMENT);

        this.fullTypeName = JDBCUtils.safeGetString(dbResult, SQLServerConstants.COL_COLUMN_TYPE);
        if (!CommonUtils.isEmpty(fullTypeName) && (isTypeEnum() || isTypeSet())) {
            enumValues = parseEnumValues(fullTypeName);
        }
*/
    }

    @NotNull
    @Override
    public SQLServerDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Property(viewable = true, editable = true, updatable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    public String getFullTypeName() {
        return fullTypeName;
    }

    public void setFullTypeName(String fullTypeName) {
        this.fullTypeName = fullTypeName;
        int divPos = fullTypeName.indexOf('(');
        if (divPos != -1) {
            super.setTypeName(fullTypeName.substring(0, divPos).trim());
        } else {
            super.setTypeName(fullTypeName);
        }
    }

    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

/*
    //@Property(viewable = true, editable = true, updatable = true, order = 40)
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    @Override
    //@Property(viewable = true, order = 41)
    public Integer getScale()
    {
        return super.getScale();
    }

    @Override
    //@Property(viewable = true, order = 42)
    public Integer getPrecision()
    {
        return super.getPrecision();
    }

    @Property(viewable = true, editable = true, updatable = true, order = 50)
    @Override
    public boolean isRequired()
    {
        return super.isRequired();
    }

    @Override
    @Property(viewable = true, editable = true, updatable = true, order = 51)
    public boolean isAutoGenerated()
    {
        return autoGenerated;
    }

    @Override
    @Property(viewable = true, editable = true, updatable = true, order = 70)
    public String getDefaultValue()
    {
        return super.getDefaultValue();
    }
*/


    @Property(viewable = true, editable = true, updatable = true, multiline = true, order = 100)
    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Nullable
    @Override
    public String getDescription() {
        return getComment();
    }

}
