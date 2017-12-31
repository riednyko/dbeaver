/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2017 Andrew Khitrin (ahitrin@gmail.com)
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

package org.jkiss.dbeaver.postgresql.pldbg.control;

import java.sql.Connection;
import java.util.List;

import org.jkiss.dbeaver.ext.postgresql.pldbg.DebugException;
import org.jkiss.dbeaver.ext.postgresql.pldbg.DebugObject;
import org.jkiss.dbeaver.ext.postgresql.pldbg.DebugSession;
import org.jkiss.dbeaver.ext.postgresql.pldbg.SessionInfo;

public interface DebugManager<SESSIONID,OBJECTID> {
	SessionInfo<SESSIONID> getSessionInfo(Connection connection) throws DebugException;
    List<? extends SessionInfo<SESSIONID>> getSessions() throws DebugException;
    DebugSession<? extends SessionInfo<SESSIONID>,? extends DebugObject<OBJECTID>,SESSIONID> getDebugSession(SESSIONID id) throws DebugException;
    List<DebugSession<?,?,SESSIONID>> getDebugSessions() throws DebugException;
    void terminateSession(SESSIONID id); 
    DebugSession<? extends SessionInfo<SESSIONID>,? extends DebugObject<OBJECTID>,SESSIONID> createDebugSession(Connection connection) throws DebugException;
    boolean isSessionExists(SESSIONID id);
    List<? extends DebugObject<OBJECTID>> getObjects(String ownerCtx, String nameCtx) throws DebugException;
}
