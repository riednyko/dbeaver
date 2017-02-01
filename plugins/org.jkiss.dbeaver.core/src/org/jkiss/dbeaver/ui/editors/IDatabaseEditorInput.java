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

package org.jkiss.dbeaver.ui.editors;

import org.eclipse.ui.IEditorInput;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPContextProvider;
import org.jkiss.dbeaver.model.preferences.DBPPropertySource;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.Collection;

/**
 * IDatabaseEditorInput
 */
public interface IDatabaseEditorInput extends INavigatorEditorInput, DBPContextProvider {

    DBNDatabaseNode getNavigatorNode();

    DBSObject getDatabaseObject();

    /**
     * Default editor page ID
     * @return page ID or null
     */
    String getDefaultPageId();

    /**
     * Default editor folder (tab) ID
     * @return folder ID or null
     */
    String getDefaultFolderId();

    /**
     * Command context
     * @return command context
     */
    @Nullable
    DBECommandContext getCommandContext();

    /**
     * Underlying object's property source
     * @return property source
     */
    DBPPropertySource getPropertySource();

    Collection<String> getAttributeNames();

    Object getAttribute(String name);

    Object setAttribute(String name, Object value);
}
