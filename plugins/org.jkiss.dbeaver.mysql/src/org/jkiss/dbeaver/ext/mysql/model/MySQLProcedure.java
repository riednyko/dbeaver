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
package org.jkiss.dbeaver.ext.mysql.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mysql.MySQLConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractProcedure;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterType;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * GenericProcedure
 */
public class MySQLProcedure extends AbstractProcedure<MySQLDataSource, MySQLCatalog> implements MySQLSourceObject
{
    //static final Log log = LogFactory.getLog(MySQLProcedure.class);

    private DBSProcedureType procedureType;
    private String resultType;
    private String bodyType;
    private String body;
    private boolean deterministic;
    private transient String clientBody;
    private String charset;
    private List<MySQLProcedureParameter> columns;

    public MySQLProcedure(MySQLCatalog catalog)
    {
        super(catalog, false);
        this.procedureType = DBSProcedureType.PROCEDURE;
        this.bodyType = "SQL";
        this.resultType = "";
        this.deterministic = false;
    }

    public MySQLProcedure(
        MySQLCatalog catalog,
        ResultSet dbResult)
    {
        super(catalog, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
    {
        setName(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_NAME));
        setDescription(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_COMMENT));
        this.procedureType = DBSProcedureType.valueOf(JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_TYPE).toUpperCase());
        this.resultType = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_DTD_IDENTIFIER);
        this.bodyType = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_BODY);
        this.body = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_DEFINITION);
        this.charset = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_CHARACTER_SET_CLIENT);
        this.deterministic = JDBCUtils.safeGetBoolean(dbResult, MySQLConstants.COL_IS_DETERMINISTIC, "YES");
        this.description = JDBCUtils.safeGetString(dbResult, MySQLConstants.COL_ROUTINE_COMMENT);
    }

    @Override
    @Property(order = 2)
    public DBSProcedureType getProcedureType()
    {
        return procedureType ;
    }

    public void setProcedureType(DBSProcedureType procedureType)
    {
        this.procedureType = procedureType;
    }

    @Property(order = 2)
    public String getResultType()
    {
        return resultType;
    }

    @Property(order = 3)
    public String getBodyType()
    {
        return bodyType;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getBody()
    {
        if (this.body == null && !persisted) {
            this.body = "BEGIN" + ContentUtils.getDefaultLineSeparator() + "END";
            if (procedureType == DBSProcedureType.FUNCTION) {
                body = "RETURNS INT" + ContentUtils.getDefaultLineSeparator() + body;
            }
        }
        return body;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getClientBody(DBRProgressMonitor monitor)
        throws DBException
    {
        if (clientBody == null) {
            StringBuilder cb = new StringBuilder(getBody().length() + 100);
            cb.append(procedureType).append(' ').append(getFullQualifiedName()).append(" (");

            int colIndex = 0;
            for (MySQLProcedureParameter column : getParameters(monitor)) {
                if (column.getParameterType() == DBSProcedureParameterType.RETURN) {
                    continue;
                }
                if (colIndex > 0) {
                    cb.append(", ");
                }
                if (getProcedureType() == DBSProcedureType.PROCEDURE) {
                    cb.append(column.getParameterType()).append(' ');
                }
                cb.append(column.getName()).append(' ');
                appendParameterType(cb, column);
                colIndex++;
            }
            cb.append(")").append(ContentUtils.getDefaultLineSeparator());
            for (MySQLProcedureParameter column : getParameters(monitor)) {
                if (column.getParameterType() == DBSProcedureParameterType.RETURN) {
                    cb.append("RETURNS ");
                    appendParameterType(cb, column);
                    cb.append(ContentUtils.getDefaultLineSeparator());
                }
            }
            if (deterministic) {
                cb.append("DETERMINISTIC").append(ContentUtils.getDefaultLineSeparator());
            }
            cb.append(getBody());
            clientBody = cb.toString();
        }
        return clientBody;
    }

    @Property(editable = true, updatable = true, order = 3)
    public boolean isDeterministic()
    {
        return deterministic;
    }

    public void setDeterministic(boolean deterministic)
    {
        this.deterministic = deterministic;
    }

    private static void appendParameterType(StringBuilder cb, MySQLProcedureParameter column)
    {
        cb.append(column.getTypeName());
        if (column.getMaxLength() > 0) {
            cb.append('(').append(column.getMaxLength()).append(')');
        }
    }

    public String getClientBody()
    {
        return clientBody;
    }

    public void setClientBody(String clientBody)
    {
        this.clientBody = clientBody;
    }

    //@Property(name = "Client Charset", order = 4)
    public String getCharset()
    {
        return charset;
    }

    @Override
    public List<MySQLProcedureParameter> getParameters(DBRProgressMonitor monitor)
        throws DBException
    {
        if (columns == null) {
            if (!isPersisted()) {
                columns = new ArrayList<MySQLProcedureParameter>();
            }
            getContainer().proceduresCache.loadChildren(monitor, getContainer(), this);
        }
        return columns;
    }

    public boolean isColumnsCached()
    {
        return this.columns != null;
    }

    public void cacheColumns(List<MySQLProcedureParameter> columns)
    {
        this.columns = columns;
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getContainer(),
            this);
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getSourceText(DBRProgressMonitor monitor) throws DBException
    {
        return getClientBody(monitor);
    }

    @Override
    public void setSourceText(String sourceText) throws DBException
    {
        setClientBody(sourceText);
    }
}
