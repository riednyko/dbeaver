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
package org.jkiss.dbeaver.ui.actions.common;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.views.IViewDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;


public class ToggleViewAction extends Action implements IPartListener
{

    private String viewId;
    private boolean listenerRegistered = false;
    private IViewDescriptor viewDescriptor;

    public ToggleViewAction(String viewId)
    {
        this.viewId = viewId;
        viewDescriptor = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
    }

    @Override
    public String getText()
    {
        if (viewDescriptor != null) {
            return viewDescriptor.getLabel();
        }
        return super.getText();
    }

    @Override
    public String getToolTipText()
    {
        if (viewDescriptor != null) {
            return viewDescriptor.getDescription();
        }
        return super.getToolTipText();
    }

    @Override
    public ImageDescriptor getImageDescriptor()
    {
        if (viewDescriptor != null) {
            return viewDescriptor.getImageDescriptor();
        }
        return super.getImageDescriptor();
    }

    @Override
    public int getStyle()
    {
        return AS_CHECK_BOX;
    }

    @Override
    public boolean isChecked()
    {
        if (!listenerRegistered) {
            IWorkbenchPage activePage = getActivePage();
            if (activePage == null) {
                return false;
            }
            activePage.addPartListener(this);
            listenerRegistered = true;
            IViewReference viewReference = activePage.findViewReference(viewId);
            setChecked(viewReference != null && viewReference.getView(false) != null);
        }

        return super.isChecked();
    }

    @Override
    public void run()
    {
        IWorkbenchPage activePage = getActivePage();
        if (activePage == null) {
            return;
        }
        try {
            IViewPart view = activePage.findView(viewId);
            if (view == null) {
                activePage.showView(viewId);
            } else {
                activePage.hideView(view);
            }
        } catch (PartInitException ex) {
            UIUtils.showErrorDialog(null, viewId, "Can't open view " + viewId, ex);
        }
    }

    private static IWorkbenchPage getActivePage()
    {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return workbenchWindow.getActivePage();
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part)
    {
    }

    @Override
    public void partOpened(IWorkbenchPart part)
    {
        if (part.getSite().getId().equals(viewId)) {
            setChecked(true);
        }
    }

    @Override
    public void partClosed(IWorkbenchPart part)
    {
        if (part.getSite().getId().equals(viewId)) {
            setChecked(false);
        }
    }

    @Override
    public void partActivated(IWorkbenchPart part)
    {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part)
    {
    }

}