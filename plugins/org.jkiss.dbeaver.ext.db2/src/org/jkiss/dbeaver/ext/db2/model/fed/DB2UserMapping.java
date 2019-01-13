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
package org.jkiss.dbeaver.ext.db2.model.fed;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.DB2GlobalObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;

import java.sql.ResultSet;

/**
 * DB2 Federated User Mapping
 * 
 * @author Denis Forveille
 */
public class DB2UserMapping extends DB2GlobalObject {

    private String authId;
    private DB2RemoteServer remoteServer;

    // -----------------
    // Constructors
    // -----------------

    public DB2UserMapping(DB2DataSource db2DataSource, ResultSet dbResult) throws DBException
    {
        super(db2DataSource, true);

        this.authId = JDBCUtils.safeGetStringTrimmed(dbResult, "AUTHID");

        String remoteServerName = JDBCUtils.safeGetStringTrimmed(dbResult, "SERVERNAME");
        remoteServer = db2DataSource.getRemoteServer(new VoidProgressMonitor(), remoteServerName);

    }

    // -----------------
    // Properties
    // -----------------

    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return authId;
    }

    @Property(viewable = true, order = 2)
    public DB2RemoteServer getRemoteServer()
    {
        return remoteServer;
    }

}
