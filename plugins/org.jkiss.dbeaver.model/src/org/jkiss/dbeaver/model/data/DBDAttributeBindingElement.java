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
package org.jkiss.dbeaver.model.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.utils.CommonUtils;

/**
 * Collection element binding info
 */
public class DBDAttributeBindingElement extends DBDAttributeBindingNested implements DBPImageProvider {
    @NotNull
    private final DBDCollection collection;
    private final int index;

    public DBDAttributeBindingElement(
        @NotNull DBDAttributeBinding parent,
        @NotNull DBDCollection collection,
        int index)
    {
        super(parent, collection.getComponentValueHandler());
        this.collection = collection;
        this.index = index;
    }

    /**
     * Attribute index in result set
     * @return attribute index (zero based)
     */
    @Override
    public int getOrdinalPosition()
    {
        return 0;
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

    @Nullable
    @Override
    public DBDCollection getSource() {
        return collection;
    }

    /**
     * Attribute label
     */
    @NotNull
    public String getLabel()
    {
        return getName();
    }

    @Nullable
    @Override
    public String getEntityName() {
        return null;
    }

    /**
     * Attribute name
     */
    @NotNull
    public String getName()
    {
        return String.valueOf(index + 1);
    }

    /**
     * Entity attribute
     */
    @Nullable
    public DBSEntityAttribute getEntityAttribute()
    {
        return null;
    }

    @Nullable
    @Override
    public Object extractNestedValue(@NotNull Object ownerValue) throws DBCException {
        if (collection.isNull()) {
            // Can happen if values was released
            return null;
        }
        return collection.getItem(index);
    }

    @Override
    public String getTypeName() {
        return collection.getComponentType().getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(collection.getComponentType());
    }

    @Override
    public int getTypeID() {
        return collection.getComponentType().getTypeID();
    }

    @Override
    public DBPDataKind getDataKind() {
        return collection.getComponentType().getDataKind();
    }

    @Override
    public Integer getScale() {
        return collection.getComponentType().getScale();
    }

    @Override
    public Integer getPrecision() {
        return collection.getComponentType().getPrecision();
    }

    @Override
    public long getMaxLength() {
        return collection.getComponentType().getMaxLength();
    }

    @Nullable
    @Override
    public DBPImage getObjectImage() {
        return DBValueFormatting.getObjectImage(collection.getComponentType());
    }

    @Override
    public String toString() {
        return collection.toString() + "@" + index;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof DBDAttributeBindingElement &&
            CommonUtils.equalObjects(collection, ((DBDAttributeBindingElement) obj).collection) &&
            index == ((DBDAttributeBindingElement) obj).index;
    }

    @Override
    public int hashCode() {
        return collection.hashCode() + index;
    }


}
