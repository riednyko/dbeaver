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
package org.jkiss.dbeaver.ui.views.navigator.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNProject;
import org.jkiss.dbeaver.model.navigator.DBNProjectDatabases;
import org.jkiss.dbeaver.model.project.DBPProjectListener;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.help.IHelpContextIds;
import org.jkiss.dbeaver.ui.views.navigator.database.NavigatorViewBase;

/**
 * ProjectExplorerView
 */
public class ProjectExplorerView extends NavigatorViewBase implements DBPProjectListener
{

    //static final Log log = LogFactory.getLog(ProjectExplorerView.class);

    public static final String VIEW_ID = "org.jkiss.dbeaver.core.projectExplorer";

    public ProjectExplorerView() {
        DBeaverCore.getInstance().getProjectRegistry().addProjectListener(this);
    }

    @Override
    public DBNNode getRootNode()
    {
        DBNProject projectNode = getModel().getRoot().getProject(DBeaverCore.getInstance().getProjectRegistry().getActiveProject());
        return projectNode != null ? projectNode : getModel().getRoot();
    }

    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        UIUtils.setHelp(parent, IHelpContextIds.CTX_PROJECT_EXPLORER);

        this.getNavigatorViewer().addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                return !(element instanceof DBNProjectDatabases);
            }
        });
        updateTitle();
    }

    @Override
    public void dispose()
    {
        DBeaverCore.getInstance().getProjectRegistry().removeProjectListener(this);
        super.dispose();
    }

    @Override
    public void handleActiveProjectChange(IProject oldValue, IProject newValue)
    {
        getNavigatorTree().reloadTree(getRootNode());
        updateTitle();
    }

    private void updateTitle()
    {
        setPartName("Project - " + getRootNode().getNodeName());
    }

}
