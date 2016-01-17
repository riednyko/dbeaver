/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.model.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.struct.*;

import java.util.List;

/**
 * Type attribute value binding info
 */
public class DBDAttributeBindingType extends DBDAttributeBindingNested implements DBPImageProvider {
    @NotNull
    private final DBSAttributeBase attribute;

    public DBDAttributeBindingType(
        @NotNull DBDAttributeBinding parent,
        @NotNull DBSAttributeBase attribute)
    {
        super(parent, DBUtils.findValueHandler(parent.getDataSource(), attribute));
        this.attribute = attribute;
    }

    /**
     * Attribute index in result set
     * @return attribute index (zero based)
     */
    @Override
    public int getOrdinalPosition()
    {
        return attribute.getOrdinalPosition();
    }

    @Override
    public boolean isRequired() {
        return attribute.isRequired();
    }

    @Override
    public boolean isAutoGenerated() {
        return attribute.isAutoGenerated();
    }

    @Override
    public boolean isPseudoAttribute() {
        return attribute.isPseudoAttribute();
    }

    @Nullable
    @Override
    public DBSObject getSource() {
        if (attribute instanceof DBSObject) {
            return ((DBSObject)attribute).getParentObject();
        }
        return null;
    }

    /**
     * Attribute label
     */
    @NotNull
    public String getLabel()
    {
        return attribute.getName();
    }

    @Nullable
    @Override
    public String getEntityName() {
        DBSObject source = getSource();
        if (source instanceof DBSEntity) {
            return source.getName();
        }
        return null;
    }

    /**
     * Attribute name
     */
    @NotNull
    public String getName()
    {
        return attribute.getName();
    }

    /**
     * Entity attribute
     */
    @Nullable
    public DBSEntityAttribute getEntityAttribute()
    {
        if (attribute instanceof DBSEntityAttribute) {
            return (DBSEntityAttribute) attribute;
        }
        return null;
    }

    @Nullable
    @Override
    public Object extractNestedValue(@NotNull Object ownerValue) throws DBCException {
        assert parent != null;
        if (parent.getDataKind() == DBPDataKind.ARRAY) {
            // If we have a collection then use first element
            if (ownerValue instanceof DBDCollection) {
                DBDCollection collection = (DBDCollection) ownerValue;
                if (collection.getItemCount() > 0) {
                    ownerValue = collection.getItem(0);
                } else {
                    return null;
                }
            }
        }
        if (ownerValue instanceof DBDStructure) {
            return ((DBDStructure) ownerValue).getAttributeValue(attribute);
        }
        throw new DBCException("Unsupported value type: " + ownerValue.getClass().getName());
    }

    @Nullable
    @Override
    public DBPImage getObjectImage() {
        if (attribute instanceof DBPImageProvider) {
            return ((DBPImageProvider) attribute).getObjectImage();
        }
        return DBUtils.getDataIcon(this);
    }

    @Override
    public String getTypeName() {
        return attribute.getTypeName();
    }

    @Override
    public int getTypeID() {
        return attribute.getTypeID();
    }

    @Override
    public DBPDataKind getDataKind() {
        return attribute.getDataKind();
    }

    @Override
    public int getScale() {
        return attribute.getScale();
    }

    @Override
    public int getPrecision() {
        return attribute.getPrecision();
    }

    @Override
    public long getMaxLength() {
        return attribute.getMaxLength();
    }
}
