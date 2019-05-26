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
package org.jkiss.dbeaver.ui.editors.sql.syntax;

import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.sql.SQLSyntaxManager;
import org.jkiss.dbeaver.model.sql.completion.SQLCompletionContext;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.dbeaver.ui.editors.sql.SQLPreferenceConstants;

/**
 * SQLContextInformer
 */
public class SQLEditorCompletionContext implements SQLCompletionContext
{
    private final SQLEditorBase editor;

    public SQLEditorCompletionContext(SQLEditorBase editor) {
        this.editor = editor;
    }

    @Override
    public DBPDataSource getDataSource() {
        return editor.getDataSource();
    }

    @Override
    public SQLSyntaxManager getSyntaxManager() {
        return editor.getSyntaxManager();
    }

    @Override
    public boolean isUseFQNames() {
        return editor.getActivePreferenceStore().getBoolean(SQLPreferenceConstants.PROPOSAL_ALWAYS_FQ);
    }

    @Override
    public boolean isReplaceWords() {
        return editor.getActivePreferenceStore().getBoolean(SQLPreferenceConstants.PROPOSAL_REPLACE_WORD);
    }

    @Override
    public boolean isShowServerHelp() {
        return editor.getActivePreferenceStore().getBoolean(SQLPreferenceConstants.SHOW_SERVER_HELP_TOPICS);
    }

    @Override
    public boolean isUseShortNames() {
        return editor.getActivePreferenceStore().getBoolean(SQLPreferenceConstants.PROPOSAL_SHORT_NAME);
    }

    @Override
    public int getInsertCase() {
        return editor.getActivePreferenceStore().getInt(SQLPreferenceConstants.PROPOSAL_INSERT_CASE);
    }
}