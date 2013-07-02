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
package org.jkiss.dbeaver.ui.search.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.controls.itemlist.ItemListControl;
import org.jkiss.dbeaver.ui.search.IObjectSearchResultPage;

import java.util.Collection;

public class SearchMetadataResultsPage extends Page implements IObjectSearchResultPage<DBNNode> {

    static final Log log = LogFactory.getLog(SearchMetadataResultsPage.class);
    private IPageSite pageSite;
    private SearchResultsControl itemList;
    private IActionBars actionBars;

    @Override
    public IPageSite getSite()
    {
        return pageSite;
    }

    @Override
    public void init(IPageSite site)
    {
        this.pageSite = site;
    }

    @Override
    public void createControl(Composite parent)
    {
        itemList = new SearchResultsControl(parent);
        itemList.createProgressPanel();
        itemList.setInfo(CoreMessages.dialog_search_objects_item_list_info);
        GridData gd = new GridData(GridData.FILL_BOTH);
        itemList.setLayoutData(gd);
        //itemList.addFocusListener(new ItemsFocusListener());
    }

    @Override
    public void dispose()
    {

    }

    @Override
    public Control getControl()
    {
        return itemList;
    }

    @Override
    public void setActionBars(IActionBars actionBars)
    {
        this.actionBars = actionBars;
    }

    @Override
    public void setFocus()
    {
        itemList.setFocus();
    }

    @Override
    public void populateObjects(DBRProgressMonitor monitor, Collection<DBNNode> objects)
    {
        itemList.appendListData(objects);
    }

    @Override
    public void clearObjects()
    {
        itemList.clearListData();
    }

    private class SearchResultsControl extends ItemListControl {
        public SearchResultsControl(Composite resultsGroup)
        {
            super(resultsGroup, SWT.BORDER, null, DBeaverCore.getInstance().getNavigatorModel().getRoot(), null);
        }

        @Override
        protected void fillCustomToolbar(ToolBarManager toolbarManager)
        {
        }
    }

}
