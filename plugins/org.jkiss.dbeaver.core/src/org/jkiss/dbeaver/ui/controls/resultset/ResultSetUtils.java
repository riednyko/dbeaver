/*
 * Copyright (C) 2010-2015 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jkiss.dbeaver.ui.controls.resultset;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityIdentifier;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.model.struct.rdb.DBSSchema;
import org.jkiss.dbeaver.model.struct.rdb.DBSTable;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;
import org.jkiss.dbeaver.model.virtual.DBVEntity;
import org.jkiss.dbeaver.model.virtual.DBVEntityConstraint;
import org.jkiss.utils.CommonUtils;

import java.util.*;

/**
 * Utils
 */
class ResultSetUtils
{
    static final Log log = Log.getLog(ResultSetUtils.class);

    public static void findValueLocators(
        DBCSession session,
        DBDAttributeBindingMeta[] bindings,
        List<Object[]> rows)
    {
        Map<DBSEntity, DBDRowIdentifier> locatorMap = new HashMap<DBSEntity, DBDRowIdentifier>();
        try {
            for (DBDAttributeBindingMeta binding : bindings) {
                DBCAttributeMetaData attrMeta = binding.getMetaAttribute();
                DBCEntityMetaData entityMeta = attrMeta.getEntityMetaData();
                Object metaSource = attrMeta.getSource();
                if (entityMeta == null && metaSource instanceof SQLQuery) {
                    entityMeta = ((SQLQuery)metaSource).getSingleSource();
                }
                DBSEntity entity = null;
                if (metaSource instanceof DBSEntity) {
                    entity = (DBSEntity)metaSource;
                } else if (entityMeta != null) {

                    final DBSObjectContainer objectContainer = DBUtils.getAdapter(DBSObjectContainer.class, session.getDataSource());
                    if (objectContainer != null) {
                        String catalogName = entityMeta.getCatalogName();
                        String schemaName = entityMeta.getSchemaName();
                        String entityName = entityMeta.getEntityName();
                        Class<? extends DBSObject> scChildType = objectContainer.getChildType(session.getProgressMonitor());
                        DBSObject entityObject;
                        if (!CommonUtils.isEmpty(catalogName) && scChildType != null && DBSSchema.class.isAssignableFrom(scChildType)) {
                            // Do not use catalog name
                            // Some data sources do not load catalog list but result set meta data contains one (e.g. DB2)
                            entityObject = DBUtils.getObjectByPath(session.getProgressMonitor(), objectContainer, null, schemaName, entityName);
                        } else {
                            entityObject = DBUtils.getObjectByPath(session.getProgressMonitor(), objectContainer, catalogName, schemaName, entityName);
                        }
                        if (entityObject == null) {
                            log.debug("Table '" + DBUtils.getSimpleQualifiedName(catalogName, schemaName, entityName) + "' not found in metadata catalog");
                        } else if (entityObject instanceof DBSEntity) {
                            entity = (DBSEntity) entityObject;
                        } else {
                            log.debug("Unsupported table class: " + entityObject.getClass().getName());
                        }
                    }
                }
                // We got table name and column name
                // To be editable we need this result   set contain set of columns from the same table
                // which construct any unique key
                if (entity != null) {
                    DBSEntityAttribute tableColumn;
                    if (attrMeta.getPseudoAttribute() != null) {
                        tableColumn = attrMeta.getPseudoAttribute().createFakeAttribute(entity, attrMeta);
                    } else {
                        tableColumn = entity.getAttribute(session.getProgressMonitor(), attrMeta.getName());
                    }

                    binding.setEntityAttribute(tableColumn);
                }
            }

            // Init row identifiers
            for (DBDAttributeBindingMeta binding : bindings) {
                DBSEntityAttribute attr = binding.getEntityAttribute();
                if (attr == null) {
                    continue;
                }
                DBSEntity entity = attr.getParentObject();
                DBDRowIdentifier rowIdentifier = locatorMap.get(entity);
                if (rowIdentifier == null) {
                    DBCEntityIdentifier entityIdentifier = getBestIdentifier(session.getProgressMonitor(), entity, bindings);
                    if (entityIdentifier != null) {
                        rowIdentifier = new DBDRowIdentifier(
                            entity,
                            entityIdentifier);
                        locatorMap.put(entity, rowIdentifier);
                    }
                }
                binding.setRowIdentifier(rowIdentifier);
            }

            // Read nested bindings
            for (DBDAttributeBinding binding : bindings) {
                binding.lateBinding(session, rows);
            }
        }
        catch (DBException e) {
            log.error("Can't extract column identifier info", e);
        }
    }

    private static DBCEntityIdentifier getBestIdentifier(@NotNull DBRProgressMonitor monitor, @NotNull DBSEntity table, DBDAttributeBinding[] bindings)
        throws DBException
    {
        List<BaseEntityIdentifier> identifiers = new ArrayList<BaseEntityIdentifier>(2);
        // Check for pseudo attrs (ROWID)
        for (DBDAttributeBinding column : bindings) {
            DBDPseudoAttribute pseudoAttribute = column.getMetaAttribute().getPseudoAttribute();
            if (pseudoAttribute != null && pseudoAttribute.getType() == DBDPseudoAttributeType.ROWID) {
                identifiers.add(new BaseEntityIdentifier(monitor, new DBDPseudoReferrer(table, column), bindings));
                break;
            }
        }

        if (table instanceof DBSTable && ((DBSTable) table).isView()) {
            // Skip physical identifiers for views. There are nothing anyway

        } else if (identifiers.isEmpty()) {

            // Check constraints
            Collection<? extends DBSEntityConstraint> constraints = table.getConstraints(monitor);
            if (constraints != null) {
                for (DBSEntityConstraint constraint : constraints) {
                    if (constraint instanceof DBSEntityReferrer && constraint.getConstraintType().isUnique()) {
                        identifiers.add(
                            new BaseEntityIdentifier(monitor, (DBSEntityReferrer)constraint, bindings));
                    }
                }
            }
            if (identifiers.isEmpty() && table instanceof DBSTable) {
                try {
                    // Check indexes only if no unique constraints found
                    Collection<? extends DBSTableIndex> indexes = ((DBSTable)table).getIndexes(monitor);
                    if (!CommonUtils.isEmpty(indexes)) {
                        for (DBSTableIndex index : indexes) {
                            if (index.isUnique()) {
                                identifiers.add(
                                    new BaseEntityIdentifier(monitor, index, bindings));
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Indexes are not supported or not available
                    // Just skip them
                    log.debug(e);
                }
            }
        }
        if (CommonUtils.isEmpty(identifiers)) {
            // No physical identifiers
            // Make new or use existing virtual identifier
            DBVEntity virtualEntity = table.getDataSource().getContainer().getVirtualModel().findEntity(table, true);
            identifiers.add(new BaseEntityIdentifier(monitor, virtualEntity.getBestIdentifier(), bindings));
        }
        if (!CommonUtils.isEmpty(identifiers)) {
            // Find PK or unique key
            BaseEntityIdentifier uniqueId = null;
            for (BaseEntityIdentifier id : identifiers) {
                DBSEntityReferrer referrer = id.getReferrer();
                if (isGoodReferrer(monitor, bindings, referrer)) {
                    if (referrer.getConstraintType() == DBSEntityConstraintType.PRIMARY_KEY) {
                        return id;
                    } else if (referrer.getConstraintType().isUnique() ||
                        (referrer instanceof DBSTableIndex && ((DBSTableIndex) referrer).isUnique()))
                    {
                        uniqueId = id;
                    }
                }
            }
            return uniqueId;
        }
        return null;
    }

    private static boolean isGoodReferrer(DBRProgressMonitor monitor, DBDAttributeBinding[] bindings, DBSEntityReferrer referrer) throws DBException
    {
        if (referrer instanceof DBDPseudoReferrer) {
            return true;
        }
        Collection<? extends DBSEntityAttributeRef> references = referrer.getAttributeReferences(monitor);
        if (references == null || references.isEmpty()) {
            return referrer instanceof DBVEntityConstraint;
        }
        for (DBSEntityAttributeRef ref : references) {
            for (DBDAttributeBinding binding : bindings) {
                if (binding.matches(ref.getAttribute(), false)) {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Basic entity identifier
     */
    public static class BaseEntityIdentifier implements DBCEntityIdentifier {

        private final DBSEntityReferrer referrer;

        private final List<DBDAttributeBinding> attributes = new ArrayList<DBDAttributeBinding>();

        public BaseEntityIdentifier(DBRProgressMonitor monitor, DBSEntityReferrer referrer, DBDAttributeBinding[] bindings) throws DBException
        {
            this.referrer = referrer;
            reloadAttributes(monitor, bindings);
        }

        public void reloadAttributes(@NotNull DBRProgressMonitor monitor, @NotNull DBDAttributeBinding[] bindings) throws DBException
        {
            this.attributes.clear();
            Collection<? extends DBSEntityAttributeRef> refs = CommonUtils.safeCollection(referrer.getAttributeReferences(monitor));
            for (DBSEntityAttributeRef cColumn : refs) {
                for (DBDAttributeBinding binding : bindings) {
                    if (binding.matches(cColumn.getAttribute(), false)) {
                        this.attributes.add(binding);
                        break;
                    }
                }
            }
        }

        @NotNull
        public DBSEntityReferrer getReferrer()
        {
            return referrer;
        }

        @NotNull
        @Override
        public List<DBDAttributeBinding> getAttributes() {
            return attributes;
        }
    }
}
