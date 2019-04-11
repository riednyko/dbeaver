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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.app.DBPDataSourceRegistry;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.registry.DataSourceRegistry;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.TextUtils;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.AbstractDataSourceHandler;
import org.jkiss.dbeaver.ui.editors.EditorUtils;
import org.jkiss.dbeaver.ui.navigator.NavigatorUtils;
import org.jkiss.dbeaver.ui.navigator.dialogs.SelectDataSourceDialog;

import java.util.*;

public class SelectActiveDataSourceHandler extends AbstractDataSourceHandler implements IElementUpdater
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        DBPDataSourceContainer dataSource = DataSourceToolbarUtils.getCurrentDataSource(HandlerUtil.getActiveWorkbenchWindow(event));
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

        return null;
    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        IWorkbenchWindow workbenchWindow = element.getServiceLocator().getService(IWorkbenchWindow.class);
        DBPDataSourceContainer dataSource = DataSourceToolbarUtils.getCurrentDataSource(workbenchWindow);
        String connectionName;
        DBPImage connectionIcon;
        if (dataSource == null) {
            connectionName = "<No active connection>";
            connectionIcon = DBIcon.TREE_DATABASE;
        } else {
            connectionName = dataSource.getName();
            connectionIcon = dataSource.getDriver().getIcon();
        }
        GC gc = new GC(workbenchWindow.getShell());
        try {
            connectionName = TextUtils.getShortText(gc, connectionName, 200);
        } finally {
            gc.dispose();
        }
        element.setText(connectionName);
        element.setIcon(DBeaverIcons.getImageDescriptor(connectionIcon));
        element.setTooltip(CoreMessages.toolbar_datasource_selector_combo_datasource_tooltip);
    }

    private static List<? extends DBPDataSourceContainer> getAvailableDataSources() {
        //Get project from active editor
        IWorkbenchWindow workbenchWindow = UIUtils.getActiveWorkbenchWindow();
        if (workbenchWindow != null && workbenchWindow.getActivePage() != null) {
            final IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
            if (activeEditor != null) {
                IFile curFile = EditorUtils.getFileFromInput(activeEditor.getEditorInput());
                if (curFile != null) {
                    DBPDataSourceContainer fileDataSource = EditorUtils.getFileDataSource(curFile);
                    if (fileDataSource != null) {
                        return fileDataSource.getRegistry().getDataSources();
                    }
                    final DBPDataSourceRegistry dsRegistry = DBWorkbench.getPlatform().getProjectManager().getDataSourceRegistry(curFile.getProject());
                    if (dsRegistry != null) {
                        return dsRegistry.getDataSources();
                    }
                }
            }

            final DBPDataSourceContainer dataSourceContainer = getDataSourceContainer(workbenchWindow.getActivePage().getActivePart());
            if (dataSourceContainer != null) {
                return dataSourceContainer.getRegistry().getDataSources();
            } else {
                return DataSourceRegistry.getAllDataSources();
            }
        }
        return Collections.emptyList();
    }

    public static class MenuContributor extends DataSourceMenuContributor {
        @Override
        protected void fillContributionItems(List<IContributionItem> menuItems) {
            IWorkbenchWindow workbenchWindow = UIUtils.getActiveWorkbenchWindow();
            if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
                return;
            }
            IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
            if (!(activeEditor instanceof IDataSourceContainerProviderEx)) {
                return;
            }

            List<? extends DBPDataSourceContainer> dataSources = getAvailableDataSources();
            List<? extends DBPDataSourceContainer> connectedDataSources = new ArrayList<>(dataSources);
            connectedDataSources.removeIf(o -> !o.isConnected());
            dataSources.removeAll(connectedDataSources);

            List<DBPDataSourceContainer> singleDataSources = new ArrayList<>();
            Map<DBPDriver, List<DBPDataSourceContainer>> driverMap = new TreeMap<>(DBUtils.nameComparator());
            for (DBPDataSourceContainer ds : dataSources) {
                List<DBPDataSourceContainer> driverDS = driverMap.computeIfAbsent(ds.getDriver(), k -> new ArrayList<>());
                driverDS.add(ds);
            }
            for (Iterator<Map.Entry<DBPDriver, List<DBPDataSourceContainer>>> driverIter = driverMap.entrySet().iterator(); driverIter.hasNext(); ) {
                List<DBPDataSourceContainer> dsList = driverIter.next().getValue();
                if (dsList.size() == 1) {
                    singleDataSources.add(dsList.get(0));
                    driverIter.remove();
                }
            }
            singleDataSources.sort(DBUtils.nameComparator());

            DBPDataSourceContainer curDataSource = getDataSourceContainer(workbenchWindow.getActivePage().getActivePart());
            for (DBPDataSourceContainer ds : connectedDataSources) {
                DBNDatabaseNode dsNode = NavigatorUtils.getNodeByObject(ds);
                menuItems.add(
                    new ActionContributionItem(
                        createDataSourceChangeAction((IDataSourceContainerProviderEx) activeEditor, curDataSource, ds, dsNode)));
            }
            menuItems.add(new Separator());
            for (Map.Entry<DBPDriver, List<DBPDataSourceContainer>> de : driverMap.entrySet()) {
                DBPDriver driver = de.getKey();
                MenuManager driverMenu = new MenuManager(
                    driver.getName(),
                    DBeaverIcons.getImageDescriptor(driver.getIcon()),
                    driver.getId());
                for (DBPDataSourceContainer ds : de.getValue()) {
                    driverMenu.add(
                        createDataSourceChangeAction(
                            (IDataSourceContainerProviderEx)activeEditor, curDataSource, ds, null));
                }
                menuItems.add(driverMenu);
            }
            menuItems.add(new Separator());
            for (DBPDataSourceContainer ds : singleDataSources) {
                menuItems.add(
                    new ActionContributionItem(
                        createDataSourceChangeAction((IDataSourceContainerProviderEx)activeEditor, curDataSource, ds, NavigatorUtils.getNodeByObject(ds))));
            }
        }

        private Action createDataSourceChangeAction(IDataSourceContainerProviderEx activeEditor, DBPDataSourceContainer curDataSource, DBPDataSourceContainer newDataSource, DBNDatabaseNode dsNode) {
            return new Action(newDataSource.getName(), Action.AS_CHECK_BOX) {
                {
                    if (dsNode != null) {
                        setImageDescriptor(DBeaverIcons.getImageDescriptor(dsNode.getNodeIcon()));
                    }
                }
                @Override
                public boolean isChecked() {
                    return newDataSource == curDataSource;
                }
                @Override
                public void run() {
                    activeEditor.setDataSourceContainer(newDataSource);
                }
            };
        }
    }

}