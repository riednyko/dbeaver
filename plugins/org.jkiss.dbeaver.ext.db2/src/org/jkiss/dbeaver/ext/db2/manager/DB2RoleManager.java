/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2013-2015 Denis Forveille (titou10.titou10@gmail.com)
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
package org.jkiss.dbeaver.ext.db2.manager;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.security.DB2Role;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;

/**
 * DB2 Role Manager
 * 
 * @author Denis Forveille
 */
public class DB2RoleManager extends DB2AbstractDropOnlyManager<DB2Role, DB2DataSource> {

    private static final String SQL_DROP = "DROP ROLE %s";

    @Override
    public String buildDropStatement(DB2Role db2Role)
    {
        String name = db2Role.getName();
        return String.format(SQL_DROP, name);
    }

    @Nullable
    @Override
    public DBSObjectCache<DB2DataSource, DB2Role> getObjectsCache(DB2Role db2Role)
    {
        return db2Role.getDataSource().getRoleCache();
    }

}
