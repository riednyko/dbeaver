/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
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
package org.jkiss.dbeaver.ext.postgresql.tasks;

import org.jkiss.dbeaver.ext.postgresql.model.PostgreTableBase;
import org.jkiss.dbeaver.model.data.json.JSONUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.sql.task.SQLToolExecuteSettings;

import java.util.Map;

public class PostgreToolTableTruncateSettings extends SQLToolExecuteSettings<PostgreTableBase> {
    private boolean isRunning;
    private boolean isOnly;
    private boolean isRestarting;
    private boolean isCascading;

    @Property(viewable = true, editable = true, updatable = true)
    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Property(viewable = true, editable = true, updatable = true)
    public boolean isOnly() {
        return isOnly;
    }

    public void setOnly(boolean only) {
        isOnly = only;
    }

    @Property(viewable = true, editable = true, updatable = true)
    public boolean isRestarting() {
        return isRestarting;
    }

    public void setRestarting(boolean restarting) {
        isRestarting = restarting;
    }

    @Property(viewable = true, editable = true, updatable = true)
    public boolean isCascading() {
        return isCascading;
    }

    public void setCascading(boolean cascading) {
        isCascading = cascading;
    }

    @Override
    public void loadConfiguration(DBRRunnableContext runnableContext, Map<String, Object> config) {
        super.loadConfiguration(runnableContext, config);
        isRunning = JSONUtils.getBoolean(config, "run_in_separate_transaction");
        isOnly = JSONUtils.getBoolean(config, "only");
        isRestarting = JSONUtils.getBoolean(config, "restart_identity");
        isCascading = JSONUtils.getBoolean(config, "cascade");
    }

    @Override
    public void saveConfiguration(Map<String, Object> config) {
        super.saveConfiguration(config);
        config.put("run_in_separate_transaction", isRunning());
        config.put("only", isOnly);
        config.put("restart_identity", isRestarting);
        config.put("cascade", isCascading);
    }
}
