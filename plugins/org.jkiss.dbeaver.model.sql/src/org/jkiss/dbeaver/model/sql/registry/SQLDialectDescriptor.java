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

package org.jkiss.dbeaver.model.sql.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.impl.AbstractContextDescriptor;
import org.jkiss.dbeaver.model.sql.SQLDialect;

import java.util.Collections;
import java.util.List;

/**
 * SQLDialectDescriptor
 */
public class SQLDialectDescriptor extends AbstractContextDescriptor {

    public static final String EXTENSION_ID = "org.jkiss.dbeaver.sqlDialect"; //$NON-NLS-1$

    private final String id;
    private final String label;
    private final String description;
    private final ObjectType implClass;
    private final DBPImage icon;

    SQLDialectDescriptor(IConfigurationElement config) {
        super(config);
        this.id = config.getAttribute("id");
        this.label = config.getAttribute("label");
        this.description = config.getAttribute("description");
        this.implClass = new ObjectType(config.getAttribute("class"));
        this.icon = iconToImage(config.getAttribute("icon"));
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public DBPImage getIcon() {
        return icon;
    }

    @NotNull
    public SQLDialect createInstance() throws DBException {
        return implClass.createInstance(SQLDialect.class);
    }

    @NotNull
    public List<String> getReservedWords() {
        return Collections.emptyList();
    }

    @NotNull
    public List<String> getDataTypes() {
        return Collections.emptyList();
    }

    @NotNull
    public List<String> getFunctions() {
        return Collections.emptyList();
    }

    @NotNull
    public List<String> getDDLKeywords() {
        return Collections.emptyList();
    }

    @NotNull
    public List<String> getTransactionKeywords() {
        return Collections.emptyList();
    }

    @NotNull
    public String getScriptDelimiter() {
        return ";";
    }

}
