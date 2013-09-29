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
package org.jkiss.dbeaver.ext.db2.model.security;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cache for DB2 Groups and Users
 * 
 * @author Denis Forveille
 */
public final class DB2GroupUserCache extends JDBCObjectCache<DB2DataSource, DB2UserBase> {

    private static String SQL;

    private DB2AuthIDType authIdType;
    private String authIdTypeName;

    // TODO DF: Add missing auth: modules, functions, columns etc..

    static {

        StringBuilder sb = new StringBuilder(1536);

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.TABAUTH");
        sb.append(" WHERE GRANTEETYPE = ?"); // 1

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.INDEXAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 2

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.SEQUENCEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 3

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.TBSPACEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 4

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.SCHEMAAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 5

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.PACKAGEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 6

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.COLAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 7

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.MODULEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 8

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.ROLEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 9

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.ROUTINEAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 10

        sb.append(" UNION ");

        sb.append("SELECT DISTINCT GRANTEE");
        sb.append("  FROM SYSCAT.XSROBJECTAUTH");
        sb.append(" WHERE GRANTEETYPE = ?");// 11

        sb.append(" ORDER BY GRANTEE");
        sb.append(" WITH UR");

        SQL = sb.toString();
    }

    public DB2GroupUserCache(DB2AuthIDType authIdType)
    {
        this.authIdType = authIdType;
        this.authIdTypeName = authIdType.name();
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCExecutionContext context, DB2DataSource db2DataSource) throws SQLException
    {
        JDBCPreparedStatement dbStat = context.prepareStatement(SQL);
        dbStat.setString(1, authIdTypeName);
        dbStat.setString(2, authIdTypeName);
        dbStat.setString(3, authIdTypeName);
        dbStat.setString(4, authIdTypeName);
        dbStat.setString(5, authIdTypeName);
        dbStat.setString(6, authIdTypeName);
        dbStat.setString(7, authIdTypeName);
        dbStat.setString(8, authIdTypeName);
        dbStat.setString(9, authIdTypeName);
        dbStat.setString(10, authIdTypeName);
        dbStat.setString(11, authIdTypeName);
        return dbStat;
    }

    @Override
    protected DB2UserBase fetchObject(JDBCExecutionContext context, DB2DataSource db2DataSource, ResultSet resultSet)
        throws SQLException, DBException
    {
        switch (authIdType) {
        case G:
            return new DB2Group(db2DataSource, resultSet);
        case U:
            return new DB2User(db2DataSource, resultSet);
        default:
            throw new DBException("Structural problem. " + authIdType + " type not implemented");
        }
    }

}