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
package org.jkiss.dbeaver.model.impl.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.edit.DBEObjectManager;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * Abstract object manager
 */
public abstract class AbstractObjectManager<OBJECT_TYPE extends DBSObject> implements DBEObjectManager<OBJECT_TYPE> {

    protected static final Log log = LogFactory.getLog(AbstractObjectManager.class);

    public static class CreateObjectReflector<OBJECT_TYPE extends DBSObject> implements DBECommandReflector<OBJECT_TYPE, DBECommand<OBJECT_TYPE>> {

        @Override
        public void redoCommand(DBECommand<OBJECT_TYPE> command)
        {
            DBUtils.fireObjectAdd(command.getObject());
        }

        @Override
        public void undoCommand(DBECommand<OBJECT_TYPE> command)
        {
            DBUtils.fireObjectRemove(command.getObject());
        }
    }

    public static class DeleteObjectReflector<OBJECT_TYPE extends DBSObject> implements DBECommandReflector<OBJECT_TYPE, DBECommand<OBJECT_TYPE>> {

        @Override
        public void redoCommand(DBECommand<OBJECT_TYPE> command)
        {
            DBUtils.fireObjectRemove(command.getObject());
        }

        @Override
        public void undoCommand(DBECommand<OBJECT_TYPE> command)
        {
            DBUtils.fireObjectAdd(command.getObject());
        }

    }

}
