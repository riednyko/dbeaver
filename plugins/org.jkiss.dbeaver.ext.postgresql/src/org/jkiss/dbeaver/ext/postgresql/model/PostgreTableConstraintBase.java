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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * PostgreTableConstraintBase
 */
public abstract class PostgreTableConstraintBase extends JDBCTableConstraint<PostgreTableBase> implements PostgreObject,DBPScriptObject {
    private static final Log log = Log.getLog(PostgreTableConstraintBase.class);

    private long oid;
    private String constrDDL;

    public PostgreTableConstraintBase(PostgreTableBase table, String name, DBSEntityConstraintType constraintType, JDBCResultSet resultSet) throws DBException {
        super(table, name, null, constraintType, true);

        this.oid = JDBCUtils.safeGetLong(resultSet, "oid");
    }

    public PostgreTableConstraintBase(PostgreTableBase table, String constraintName, DBSEntityConstraintType constraintType) {
        super(table, constraintName, null, constraintType, false);
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context)
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            getTable(),
            this);
    }

    @NotNull
    @Override
    public PostgreDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @NotNull
    @Override
    public PostgreDatabase getDatabase() {
        return getParentObject().getDatabase();
    }

    @Override
    public long getObjectId() {
        return oid;
    }

    abstract void cacheAttributes(DBRProgressMonitor monitor, List<? extends PostgreTableConstraintColumn> children, boolean secondPass);

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException
    {
        if (constrDDL == null && isPersisted()) {
            try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Read constraint definition")) {
                constrDDL = JDBCUtils.queryString(session, "SELECT pg_catalog.pg_get_constraintdef(?)", getObjectId());
            } catch (SQLException e) {
                throw new DBException(e, getDataSource());
            }
            constrDDL = SQLUtils.formatSQL(getDataSource(), constrDDL);
        }
        return constrDDL;
    }

}
