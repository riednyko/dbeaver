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
package org.jkiss.dbeaver.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.utils.CommonUtils;

/**
 * Base dialog with title and image
 */
public class BaseDialog extends Dialog
{

    private String title;
    private Image icon;

    public BaseDialog(Shell parentShell, String title, @Nullable Image icon)
    {
        super(parentShell);
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Image getImage() {
        return icon;
    }

    public void setImage(Image image)
    {
        this.icon = image;
    }

    protected boolean isResizable() {
        return true;
    }

    @Override
    public void create()
    {
        super.create();
        getShell().setText(title);
        if (icon != null) {
            getShell().setImage(icon);
        }
    }
}
