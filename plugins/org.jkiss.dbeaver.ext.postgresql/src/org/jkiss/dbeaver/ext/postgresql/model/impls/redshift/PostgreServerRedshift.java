/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ext.postgresql.model.impls.redshift;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTableBase;
import org.jkiss.dbeaver.ext.postgresql.model.impls.PostgreServerExtensionBase;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * PostgreServerRedshift
 */
public class PostgreServerRedshift extends PostgreServerExtensionBase {

    public PostgreServerRedshift(PostgreDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public String getServerTypeName() {
        return "Redshift";
    }

    @Override
    public boolean supportsOids() {
        return false;
    }

    @Override
    public boolean supportsIndexes() {
        return false;
    }

    @Override
    public boolean supportsMaterializedViews() {
        return false;
    }

    @Override
    public boolean supportsPartitions() {
        return false;
    }

    @Override
    public boolean supportsInheritance() {
        return false;
    }

    @Override
    public boolean supportsTriggers() {
        return false;
    }

    @Override
    public boolean supportsExtensions() {
        return false;
    }

    @Override
    public boolean supportsEncodings() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsRoles() {
        return false;
    }

    @Override
    public boolean supportsLocks() {
        return false;
    }

    @Override
    public boolean supportsForeignServers() {
        return false;
    }

    @Override
    public boolean isSupportsLimits() {
        return true;
    }

    @Override
    public boolean supportsClientInfo() {
        return false;
    }

    @Override
    public String readTableDDL(DBRProgressMonitor monitor, PostgreTableBase table) throws DBException {
        try (JDBCSession session = DBUtils.openMetaSession(monitor, table, "Load Redshift table DDL")) {
            try (JDBCPreparedStatement dbStat = session.prepareStatement(
                RedshiftConstants.DDL_EXTRACT_VIEW + "\n" +
                    "WHERE schemaname=? AND tablename=?")) {
                dbStat.setString(1, table.getSchema().getName());
                dbStat.setString(2, table.getName());
                try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                    StringBuilder sql = new StringBuilder();
                    while (resultSet.next()) {
                        String line = resultSet.getString("ddl");
                        if (line == null) {
                            continue;
                        }
                        sql.append(line).append("\n");
                    }
                    String ddl = sql.toString().trim();
                    if (ddl.endsWith(";")) {
                        ddl = ddl.substring(0, ddl.length() - 1).trim();
                    }
                    return ddl;
                }
            }
        } catch (Exception e) {
            throw new DBException(e, table.getDataSource());
        }
    }
}

