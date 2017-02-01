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
package org.jkiss.dbeaver.model.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;

import java.util.List;

/**
 * Column value
 */
public class DBDAttributeValue {

    @NotNull
    private final DBSAttributeBase attribute;
    @Nullable
    private final Object value;

    public DBDAttributeValue(@NotNull DBSAttributeBase attribute, @Nullable Object value) {
        this.attribute = attribute;
        this.value = value;
    }

    @NotNull
    public DBSAttributeBase getAttribute() {
        return attribute;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public String toString()
    {
        return attribute.getName() + "=" + value;
    }

    public static DBSAttributeBase[] getAttributes(List<DBDAttributeValue> attrValues)
    {
        DBSAttributeBase[] attributes = new DBSAttributeBase[attrValues.size()];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = attrValues.get(i).attribute;
        }
        return attributes;
    }

    public static Object[] getValues(List<DBDAttributeValue> attrValues)
    {
        Object[] values = new Object[attrValues.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = attrValues.get(i).value;
        }
        return values;
    }

}
