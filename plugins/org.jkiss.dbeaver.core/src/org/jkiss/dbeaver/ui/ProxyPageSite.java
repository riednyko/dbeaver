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
package org.jkiss.dbeaver.ui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageSite;

/**
* ProxyPageSite
*/
public class ProxyPageSite implements IPageSite {

    private final IWorkbenchPartSite partSite;

    public ProxyPageSite(IWorkbenchPartSite partSite)
    {
        this.partSite = partSite;
    }

    @Override
    public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider)
    {
        partSite.registerContextMenu(menuId, menuManager, selectionProvider);
    }

    @Override
    public IActionBars getActionBars()
    {
        if (partSite instanceof IEditorSite) {
            return ((IEditorSite)partSite).getActionBars();
        } else if (partSite instanceof IViewSite) {
            return ((IViewSite)partSite).getActionBars();
        } else {
            return null;
        }
    }

    @Override
    public IWorkbenchPage getPage()
    {
        return partSite.getPage();
    }

    @Override
    public ISelectionProvider getSelectionProvider()
    {
        return partSite.getSelectionProvider();
    }

    @Override
    public Shell getShell()
    {
        return partSite.getShell();
    }

    @Override
    public IWorkbenchWindow getWorkbenchWindow()
    {
        return partSite.getWorkbenchWindow();
    }

    @Override
    public void setSelectionProvider(ISelectionProvider provider)
    {
        partSite.setSelectionProvider(provider);
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        return partSite.getAdapter(adapter);
    }

    @Override
    public Object getService(Class api)
    {
        return partSite.getService(api);
    }

    @Override
    public boolean hasService(Class api)
    {
        return partSite.hasService(api);
    }
}
