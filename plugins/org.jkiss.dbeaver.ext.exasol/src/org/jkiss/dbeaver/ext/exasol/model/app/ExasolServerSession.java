/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2016-2016 Karl Griesser (fullref@gmail.com)
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.ext.exasol.model.app;

import org.jkiss.dbeaver.model.admin.sessions.DBAServerSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * @author Karl Griesser
 */
public class ExasolServerSession implements DBAServerSession {

    private BigDecimal sessionID;
    private String userName;
    private String status;
    private String commandName;
    private Integer stmtId;
    private String Duration;
    private Integer queryTimeout;
    private String activity;
    private BigDecimal tempDbRam;
    private Timestamp loginTime;
    private String client;
    private String driver;
    private Boolean encrypted;
    private String host;
    private String osUser;
    private String osName;
    private String scopeSchema;
    private String priority;
    private Boolean nice;
    private Integer resources;
    private String sqlText;


    public ExasolServerSession(ResultSet dbResult) {
        this.sessionID = JDBCUtils.safeGetBigDecimal(dbResult, "SESSION_ID");
        this.userName = JDBCUtils.safeGetString(dbResult, "USER_NAME");
        this.status = JDBCUtils.safeGetString(dbResult, "STATUS");
        this.commandName = JDBCUtils.safeGetString(dbResult, "COMMAND_NAME");
        this.stmtId = JDBCUtils.safeGetInteger(dbResult, "STMT_ID");
        this.Duration = JDBCUtils.safeGetString(dbResult, "DURATION");
        this.queryTimeout = JDBCUtils.safeGetInteger(dbResult, "QUERY_TIMEOUT");
        this.activity = JDBCUtils.safeGetString(dbResult, "ACTIVITY");
        this.tempDbRam = JDBCUtils.safeGetBigDecimal(dbResult, "TEMP_DB_RAM");
        this.loginTime = JDBCUtils.safeGetTimestamp(dbResult, "LOGIN_TIME");
        this.client = JDBCUtils.safeGetString(dbResult, "CLIENT");
        this.driver = JDBCUtils.safeGetString(dbResult, "DRIVER");
        this.encrypted = JDBCUtils.safeGetBoolean(dbResult, "ENCRYPTED");
        this.host = JDBCUtils.safeGetString(dbResult, "HOST");
        this.osUser = JDBCUtils.safeGetString(dbResult, "OS_USER");
        this.osName = JDBCUtils.safeGetString(dbResult, "OS_NAME");
        this.scopeSchema = JDBCUtils.safeGetString(dbResult, "SCOPE_SCHEMA");
        this.priority = JDBCUtils.safeGetString(dbResult, "PRIORITY");
        this.nice = JDBCUtils.safeGetBoolean(dbResult, "NICE");
        this.resources = JDBCUtils.safeGetInteger(dbResult, "RESOURCES");
        this.sqlText = JDBCUtils.safeGetString(dbResult, "SQL_TEXT");
    }

    @Override
    public String toString() {
        return this.sessionID.toString();
    }

    @Override
    public String getActiveQuery() {
        if ( (! this.status.equals("IDLE") )  )
        {
        	return this.stmtId.toString();
        } else
        {
        	return null;
        }
    }

    @Property(viewable = true, editable = false, order = 1)
    public BigDecimal getSessionID() {
        return sessionID;
    }

    @Property(viewable = true, editable = false, order = 2)
    public String getUserName() {
        return userName;
    }

    @Property(viewable = true, editable = false, order = 2)
    public String getStatus() {
        return status;
    }

    @Property(viewable = true, editable = false, order = 3)
    public String getCommandName() {
        return commandName;
    }

    @Property(viewable = true, editable = false, order = 4)
    public Integer getStmtId() {
        return stmtId;
    }

    @Property(viewable = true, editable = false, order = 5)
    public String getDuration() {
        return Duration;
    }

    @Property(viewable = true, editable = false, order = 6)
    public Integer getQueryTimeout() {
        return queryTimeout;
    }

    @Property(viewable = true, editable = false, order = 7)
    public String getActivity() {
        return activity;
    }

    @Property(viewable = true, editable = false, order = 8)
    public BigDecimal getTempDbRam() {
        return tempDbRam;
    }

    @Property(viewable = true, editable = false, order = 9)
    public Timestamp getLoginTime() {
        return loginTime;
    }

    @Property(viewable = true, editable = false, order = 10)
    public String getClient() {
        return client;
    }

    @Property(viewable = true, editable = false, order = 11)
    public String getDriver() {
        return driver;
    }

    @Property(viewable = true, editable = false, order = 12)
    public Boolean getEncrypted() {
        return encrypted;
    }

    @Property(viewable = true, editable = false, order = 13)
    public String getHost() {
        return host;
    }

    @Property(viewable = true, editable = false, order = 14)
    public String getOsUser() {
        return osUser;
    }

    @Property(viewable = true, editable = false, order = 15)
    public String getOsName() {
        return osName;
    }

    @Property(viewable = true, editable = false, order = 16)
    public String getScopeSchema() {
        return scopeSchema;
    }

    @Property(viewable = true, editable = false, order = 17)
    public String getPriority() {
        return priority;
    }

    @Property(viewable = true, editable = false, order = 18)
    public Boolean getNice() {
        return nice;
    }

    @Property(viewable = true, editable = false, order = 19)
    public Integer getResources() {
        return resources;
    }

    @Property(viewable = true, editable = false, order = 20)
    public String getSqlText() {
        return sqlText;
    }


}
