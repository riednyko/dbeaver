/*
 * Copyright (C) 2010-2015 Serge Rieder
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
package org.jkiss.dbeaver.model.impl.jdbc.edit;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.impl.edit.AbstractObjectManager;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * JDBCObjectManager
 */
public abstract class JDBCObjectManager<OBJECT_TYPE extends DBSObject> extends AbstractObjectManager<OBJECT_TYPE> {

    protected static final Log log = Log.getLog(JDBCObjectManager.class);

    @Override
    public void executePersistAction(DBCSession session, DBECommand<OBJECT_TYPE> command, DBEPersistAction action) throws DBException
    {
        String script = action.getScript();
        if (script == null) {
            action.handleExecute(null);
        } else {
            DBCStatement dbStat = DBUtils.createStatement(session, script);
            try {
                dbStat.executeStatement();
                action.handleExecute(null);
            } catch (DBCException e) {
                action.handleExecute(e);
                throw e;
            } finally {
                dbStat.close();
            }
        }
    }

}
