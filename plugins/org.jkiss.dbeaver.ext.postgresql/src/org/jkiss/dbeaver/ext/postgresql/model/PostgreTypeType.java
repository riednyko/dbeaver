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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPNamedObject;

/**
 * PostgreTypeType
 */
public enum PostgreTypeType implements DBPNamedObject
{
    b("Base"),
    c("Composite"),
    d("Domain"),
    e("Enum type"),
    p("Pseudo-type"),
    r("Range");

    private final String desc;

    PostgreTypeType(String desc) {
        this.desc = desc;
    }

    @NotNull
    @Override
    public String getName() {
        return desc;
    }
}
