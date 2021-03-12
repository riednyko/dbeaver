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
package org.jkiss.dbeaver.ext.clickhouse.edit;

import org.jkiss.dbeaver.ext.clickhouse.model.ClickhouseTable;
import org.jkiss.dbeaver.ext.generic.edit.GenericTableManager;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * Clickhouse table manager
 */
public class ClickhouseTableManager extends GenericTableManager {

    @Override
    protected String getDropTableType(GenericTableBase table) {
        // Both tables and views must be deleted with DROP TABLE
        return "TABLE";
    }

    @Override
    protected void appendTableModifiers(DBRProgressMonitor monitor, GenericTableBase table, NestedObjectCommand tableProps, StringBuilder ddl, boolean alter) {
        if (table instanceof ClickhouseTable) {
            ddl.append(" ENGINE = Log");
        }
    }
}
