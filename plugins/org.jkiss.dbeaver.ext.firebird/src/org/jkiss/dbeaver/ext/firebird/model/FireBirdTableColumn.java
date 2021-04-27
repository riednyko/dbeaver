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
package org.jkiss.dbeaver.ext.firebird.model;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.ext.generic.model.GenericTableColumn;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.meta.PropertyLength;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSTypedObjectExt4;

public class FireBirdTableColumn extends GenericTableColumn implements DBPNamedObject2, DBSTypedObjectExt4<FireBirdDataType> {

    private FireBirdDataType dataType;

    public FireBirdTableColumn(GenericTableBase table) {
        super(table);
    }

    public FireBirdTableColumn(DBRProgressMonitor monitor, JDBCResultSet dbResult, GenericTableBase table, String columnName, String typeName, int valueType, int sourceType, int ordinalPosition, long columnSize, long charLength, Integer scale, Integer precision, int radix, boolean notNull, String remarks, String defaultValue, boolean autoIncrement, boolean autoGenerated) throws DBException {
        super(table, columnName, typeName, valueType, sourceType, ordinalPosition, columnSize, charLength, scale, precision, radix, notNull, remarks, defaultValue, autoIncrement, autoGenerated);
        String domainTypeName = getDomainTypeName(monitor);
        if (domainTypeName != null) {
            dataType = (FireBirdDataType) table.getDataSource().getLocalDataType(domainTypeName);
        } else {
            dataType = (FireBirdDataType) table.getDataSource().getLocalDataType(typeName);
        }
    }

    @Override
    public GenericTableBase getTable() {
        return super.getTable();
    }

    @Override
    public DBPDataKind getDataKind() {
        return dataType == null ? super.getDataKind() : dataType.getDataKind();
    }

    @Property(order = 21)
    public String getDomainTypeName(DBRProgressMonitor monitor) throws DBException {
        GenericTableBase table = getTable();
        if (table instanceof FireBirdTableBase) {
            return ((FireBirdTableBase)table).getColumnDomainType(monitor, this);
        }
        return null;
    }

    @Property(order = 22, viewable = true)
    public String getCharset() {
        if (dataType != null) {
            return dataType.getCharsetName();
        }
        return null;
    }

    @Property(viewable = true, editable = true, updatable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

    @Property(viewable = true, editable = true, updatable = true, order = 40)
    @Override
    public long getMaxLength() {
        return super.getMaxLength();
    }

    @Property(viewable = true, editable = true, updatable = true, order = 70)
    @Override
    public String getDefaultValue()
    {
        return super.getDefaultValue();
    }

    @Nullable
    @Override
    @Property(viewable = true, editable = true, updatable = true, length = PropertyLength.MULTILINE, order = 100)
    public String getDescription()
    {
        return super.getDescription();
    }

    @Override
    public boolean isAutoGenerated() {
        return super.isAutoGenerated();
    }

    @Override
    public void setDataType(FireBirdDataType dataType) {
        this.dataType = dataType;
        this.typeName = dataType.getTypeName();
    }
}
