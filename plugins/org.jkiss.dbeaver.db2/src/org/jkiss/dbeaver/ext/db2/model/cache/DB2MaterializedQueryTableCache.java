/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.model.cache;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2MaterializedQueryTable;
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.ext.db2.model.DB2TableColumn;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2TableType;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCStructCache;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cache for DB2 MQT
 * 
 * @author Denis Forveille
 */
public final class DB2MaterializedQueryTableCache extends JDBCStructCache<DB2Schema, DB2MaterializedQueryTable, DB2TableColumn> {

    private static final String SQL_VIEWS;
    private static final String SQL_COLS_TAB = "SELECT * FROM SYSCAT.COLUMNS WHERE TABSCHEMA=? AND TABNAME = ? ORDER BY COLNO WITH UR";
    private static final String SQL_COLS_ALL = "SELECT * FROM SYSCAT.COLUMNS WHERE TABSCHEMA=? ORDER BY TABNAME, COLNO WITH UR";

    static {
        StringBuilder sb = new StringBuilder(512);
        sb.append("SELECT *");
        sb.append(" FROM SYSCAT.TABLES T");
        sb.append("    , SYSCAT.VIEWS V");
        sb.append(" WHERE V.VIEWSCHEMA = ?");
        sb.append("   AND T.TABSCHEMA = V.VIEWSCHEMA");
        sb.append("   AND T.TABNAME = V.VIEWNAME");
        sb.append("   AND T.TYPE = '" + DB2TableType.S.name() + "'");
        sb.append(" ORDER BY T.TABNAME");
        sb.append(" WITH UR");

        SQL_VIEWS = sb.toString();
    }

    public DB2MaterializedQueryTableCache()
    {
        super("TABNAME");
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCSession session, DB2Schema db2Schema) throws SQLException
    {
        final JDBCPreparedStatement dbStat = session.prepareStatement(SQL_VIEWS);
        dbStat.setString(1, db2Schema.getName());
        return dbStat;
    }

    @Override
    protected DB2MaterializedQueryTable fetchObject(JDBCSession session, DB2Schema db2Schema, ResultSet dbResult)
        throws SQLException, DBException
    {
        return new DB2MaterializedQueryTable(session.getProgressMonitor(), db2Schema, dbResult);
    }

    @Override
    protected JDBCStatement prepareChildrenStatement(JDBCSession session, DB2Schema db2Schema,
        DB2MaterializedQueryTable forMqt) throws SQLException
    {

        String sql;
        if (forMqt != null) {
            sql = SQL_COLS_TAB;
        } else {
            sql = SQL_COLS_ALL;
        }
        JDBCPreparedStatement dbStat = session.prepareStatement(sql);
        dbStat.setString(1, db2Schema.getName());
        if (forMqt != null) {
            dbStat.setString(2, forMqt.getName());
        }
        return dbStat;
    }

    @Override
    protected DB2TableColumn fetchChild(JDBCSession session, DB2Schema db2Schema, DB2MaterializedQueryTable db2MQT,
        ResultSet dbResult) throws SQLException, DBException
    {
        return new DB2TableColumn(session.getProgressMonitor(), db2MQT, dbResult);
    }

}
