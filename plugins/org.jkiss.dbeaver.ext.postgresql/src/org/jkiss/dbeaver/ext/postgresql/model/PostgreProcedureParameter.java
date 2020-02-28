/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;

/**
 * PostgreProcedureParameter
 */
public class PostgreProcedureParameter implements DBSProcedureParameter, DBSAttributeBase, DBSObject
{
    private PostgreProcedure procedure;
    private String paramName;
    private int ordinalPosition;
    private PostgreDataType dataType;
    private DBSProcedureParameterKind parameterKind;
    private String defaultValue;

    public PostgreProcedureParameter(
        PostgreProcedure procedure,
        String paramName,
        PostgreDataType dataType,
        DBSProcedureParameterKind parameterKind,
        int ordinalPosition)
    {
        this.procedure = procedure;
        this.paramName = paramName;
        this.dataType = dataType;
        this.parameterKind = parameterKind;
        this.ordinalPosition = ordinalPosition;
    }

    @NotNull
    @Override
    public PostgreDataSource getDataSource()
    {
        return procedure.getDataSource();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public PostgreProcedure getParentObject()
    {
        return procedure;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return paramName;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 2)
    public PostgreDataType getParameterType() {
        return dataType;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 3)
    public DBSProcedureParameterKind getParameterKind()
    {
        return parameterKind;
    }

    @Override
    @Property(viewable = true, order = 4)
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isAutoGenerated() {
        return false;
    }

    @Override
    public String getTypeName() {
        return dataType.getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return dataType.getFullTypeName();
    }

    @Override
    public int getTypeID() {
        return dataType.getTypeID();
    }

    @Override
    public DBPDataKind getDataKind() {
        return dataType.getDataKind();
    }

    @Override
    public Integer getScale() {
        return dataType.getScale();
    }

    @Override
    public Integer getPrecision() {
        return dataType.getPrecision();
    }

    @Override
    public long getMaxLength() {
        return dataType.getMaxLength();
    }

    @Property(viewable = true, order = 5)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
