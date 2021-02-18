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
package org.jkiss.dbeaver.ui.editors.sql.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.runtime.ui.UIServiceSQL;

public class SQLEditorHandlerOpenConsole extends AbstractHandler {

    private static final Log log = Log.getLog(SQLEditorHandlerOpenConsole.class);

    public SQLEditorHandlerOpenConsole()
    {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        try {
            SQLNavigatorContext navContext = SQLEditorHandlerOpenEditor.getCurrentContext(event);

            UIServiceSQL serviceSQL = DBWorkbench.getService(UIServiceSQL.class);
            if (serviceSQL != null) {
                serviceSQL.openSQLConsole(navContext.getDataSourceContainer(), navContext.getExecutionContext(), navContext.getSelectedObject(), "Console", "");
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
