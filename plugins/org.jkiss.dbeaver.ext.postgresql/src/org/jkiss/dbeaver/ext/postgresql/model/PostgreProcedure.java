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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.PostgreConstants;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractProcedure;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Locale;

/**
 * PostgreProcedure
 */
public class PostgreProcedure extends AbstractProcedure<PostgreDataSource, PostgreSchema> implements PostgreScriptObject
{
    //static final Log log = Log.getLog(PostgreProcedure.class);

    private DBSProcedureType procedureType;
    private String resultType;
    private String bodyType;
    private String body;
    private boolean deterministic;
    private transient String clientBody;
    private String charset;

    public PostgreProcedure(
        PostgreSchema catalog,
        ResultSet dbResult)
    {
        super(catalog, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
    {
        setName(JDBCUtils.safeGetString(dbResult, "proname"));
        setDescription(JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_ROUTINE_COMMENT));
        this.procedureType = DBSProcedureType.PROCEDURE;
        this.resultType = JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_DTD_IDENTIFIER);
        this.bodyType = JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_ROUTINE_BODY);
        this.body = JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_ROUTINE_DEFINITION);
        this.charset = JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_CHARACTER_SET_CLIENT);
        this.deterministic = JDBCUtils.safeGetBoolean(dbResult, PostgreConstants.COL_IS_DETERMINISTIC, "YES");
        this.description = JDBCUtils.safeGetString(dbResult, PostgreConstants.COL_ROUTINE_COMMENT);
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
            this.body = "BEGIN" + GeneralUtils.getDefaultLineSeparator() + "END";
            if (procedureType == DBSProcedureType.FUNCTION) {
                body = "RETURNS INT" + GeneralUtils.getDefaultLineSeparator() + body;
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
            cb.append("CREATE ").append(procedureType).append(' ').append(getFullQualifiedName()).append(" (");

            int colIndex = 0;
            for (PostgreProcedureParameter column : CommonUtils.safeCollection(getParameters(monitor))) {
                if (column.getParameterKind() == DBSProcedureParameterKind.RETURN) {
                    continue;
                }
                if (colIndex > 0) {
                    cb.append(", ");
                }
                if (getProcedureType() == DBSProcedureType.PROCEDURE) {
                    cb.append(column.getParameterKind()).append(' ');
                }
                cb.append(column.getName()).append(' ');
                appendParameterType(cb, column);
                colIndex++;
            }
            cb.append(")").append(GeneralUtils.getDefaultLineSeparator());
            for (PostgreProcedureParameter column : CommonUtils.safeCollection(getParameters(monitor))) {
                if (column.getParameterKind() == DBSProcedureParameterKind.RETURN) {
                    cb.append("RETURNS ");
                    appendParameterType(cb, column);
                    cb.append(GeneralUtils.getDefaultLineSeparator());
                }
            }
            if (deterministic) {
                cb.append("DETERMINISTIC").append(GeneralUtils.getDefaultLineSeparator());
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

    private static void appendParameterType(StringBuilder cb, PostgreProcedureParameter column)
    {
        cb.append(column.getTypeName());
        if (column.getDataKind() == DBPDataKind.STRING && column.getMaxLength() > 0) {
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
    public Collection<PostgreProcedureParameter> getParameters(DBRProgressMonitor monitor)
        throws DBException
    {
        return null;
    }

    @NotNull
    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getContainer(),
            this);
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor) throws DBException
    {
        return getClientBody(monitor);
    }

    @Override
    public void setObjectDefinitionText(String sourceText) throws DBException
    {
        setClientBody(sourceText);
    }
}
