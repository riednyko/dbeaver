/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2013-2015 Denis Forveille (titou10.titou10@gmail.com)
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
package org.jkiss.dbeaver.ext.db2.model.cache;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2Trigger;
import org.jkiss.dbeaver.ext.db2.model.DB2TriggerDep;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;

import java.sql.SQLException;

/**
 * Cache for dependencies on DB2 Triggers
 * 
 * @author Denis Forveille
 */
public class DB2TriggerDepCache extends JDBCObjectCache<DB2Trigger, DB2TriggerDep> {

    private static final String SQL = "SELECT * FROM SYSCAT.TRIGDEP WHERE TRIGSCHEMA = ? AND TRIGNAME = ? ORDER BY BSCHEMA,BNAME WITH UR";

    @NotNull
    @Override
    protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull DB2Trigger db2Trigger) throws SQLException
    {
        final JDBCPreparedStatement dbStat = session.prepareStatement(SQL);
        dbStat.setString(1, db2Trigger.getParentObject().getName());
        dbStat.setString(2, db2Trigger.getName());
        return dbStat;
    }

    @Override
    protected DB2TriggerDep fetchObject(@NotNull JDBCSession session, @NotNull DB2Trigger db2Trigger, @NotNull JDBCResultSet resultSet)
        throws SQLException, DBException
    {
        return new DB2TriggerDep(session.getProgressMonitor(), db2Trigger, resultSet);
    }
}