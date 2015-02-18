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
package org.jkiss.dbeaver.ui.editors.entity.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.*;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.ext.IDatabaseEditor;
import org.jkiss.dbeaver.ext.IDatabaseEditorContributorUser;
import org.jkiss.dbeaver.ext.ui.IProgressControlProvider;
import org.jkiss.dbeaver.ext.ui.IRefreshableContainer;
import org.jkiss.dbeaver.ext.ui.IRefreshablePart;
import org.jkiss.dbeaver.ext.ui.ISearchContextProvider;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseFolder;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.registry.editor.EntityEditorDescriptor;
import org.jkiss.dbeaver.registry.tree.DBXTreeItem;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.navigator.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.controls.ProgressPageControl;
import org.jkiss.dbeaver.ui.controls.folders.*;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.dbeaver.ui.editors.entity.GlobalContributorManager;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ObjectPropertiesEditor
 */
public class ObjectPropertiesEditor extends AbstractDatabaseObjectEditor<DBSObject>
    implements IRefreshablePart, IProgressControlProvider, IFolderContainer, ISearchContextProvider, IRefreshableContainer
{
    static final Log log = Log.getLog(ObjectPropertiesEditor.class);

    private FolderComposite folderComposite;
    private ObjectEditorPageControl pageControl;
    private final List<IFolderListener> folderListeners = new ArrayList<IFolderListener>();
    private String curFolderId;

    private final List<IRefreshablePart> refreshClients = new ArrayList<IRefreshablePart>();
    private final List<ISaveablePart> nestedSaveable = new ArrayList<ISaveablePart>();
    private final Map<IFolder, IEditorActionBarContributor> pageContributors = new HashMap<IFolder, IEditorActionBarContributor>();

    public ObjectPropertiesEditor()
    {
    }

    @Override
    public void createPartControl(Composite parent)
    {
        // Add lazy props listener
        //PropertiesContributor.getInstance().addLazyListener(this);

        pageControl = new ObjectEditorPageControl(parent, SWT.NONE, this);

        DBNNode node = getEditorInput().getTreeNode();

        Composite container = new Composite(pageControl, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.verticalSpacing = 5;
        gl.horizontalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        container.setLayout(gl);

        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (node == null) {
            return;
        }
        createPathPanel(node, container);
        //createNamePanel(node, container);

        pageControl.createProgressPanel();

        createPropertyBrowser(container);
    }

    private void createPathPanel(DBNNode node, Composite container)
    {
        // Path
        Composite infoGroup = new Composite(container, SWT.BORDER);//createControlGroup(container, "Path", 3, GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING, 0);
        infoGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        infoGroup.setLayout(new RowLayout());

        List<DBNDatabaseNode> nodeList = new ArrayList<DBNDatabaseNode>();
        for (DBNNode n = node; n != null; n = n.getParentNode()) {
            if (n instanceof DBNDatabaseNode) {
                nodeList.add(0, (DBNDatabaseNode)n);
            }
        }
        for (final DBNDatabaseNode databaseNode : nodeList) {
            createPathRow(
                infoGroup,
                databaseNode.getNodeIconDefault(),
                databaseNode.getNodeType(),
                databaseNode.getNodeName(),
                databaseNode == node ? null : new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        NavigatorHandlerObjectOpen.openEntityEditor(databaseNode, null, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                    }
                });
        }
    }

    private void createPropertyBrowser(Composite container)
    {
        // Properties
        Composite propsPlaceholder = new Composite(container, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        propsPlaceholder.setLayoutData(gd);
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        propsPlaceholder.setLayout(gl);

        folderComposite = new FolderComposite(propsPlaceholder, SWT.BORDER);
        folderComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Load properties
        FolderInfo[] folders = collectFolders(this);
        folderComposite.setFolders(folders);

        // Collect section contributors
        GlobalContributorManager contributorManager = GlobalContributorManager.getInstance();
        for (FolderInfo folder : folderComposite.getFolders()) {
            IFolder page = folder.getContents();
            if (page instanceof IDatabaseEditorContributorUser) {
                IEditorActionBarContributor contributor = ((IDatabaseEditorContributorUser) page).getContributor(contributorManager);
                if (contributor != null) {
                    contributorManager.addContributor(contributor, this);
                    pageContributors.put(page, contributor);
                }
            }
            if (page instanceof ISaveablePart) {
                nestedSaveable.add((ISaveablePart) page);
            }
        }

        final String folderId = getEditorInput().getDefaultFolderId();
        if (folderId != null) {
            folderComposite.switchFolder(folderId);
        }

        folderComposite.addFolderListener(new IFolderListener() {
            @Override
            public void folderSelected(String folderId) {
                if (CommonUtils.equalObjects(curFolderId, folderId)) {
                    return;
                }
                synchronized (folderListeners) {
                    curFolderId = folderId;
                    for (IFolderListener listener : folderListeners) {
                        listener.folderSelected(folderId);
                    }
                }
            }

        });
    }

    @Override
    public void activatePart()
    {
        //getSite().setSelectionProvider();
    }

    @Override
    public void dispose()
    {
        // Remove contributors
        GlobalContributorManager contributorManager = GlobalContributorManager.getInstance();
        for (IEditorActionBarContributor contributor : pageContributors.values()) {
            contributorManager.removeContributor(contributor, this);
        }
        pageContributors.clear();
        //PropertiesContributor.getInstance().removeLazyListener(this);

        super.dispose();
    }

    @Override
    public void setFocus()
    {
        folderComposite.setFocus();
        IFolder selectedPage = folderComposite.getActiveFolder();
        if (selectedPage != null) {
            selectedPage.setFocus();
//            IEditorActionBarContributor contributor = pageContributors.get(selectedPage);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        for (ISaveablePart sp : nestedSaveable) {
            sp.doSave(monitor);
        }
    }

    @Override
    public void doSaveAs()
    {
        Object activeFolder = getActiveFolder();
        if (activeFolder instanceof ISaveablePart) {
            ((ISaveablePart) activeFolder).doSaveAs();
        }
    }

    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty()
    {
        for (ISaveablePart sp : nestedSaveable) {
            if (sp.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }

    private void createPathRow(Composite infoGroup, Image image, String label, String value, @Nullable SelectionListener selectionListener)
    {
        UIUtils.createImageLabel(infoGroup, image);
        //UIUtils.createControlLabel(infoGroup, label);

        Link objectLink = new Link(infoGroup, SWT.NONE);
        //objectLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (selectionListener == null) {
            objectLink.setText(value);
            objectLink.setToolTipText(label);
        } else {
            objectLink.setText("<A>" + value + "</A>   ");
            objectLink.addSelectionListener(selectionListener);
            objectLink.setToolTipText("Open " + label + " Editor");
        }
    }

    @Nullable
    @Override
    public ProgressPageControl getProgressControl()
    {
        return pageControl;
    }

    @Nullable
    @Override
    public IFolder getActiveFolder()
    {
        return folderComposite.getActiveFolder();
    }

    @Override
    public void switchFolder(String folderId)
    {
        folderComposite.switchFolder(folderId);
    }

    @Override
    public void addFolderListener(IFolderListener listener)
    {
        synchronized (folderListeners) {
            folderListeners.add(listener);
        }
    }

    @Override
    public void removeFolderListener(IFolderListener listener)
    {
        synchronized (folderListeners) {
            folderListeners.remove(listener);
        }
    }

    @Nullable
    private ISearchContextProvider getFolderSearch()
    {
        Object activeFolder = getActiveFolder();
        if (activeFolder instanceof ISearchContextProvider) {
            return (ISearchContextProvider)activeFolder;
        }
        return null;
    }

    @Override
    public boolean isSearchPossible()
    {
        return true;
    }

    @Override
    public boolean isSearchEnabled()
    {
        ISearchContextProvider provider = getFolderSearch();
        return provider != null && provider.isSearchEnabled();
    }

    @Override
    public boolean performSearch(SearchType searchType)
    {
        return getFolderSearch().performSearch(searchType);
    }

    @Override
    public void addRefreshClient(IRefreshablePart part)
    {
        synchronized (refreshClients) {
            refreshClients.add(part);
        }
    }

    @Override
    public void removeRefreshClient(IRefreshablePart part)
    {
        synchronized (refreshClients) {
            refreshClients.add(part);
        }
    }

    @Override
    public void refreshPart(Object source, boolean force) {
        synchronized (refreshClients) {
            for (IRefreshablePart part : refreshClients) {
                part.refreshPart(source, force);
            }
        }
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        Object result = null;
        final Object activeFolder = getActiveFolder();
        if (activeFolder != null) {
            if (adapter.isAssignableFrom(activeFolder.getClass())) {
                result = activeFolder;
            } else if (activeFolder instanceof IAdaptable) {
                result = ((IAdaptable) activeFolder).getAdapter(adapter);
            }
        }
        return result == null ? super.getAdapter(adapter) : result;
    }

    public FolderInfo[] collectFolders(IWorkbenchPart part)
    {
        List<FolderInfo> tabList = new ArrayList<FolderInfo>();
        makeStandardPropertiesTabs(tabList);
        if (part instanceof IDatabaseEditor) {
            makeDatabaseEditorTabs((IDatabaseEditor)part, tabList);
        }
        return tabList.toArray(new FolderInfo[tabList.size()]);
    }

    private void makeStandardPropertiesTabs(List<FolderInfo> tabList)
    {
        tabList.add(new FolderInfo(
            //PropertiesContributor.CATEGORY_INFO,
            PropertiesContributor.TAB_STANDARD,
            CoreMessages.ui_properties_category_information,
            DBIcon.TREE_INFO.getImage(),
            null,
            new FolderPageProperties(getEditorInput())));
    }

    private void makeDatabaseEditorTabs(IDatabaseEditor part, List<FolderInfo> tabList)
    {
        final DBNDatabaseNode node = part.getEditorInput().getTreeNode();
        final DBSObject object = node.getObject();

        // Collect tabs from navigator tree model
        final List<NavigatorTabInfo> tabs = new ArrayList<NavigatorTabInfo>();
        DBRRunnableWithProgress tabsCollector = new DBRRunnableWithProgress() {
            @Override
            public void run(DBRProgressMonitor monitor)
            {
                tabs.addAll(collectNavigatorTabs(monitor, node));
            }
        };
        try {
            if (node.isLazyNode()) {
                DBeaverUI.runInProgressService(tabsCollector);
            } else {
                tabsCollector.run(VoidProgressMonitor.INSTANCE);
            }
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException());
        } catch (InterruptedException e) {
            // just go further
        }

        for (NavigatorTabInfo tab : tabs) {
            addNavigatorNodeTab(part, tabList, tab);
        }

        // Query for entity editors
        List<EntityEditorDescriptor> editors = DBeaverCore.getInstance().getEditorsRegistry().getEntityEditors(object, null);
        if (!CommonUtils.isEmpty(editors)) {
            for (EntityEditorDescriptor descriptor : editors) {
                if (descriptor.getType() == EntityEditorDescriptor.Type.folder) {
                    tabList.add(new FolderInfo(
                        //PropertiesContributor.CATEGORY_STRUCT,
                        descriptor.getId(),
                        descriptor.getName(),
                        descriptor.getIcon(),
                        descriptor.getDescription(),
                        new FolderPageEditor(this, descriptor)));
                }
            }
        }
    }

    private void addNavigatorNodeTab(final IDatabaseEditor part, List<FolderInfo> tabList, final NavigatorTabInfo tabInfo)
    {
        tabList.add(new FolderInfo(
            //PropertiesContributor.CATEGORY_STRUCT,
            tabInfo.getName(),
            tabInfo.getName(),
            tabInfo.node.getNodeIconDefault(),
            null,
            new FolderPageNode(part, tabInfo.node, tabInfo.meta)));
    }

    private static class NavigatorTabInfo {
        final DBNDatabaseNode node;
        final DBXTreeNode meta;
        private NavigatorTabInfo(DBNDatabaseNode node)
        {
            this(node, null);
        }
        private NavigatorTabInfo(DBNDatabaseNode node, DBXTreeNode meta)
        {
            this.node = node;
            this.meta = meta;
        }
        public String getName()
        {
            return meta == null ? node.getNodeName() : meta.getChildrenType(node.getObject().getDataSource());
        }
    }


    private static List<NavigatorTabInfo> collectNavigatorTabs(DBRProgressMonitor monitor, DBNNode node)
    {
        List<NavigatorTabInfo> tabs = new ArrayList<NavigatorTabInfo>();

        // Add all nested folders as tabs
        if (node instanceof DBNDataSource && !((DBNDataSource)node).getDataSourceContainer().isConnected()) {
            // Do not add children tabs
        } else if (node != null) {
            try {
                List<? extends DBNNode> children = node.getChildren(monitor);
                if (children != null) {
                    for (DBNNode child : children) {
                        if (child instanceof DBNDatabaseFolder) {
                            monitor.subTask(CoreMessages.ui_properties_task_add_folder + child.getNodeName() + "'"); //$NON-NLS-2$
                            tabs.add(new NavigatorTabInfo((DBNDatabaseFolder)child));
                        }
                    }
                }
            } catch (DBException e) {
                log.error("Error initializing property tabs", e); //$NON-NLS-1$
            }
            // Add itself as tab (if it has child items)
            if (node instanceof DBNDatabaseNode) {
                DBNDatabaseNode databaseNode = (DBNDatabaseNode)node;
                List<DBXTreeNode> subNodes = databaseNode.getMeta().getChildren(databaseNode);
                if (subNodes != null) {
                    for (DBXTreeNode child : subNodes) {
                        if (child instanceof DBXTreeItem) {
                            try {
                                if (!((DBXTreeItem)child).isOptional() || databaseNode.hasChildren(monitor, child)) {
                                    monitor.subTask(CoreMessages.ui_properties_task_add_node + node.getNodeName() + "'"); //$NON-NLS-2$
                                    tabs.add(new NavigatorTabInfo((DBNDatabaseNode)node, child));
                                }
                            } catch (DBException e) {
                                log.debug("Can't add child items tab", e); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        return tabs;
    }

}