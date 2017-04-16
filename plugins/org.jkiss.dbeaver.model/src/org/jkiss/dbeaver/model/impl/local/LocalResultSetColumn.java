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

package org.jkiss.dbeaver.model.impl.local;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

/**
 * LocalResultSetColumn
 */
public class LocalResultSetColumn implements DBCAttributeMetaData
{
    private final LocalResultSet resultSet;
    private final int index;
    private final String label;
    private final DBPDataKind dataKind;
    private final DBSTypedObject typedObject;

    public LocalResultSetColumn(LocalResultSet resultSet, int index, String label, DBPDataKind dataKind)
    {
        this.resultSet = resultSet;
        this.index = index;
        this.label = label;
        this.dataKind = dataKind;
        this.typedObject = null;
    }

    public LocalResultSetColumn(LocalResultSet resultSet, int index, String label, DBSTypedObject typedObject)
    {
        this.resultSet = resultSet;
        this.index = index;
        this.label = label;
        this.dataKind = typedObject.getDataKind();
        this.typedObject = typedObject;
    }

    @Property(viewable = true, order = 1)
    @Override
    public int getOrdinalPosition()
    {
        return index;
    }

    @Nullable
    @Override
    public Object getSource() {
        return null;
    }

    @Property(viewable = true, order = 2)
    @NotNull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Property(viewable = true, order = 3)
    @Nullable
    @Override
    public String getEntityName()
    {
        return null;
    }

    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    @Nullable
    @Override
    public DBCEntityMetaData getEntityMetaData()
    {
        return null;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Property(viewable = true, order = 4)
    @Override
    public boolean isAutoGenerated() {
        return false;
    }

    @NotNull
    @Override
    public String getName()
    {
        return label;
    }

    @Property(viewable = true, order = 5)
    @Override
    public String getTypeName()
    {
        return typedObject == null ?
            DBUtils.getDefaultDataTypeName(resultSet.getSession().getDataSource(), dataKind) :
            typedObject.getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return typedObject == null ? DBUtils.getFullTypeName(this) : typedObject.getFullTypeName();
    }

    @Override
    public int getTypeID()
    {
        return typedObject == null ? 0 : typedObject.getTypeID();
    }

    @Override
    public DBPDataKind getDataKind()
    {
        return dataKind;
    }

    @Override
    public int getScale()
    {
        return typedObject == null ? 0 : typedObject.getScale();
    }

    @Override
    public int getPrecision()
    {
        return typedObject == null ? 0 : typedObject.getPrecision();
    }

    @Override
    public long getMaxLength()
    {
        return typedObject == null ? 0 : typedObject.getMaxLength();
    }

}
