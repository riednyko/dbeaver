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
package org.jkiss.dbeaver.ext.generic.views;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.GenericMessages;
import org.jkiss.dbeaver.ext.ui.ICompositeDialogPage;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.model.DBPDriver;
import org.jkiss.dbeaver.registry.DriverDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionPageAbstract;
import org.jkiss.dbeaver.ui.dialogs.connection.DriverPropertiesDialogPage;
import org.jkiss.utils.CommonUtils;

import java.io.File;
import java.util.*;
import java.util.List;

/**
 * GenericConnectionPage
 */
public class GenericConnectionPage extends ConnectionPageAbstract implements ICompositeDialogPage
{
    // Driver name
    private Text driverText;
    // Host/port
    private Text hostText;
    private Text portText;
    // server/DB/path
    private Text serverText;
    private Text dbText;
    private Text pathText;
    // Login
    private Text userNameText;
    private Text passwordText;
    // URL
    private Text urlText;

    private boolean isCustom;
    private DriverDescriptor.MetaURL metaURL;

    private Composite settingsGroup;

    private Map<String, List<Control>> propGroupMap = new HashMap<String, List<Control>>();

    private static final String GROUP_URL = "url"; //$NON-NLS-1$
    private static final String GROUP_HOST = "host"; //$NON-NLS-1$
    private static final String GROUP_SERVER = "server"; //$NON-NLS-1$
    private static final String GROUP_DB = "db"; //$NON-NLS-1$
    private static final String GROUP_PATH = "path"; //$NON-NLS-1$
    private static final String GROUP_LOGIN = "login"; //$NON-NLS-1$
    private boolean activated;

    @Override
    public void createControl(Composite composite)
    {
        ModifyListener textListener = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (activated) {
                    saveAndUpdate();
                }
            }
        };

        settingsGroup = new Composite(composite, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        settingsGroup.setLayout(gl);
        GridData gd = new GridData(GridData.FILL_BOTH);
        settingsGroup.setLayoutData(gd);

        {
            Label urlLabel = new Label(settingsGroup, SWT.NONE);
            urlLabel.setText(GenericMessages.dialog_connection_jdbc_url_);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
            urlLabel.setLayoutData(gd);

            urlText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 3;
            gd.grabExcessHorizontalSpace = true;
            //gd.widthHint = 355;
            urlText.setLayoutData(gd);
            urlText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e)
                {
                    site.updateButtons();
                }
            });

            addControlToGroup(GROUP_URL, urlLabel);
            addControlToGroup(GROUP_URL, urlText);
        }
        {
            Label hostLabel = new Label(settingsGroup, SWT.NONE);
            hostLabel.setText(GenericMessages.dialog_connection_host_label);
            hostLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            hostText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            hostText.setLayoutData(gd);
            hostText.addModifyListener(textListener);

            Label portLabel = new Label(settingsGroup, SWT.NONE);
            portLabel.setText(GenericMessages.dialog_connection_port_label);
            portLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            portText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.CENTER);
            gd.widthHint = 60;
            portText.setLayoutData(gd);
            //portText.addVerifyListener(UIUtils.INTEGER_VERIFY_LISTENER);
            portText.addModifyListener(textListener);

            addControlToGroup(GROUP_HOST, hostLabel);
            addControlToGroup(GROUP_HOST, hostText);
            addControlToGroup(GROUP_HOST, portLabel);
            addControlToGroup(GROUP_HOST, portText);
        }

        {
            Label serverLabel = new Label(settingsGroup, SWT.NONE);
            serverLabel.setText(GenericMessages.dialog_connection_server_label);
            serverLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            serverText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            //gd.widthHint = 270;
            serverText.setLayoutData(gd);
            serverText.addModifyListener(textListener);

            Control emptyLabel = createEmptyLabel(settingsGroup, 1);

            addControlToGroup(GROUP_SERVER, serverLabel);
            addControlToGroup(GROUP_SERVER, serverText);
            addControlToGroup(GROUP_SERVER, emptyLabel);
        }

        {
            Label dbLabel = new Label(settingsGroup, SWT.NONE);
            dbLabel.setText(GenericMessages.dialog_connection_database_schema_label);
            dbLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            dbText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            //gd.widthHint = 270;
            //gd.horizontalSpan = 3;
            dbText.setLayoutData(gd);
            dbText.addModifyListener(textListener);

            Control emptyLabel = createEmptyLabel(settingsGroup, 1);

            addControlToGroup(GROUP_DB, dbLabel);
            addControlToGroup(GROUP_DB, dbText);
            addControlToGroup(GROUP_DB, emptyLabel);
        }

        {
            Label pathLabel = new Label(settingsGroup, SWT.NONE);
            pathLabel.setText(GenericMessages.dialog_connection_path_label);
            pathLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            pathText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            //gd.grabExcessHorizontalSpace = true;
            //gd.widthHint = 200;
            gd.horizontalSpan = 2;
            pathText.setLayoutData(gd);
            pathText.addModifyListener(textListener);

            Button browseButton = new Button(settingsGroup, SWT.PUSH);
            browseButton.setText(GenericMessages.dialog_connection_browse_button);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            gd.horizontalSpan = 1;
            browseButton.setLayoutData(gd);
            browseButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if (metaURL.getAvailableProperties().contains(DriverDescriptor.PROP_FILE)) {
                        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
                        dialog.setFileName(pathText.getText());
                        dialog.setText(GenericMessages.dialog_connection_db_file_chooser_text);
                        String file = dialog.open();
                        if (file != null) {
                            pathText.setText(file);
                        }
                    } else {
                        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
                        final String curPath = pathText.getText();
                        File curFolder = new File(curPath);
                        if (curFolder.exists()) {
                            if (curFolder.isDirectory()) {
                                dialog.setFilterPath(curFolder.getAbsolutePath());
                            } else {
                                dialog.setFilterPath(curFolder.getParentFile().getAbsolutePath());
                            }
                        }
                        dialog.setText(GenericMessages.dialog_connection_db_folder_chooser_text);
                        dialog.setMessage(GenericMessages.dialog_connection_db_folder_chooser_message);
                        String folder = dialog.open();
                        if (folder != null) {
                            pathText.setText(folder);
                        }
                    }
                }
            });

            addControlToGroup(GROUP_PATH, pathLabel);
            addControlToGroup(GROUP_PATH, pathText);
            addControlToGroup(GROUP_PATH, browseButton);
        }

        {
            Label userNameLabel = new Label(settingsGroup, SWT.NONE);
            userNameLabel.setText(GenericMessages.dialog_connection_user_name_label);
            userNameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            userNameText = new Text(settingsGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            userNameText.setLayoutData(gd);
            userNameText.addModifyListener(textListener);

            Control emptyLabel = createEmptyLabel(settingsGroup, 2);

            Label passwordLabel = new Label(settingsGroup, SWT.NONE);
            passwordLabel.setText(GenericMessages.dialog_connection_password_label);
            passwordLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            passwordText = new Text(settingsGroup, SWT.BORDER | SWT.PASSWORD);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            passwordText.setLayoutData(gd);
            passwordText.addModifyListener(textListener);

            addControlToGroup(GROUP_LOGIN, userNameLabel);
            addControlToGroup(GROUP_LOGIN, userNameText);
            addControlToGroup(GROUP_LOGIN, emptyLabel);
            addControlToGroup(GROUP_LOGIN, passwordLabel);
            addControlToGroup(GROUP_LOGIN, passwordText);
        }

        createAdvancedButtons(settingsGroup, true);

        Composite placeholder = UIUtils.createPlaceholder(settingsGroup, 1);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END);
        gd.horizontalSpan = 4;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        placeholder.setLayoutData(gd);

        Label divLabel = new Label(settingsGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 4;
        divLabel.setLayoutData(gd);

        {
            Label driverLabel = new Label(settingsGroup, SWT.NONE);
            driverLabel.setText(GenericMessages.dialog_connection_driver);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
            driverLabel.setLayoutData(gd);

            driverText = new Text(settingsGroup, SWT.READ_ONLY);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            gd.grabExcessHorizontalSpace = true;
            //gd.widthHint = 200;
            driverText.setLayoutData(gd);

            Button driverButton = new Button(settingsGroup, SWT.PUSH);
            driverButton.setText(GenericMessages.dialog_connection_edit_driver_button);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
            driverButton.setLayoutData(gd);
            driverButton.addSelectionListener(new SelectionListener()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if (site.openDriverEditor()) {
                        parseSampleURL(site.getDriver());
                        saveAndUpdate();
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e)
                {
                }
            });
        }
        setControl(settingsGroup);
    }

    private Control createEmptyLabel(Composite parent, int verticalSpan)
    {
        Label emptyLabel = new Label(parent, SWT.NONE);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gd.horizontalSpan = 2;
        gd.verticalSpan = verticalSpan;
        gd.widthHint = 0;
        emptyLabel.setLayoutData(gd);
        return emptyLabel;
    }

    @Override
    public boolean isComplete()
    {
        if (isCustom) {
            return !CommonUtils.isEmpty(urlText.getText());
        } else {
            if (metaURL == null) {
                return false;
            }
            for (String prop : metaURL.getRequiredProperties()) {
                if (
                    (prop.equals(DriverDescriptor.PROP_HOST) && CommonUtils.isEmpty(hostText.getText())) ||
                        (prop.equals(DriverDescriptor.PROP_PORT) && CommonUtils.isEmpty(portText.getText())) ||
                        (prop.equals(DriverDescriptor.PROP_DATABASE) && CommonUtils.isEmpty(dbText.getText()))) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    protected boolean isCustomURL()
    {
        return isCustom;
    }

    @Override
    public void loadSettings()
    {
        // Load values from new connection info
        DBPConnectionInfo connectionInfo = site.getConnectionInfo();
        if (site.getDriver() != null) {
            driverText.setText(CommonUtils.toString(site.getDriver().getFullName()));
        }
        if (connectionInfo != null) {
            this.parseSampleURL(site.getDriver());
            if (!isCustom) {
                if (hostText != null) {
                    if (!CommonUtils.isEmpty(connectionInfo.getHostName())) {
                        hostText.setText(CommonUtils.getString(connectionInfo.getHostName()));
                    } else {
                        hostText.setText("localhost"); //$NON-NLS-1$
                    }
                }
                if (portText != null) {
                    if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
                        portText.setText(String.valueOf(connectionInfo.getHostPort()));
                    } else if (site.getDriver().getDefaultPort() != null) {
                        portText.setText(site.getDriver().getDefaultPort());
                    } else {
                        portText.setText(""); //$NON-NLS-1$
                    }
                }
                if (serverText != null) {
                    serverText.setText(CommonUtils.getString(connectionInfo.getServerName()));
                }
                if (dbText != null) {
                    dbText.setText(CommonUtils.getString(connectionInfo.getDatabaseName()));
                }
                if (pathText != null) {
                    pathText.setText(CommonUtils.getString(connectionInfo.getDatabaseName()));
                }
            } else {
                hostText.setText(""); //$NON-NLS-1$
                portText.setText(""); //$NON-NLS-1$
                serverText.setText(""); //$NON-NLS-1$
                dbText.setText(""); //$NON-NLS-1$
                pathText.setText(""); //$NON-NLS-1$
            }
            if (userNameText != null) {
                userNameText.setText(CommonUtils.getString(connectionInfo.getUserName()));
            }
            if (passwordText != null) {
                passwordText.setText(CommonUtils.getString(connectionInfo.getUserPassword()));
            }

            if (urlText != null) {
                if (connectionInfo.getUrl() != null) {
                    urlText.setText(CommonUtils.getString(connectionInfo.getUrl()));
                } else {
                    urlText.setText(""); //$NON-NLS-1$
                }
            }
        }

        super.loadSettings();

        activated = true;
    }

    @Override
    protected void saveSettings(DBPConnectionInfo connectionInfo)
    {
        if (connectionInfo != null) {
            final Set<String> properties = metaURL == null ? Collections.<String>emptySet() : metaURL.getAvailableProperties();

            if (hostText != null && properties.contains(DriverDescriptor.PROP_HOST)) {
                connectionInfo.setHostName(hostText.getText());
            }
            if (portText != null && properties.contains(DriverDescriptor.PROP_PORT)) {
                connectionInfo.setHostPort(portText.getText());
            }
            if (serverText != null && properties.contains(DriverDescriptor.PROP_SERVER)) {
                connectionInfo.setServerName(serverText.getText());
            }
            if (dbText != null && properties.contains(DriverDescriptor.PROP_DATABASE)) {
                connectionInfo.setDatabaseName(dbText.getText());
            }
            if (pathText != null && (properties.contains(DriverDescriptor.PROP_FOLDER) || properties.contains(DriverDescriptor.PROP_FILE))) {
                connectionInfo.setDatabaseName(pathText.getText());
            }
            if (userNameText != null) {
                connectionInfo.setUserName(userNameText.getText());
            }
            if (passwordText != null) {
                connectionInfo.setUserPassword(passwordText.getText());
            }
            super.saveSettings(connectionInfo);
            if (urlText != null && connectionInfo.getUrl() != null) {
                urlText.setText(connectionInfo.getUrl());
            }
        }
    }

    private void parseSampleURL(DBPDriver driver)
    {
        metaURL = null;

        if (!CommonUtils.isEmpty(driver.getSampleURL())) {
            isCustom = false;
            try {
                metaURL = DriverDescriptor.parseSampleURL(driver.getSampleURL());
            } catch (DBException e) {
                setErrorMessage(e.getMessage());
            }
            final Set<String> properties = metaURL.getAvailableProperties();
            urlText.setEditable(false);
            urlText.setBackground(urlText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

            showControlGroup(GROUP_HOST, properties.contains(DriverDescriptor.PROP_HOST));
            showControlGroup(GROUP_SERVER, properties.contains(DriverDescriptor.PROP_SERVER));
            showControlGroup(GROUP_DB, properties.contains(DriverDescriptor.PROP_DATABASE));
            showControlGroup(GROUP_PATH, properties.contains(DriverDescriptor.PROP_FOLDER) || properties.contains(DriverDescriptor.PROP_FILE));
        } else {
            isCustom = true;
            showControlGroup(GROUP_HOST, false);
            showControlGroup(GROUP_SERVER, false);
            showControlGroup(GROUP_DB, false);
            showControlGroup(GROUP_PATH, false);
            urlText.setEditable(true);
            urlText.setBackground(null);
        }
        showControlGroup(GROUP_LOGIN, !driver.isAnonymousAccess());

        settingsGroup.layout();
    }

    private void saveAndUpdate()
    {
        saveSettings(site.getConnectionInfo());
        site.updateButtons();
    }

    private void showControlGroup(String group, boolean show)
    {
        List<Control> controlList = propGroupMap.get(group);
        if (controlList != null) {
            for (Control control : controlList) {
                GridData gd = (GridData)control.getLayoutData();
                if (gd == null) {
                    gd = new GridData(GridData.BEGINNING);
                    control.setLayoutData(gd);
                }
                gd.exclude = !show;
                control.setVisible(show);
            }
        }
    }

    private void addControlToGroup(String group, Control control)
    {
        List<Control> controlList = propGroupMap.get(group);
        if (controlList == null) {
            controlList = new ArrayList<Control>();
            propGroupMap.put(group, controlList);
        }
        controlList.add(control);
    }

    @Override
    public IDialogPage[] getSubPages()
    {
        return new IDialogPage[] {
            new DriverPropertiesDialogPage(this)
        };
    }

}
