/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ui.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProcessListener;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithResult;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.registry.DataSourceRegistry;
import org.jkiss.dbeaver.registry.ProjectRegistry;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DatabaseEditorInputFactory implements IElementFactory
{
    static final Log log = LogFactory.getLog(DatabaseEditorInputFactory.class);

    static final String ID_FACTORY = DatabaseEditorInputFactory.class.getName(); //$NON-NLS-1$

    private static final String TAG_CLASS = "class"; //$NON-NLS-1$
    private static final String TAG_DATA_SOURCE = "data-source"; //$NON-NLS-1$
    private static final String TAG_NODE = "node"; //$NON-NLS-1$
    private static final String TAG_ACTIVE_PAGE = "page"; //$NON-NLS-1$
    private static final String TAG_ACTIVE_FOLDER = "folder"; //$NON-NLS-1$

    public DatabaseEditorInputFactory()
    {
    }

    @Override
     public IAdaptable createElement(IMemento memento)
    {
        // Get the node path.
        final String inputClass = memento.getString(TAG_CLASS);
        final String nodePath = memento.getString(TAG_NODE);
        final String dataSourceId = memento.getString(TAG_DATA_SOURCE);
        if (nodePath == null || inputClass == null || dataSourceId == null) {
            log.error("Corrupted memento"); //$NON-NLS-2$
            return null;
        }
        final String activePageId = memento.getString(TAG_ACTIVE_PAGE);
        final String activeFolderId = memento.getString(TAG_ACTIVE_FOLDER);

        DataSourceDescriptor dataSourceContainer = null;
        ProjectRegistry projectRegistry = DBeaverCore.getInstance().getProjectRegistry();
        for (IProject project : DBeaverCore.getInstance().getLiveProjects()) {
            DataSourceRegistry dataSourceRegistry = projectRegistry.getDataSourceRegistry(project);
            if (dataSourceRegistry != null) {
                dataSourceContainer = dataSourceRegistry.getDataSource(dataSourceId);
                if (dataSourceContainer != null) {
                    break;
                }
            }
        }
        if (dataSourceContainer == null) {
            log.error("Can't find data source '" + dataSourceId + "'"); //$NON-NLS-2$
            return null;
        }
        final DBSDataSourceContainer dsObject = dataSourceContainer;

        DBRRunnableWithResult<IEditorInput> opener = new DBRRunnableWithResult<IEditorInput>() {
            private IStatus errorStatus;
            @Override
            public void run(final DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                DBNDataSource dsNode = null;
                try {
                    dsNode = (DBNDataSource)DBeaverCore.getInstance().getNavigatorModel().getNodeByObject(dsObject);
                    dsNode.initializeNode(monitor, new DBRProcessListener() {
                        @Override
                        public void onProcessFinish(IStatus status)
                        {
                            if (!status.isOK()) {
                                errorStatus = status;
                                return;
                            }
                            try {
                                DBNNode node = DBeaverCore.getInstance().getNavigatorModel().getNodeByPath(monitor, nodePath);
                                if (node != null) {
                                    Class<?> aClass = Class.forName(inputClass);
                                    Constructor<?> constructor ;
                                    for (Class nodeType = node.getClass(); ; nodeType = nodeType.getSuperclass()) {
                                        try {
                                            constructor = aClass.getConstructor(nodeType);
                                            break;
                                        } catch (NoSuchMethodException e) {
                                            // No such constructor
                                        }
                                    }
                                    if (constructor != null) {
                                        DatabaseEditorInput input = DatabaseEditorInput.class.cast(constructor.newInstance(node));
                                        input.setDefaultPageId(activePageId);
                                        input.setDefaultFolderId(activeFolderId);
                                        result = input;
                                    } else {
                                        throw new DBException("Can't create object instance [" + inputClass + "]");
                                    }
                                }
                            } catch (Exception e) {
                                errorStatus = new Status(IStatus.ERROR, DBeaverCore.getCorePluginID(), e.getMessage(), e);
                                log.error(e);
                            }
                        }
                    });
                } catch (Exception e) {
                    errorStatus = new Status(IStatus.ERROR, DBeaverCore.getCorePluginID(), e.getMessage(), e);
                }
                if (result == null && errorStatus != null) {
                    result = new ErrorEditorInput(errorStatus, dsNode);
                }
            }
        };
        try {
            DBeaverUI.runInProgressService(opener);
        } catch (InvocationTargetException e) {
            log.error("Error initializing database editor input", e.getTargetException());
        } catch (InterruptedException e) {
            // ignore
        }
        return opener.getResult();
    }

    public static void saveState(IMemento memento, DatabaseEditorInput input)
    {
        DBPDataSource dataSource = input.getDataSource();
        if (dataSource == null) {
            // Detached - nothing to save
            return;
        }
        DBNDatabaseNode node = input.getTreeNode();
        memento.putString(TAG_CLASS, input.getClass().getName());
        memento.putString(TAG_DATA_SOURCE, dataSource.getContainer().getId());
        memento.putString(TAG_NODE, node.getNodeItemPath());
        if (!CommonUtils.isEmpty(input.getDefaultPageId())) {
            memento.putString(TAG_ACTIVE_PAGE, input.getDefaultPageId());
        }
        if (!CommonUtils.isEmpty(input.getDefaultFolderId())) {
            memento.putString(TAG_ACTIVE_FOLDER, input.getDefaultFolderId());
        }
    }

}