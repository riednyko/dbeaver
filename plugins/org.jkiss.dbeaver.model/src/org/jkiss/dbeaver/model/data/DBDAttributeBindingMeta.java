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
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.util.List;

/**
 * Attribute value binding info
 */
public class DBDAttributeBindingMeta extends DBDAttributeBinding {
    @NotNull
    private final DBCAttributeMetaData metaAttribute;
    @Nullable
    private DBSEntityAttribute entityAttribute;
    @Nullable
    private DBDRowIdentifier rowIdentifier;
    @Nullable
    private List<DBSEntityReferrer> referrers;

    public DBDAttributeBindingMeta(
        @NotNull DBPDataSource dataSource,
        @NotNull DBCAttributeMetaData metaAttribute)
    {
        super(dataSource, null, DBUtils.findValueHandler(dataSource, metaAttribute));
        this.metaAttribute = metaAttribute;
    }

    /**
     * Attribute index in result set
     * @return attribute index (zero based)
     */
    @Override
    public int getOrdinalPosition()
    {
        return metaAttribute.getOrdinalPosition();
    }

    @Override
    public boolean isRequired() {
        return getAttribute().isRequired();
    }

    @Override
    public boolean isAutoGenerated() {
        return getAttribute().isAutoGenerated();
    }

    @Override
    public boolean isPseudoAttribute() {
        return getAttribute().isPseudoAttribute();
    }

    @Override
    public String getTypeName() {
        return getAttribute().getTypeName();
    }

    @Override
    public int getTypeID() {
        return getAttribute().getTypeID();
    }

    @Override
    public DBPDataKind getDataKind() {
        return getAttribute().getDataKind();
    }

    @Override
    public int getScale() {
        return getAttribute().getScale();
    }

    @Override
    public int getPrecision() {
        return getAttribute().getPrecision();
    }

    @Override
    public long getMaxLength() {
        return getAttribute().getMaxLength();
    }

    /**
     * Attribute label
     */
    @NotNull
    public String getLabel()
    {
        return metaAttribute.getLabel();
    }

    /**
     * Attribute name
     */
    @NotNull
    public String getName()
    {
        return metaAttribute.getName();
    }

    /**
     * Meta attribute (obtained from result set)
     */
    @NotNull
    public DBCAttributeMetaData getMetaAttribute() {
        return metaAttribute;
    }

    /**
     * Entity attribute (may be null)
     */
    @Nullable
    public DBSEntityAttribute getEntityAttribute()
    {
        return entityAttribute;
    }

    /**
     * Row identifier (may be null)
     */
    @Nullable
    public DBDRowIdentifier getRowIdentifier() {
        return rowIdentifier;
    }

    @Nullable
    @Override
    public List<DBSEntityReferrer> getReferrers() {
        return referrers;
    }

    @Nullable
    @Override
    public Object extractNestedValue(@NotNull Object ownerValue) throws DBCException {
        throw new DBCException("Meta binding doesn't support nested values");
    }

    /**
     * Sets entity attribute
     * @return true if attribute type differs from meta attribute type.
     */
    public boolean setEntityAttribute(@Nullable DBSEntityAttribute entityAttribute) {
        this.entityAttribute = entityAttribute;
        if (entityAttribute != null && !haveEqualsTypes(metaAttribute, entityAttribute)) {
            valueRenderer = valueHandler = DBUtils.findValueHandler(dataSource, entityAttribute);
            return true;
        }
        return false;
    }

    public static boolean haveEqualsTypes(DBSTypedObject object1, DBSTypedObject object2) {
        return object1.getTypeID() == object2.getTypeID() &&
            object1.getTypeName().equalsIgnoreCase(object2.getTypeName());
    }

    public void setRowIdentifier(@Nullable DBDRowIdentifier rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    @Override
    public void lateBinding(@NotNull DBCSession session, List<Object[]> rows) throws DBException {
        DBSEntityAttribute entityAttribute = getEntityAttribute();
        if (entityAttribute != null) {
            referrers = DBUtils.getAttributeReferrers(session.getProgressMonitor(), entityAttribute);
        }
        super.lateBinding(session, rows);
    }
}
