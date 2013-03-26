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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.dialogs.connection.SelectDataSourceDialog;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorInput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NavigatorHandlerAssociateScript extends NavigatorHandlerObjectBase {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final Shell activeShell = HandlerUtil.getActiveShell(event);
        List<IFile> scripts = new ArrayList<IFile>();
        final ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            for (Iterator iter = ((IStructuredSelection)selection).iterator(); iter.hasNext(); ) {
                final DBNNode node = RuntimeUtils.getObjectAdapter(iter.next(), DBNNode.class);
                if (node instanceof DBNResource) {
                    IResource resource = ((DBNResource) node).getResource();
                    if (resource instanceof IFile) {
                        scripts.add((IFile) resource);
                    }
                }
            }
        }
        if (!scripts.isEmpty()) {
            DataSourceDescriptor dataSourceDescriptor = SelectDataSourceDialog.selectDataSource(activeShell);
            if (dataSourceDescriptor != null) {
                for (IFile script : scripts) {
                    SQLEditorInput.setScriptDataSource(script, dataSourceDescriptor, true);
                }
            }
        }
        return null;
    }

}