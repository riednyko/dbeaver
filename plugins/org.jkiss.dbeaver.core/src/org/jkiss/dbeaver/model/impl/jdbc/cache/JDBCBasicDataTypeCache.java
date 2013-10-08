/*
 * Copyright (C) 2010-2013 Serge Rieder
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

package org.jkiss.dbeaver.model.impl.jdbc.cache;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCDataType;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JDBCBasicDataTypeCache extends JDBCObjectCache<JDBCDataSource, DBSDataType> {
    private final DBSObject owner;

    public JDBCBasicDataTypeCache(DBSObject owner)
    {
        this.owner = owner;
        setCaseSensitive(false);
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCSession session, JDBCDataSource owner) throws SQLException
    {
        return session.getMetaData().getTypeInfo().getSource();
    }

    @Override
    protected JDBCDataType fetchObject(JDBCSession session, JDBCDataSource owner, ResultSet dbResult) throws SQLException, DBException
    {
        return new JDBCDataType(
            this.owner,
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.DATA_TYPE),
            JDBCUtils.safeGetString(dbResult, JDBCConstants.TYPE_NAME),
            JDBCUtils.safeGetString(dbResult, JDBCConstants.LOCAL_TYPE_NAME),
            JDBCUtils.safeGetBoolean(dbResult, JDBCConstants.UNSIGNED_ATTRIBUTE),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.SEARCHABLE) != 0,
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.PRECISION),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.MINIMUM_SCALE),
            JDBCUtils.safeGetInt(dbResult, JDBCConstants.MAXIMUM_SCALE));
    }

    // SQL-92 standard types
    // plus a few de-facto standard types
    public void fillStandardTypes(DBSObject owner)
    {
        cacheObject(new JDBCDataType(
            owner, Types.INTEGER, "INTEGER", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.FLOAT, "FLOAT", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.REAL, "REAL", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.DOUBLE, "DOUBLE PRECISION", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.NUMERIC, "NUMBER", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.DECIMAL, "DECIMAL", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.SMALLINT, "SMALLINT", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.BIGINT, "BIGINT", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.BIT, "BIT", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.VARCHAR, "VARCHAR", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.VARBINARY, "VARBINARY", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.DATE, "DATE", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.TIME, "TIME", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.TIMESTAMP, "TIMESTAMP", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.BLOB, "BLOB", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.CLOB, "CLOB", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.BOOLEAN, "BOOLEAN", null, false, true, 0, 0, 0));
        cacheObject(new JDBCDataType(
            owner, Types.OTHER, "OBJECT", null, false, true, 0, 0, 0));
    }

}
