/*
 * DBeaver - Universal Database Manager
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
package org.jkiss.dbeaver.model.impl.jdbc.cache;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.SQLException;

/**
 * Various objects cache.
 * Simple cache which may read objects from database and keep them.
 */
public abstract class JDBCStructLookupCache<OWNER extends DBSObject, OBJECT extends DBSObject, CHILD extends DBSObject> extends JDBCStructCache<OWNER, OBJECT, CHILD> implements JDBCObjectLookup<OWNER, OBJECT>
{

    public JDBCStructLookupCache(Object objectNameColumn) {
        super(objectNameColumn);
    }

    public OBJECT refreshObject(@NotNull DBRProgressMonitor monitor, @Nullable OWNER owner, @NotNull OBJECT oldObject)
        throws DBException
    {
        String objectName = oldObject.getName();
        if (!isCached()) {
            this.loadObjects(monitor, owner);
        } else {
            OBJECT newObject = this.reloadObject(monitor, owner, oldObject);
            if (isChildrenCached(oldObject)) {
                clearChildrenCache(oldObject);
            }
            removeObject(oldObject);
            if (newObject != null) {
                cacheObject(newObject);
            }
            return newObject;
        }
        return getCachedObject(objectName);
    }


    protected OBJECT reloadObject(DBRProgressMonitor monitor, OWNER owner, OBJECT object)
        throws DBException
    {
        DBPDataSource dataSource = owner.getDataSource();
        if (dataSource == null) {
            throw new DBException("Not connected to database");
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, dataSource, "Reload object '" + object.getName() + "' from " + owner.getName())) {
            try (JDBCStatement dbStat = prepareLookupStatement(session, owner, object, null)) {
                dbStat.setFetchSize(1);
                dbStat.executeStatement();
                JDBCResultSet dbResult = dbStat.getResultSet();
                if (dbResult != null) {
                    try {
                        if (dbResult.next()) {
                            return fetchObject(session, owner, dbResult);
                        }
                    } finally {
                        dbResult.close();
                    }
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new DBException(ex, dataSource);
        }
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull OWNER owner)
        throws SQLException
    {
        return prepareLookupStatement(session, owner, null, null);
    }

}
