/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2018 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
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


package org.jkiss.dbeaver.debug.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.debug.DBGConstants;
import org.jkiss.dbeaver.debug.core.DebugCore;
import org.jkiss.dbeaver.debug.ui.internal.DebugUIMessages;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.ui.DBUserInterface;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class DatabaseLaunchShortcut implements ILaunchShortcut2 {

    private static final Log log = Log.getLog(DatabaseLaunchShortcut.class);

    private final String configurationTypeId;
    private final String launchObjectName;
    
    private IWorkbenchPartSite workbenchPartSite;

    public DatabaseLaunchShortcut(String typeId, String objectName) {
        this.configurationTypeId = typeId;
        this.launchObjectName = objectName;
    }

    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWindow != null) {
                IWorkbenchPage activePage = activeWindow.getActivePage();
                if (activePage != null) {
                    IWorkbenchPart activePart = activePage.getActivePart();
                    if (activePart != null) {
                        workbenchPartSite = activePart.getSite();
                    }
                }
            }
            Object[] array = ((IStructuredSelection) selection).toArray();
            searchAndLaunch(array, mode, getSelectionEmptyMessage());
        }
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        IEditorSite editorSite = editor.getEditorSite();
        workbenchPartSite = editorSite;
        ISelection selection = editorSite.getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            Object[] array = ((IStructuredSelection) selection).toArray();
            searchAndLaunch(array, mode, getEditorEmptyMessage());
        } else {
            DBSObject databaseObject = DebugUI.extractDatabaseObject(editor);
            if (databaseObject != null) {
                Object[] array = new Object[] { databaseObject };
                searchAndLaunch(array, mode, getEditorEmptyMessage());
            }
        }

    }
    
    protected IWorkbenchPartSite getWorkbenchPartSite() {
        return workbenchPartSite;
    }

    protected String getSelectionEmptyMessage() {
        String message = DebugUIMessages.DatabaseLaunchShortcut_e_selection_empty;
        return NLS.bind(message, launchObjectName);
    }

    protected String getEditorEmptyMessage() {
        String message = DebugUIMessages.DatabaseLaunchShortcut_e_editor_empty;
        return NLS.bind(message, launchObjectName);
    }

    protected String getLaunchableSelectionTitle(String mode) {
        String message = DebugUIMessages.DatabaseLaunchShortcut_select_title;
        return NLS.bind(message, launchObjectName);
    }

    protected String getLaunchableSelectionMessage(String mode) {
        String message = DebugUIMessages.DatabaseLaunchShortcut_select_message;
        return NLS.bind(message, launchObjectName);
    }

    protected ILabelProvider getLaunchableSelectionRenderer() {
        return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
    }

    protected Shell getShell() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    protected void searchAndLaunch(Object[] scope, String mode, String emptyMessage) {
        List<DBSObject> extracted = DebugCore.extractLaunchable(scope);
        DBSObject launchable = null;
        if (extracted.size() == 0) {
            MessageDialog.openError(getShell(), DebugUIMessages.DatabaseLaunchShortcut_e_launch, emptyMessage);
        } else if (extracted.size() > 1) {
            launchable = selectLaunchable(getShell(), extracted, mode);
        } else {
            launchable = extracted.get(0);
        }
        if (launchable != null) {
            try {
                launch(launchable, mode);
            } catch (CoreException e) {
                DBUserInterface.getInstance().showError(DebugUIMessages.DatabaseLaunchShortcut_e_launch, "Cannot launch debug", e.getStatus());
            }
        }
    }

    protected void launch(DBSObject launchable, String mode) throws CoreException {
        Map<String, Object> databaseContext = DebugCore.resolveDatabaseContext(launchable);
        List<ILaunchConfiguration> configs = getCandidates(launchable, getConfigurationType(), databaseContext);
        if (configs != null) {
            ILaunchConfiguration config = null;
            int count = configs.size();
            if (count == 1) {
                config = configs.get(0);
            } else if (count > 1) {
                config = chooseConfiguration(configs, mode);
                if (config == null) {
                    return;
                }
            }
            if (config == null) {
                config = createConfiguration(launchable);
            }
            if (config != null) {
                DebugCore.postDebuggerSourceEvent(config.getAttribute(DBGConstants.ATTR_NODE_PATH, (String) null));
                DebugUITools.launch(config, mode);
            }
        }
    }

    protected ILaunchConfigurationType getConfigurationType() {
        ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
        String configurationTypeId = getConfigurationTypeId();
        return lm.getLaunchConfigurationType(configurationTypeId);
    }

    protected String getConfigurationTypeId() {
        return configurationTypeId;
    }
    
    protected String getLaunchObjectName() {
        return launchObjectName;
    }

    protected DBSObject selectLaunchable(Shell shell, List<DBSObject> launchables, String mode) {
        String title = getLaunchableSelectionTitle(mode);
        String message = getLaunchableSelectionMessage(mode);
        ILabelProvider renderer = getLaunchableSelectionRenderer();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, renderer);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setElements(launchables.toArray());
        dialog.setMultipleSelection(false);

        if (dialog.open() != Window.OK) {
            return null;
        }

        return (DBSObject) dialog.getFirstResult();
    }

    protected List<ILaunchConfiguration> getCandidates(DBSObject launchable, ILaunchConfigurationType configType, Map<String, Object> databaseContext) {
        List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
        try {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfiguration[] configs = launchManager.getLaunchConfigurations(configType);
            candidateConfigs = new ArrayList<>(configs.length);
            for (ILaunchConfiguration config : configs) {
                if (isCandidate(config, launchable, databaseContext)) {
                    candidateConfigs.add(config);
                }
            }
        } catch (CoreException e) {
            log.log(e.getStatus());
        }
        return candidateConfigs;
    }

    protected boolean isCandidate(ILaunchConfiguration config, DBSObject launchable, Map<String, Object> databaseContext) {
        if (!config.exists()) {
            return false;
        }

        try {
            String dsId = config.getAttribute(DBGConstants.ATTR_DATASOURCE_ID, (String) null);
            return CommonUtils.equalObjects(dsId, launchable.getDataSource().getContainer().getId());
        } catch (CoreException e) {
            return false;
        }
    }

    protected ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) {
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setElements(configList.toArray());
        dialog.setTitle(DebugUIMessages.DatabaseLaunchShortcut_select_configuration_title);
        dialog.setMessage(DebugUIMessages.DatabaseLaunchShortcut_select_configuration_message);
        dialog.setMultipleSelection(false);
        int result = dialog.open();
        labelProvider.dispose();
        if (result == Window.OK) {
            return (ILaunchConfiguration) dialog.getFirstResult();
        }
        return null;
    }

    protected abstract ILaunchConfiguration createConfiguration(DBSObject launchable) throws CoreException;

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
        // let the framework resolve configurations based on resource mapping
        return null;
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
        // let the framework resolve configurations based on resource mapping
        return null;
    }

    @Override
    public IResource getLaunchableResource(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (ss.size() == 1) {
                Object element = ss.getFirstElement();
                if (element instanceof IAdaptable) {
                    return getLaunchableResource((IAdaptable) element);
                }
            }
        }
        return null;
    }

    @Override
    public IResource getLaunchableResource(IEditorPart editorpart) {
        return getLaunchableResource(editorpart.getEditorInput());
    }

    protected IResource getLaunchableResource(IAdaptable adaptable) {
        return Adapters.adapt(adaptable, IResource.class);
    }

}
