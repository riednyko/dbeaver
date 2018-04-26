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
package org.jkiss.dbeaver.model.impl.struct;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPToolTipObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBPositiveNumberTransformer;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;

/**
 * AbstractAttribute
 */
public abstract class AbstractAttribute implements DBSAttributeBase, DBPToolTipObject
{
    protected String name;
    protected int valueType;
    protected long maxLength;
    protected boolean required;
    protected boolean autoGenerated;
    protected Integer scale;
    protected Integer precision;
    protected String typeName;
    protected int ordinalPosition;

    protected AbstractAttribute()
    {
    }

    // Copy constructor
    protected AbstractAttribute(DBSAttributeBase source)
    {
        this.name = source.getName();
        this.valueType = source.getTypeID();
        this.maxLength = source.getMaxLength();
        this.scale = source.getScale();
        this.precision = source.getPrecision();
        this.required = source.isRequired();
        this.autoGenerated = source.isAutoGenerated();
        this.typeName = source.getTypeName();
        this.ordinalPosition = source.getOrdinalPosition();
    }

    protected AbstractAttribute(
            String name,
            String typeName,
            int valueType,
            int ordinalPosition,
            long maxLength,
            Integer scale,
            Integer precision,
            boolean required,
            boolean autoGenerated)
    {
        this.name = name;
        this.valueType = valueType;
        this.maxLength = maxLength;
        this.scale = scale;
        this.precision = precision;
        this.required = required;
        this.autoGenerated = autoGenerated;
        this.typeName = typeName;
        this.ordinalPosition = ordinalPosition;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 10, valueTransformer = DBObjectNameCaseTransformer.class)
    public String getName()
    {
        return name;
    }

    public void setName(String columnName)
    {
        this.name = columnName;
    }

    @Override
    @Property(viewable = true, order = 20)
    public String getTypeName()
    {
        return typeName;
    }

    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(this);
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    @Property(viewable = true, order = 15)
    public int getOrdinalPosition()
    {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition)
    {
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public int getTypeID()
    {
        return valueType;
    }

    public void setValueType(int valueType)
    {
        this.valueType = valueType;
    }

    @Override
    @Property(viewable = true, order = 40)
    public long getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(long maxLength)
    {
        this.maxLength = maxLength;
    }

    @Override
    @Property(viewable = false, valueRenderer = DBPositiveNumberTransformer.class, order = 41)
    public Integer getScale()
    {
        return scale;
    }

    public void setScale(Integer scale)
    {
        this.scale = scale;
    }

    @Override
    @Property(viewable = false, valueRenderer = DBPositiveNumberTransformer.class, order = 42)
    public Integer getPrecision()
    {
        return precision;
    }

    public void setPrecision(Integer precision)
    {
        this.precision = precision;
    }

    @Property(viewable = true, order = 50)
    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    @Property(viewable = true, order = 55)
    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    @Nullable
    public String getDescription()
    {
        return null;
    }

    public boolean isPersisted()
    {
        return true;
    }

    @Override
    public String getObjectToolTip() {
        return DBUtils.getFullTypeName(this);
    }

    @Override
    public String toString() {
        return name + ", type=" + typeName + ", pos=" + ordinalPosition;
    }

}
