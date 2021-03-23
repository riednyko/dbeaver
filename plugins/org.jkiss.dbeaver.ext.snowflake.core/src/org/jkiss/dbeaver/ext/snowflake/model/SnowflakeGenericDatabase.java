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
package org.jkiss.dbeaver.ext.snowflake.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericCatalog;
import org.jkiss.dbeaver.ext.generic.model.GenericDataSource;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

class SnowflakeGenericDatabase extends GenericCatalog {
    SnowflakeGenericDatabase(@NotNull GenericDataSource dataSource, @NotNull String catalogName) {
        super(dataSource, catalogName);
    }

    @Nullable
    @Override
    public GenericSchema getSchema(@NotNull DBRProgressMonitor monitor, @NotNull String name) throws DBException {
        return DBUtils.findObject(getSchemas(monitor), name, SnowflakeSQLDialect.isCaseInsensitiveIdentifier(name));
    }
}
