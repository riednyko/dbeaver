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

import org.eclipse.debug.core.model.DebugElement;
import org.jkiss.dbeaver.debug.DBGController;

public class DatabaseDebugElement extends DebugElement {

    public DatabaseDebugElement(IDatabaseDebugTarget target) {
        super(target);
    }

    public IDatabaseDebugTarget geDatabaseDebugTarget() {
        return (IDatabaseDebugTarget) getDebugTarget();
    }
    
    public DBGController getController() {
        return geDatabaseDebugTarget().getController();
    }

    @Override
    public String getModelIdentifier() {
        return getDebugTarget().getModelIdentifier();
    }

}
