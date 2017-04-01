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

package org.jkiss.dbeaver.model.sql;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPDataSource;

/**
 * SQL control command
 */
public class SQLControlCommand implements SQLScriptElement {

    private final DBPDataSource dataSource;
    private final String command;
    private final String parameter;
    private final int offset;
    private final int length;
    private Object data;

    public SQLControlCommand(DBPDataSource dataSource, SQLSyntaxManager syntaxManager, String text, int offset, int length) {
        this.dataSource = dataSource;

        if (text.startsWith(syntaxManager.getControlCommandPrefix())) {
            text = text.substring(syntaxManager.getControlCommandPrefix().length());
        }
        int divPos = -1;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                divPos = i;
                break;
            }
        }

        this.command = divPos == -1 ? text : text.substring(0, divPos);
        this.parameter = divPos == -1 ? null : text.substring(divPos + 1).trim();
        this.offset = offset;
        this.length = length;
    }

    public DBPDataSource getDataSource() {
        return dataSource;
    }

    @NotNull
    @Override
    public String getOriginalText() {
        return command;
    }

    @NotNull
    @Override
    public String getText() {
        return command;
    }

    public String getParameter() {
        return parameter;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public void reset() {

    }

    @Override
    public String toString() {
        return command;
    }
}
