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

package org.jkiss.dbeaver.ui.editors.sql.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.registry.DataSourceRegistry;
import org.jkiss.dbeaver.registry.ProjectRegistry;
import org.jkiss.dbeaver.ui.actions.DataSourceHandler;
import org.jkiss.dbeaver.ui.dialogs.connection.SelectDataSourceDialog;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorControl;

public abstract class BaseSQLEditorHandler extends DataSourceHandler {

    static final Log log = LogFactory.getLog(BaseSQLEditorHandler.class);

    protected SQLEditorBase getActiveControl(ExecutionEvent event)
    {
        Object control = HandlerUtil.getVariable(event, ISources.ACTIVE_FOCUS_CONTROL_NAME);
        if (!(control instanceof Composite)) return null;
        for (Composite parent = ((Composite)control).getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof SQLEditorControl) {
                return ((SQLEditorControl) parent).getEditor();
            }
        }
        return null;
    }

    protected DBSDataSourceContainer getCurrentConnection(ExecutionEvent event)
    {
        DBSDataSourceContainer dataSourceContainer = getDataSourceContainer(event, false);
        final ProjectRegistry projectRegistry = DBeaverCore.getInstance().getProjectRegistry();
        IProject project = dataSourceContainer != null ? dataSourceContainer.getRegistry().getProject() : projectRegistry.getActiveProject();
        if (dataSourceContainer == null) {
            final DataSourceRegistry dataSourceRegistry = projectRegistry.getDataSourceRegistry(project);
            if (dataSourceRegistry.getDataSources().size() == 1) {
                dataSourceContainer = dataSourceRegistry.getDataSources().get(0);
            } else if (!dataSourceRegistry.getDataSources().isEmpty()) {
                dataSourceContainer = SelectDataSourceDialog.selectDataSource(
                    HandlerUtil.getActiveShell(event));
            }
        }
        return dataSourceContainer;
    }

    protected IFolder getCurrentFolder(ExecutionEvent event)
    {
        final ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
            final Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNResource && ((DBNResource)element).getResource() instanceof IFolder) {
                return (IFolder) ((DBNResource)element).getResource();
            }
        }
        return null;
    }

}