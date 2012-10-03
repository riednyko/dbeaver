/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jkiss.dbeaver.model.qm;

import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPTransactionIsolation;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSavepoint;
import org.jkiss.dbeaver.model.exec.DBCStatement;

/**
 * Query manager execution handler.
 * Handler methods are invoked right at time of DBC operation, so they should work as fast as possible.
 * Implementers should not invoke any DBC execution function in passed objects - otherwise execution handling may enter infinite recursion.
 */
public interface QMExecutionHandler {

    String getHandlerName();

    void handleSessionStart(DBPDataSource dataSource, boolean transactional);

    void handleSessionEnd(DBPDataSource dataSource);

    void handleContextOpen(DBCExecutionContext context);

    void handleContextClose(DBCExecutionContext context);

    void handleTransactionAutocommit(DBCExecutionContext context, boolean autoCommit);

    void handleTransactionIsolation(DBCExecutionContext context, DBPTransactionIsolation level);

    void handleTransactionCommit(DBCExecutionContext context);

    void handleTransactionSavepoint(DBCSavepoint savepoint);

    void handleTransactionRollback(DBCExecutionContext context, DBCSavepoint savepoint);

    void handleStatementOpen(DBCStatement statement);

    void handleStatementExecuteBegin(DBCStatement statement);

    void handleStatementExecuteEnd(DBCStatement statement, long rows, Throwable error);

    void handleStatementBind(DBCStatement statement, Object column, Object value);

    void handleStatementClose(DBCStatement statement);

    void handleResultSetOpen(DBCResultSet resultSet);

    void handleResultSetClose(DBCResultSet resultSet, long rowCount);

    void handleScriptBegin(DBCExecutionContext context);
    
    void handleScriptEnd(DBCExecutionContext context);

}
