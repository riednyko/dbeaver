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
package org.jkiss.dbeaver.model.virtual;

import com.google.gson.stream.JsonWriter;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.json.JSONUtils;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraint;
import org.jkiss.utils.ArrayUtils;
import org.jkiss.utils.CommonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * DBVModelSerializerLegacy
 */
class DBVModelSerializerModern implements DBVModelSerializer
{
    private static final Log log = Log.getLog(DBVModelSerializerModern.class);

    static void serializeContainer(JsonWriter json, DBVContainer object) throws IOException {
        if (!object.hasValuableData()) {
            // nothing to save
            return;
        }
        if (object instanceof DBVModel) {
            json.name(((DBVModel) object).getId());
        } else {
            json.name(object.getName());
        }
        json.beginObject();

        for (DBVEntity entity : object.getEntities()) {
            if (entity.hasValuableData()) {
                serializeEntity(json, entity);
            }
        }

        // Containers
        for (DBVContainer container : object.getContainers()) {
            serializeContainer(json, container);
        }

        json.endObject();
    }

    private static void serializeEntity(JsonWriter json, DBVEntity entity) throws IOException {

        json.name(DBVContainer.ENTITY_PREFIX + entity.getName());

        json.beginObject();
        JSONUtils.fieldNE(json, ATTR_DESCRIPTION, entity.getDescriptionColumnNames());
        JSONUtils.serializeProperties(json, "properties", entity.properties);

        if (!CommonUtils.isEmpty(entity.entityAttributes)) {
            // Attributes
            json.name("attributes");
            json.beginObject();
            for (DBVEntityAttribute attr : entity.entityAttributes) {
                if (!attr.hasValuableData()) {
                    continue;
                }
                json.name(attr.getName());
                json.beginObject();
                final DBVTransformSettings transformSettings = attr.getTransformSettings();
                if (transformSettings != null && transformSettings.hasValuableData()) {
                    json.name("transforms");
                    json.beginObject();
                    JSONUtils.fieldNE(json, "custom", transformSettings.getCustomTransformer());
                    JSONUtils.serializeStringList(json, "include", transformSettings.getIncludedTransformers());
                    JSONUtils.serializeStringList(json, "exclude", transformSettings.getExcludedTransformers());
                    JSONUtils.serializeProperties(json, "properties", transformSettings.getTransformOptions());
                    json.endObject();
                }
                JSONUtils.serializeProperties(json, "properties", attr.properties);
                json.endObject();
            }
            json.endObject();
        }

        if (!CommonUtils.isEmpty(entity.entityConstraints)) {
            // Constraints
            json.name("constraints");
            json.beginObject();
            for (DBVEntityConstraint c : entity.entityConstraints) {
                if (c.hasAttributes()) {
                    json.name(c.getName());
                    json.beginObject();
                    JSONUtils.field(json, "type", c.getConstraintType().getName());
                    List<DBVEntityConstraintColumn> attrRefs = c.getAttributeReferences(null);
                    if (!CommonUtils.isEmpty(attrRefs)) {
                        json.name("attributes");
                        json.beginArray();
                        for (DBVEntityConstraintColumn cc : attrRefs) {
                            json.value(cc.getAttributeName());
                        }
                        json.endArray();
                    }
                    json.endObject();
                }
            }
            json.endObject();
        }

        if (!CommonUtils.isEmpty(entity.entityForeignKeys)) {
            // Foreign keys
            json.name("foreign-keys");
            json.beginArray();
            for (DBVEntityForeignKey fk : CommonUtils.safeCollection(entity.entityForeignKeys)) {
                json.beginObject();
                DBSEntity refEntity = fk.getAssociatedEntity();
                JSONUtils.field(json, "entity", DBUtils.getObjectFullId(refEntity));
                DBSEntityConstraint refConstraint = fk.getReferencedConstraint();
                if (refConstraint != null) {
                    JSONUtils.field(json, "constraint", refConstraint.getName());
                }
                List<DBVEntityForeignKeyColumn> refAttrs = fk.getAttributeReferences(null);
                if (!CommonUtils.isEmpty(refAttrs)) {
                    json.name("attributes");
                    json.beginObject();
                    for (DBVEntityForeignKeyColumn cc : refAttrs) {
                        json.name(cc.getAttributeName());
                        json.value(cc.getRefAttributeName());
                    }
                    json.endObject();
                }
                json.endObject();
            }
            json.endArray();
        }

        // Colors
        if (!CommonUtils.isEmpty(entity.colorOverrides)) {
            json.name("colors");
            json.beginArray();
            for (DBVColorOverride color : entity.colorOverrides) {
                json.beginObject();
                JSONUtils.field(json, "name", color.getAttributeName());
                JSONUtils.field(json, "operator", color.getOperator().name());
                if (color.isRange()) {
                    JSONUtils.field(json, "range", true);
                }
                if (color.isSingleColumn()) {
                    JSONUtils.field(json, "single-column", true);
                }
                JSONUtils.fieldNE(json, "foreground", color.getColorForeground());
                JSONUtils.fieldNE(json, "foreground2", color.getColorForeground2());
                JSONUtils.fieldNE(json, "background", color.getColorBackground());
                JSONUtils.fieldNE(json, "background2", color.getColorBackground2());
                if (!ArrayUtils.isEmpty(color.getAttributeValues())) {
                    JSONUtils.serializeObjectList(json, "values", Arrays.asList(color.getAttributeValues()));
                }
                json.endObject();
            }
            json.endArray();
        }

        json.endObject();
    }

}
