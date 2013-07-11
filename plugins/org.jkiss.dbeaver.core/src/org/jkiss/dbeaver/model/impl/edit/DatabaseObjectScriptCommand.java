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
package org.jkiss.dbeaver.model.impl.edit;

import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * Script command
 */
public class DatabaseObjectScriptCommand<OBJECT_TYPE extends DBSObject> extends DBECommandAbstract<OBJECT_TYPE> {

    private String script;

    public DatabaseObjectScriptCommand(OBJECT_TYPE object, String title, String script)
    {
        super(object, title);
        this.script = script;
    }

    @Override
    public void updateModel()
    {
    }

    @Override
    public IDatabasePersistAction[] getPersistActions()
    {
        return new IDatabasePersistAction[] {
            new AbstractDatabasePersistAction(
                getTitle(),
                script)
        };
    }

}