/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.actions.datasource;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.IDataSourceContainerProvider;
import org.jkiss.dbeaver.model.IDataSourceContainerProviderEx;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.TextUtils;
import org.jkiss.dbeaver.ui.actions.AbstractDataSourceHandler;
import org.jkiss.dbeaver.ui.navigator.dialogs.SelectDataSourceDialog;

import java.util.Map;

public class SelectActiveDataSourceHandler extends AbstractDataSourceHandler implements IElementUpdater
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        DBPDataSourceContainer dataSource = getCurrentDataSource(HandlerUtil.getActiveWorkbenchWindow(event));
        IProject activeProject = dataSource != null ? dataSource.getRegistry().getProject() : DBWorkbench.getPlatform().getProjectManager().getActiveProject();

        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (!(activeEditor instanceof IDataSourceContainerProviderEx)) {
            return null;
        }

        SelectDataSourceDialog dialog = new SelectDataSourceDialog(
            HandlerUtil.getActiveShell(event),
            activeProject, dataSource);
        dialog.setModeless(true);
        if (dialog.open() == IDialogConstants.CANCEL_ID) {
            return null;
        }
        DBPDataSourceContainer newDataSource = dialog.getDataSource();
        if (newDataSource == dataSource) {
            return null;
        }

        ((IDataSourceContainerProviderEx) activeEditor).setDataSourceContainer(newDataSource);

        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        commandService.refreshElements("org.jkiss.dbeaver.core.select.connection", null);
        commandService.refreshElements("org.jkiss.dbeaver.core.select.schema", null);

        return null;
    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        IWorkbenchWindow workbenchWindow = element.getServiceLocator().getService(IWorkbenchWindow.class);
        DBPDataSourceContainer dataSource = getCurrentDataSource(workbenchWindow);
        String connectionName;
        if (dataSource == null) {
            connectionName = "<No active connection>";
            element.setIcon(DBeaverIcons.getImageDescriptor(DBIcon.TREE_DATABASE));
        } else {
            connectionName = dataSource.getName();
            element.setIcon(DBeaverIcons.getImageDescriptor(dataSource.getDriver().getIcon()));
        }
        GC gc = new GC(workbenchWindow.getShell());
        try {
            connectionName = TextUtils.getShortText(gc, connectionName, 150);
            for (;;) {
                int textWidth = gc.textExtent(connectionName).x;
                if (textWidth >= 150) {
                    break;
                }
                connectionName += " ";
            }
            if (connectionName.endsWith(" ")) {
                connectionName = connectionName.substring(0, connectionName.length() - 1) + ".";
            }
        } finally {
            gc.dispose();
        }
        element.setText(connectionName);
    }

    private DBPDataSourceContainer getCurrentDataSource(IWorkbenchWindow workbenchWindow) {
        if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
            return null;
        }
        IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
        if (activeEditor == null) {
            return null;
        }

        if (activeEditor instanceof IDataSourceContainerProvider) {
            return ((IDataSourceContainerProvider) activeEditor).getDataSourceContainer();
        }
        return null;
    }
}