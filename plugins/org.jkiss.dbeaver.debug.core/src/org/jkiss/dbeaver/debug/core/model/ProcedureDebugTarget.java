/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017 Alexander Fedorov (alexander.fedorov@jkiss.org)
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
package org.jkiss.dbeaver.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.jkiss.dbeaver.debug.DBGController;
import org.jkiss.dbeaver.debug.core.DebugCore;
import org.jkiss.dbeaver.debug.internal.core.DebugCoreMessages;

public class ProcedureDebugTarget extends DatabaseDebugTarget<DBGController> {

    public ProcedureDebugTarget(ILaunch launch, IProcess process, DBGController controller) {
        super(DebugCore.MODEL_IDENTIFIER_PROCEDURE, launch, process, controller);
    }

    @Override
    protected DatabaseThread newThread(DBGController controller) {
        return new ProcedureThread(this, controller);
    }

    @Override
    protected String getConfiguredName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getName();
    }

    @Override
    protected String getDefaultName() {
        return DebugCoreMessages.ProcedureDebugTarget_name_default;
    }

}
