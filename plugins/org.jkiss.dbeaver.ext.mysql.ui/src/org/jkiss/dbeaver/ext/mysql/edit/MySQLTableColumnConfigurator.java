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

package org.jkiss.dbeaver.ext.mysql.edit;

import org.jkiss.dbeaver.ext.mysql.model.MySQLTableBase;
import org.jkiss.dbeaver.ext.mysql.model.MySQLTableColumn;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.editors.object.struct.AttributeEditPage;

/**
 * MySQL table column manager
 */
public class MySQLTableColumnConfigurator implements DBEObjectConfigurator<MySQLTableBase, MySQLTableColumn> {
    @Override
    public MySQLTableColumn configureObject(DBRProgressMonitor monitor, MySQLTableBase table, MySQLTableColumn column) {
        return new UITask<MySQLTableColumn>() {
            @Override
            protected MySQLTableColumn runTask() {
                AttributeEditPage page = new AttributeEditPage(null, column);
                if (!page.edit()) {
                    return null;
                }
                return column;
            }
        }.execute();
    }

}
