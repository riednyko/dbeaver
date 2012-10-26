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
package org.jkiss.dbeaver.model.impl.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorInput;
import org.jkiss.dbeaver.ui.preferences.PrefConstants;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Scripts handler
 */
public class ScriptsHandlerImpl extends AbstractResourceHandler {

    static final Log log = LogFactory.getLog(ScriptsHandlerImpl.class);

    public static final String SCRIPT_FILE_EXTENSION = "sql"; //$NON-NLS-1$

    public static IFolder getScriptsFolder(IProject project, boolean forceCreate) throws CoreException
    {
        final IFolder scriptsFolder = DBeaverCore.getInstance().getProjectRegistry().getResourceDefaultRoot(project, ScriptsHandlerImpl.class);
        if (!scriptsFolder.exists() && forceCreate) {
            scriptsFolder.create(true, true, new NullProgressMonitor());
        }
        return scriptsFolder;
    }

    public static IFile findRecentScript(IProject project, DBSDataSourceContainer container) throws CoreException
    {
        List<IFile> scripts = new ArrayList<IFile>();
        findRecentScripts(ScriptsHandlerImpl.getScriptsFolder(project, false), container, scripts);
        long recentTimestamp = 0l;
        IFile recentFile = null;
        for (IFile file : scripts) {
            if (file.getLocalTimeStamp() > recentTimestamp) {
                recentTimestamp = file.getLocalTimeStamp();
                recentFile = file;
            }
        }
        return recentFile;
    }

    public static void findRecentScripts(IFolder folder, DBSDataSourceContainer container, List<IFile> result)
    {
        try {
            for (IResource resource : folder.members()) {
                if (resource instanceof IFile) {
                    if (SQLEditorInput.getScriptDataSource((IFile) resource) == container) {
                        result.add((IFile) resource);
                    }
                } else if (resource instanceof IFolder) {
                    findRecentScripts((IFolder) resource, container, result);
                }

            }
        } catch (CoreException e) {
            log.debug(e);
        }
    }

    public static IFile createNewScript(IProject project, IFolder folder, DBSDataSourceContainer dataSourceContainer) throws CoreException
    {
        final IProgressMonitor progressMonitor = new NullProgressMonitor();

        // Get folder
        IFolder scriptsFolder = folder;
        if (scriptsFolder == null) {
            scriptsFolder = ScriptsHandlerImpl.getScriptsFolder(project, true);
            if (dataSourceContainer != null && dataSourceContainer.getPreferenceStore().getBoolean(PrefConstants.SCRIPT_AUTO_FOLDERS)) {
                IFolder dbFolder = scriptsFolder.getFolder(CommonUtils.escapeFileName(dataSourceContainer.getName()));
                if (dbFolder != null) {
                    if (!dbFolder.exists()) {
                        dbFolder.create(true, true, progressMonitor);
                    }
                    scriptsFolder = dbFolder;
                }
            }
        }

        // Make new script file
        IFile tempFile = ContentUtils.getUniqueFile(scriptsFolder, "Script", SCRIPT_FILE_EXTENSION);
        tempFile.create(new ByteArrayInputStream(new byte[]{}), true, progressMonitor);

        // Save ds container reference
        if (dataSourceContainer != null) {
            SQLEditorInput.setScriptDataSource(tempFile, dataSourceContainer);
        }

        return tempFile;
    }

    @Override
    public int getFeatures(IResource resource)
    {
        if (resource instanceof IFile) {
            return FEATURE_OPEN | FEATURE_DELETE | FEATURE_RENAME;
        }
        return super.getFeatures(resource);
    }

    @Override
    public String getTypeName(IResource resource)
    {
        if (resource instanceof IFolder) {
            return "script folder"; //$NON-NLS-1$
        } else {
            return "script"; //$NON-NLS-1$
        }
    }

    @Override
    public String getResourceDescription(IResource resource)
    {
        if (resource instanceof IFile) {
            return new SQLEditorInput((IFile)resource).getName();
        }
        return super.getResourceDescription(resource);
    }

    @Override
    public DBNResource makeNavigatorNode(DBNNode parentNode, IResource resource) throws CoreException, DBException
    {
        DBNResource node = super.makeNavigatorNode(parentNode, resource);
        if (resource instanceof IFolder) {
            if (resource.getParent() instanceof IProject) {
                node.setResourceImage(DBIcon.SCRIPTS.getImage());
            }
        } else {
            node.setResourceImage(DBIcon.SQL_SCRIPT.getImage());
        }
        return node;
    }

    @Override
    public void openResource(IResource resource, IWorkbenchWindow window) throws CoreException, DBException
    {
        if (resource instanceof IFile) {
            SQLEditorInput sqlInput = new SQLEditorInput((IFile)resource);
            window.getActivePage().openEditor(
                sqlInput,
                SQLEditor.class.getName());
        } else {
            //throw new DBException("Cannot open folder");
        }
    }

}
