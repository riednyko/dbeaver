/*
 * Copyright (C) 2010-2013 Serge Rieder
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
package org.jkiss.dbeaver.ui.views.navigator.database;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDataSourceContainerProvider;
import org.jkiss.dbeaver.ext.ui.INavigatorModelView;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.navigator.*;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.ui.NavigatorUtils;
import org.jkiss.dbeaver.ui.actions.navigator.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.properties.tabbed.PropertyPageStandard;
import org.jkiss.utils.CommonUtils;

public abstract class NavigatorViewBase extends ViewPart implements INavigatorModelView, IDataSourceContainerProvider
{
    private DBNModel model;
    private DatabaseNavigatorTree tree;
    private transient Object lastSelection;

    protected NavigatorViewBase()
    {
        super();
        model = DBeaverCore.getInstance().getNavigatorModel();
    }

    public DBNModel getModel()
    {
        return model;
    }

    protected DatabaseNavigatorTree getNavigatorTree()
    {
        return tree;
    }

    @Override
    public TreeViewer getNavigatorViewer()
    {
        return tree.getViewer();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent)
    {
        this.tree = createNavigatorTree(parent, getRootNode());

        getViewSite().setSelectionProvider(tree.getViewer());
    }

    protected DatabaseNavigatorTree createNavigatorTree(Composite parent, DBNNode rootNode)
    {
        // Create tree
        DatabaseNavigatorTree navigatorTree = new DatabaseNavigatorTree(parent, rootNode, getTreeStyle());

        navigatorTree.getViewer().addSelectionChangedListener(
            new ISelectionChangedListener()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent event)
                {
                    IStructuredSelection structSel = (IStructuredSelection)event.getSelection();
                    if (!structSel.isEmpty()) {
                        lastSelection = structSel.getFirstElement();
                        if (lastSelection instanceof DBNNode) {
                            String desc = ((DBNNode)lastSelection).getNodeDescription();
                            if (CommonUtils.isEmpty(desc)) {
                                desc = ((DBNNode)lastSelection).getNodeName();
                            }
                            getViewSite().getActionBars().getStatusLineManager().setMessage(desc);
                        }
                    } else {
                        lastSelection = null;
                    }
                }
            }
        );
        navigatorTree.getViewer().addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection)tree.getViewer().getSelection();
                if (selection.size() == 1) {
                    DBNNode node = (DBNNode)selection.getFirstElement();
                    if (node instanceof DBNResource) {
                        if (((DBNResource) node).getResource() instanceof IFolder) {
                            if (Boolean.TRUE.equals(tree.getViewer().getExpandedState(node))) {
                                tree.getViewer().collapseToLevel(node, 1);
                            } else {
                                tree.getViewer().expandToLevel(node, 1);
                            }

                        } else {
                            NavigatorHandlerObjectOpen.openResource(
                                ((DBNResource) node).getResource(),
                                getSite().getWorkbenchWindow());
                        }
                    } else if (node instanceof DBNDatabaseNode && node.allowsOpen()) {
                        NavigatorHandlerObjectOpen.openEntityEditor(
                            (DBNDatabaseNode) node,
                            null,
                            getSite().getWorkbenchWindow());
                    }
                }
            }

        });

        // Hook context menu
        NavigatorUtils.addContextMenu(this.getSite(), navigatorTree.getViewer(), navigatorTree.getViewer().getControl());
        // Add drag and drop support
        NavigatorUtils.addDragAndDropSupport(navigatorTree.getViewer());

        return navigatorTree;
    }

    protected int getTreeStyle()
    {
        return SWT.MULTI;
    }

    @Override
    public void dispose()
    {
        model = null;
        super.dispose();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus()
    {
        tree.getViewer().getControl().setFocus();
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        if (adapter == IPropertySheetPage.class) {
            return new PropertyPageStandard();
        }
        return super.getAdapter(adapter);
    }

    public void showNode(DBNNode node) {
        tree.showNode(node);
    }

    @Override
    public DBSDataSourceContainer getDataSourceContainer()
    {
        if (lastSelection instanceof DBNDatabaseNode) {
            if (lastSelection instanceof DBNDataSource) {
                return ((DBNDataSource)lastSelection).getDataSourceContainer();
            } else if (((DBNDatabaseNode) lastSelection).getObject() != null) {
                final DBPDataSource dataSource = ((DBNDatabaseNode) lastSelection).getObject().getDataSource();
                if (dataSource != null) {
                    return dataSource.getContainer();
                }
            }
        }
        return null;
    }
}
