/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
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

package org.jkiss.dbeaver.ext.postgresql.debug.internal.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import org.jkiss.dbeaver.runtime.DBRResult;
import org.jkiss.dbeaver.runtime.DefaultResult;

public class PostgreDebugSessionWorker implements Callable<DBRResult> {

    private final Connection conn;
    private String sql = "";

    public void execSQL(String sqlCommand)
    {
        this.sql = sqlCommand;
    }

    public PostgreDebugSessionWorker(Connection conn)
    {
        this.conn = conn;
    }

    @Override
    public DBRResult call() throws Exception
    {

        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery(sql);
            return DefaultResult.ok();

        } catch (SQLException e) {
            String message = String.format("Failed to execute %s", sql);
            return DefaultResult.error(message, e);
        }
    }

}
