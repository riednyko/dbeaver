/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.model.impl.jdbc.data.handlers;

import org.jkiss.dbeaver.Log;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.data.DBDStructure;
import org.jkiss.dbeaver.model.data.DBDValueHandlerComposite;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCStructImpl;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCStruct;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCStructDynamic;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCStructStatic;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCStructUnknown;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;

/**
 * JDBC Struct value handler.
 * Handle STRUCT types.
 *
 * @author Serge Rider
 */
public class JDBCStructValueHandler extends JDBCComplexValueHandler implements DBDValueHandlerComposite {

    static final Log log = Log.getLog(JDBCStructValueHandler.class);

    public static final JDBCStructValueHandler INSTANCE = new JDBCStructValueHandler();

    /**
     * NumberFormat is not thread safe thus this method is synchronized.
     */
    @NotNull
    @Override
    public synchronized String getValueDisplayString(@NotNull DBSTypedObject column, Object value, @NotNull DBDDisplayFormat format)
    {
        if (value instanceof JDBCStruct) {
            if (format == DBDDisplayFormat.UI) {

            }
            return ((JDBCStruct) value).getStringRepresentation();
        } else {
            return String.valueOf(value);
        }
    }

    @NotNull
    @Override
    public Class<JDBCStruct> getValueObjectType(@NotNull DBSTypedObject attribute)
    {
        return JDBCStruct.class;
    }

    @Override
    protected void bindParameter(
        JDBCSession session,
        JDBCPreparedStatement statement,
        DBSTypedObject paramType,
        int paramIndex,
        Object value)
        throws DBCException, SQLException
    {
        if (value == null) {
            statement.setNull(paramIndex, Types.STRUCT);
        } else if (value instanceof DBDStructure) {
            DBDStructure struct = (DBDStructure) value;
            if (struct.isNull()) {
                statement.setNull(paramIndex, Types.STRUCT);
            } else {
                statement.setObject(paramIndex, struct.getRawValue(), Types.STRUCT);
            }
        } else {
            throw new DBCException("Struct parameter type '" + value.getClass().getName() + "' not supported");
        }
    }

    @Override
    public Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, Object object, boolean copy) throws DBCException
    {
        String typeName;
        try {
            if (object instanceof Struct) {
                typeName = ((Struct) object).getSQLTypeName();
            } else {
                typeName = type.getTypeName();
            }
        } catch (SQLException e) {
            throw new DBCException(e, session.getDataSource());
        }
        DBSDataType dataType = null;
        try {
            dataType = DBUtils.resolveDataType(session.getProgressMonitor(), session.getDataSource(), typeName);
        } catch (DBException e) {
            log.debug("Error resolving data type '" + typeName + "'", e);
        }
        if (dataType == null) {
            if (object instanceof Struct) {
                return new JDBCStructDynamic(session, (Struct) object, null);
            } else {
                return new JDBCStructUnknown(session, object);
            }
        }
        if (object == null) {
            return new JDBCStructStatic(session, dataType, new JDBCStructImpl(dataType.getTypeName(), null));
        } else if (object instanceof JDBCStructStatic) {
            return copy ? ((JDBCStructStatic) object).cloneValue(session.getProgressMonitor()) : object;
        } else if (object instanceof Struct) {
            return new JDBCStructStatic(session, dataType, (Struct) object);
        } else {
            return new JDBCStructUnknown(session, object);
        }
    }

}