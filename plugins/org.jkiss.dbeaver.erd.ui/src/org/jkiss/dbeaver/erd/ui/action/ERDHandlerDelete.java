/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
package org.jkiss.dbeaver.erd.ui.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.erd.ui.editor.ERDEditorAdapter;
import org.jkiss.dbeaver.erd.ui.editor.ERDEditorPart;
import org.jkiss.dbeaver.erd.ui.model.ERDDatabaseObjectModifyCommand;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNUtils;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.navigator.dialogs.ConfirmNavigatorNodesDeleteDialog;

import java.util.ArrayList;
import java.util.List;

public class ERDHandlerDelete extends AbstractHandler {
    public ERDHandlerDelete() {

    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Control control = (Control) HandlerUtil.getVariable(event, ISources.ACTIVE_FOCUS_CONTROL_NAME);
        if (control != null) {
            ERDEditorPart editor = ERDEditorAdapter.getEditor(control);
            if (editor != null && !editor.isReadOnly()) {

                ERDDeleteAction deleteAction = new ERDDeleteAction(editor);
                deleteAction.update();
                if (deleteAction.isEnabled()) {
                    // Show confirmation
                    List selectedObjects = deleteAction.getSelectedObjects();
                    List<DBNNode> selectedNodes = new ArrayList<>();
                    Command deleteCommand = deleteAction.createDeleteCommand(selectedObjects);
                    if (deleteCommand instanceof CompoundCommand) {
                        for (Object nc : ((CompoundCommand) deleteCommand).getCommands()) {
                            if (nc instanceof ERDDatabaseObjectModifyCommand) {
                                DBSObject object = ((ERDDatabaseObjectModifyCommand) nc).getDatabaseObject();
                                if (object != null) {
                                    // Its ok to use void monitor here
                                    DBNNode node = DBNUtils.getNodeByObject(new VoidProgressMonitor(), object, true);
                                    if (node != null) {
                                        selectedNodes.add(node);
                                    }
                                }
                            }
                        }
                    }
                    if (!selectedNodes.isEmpty() && ConfirmNavigatorNodesDeleteDialog.of(
                        HandlerUtil.getActiveShell(event),
                        selectedNodes,
                        null
                    ).open() != IDialogConstants.YES_ID) {
                        return null;
                    }
                    deleteAction.run();
                }
            }
        }
        return null;
    }

    private static class ERDDeleteAction extends DeleteAction {
        ERDDeleteAction(ERDEditorPart editor) {
            super((IWorkbenchPart) editor);
        }

        @Override
        public List getSelectedObjects() {
            return super.getSelectedObjects();
        }
    }

}
