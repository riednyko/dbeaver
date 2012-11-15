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
package org.jkiss.dbeaver.core.application.about;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.jkiss.dbeaver.DBeaverConstants;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.core.application.Activator;
import org.jkiss.dbeaver.ui.UIUtils;

/**
 * About box
 */
public class AboutBoxDialog extends Dialog
{
    private final Font TITLE_FONT;

    private Image ABOUT_IMAGE = Activator.getImageDescriptor("icons/about_circle.png").createImage();

    public AboutBoxDialog(Shell shell)
    {
        super(shell);
        TITLE_FONT = new Font(shell.getDisplay(), CoreMessages.dialog_about_font, 20, SWT.NORMAL);
    }

    @Override
    public boolean close() {
        TITLE_FONT.dispose();
        return super.close();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(CoreMessages.dialog_about_title);
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Color background = JFaceColors.getBannerBackground(parent.getDisplay());
        //Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
        parent.setBackground(background);

        Composite group = new Composite(parent, SWT.NONE);
        group.setBackground(background);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        group.setLayout(layout);

        GridData gd;

        IProduct product = Platform.getProduct();
        String productVersion = DBeaverCore.getVersion().toString();

        Label titleLabel = new Label(group, SWT.NONE);
        titleLabel.setBackground(background);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setText(product.getProperty(DBeaverConstants.PRODUCT_PROP_SUB_TITLE));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.CENTER;
        titleLabel.setLayoutData(gd);
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
                    @Override
                    public void run() {
                        // Do not create InstallationDialog directly
                        // but execute "org.eclipse.ui.help.installationDialog" command
                        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        IHandlerService service = (IHandlerService) workbenchWindow.getService(IHandlerService.class);
                        if (service != null) {
                            try {
                                service.executeCommand("org.eclipse.ui.help.installationDialog", null); //$NON-NLS-1$
                            } catch (Exception e1) {
                                // just ignore error
                            }
                        }
                    }
                });
            }
        });
        
        Label imageLabel = new Label(group, SWT.NONE);
        imageLabel.setBackground(background);

        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.horizontalAlignment = GridData.CENTER;
        gd.grabExcessHorizontalSpace = true;
        imageLabel.setLayoutData(gd);
        imageLabel.setImage(ABOUT_IMAGE);

        Label versionLabel = new Label(group, SWT.NONE);
        versionLabel.setBackground(background);
        versionLabel.setText(CoreMessages.dialog_about_label_version + productVersion);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.CENTER;
        versionLabel.setLayoutData(gd);

        Label authorLabel = new Label(group, SWT.NONE);
        authorLabel.setBackground(background);
        authorLabel.setText(product.getProperty(DBeaverConstants.PRODUCT_PROP_COPYRIGHT));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.CENTER;
        authorLabel.setLayoutData(gd);

        Link siteLink = new Link(group, SWT.NONE);
        siteLink.setText(UIUtils.makeAnchor(product.getProperty(DBeaverConstants.PRODUCT_PROP_WEBSITE)));
        siteLink.setBackground(background);
        siteLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch(e.text);
            }
        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.CENTER;
        siteLink.setLayoutData(gd);

        Link emailLink = new Link(group, SWT.NONE);
        emailLink.setText(UIUtils.makeAnchor(product.getProperty(DBeaverConstants.PRODUCT_PROP_EMAIL)));
        emailLink.setBackground(background);
        emailLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch("mailto:" + e.text); //$NON-NLS-1$
            }
        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.CENTER;
        emailLink.setLayoutData(gd);

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.CENTER; 
        parent.setLayoutData(gd);
        parent.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
        Button button = createButton(
            parent,
            IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL,
            true);
        button.setFocus();
    }

}