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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabaseEditor;
import org.jkiss.dbeaver.model.edit.DBEObjectMaker;
import org.jkiss.dbeaver.model.edit.DBEObjectManager;
import org.jkiss.dbeaver.model.navigator.DBNContainer;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.registry.EntityEditorsRegistry;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.editors.entity.EntityEditor;
import org.jkiss.dbeaver.ui.editors.entity.EntityEditorInput;
import org.jkiss.dbeaver.ui.views.navigator.database.DatabaseNavigatorView;

import java.lang.reflect.InvocationTargetException;

public abstract class NavigatorHandlerObjectCreateBase extends NavigatorHandlerObjectBase {

    protected boolean createNewObject(final IWorkbenchWindow workbenchWindow, DBNNode element, DBNDatabaseNode copyFrom)
    {
        try {
            DBNContainer container = null;
            if (element instanceof DBNContainer) {
                container = (DBNContainer) element;
            } else {
                DBNNode parentNode = element.getParentNode();
                if (parentNode instanceof DBNContainer) {
                    container = (DBNContainer) parentNode;
                }
            }
            if (container == null) {
                throw new DBException("Can't detect container for '" + element.getNodeName() + "'");
            }
            Class<?> childType = container.getChildrenClass();
            if (childType == null) {
                throw new DBException("Can't determine child element type for container '" + container + "'");
            }

            DBSObject sourceObject = copyFrom == null ? null : copyFrom.getObject();
            if (sourceObject != null && sourceObject.getClass() != childType) {
                throw new DBException("Can't create '" + childType.getName() + "' from '" + sourceObject.getClass().getName() + "'");
            }

            final EntityEditorsRegistry editorsRegistry = DBeaverCore.getInstance().getEditorsRegistry();
            DBEObjectManager<?> objectManager = editorsRegistry.getObjectManager(childType);
            if (objectManager == null) {
                throw new DBException("Object manager not found for type '" + childType.getName() + "'");
            }
            DBEObjectMaker objectMaker = (DBEObjectMaker) objectManager;
            final boolean openEditor = (objectMaker.getMakerOptions() & DBEObjectMaker.FEATURE_EDITOR_ON_CREATE) != 0;
            CommandTarget commandTarget = getCommandTarget(
                workbenchWindow,
                container,
                childType,
                openEditor);

            final Object parentObject = container.getValueObject();
            DBSObject result = objectMaker.createNewObject(workbenchWindow, commandTarget.getEditor(), commandTarget.getContext(), parentObject, sourceObject);
            if (result == null) {
                return true;
            }
            if (commandTarget == null) {
                throw new DBException("Non-database container '" + container + "' must save new objects itself - command context is not accessible");
            }

            if ((objectMaker.getMakerOptions() & DBEObjectMaker.FEATURE_SAVE_IMMEDIATELY) != 0) {
                // Save object manager's content
                ObjectSaver objectSaver = new ObjectSaver(commandTarget.getContext());
                DBeaverCore.getInstance().runInProgressService(objectSaver);
            }

            final DBNNode newChild = DBeaverCore.getInstance().getNavigatorModel().findNode(result);
            if (newChild != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run()
                    {
                        DatabaseNavigatorView view = UIUtils.findView(workbenchWindow, DatabaseNavigatorView.class);
                        if (view != null) {
                            view.showNode(newChild);
                        }
                    }
                });
                IDatabaseEditor editor = commandTarget.getEditor();
                if (editor != null) {
                    // Just activate existing editor
                    workbenchWindow.getActivePage().activate(editor);
                } else if (openEditor && newChild instanceof DBNDatabaseNode) {
                    // Open new one with existing context
                    EntityEditorInput editorInput = new EntityEditorInput(
                        (DBNDatabaseNode) newChild,
                        commandTarget.getContext());
                    workbenchWindow.getActivePage().openEditor(
                        editorInput,
                        EntityEditor.class.getName());
                }
            } else {
                throw new DBException("Can't find node corresponding to new object");
            }
        } catch (InterruptedException e) {
            // do nothing
        }
        catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException)e).getTargetException();
            }
            UIUtils.showErrorDialog(workbenchWindow.getShell(), "Create object", null, e);
            return false;
        }

        return true;
    }

}