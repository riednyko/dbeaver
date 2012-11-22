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
package org.jkiss.dbeaver.ui.actions.navigator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabaseEditor;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.ui.IFolderedPart;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEStructEditor;
import org.jkiss.dbeaver.model.impl.edit.DBECommandContextImpl;
import org.jkiss.dbeaver.model.navigator.DBNContainer;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseFolder;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.ViewSQLDialog;
import org.jkiss.dbeaver.ui.views.navigator.database.DatabaseNavigatorView;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class NavigatorHandlerObjectBase extends AbstractHandler {

    static final Log log = LogFactory.getLog(NavigatorHandlerObjectBase.class);

    static boolean updateUI = true;

    protected static class CommandTarget {
        private DBECommandContext context;
        private IDatabaseEditor editor;
        private IDatabaseEditorInput editorInput;
        private CommandTarget()
        {
        }
        private CommandTarget(DBECommandContextImpl context)
        {
            this.context = context;
        }
        public CommandTarget(IDatabaseEditor editor)
        {
            this.editor = editor;
            this.context = editor.getEditorInput().getCommandContext();
        }

        public DBECommandContext getContext()
        {
            return context;
        }
        public IDatabaseEditor getEditor()
        {
            return editor;
        }
        public IDatabaseEditorInput getEditorInput()
        {
            return editorInput;
        }
    }

    protected static CommandTarget getCommandTarget(
        IWorkbenchWindow workbenchWindow,
        DBNContainer container,
        Class<?> childType,
        boolean openEditor)
        throws DBException
    {
        final Object parentObject = container.getValueObject();

        DBSObject objectToSeek = null;
        if (parentObject instanceof DBSObject) {
            final DBEStructEditor parentStructEditor = DBeaverCore.getInstance().getEditorsRegistry().getObjectManager(parentObject.getClass(), DBEStructEditor.class);
            if (parentStructEditor != null && RuntimeUtils.isTypeSupported(childType, parentStructEditor.getChildTypes())) {
                objectToSeek = (DBSObject) parentObject;
            }
        }
        if (objectToSeek != null) {
            for (final IEditorReference editorRef : workbenchWindow.getActivePage().getEditorReferences()) {
                final IEditorPart editor = editorRef.getEditor(false);
                if (editor instanceof IDatabaseEditor) {
                    final IDatabaseEditorInput editorInput = ((IDatabaseEditor) editor).getEditorInput();
                    if (editorInput.getDatabaseObject() == objectToSeek) {
                        workbenchWindow.getActivePage().activate(editor);
                        switchEditorFolder(container, editor);
                        return new CommandTarget((IDatabaseEditor) editor);
                    }
                }
            }

            if (openEditor && container instanceof DBNDatabaseNode) {
                final IDatabaseEditor editor = (IDatabaseEditor) NavigatorHandlerObjectOpen.openEntityEditor(
                    (DBNDatabaseNode) container,
                    null,
                    workbenchWindow);
                if (editor != null) {
                    switchEditorFolder(container, editor);
                    return new CommandTarget(editor);
                }
            }
        }
        if (container instanceof DBNDatabaseNode) {
            // No editor found and no need to create one - create new command context
            DBSDataSourceContainer dsContainer = ((DBNDatabaseNode) container).getObject().getDataSource().getContainer();
            return new CommandTarget(new DBECommandContextImpl(dsContainer));
        } else {
            return new CommandTarget();
        }
    }

    private static void switchEditorFolder(DBNContainer container, IEditorPart editor)
    {
        if (editor instanceof IFolderedPart && container instanceof DBNDatabaseFolder) {
            ((IFolderedPart) editor).switchFolder(container.getChildrenType());
        }
    }

    public static DBNDatabaseNode getNodeByObject(DBSObject object)
    {
        DBNModel model = DBeaverCore.getInstance().getNavigatorModel();
        DBNDatabaseNode node = model.findNode(object);
        if (node == null) {
            NodeLoader nodeLoader = new NodeLoader(model, object);
            try {
                DBeaverCore.getInstance().runInProgressService(nodeLoader);
            } catch (InvocationTargetException e) {
                log.warn("Could not load node for object '" + object.getName() + "'", e.getTargetException());
            } catch (InterruptedException e) {
                // do nothing
            }
            node = nodeLoader.node;
        }
        return node;
    }

    protected static boolean showScript(IWorkbenchWindow workbenchWindow, DBECommandContext commandContext, String dialogTitle)
    {
        Collection<? extends DBECommand> commands = commandContext.getFinalCommands();
        StringBuilder script = new StringBuilder();
        for (DBECommand command : commands) {
            script.append(DBUtils.generateScript(
                commandContext.getDataSourceContainer().getDataSource(),
                command.getPersistActions()));
        }
        DatabaseNavigatorView view = UIUtils.findView(workbenchWindow, DatabaseNavigatorView.class);
        if (view != null) {
            ViewSQLDialog dialog = new ViewSQLDialog(
                view.getSite(),
                commandContext.getDataSourceContainer(),
                dialogTitle,
                script.toString());
            dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
            dialog.setShowSaveButton(true);
            return dialog.open() == IDialogConstants.PROCEED_ID;
        } else {
            return false;
        }
    }

    private static class NodeLoader implements DBRRunnableWithProgress {
        private final DBNModel model;
        private final DBSObject object;
        private DBNDatabaseNode node;

        public NodeLoader(DBNModel model, DBSObject object)
        {
            this.model = model;
            this.object = object;
        }

        @Override
        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            node = model.getNodeByObject(monitor, object, true);
        }
    }

    protected static class ObjectSaver implements DBRRunnableWithProgress {
        private final DBECommandContext commander;

        public ObjectSaver(DBECommandContext commandContext)
        {
            this.commander = commandContext;
        }

        @Override
        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            try {
                commander.saveChanges(monitor);
            } catch (DBException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

}