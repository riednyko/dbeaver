/*
 * Copyright (C) 2010-2014 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ext.oracle.edit;

import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.oracle.model.*;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.AbstractDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.JDBCTableManager;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle table manager
 */
public class OracleTableManager extends JDBCTableManager<OracleTable, OracleSchema> implements DBEObjectRenamer<OracleTable> {

    private static final Class<?>[] CHILD_TYPES = {
        OracleTableColumn.class,
        OracleTableConstraint.class,
        OracleTableForeignKey.class,
        OracleTableIndex.class
    };

    @Override
    public DBSObjectCache<? extends DBSObject, OracleTable> getObjectsCache(OracleTable object)
    {
        return (DBSObjectCache) object.getSchema().tableCache;
    }

    @Override
    protected OracleTable createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, OracleSchema parent, Object copyFrom)
    {
        return new OracleTable(
            parent,
            DBObjectNameCaseTransformer.transformName(parent, "NewTable")); //$NON-NLS-1$
    }

    @Override
    protected IDatabasePersistAction[] makeObjectModifyActions(ObjectChangeCommand command)
    {
        final OracleTable table = command.getObject();
        boolean hasComment = command.getProperty("comment") != null;
        List<IDatabasePersistAction> actions = new ArrayList<IDatabasePersistAction>(2);
        if (!hasComment || command.getProperties().size() > 1) {
            StringBuilder query = new StringBuilder("ALTER TABLE "); //$NON-NLS-1$
            query.append(command.getObject().getFullQualifiedName()).append(" "); //$NON-NLS-1$
            appendTableModifiers(command.getObject(), command, query);
            actions.add(new AbstractDatabasePersistAction(query.toString()));
        }
        if (hasComment) {
            actions.add(new AbstractDatabasePersistAction(
                "Comment table",
                "COMMENT ON TABLE " + table.getFullQualifiedName() +
                    " IS '" + table.getComment() + "'"));
        }

        return actions.toArray(new IDatabasePersistAction[actions.size()]);
    }

    @Override
    protected void appendTableModifiers(OracleTable table, NestedObjectCommand tableProps, StringBuilder ddl)
    {
    }

    @Override
    protected IDatabasePersistAction[] makeObjectRenameActions(ObjectRenameCommand command)
    {
        return new IDatabasePersistAction[] {
            new AbstractDatabasePersistAction(
                "Rename table",
                "RENAME TABLE " + command.getObject().getFullQualifiedName() + //$NON-NLS-1$
                    " TO " + DBUtils.getQuotedIdentifier(command.getObject().getDataSource(), command.getNewName())) //$NON-NLS-1$
        };
    }

    @Override
    public Class<?>[] getChildTypes()
    {
        return CHILD_TYPES;
    }

    @Override
    public void renameObject(DBECommandContext commandContext, OracleTable object, String newName) throws DBException
    {
        processObjectRename(commandContext, object, newName);
    }

}
