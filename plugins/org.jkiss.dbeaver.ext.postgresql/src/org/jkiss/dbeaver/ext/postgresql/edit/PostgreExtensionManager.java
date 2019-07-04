/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2019 Andrew Khitrin (ahitrin@gmail.com)
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

package org.jkiss.dbeaver.ext.postgresql.edit;

import java.util.List;
import java.util.Map;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDatabase;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreExtension;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

public class PostgreExtensionManager extends SQLObjectEditor<PostgreExtension, PostgreDatabase>{

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return FEATURE_SAVE_IMMEDIATELY;
    }

    @Override
    public DBSObjectCache<PostgreDatabase, PostgreExtension> getObjectsCache(PostgreExtension object) {
        return object.getDatabase().extensionCache;
    }

    @Override
    protected PostgreExtension createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context,
            Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        return new PostgreExtension((PostgreDatabase) container);
    }


    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, List<DBEPersistAction> actions,
            SQLObjectEditor<PostgreExtension, PostgreDatabase>.ObjectCreateCommand command,
            Map<String, Object> options) {
        final PostgreExtension extension = command.getObject();
        final StringBuilder script = new StringBuilder("CREATE EXTENSION " + DBUtils.getQuotedIdentifier(extension));
        script.append(" SCHEMA ").append(extension.getSchema());

        actions.add(
            new SQLDatabasePersistAction("Create extension", script.toString()) //$NON-NLS-2$
        );
    }

    @Override
    protected void addObjectDeleteActions(List<DBEPersistAction> actions,
            SQLObjectEditor<PostgreExtension, PostgreDatabase>.ObjectDeleteCommand command,
            Map<String, Object> options) {
    }

    @Override
    public boolean canCreateObject(Object container) {
         return true;
    }

    @Override
    public boolean canDeleteObject(PostgreExtension object) {
        return false;
    }

    @Override
    public boolean canEditObject(PostgreExtension object) {
        return false;
    }
    
    

  
}
