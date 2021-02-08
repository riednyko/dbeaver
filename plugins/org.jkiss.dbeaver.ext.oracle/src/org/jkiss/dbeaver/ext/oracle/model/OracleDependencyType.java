/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
package org.jkiss.dbeaver.ext.oracle.model;

public enum OracleDependencyType {
    HARD("HARD"),
    REF("REF");

    private final String type;

    OracleDependencyType(String type) {
        this.type = type;
    }

    public static OracleDependencyType getByType(String type) {
        if ("HARD".equals(type)) {
            return HARD;
        }
        if ("REF".equals(type)) {
            return REF;
        }
        return null;
    }

    public String getType() {
        return type;
    }
}
