/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
    public boolean isPseudoAttribute() {
        return false;
    }

    @Override
    public String getTypeName() {
        return dataType.getTypeName();
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
    public int getScale() {
        return dataType.getScale();
    }

    @Override
    public int getPrecision() {
        return dataType.getPrecision();
    }

    @Override
    public long getMaxLength() {
        return dataType.getMaxLength();
    }
}
