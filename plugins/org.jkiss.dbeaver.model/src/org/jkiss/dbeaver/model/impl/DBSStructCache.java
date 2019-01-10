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

package org.jkiss.dbeaver.model.impl;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.Collection;

/**
 * Structure objects cache
 */
public interface DBSStructCache<OWNER extends DBSObject, OBJECT extends DBSObject, CHILD extends DBSObject>
    extends DBSObjectCache<OWNER, OBJECT>
{

    DBSObjectCache<OBJECT, CHILD> getChildrenCache(final OBJECT forObject);

    @Nullable
    Collection<CHILD> getChildren(DBRProgressMonitor monitor, OWNER owner, final OBJECT forObject)
        throws DBException;

    @Nullable
    CHILD getChild(DBRProgressMonitor monitor, OWNER owner, final OBJECT forObject, String objectName)
        throws DBException;

    void clearChildrenCache(OBJECT forParent);
}
