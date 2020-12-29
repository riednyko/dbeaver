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
package org.jkiss.dbeaver.model.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ModelPreferences;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.struct.DBSDataContainer;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.utils.CommonUtils;

import java.util.List;

/**
 * Attribute value binding info
 */
public class DBDAttributeBindingMeta extends DBDAttributeBinding {
    @NotNull
    private DBSDataContainer dataContainer;
    @NotNull
    private final DBCAttributeMetaData metaAttribute;
    @Nullable
    private DBSEntityAttribute entityAttribute;
    @Nullable
    private DBDRowIdentifier rowIdentifier;
    @Nullable
    private String rowIdentifierStatus;
    @Nullable
    private List<DBSEntityReferrer> referrers;
    @Nullable
    private DBDPseudoAttribute pseudoAttribute;

    private boolean showLabel;

    public DBDAttributeBindingMeta(
        @NotNull DBSDataContainer dataContainer,
        @NotNull DBCSession session,
        @NotNull DBCAttributeMetaData metaAttribute) {
        super(DBUtils.findValueHandler(session, metaAttribute));
        this.dataContainer = dataContainer;
        this.metaAttribute = metaAttribute;

        DBPDataSource dataSource = dataContainer.getDataSource();
        showLabel = dataSource == null || !dataSource.getContainer().getPreferenceStore().getBoolean(ModelPreferences.RESULT_SET_IGNORE_COLUMN_LABEL);
    }

    @Override
    public DBPDataSource getDataSource() {
        return dataContainer.getDataSource();
    }

    @Nullable
    @Override
    public DBDAttributeBinding getParentObject() {
        return null;
    }

    /**
     * Attribute index in result set
     *
     * @return attribute index (zero based)
     */
    @Override
    public int getOrdinalPosition() {
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
        return pseudoAttribute != null;
    }

    @NotNull
    @Override
    public DBSDataContainer getDataContainer() {
        return dataContainer;
    }

    @Override
    public String getTypeName() {
        return getAttribute().getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return getAttribute().getFullTypeName();
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
    public Integer getScale() {
        return getAttribute().getScale();
    }

    @Override
    public Integer getPrecision() {
        return getAttribute().getPrecision();
    }

    @Override
    public long getMaxLength() {
        return getAttribute().getMaxLength();
    }

    @Override
    public long getTypeModifiers() {
        return getAttribute().getTypeModifiers();
    }

    /**
     * Attribute label
     */
    @NotNull
    public String getLabel() {
        if (!showLabel) {
            // Return name if label is ignored
            return getName();
        }
        return metaAttribute.getLabel();
    }

    /**
     * Attribute name
     */
    @NotNull
    public String getName() {
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
    public DBSEntityAttribute getEntityAttribute() {
        return entityAttribute;
    }

    /**
     * Row identifier (may be null)
     */
    @Nullable
    public DBDRowIdentifier getRowIdentifier() {
        return rowIdentifier;
    }

    @Override
    public String getRowIdentifierStatus() {
        return rowIdentifierStatus;
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
     *
     * @return true if attribute type differs from meta attribute type.
     */
    public boolean setEntityAttribute(@Nullable DBSEntityAttribute entityAttribute, boolean updateHandler) {
        this.entityAttribute = entityAttribute;
        if (updateHandler && entityAttribute != null && !haveEqualsTypes(metaAttribute, entityAttribute)) {
            DBDValueHandler newValueHandler = DBUtils.findValueHandler(getDataSource(), entityAttribute);
            if (newValueHandler != getDataSource().getContainer().getDefaultValueHandler()) {
                // Change value handler only if it ws real
                valueHandler = newValueHandler;
            }
            return true;
        }
        return false;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public static boolean haveEqualsTypes(DBSTypedObject object1, DBSTypedObject object2) {
        return object1.getTypeID() == object2.getTypeID() &&
            object1.getDataKind() == object2.getDataKind() &&
            object1.getTypeName() != null && object1.getTypeName().equalsIgnoreCase(object2.getTypeName());
    }

    public void setRowIdentifier(@Nullable DBDRowIdentifier rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public void setRowIdentifierStatus(@Nullable String rowIdentifierStatus) {
        this.rowIdentifierStatus = rowIdentifierStatus;
    }

    @Override
    public void lateBinding(@NotNull DBCSession session, List<Object[]> rows) throws DBException {
        DBSEntityAttribute entityAttribute = getEntityAttribute();
        if (entityAttribute != null) {
            referrers = DBUtils.getAttributeReferrers(session.getProgressMonitor(), entityAttribute, true);
        }
        super.lateBinding(session, rows);
    }

    @Nullable
    public DBDPseudoAttribute getPseudoAttribute() {
        return pseudoAttribute;
    }

    public void setPseudoAttribute(@Nullable DBDPseudoAttribute pseudoAttribute) {
        this.pseudoAttribute = pseudoAttribute;
    }

    @Override
    public String toString() {
        return metaAttribute.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DBDAttributeBindingMeta) {
            DBCAttributeMetaData cmpMeta = ((DBDAttributeBindingMeta) obj).metaAttribute;
            return
                CommonUtils.equalObjects(metaAttribute.getName(), cmpMeta.getName()) &&
                    CommonUtils.equalObjects(metaAttribute.getLabel(), cmpMeta.getLabel()) &&
                    CommonUtils.equalObjects(metaAttribute.getEntityName(), cmpMeta.getEntityName()) &&
                    metaAttribute.getOrdinalPosition() == cmpMeta.getOrdinalPosition() &&
                    metaAttribute.getTypeID() == cmpMeta.getTypeID() &&
                    CommonUtils.equalObjects(metaAttribute.getTypeName(), cmpMeta.getTypeName())
                ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return
            CommonUtils.notEmpty(metaAttribute.getName()).hashCode() +
            CommonUtils.notEmpty(metaAttribute.getLabel()).hashCode() +
            CommonUtils.notEmpty(metaAttribute.getEntityName()).hashCode() +
            metaAttribute.getOrdinalPosition() +
            metaAttribute.getTypeID() +
            CommonUtils.notEmpty(metaAttribute.getTypeName()).hashCode();
    }
}
