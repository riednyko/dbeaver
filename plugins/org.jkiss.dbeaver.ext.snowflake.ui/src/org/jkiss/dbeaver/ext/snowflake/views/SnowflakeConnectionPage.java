/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.snowflake.views;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.snowflake.SnowflakeUIActivator;
import org.jkiss.dbeaver.ext.snowflake.SnowflakeConstants;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.ui.ICompositeDialogPage;
import org.jkiss.dbeaver.ui.IDataSourceConnectionTester;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionPageAbstract;
import org.jkiss.dbeaver.ui.dialogs.connection.DriverPropertiesDialogPage;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SnowflakeConnectionPage
 */
public class SnowflakeConnectionPage extends ConnectionPageAbstract implements ICompositeDialogPage, IDataSourceConnectionTester
{
    private static final Log log = Log.getLog(SnowflakeConnectionPage.class);

    private Text hostText;
    private Text portText;
    private Combo dbText;
    private Combo warehouseText;
    private Combo schemaText;
    private Combo roleText;
    private Combo authTypeCombo;
    private Text usernameText;
    private Text passwordText;

    private static ImageDescriptor logoImage = SnowflakeUIActivator.getImageDescriptor("icons/snowflake_logo.png");

    @Override
    public void dispose()
    {
        super.dispose();
    }

    @Override
    public void createControl(Composite composite)
    {
        setImageDescriptor(logoImage);

        Composite control = new Composite(composite, SWT.NONE);
        control.setLayout(new GridLayout(1, false));
        control.setLayoutData(new GridData(GridData.FILL_BOTH));
        ModifyListener textListener = e -> site.updateButtons();

        {
            Composite addrGroup = UIUtils.createControlGroup(control, "Connection", 4, 0, 0);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            addrGroup.setLayoutData(gd);

            UIUtils.createControlLabel(addrGroup, "Host");

            hostText = new Text(addrGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            hostText.setLayoutData(gd);
            hostText.addModifyListener(textListener);

            UIUtils.createControlLabel(addrGroup, "Port");

            portText = new Text(addrGroup, SWT.BORDER);
            gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            gd.widthHint = 40;
            portText.setLayoutData(gd);
            portText.addVerifyListener(UIUtils.getIntegerVerifyListener(Locale.getDefault()));
            portText.addModifyListener(textListener);

            UIUtils.createControlLabel(addrGroup, "Database");

            dbText = new Combo(addrGroup, SWT.BORDER | SWT.DROP_DOWN);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalSpan = 3;
            dbText.setLayoutData(gd);
            dbText.addModifyListener(textListener);

            UIUtils.createControlLabel(addrGroup, "Warehouse");

            warehouseText = new Combo(addrGroup, SWT.BORDER | SWT.DROP_DOWN);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalSpan = 3;
            warehouseText.setLayoutData(gd);
            warehouseText.addModifyListener(textListener);

            UIUtils.createControlLabel(addrGroup, "Schema");

            schemaText = new Combo(addrGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalSpan = 3;
            schemaText.setLayoutData(gd);
            schemaText.addModifyListener(textListener);
        }

        {
            Composite ph = UIUtils.createPlaceholder(control, 2);
            CLabel infoLabel = UIUtils.createInfoLabel(ph, "");
            Link testLink = new Link(ph, SWT.NONE);
            testLink.setText("Click on <a>Test Connection</a> to load warehouse/schema list from the server");
            GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
            gd.grabExcessHorizontalSpace = true;
            testLink.setLayoutData(gd);
            testLink.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    site.testConnection();
                }
            });
        }

        {
            Composite addrGroup = UIUtils.createControlGroup(control, "Security", 4, 0, 0);
            addrGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            UIUtils.createControlLabel(addrGroup, "User");

            usernameText = new Text(addrGroup, SWT.BORDER);
            GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
            usernameText.setLayoutData(gd);
            usernameText.addModifyListener(textListener);

            UIUtils.createEmptyLabel(addrGroup, 2, 1);

            UIUtils.createControlLabel(addrGroup, "Password");

            passwordText = new Text(addrGroup, SWT.BORDER | SWT.PASSWORD);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
            passwordText.setLayoutData(gd);
            passwordText.addModifyListener(textListener);

            createSavePasswordButton(addrGroup, 2);

            UIUtils.createControlLabel(addrGroup, "Role");

            roleText = new Combo(addrGroup, SWT.BORDER | SWT.DROP_DOWN);
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
            roleText.setLayoutData(gd);
            roleText.addModifyListener(textListener);

            //UIUtils.createEmptyLabel(addrGroup, 2, 1);

            UIUtils.createControlLabel(addrGroup, "Authenticator");
            authTypeCombo = new Combo(addrGroup, SWT.BORDER | SWT.DROP_DOWN);
            authTypeCombo.add("");
            authTypeCombo.add("snowflake");
            authTypeCombo.add("externalbrowser");
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
            authTypeCombo.setLayoutData(gd);
            authTypeCombo.addModifyListener(textListener);
        }

        createDriverPanel(control);
        setControl(control);
    }

    @Override
    public boolean isComplete()
    {
        return hostText != null &&
            !CommonUtils.isEmpty(hostText.getText());
    }

    @Override
    public void loadSettings()
    {
        super.loadSettings();

        // Load values from new connection info
        DBPConnectionConfiguration connectionInfo = site.getActiveDataSource().getConnectionConfiguration();
        if (hostText != null) {
            if (CommonUtils.isEmpty(connectionInfo.getHostName())) {
                hostText.setText(SnowflakeConstants.DEFAULT_HOST_PREFIX);
            } else {
                hostText.setText(connectionInfo.getHostName());
            }
        }
        if (portText != null) {
            if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
                portText.setText(String.valueOf(connectionInfo.getHostPort()));
            } else if (site.getDriver().getDefaultPort() != null) {
                portText.setText(site.getDriver().getDefaultPort());
            } else {
                portText.setText("");
            }
        }
        if (dbText != null) {
            String databaseName = connectionInfo.getDatabaseName();
            if (CommonUtils.isEmpty(databaseName)) {
                databaseName = SnowflakeConstants.DEFAULT_DB_NAME;
            }
            dbText.setText(databaseName);
        }
        if (warehouseText != null) {
            warehouseText.setText(CommonUtils.notEmpty(connectionInfo.getServerName()));
        }
        if (schemaText != null) {
            schemaText.setText(CommonUtils.notEmpty(connectionInfo.getProviderProperty(SnowflakeConstants.PROP_SCHEMA)));
        }
        if (usernameText != null) {
            usernameText.setText(CommonUtils.notEmpty(connectionInfo.getUserName()));
        }
        if (roleText != null) {
            roleText.setText(CommonUtils.notEmpty(connectionInfo.getProviderProperty(SnowflakeConstants.PROP_ROLE)));
        }
        if (authTypeCombo != null) {
            roleText.setText(CommonUtils.notEmpty(connectionInfo.getProviderProperty(SnowflakeConstants.PROP_AUTHENTICATOR)));
        }
        if (passwordText != null) {
            passwordText.setText(CommonUtils.notEmpty(connectionInfo.getUserPassword()));
        }
    }

    @Override
    public void saveSettings(DBPDataSourceContainer dataSource)
    {
        DBPConnectionConfiguration connectionInfo = dataSource.getConnectionConfiguration();
        if (hostText != null) {
            connectionInfo.setHostName(hostText.getText().trim());
        }
        if (portText != null) {
            connectionInfo.setHostPort(portText.getText().trim());
        }
        if (dbText != null) {
            connectionInfo.setDatabaseName(dbText.getText().trim());
        }
        if (warehouseText != null) {
            connectionInfo.setServerName(warehouseText.getText().trim());
        }
        if (schemaText != null) {
            connectionInfo.setProviderProperty(SnowflakeConstants.PROP_SCHEMA, schemaText.getText().trim());
        }
        if (usernameText != null) {
            connectionInfo.setUserName(usernameText.getText().trim());
        }
        if (roleText != null) {
            connectionInfo.setProviderProperty(SnowflakeConstants.PROP_ROLE, roleText.getText().trim());
        }
        if (authTypeCombo != null) {
            connectionInfo.setProviderProperty(SnowflakeConstants.PROP_AUTHENTICATOR, authTypeCombo.getText().trim());
        }
        if (passwordText != null) {
            connectionInfo.setUserPassword(passwordText.getText());
        }
        super.saveSettings(dataSource);
    }

    @Override
    public void testConnection(DBCSession session) {
        try {
            loadDictList(session, dbText, "SHOW DATABASES");
            loadDictList(session, warehouseText, "SHOW WAREHOUSES");
            loadDictList(session, schemaText, "SHOW SCHEMAS");
            loadDictList(session, roleText, "SHOW ROLES");
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static void loadDictList(DBCSession session, Combo combo, String query) throws DBCException {
        List<String> result = new ArrayList<>();
        session.getProgressMonitor().subTask("Exec " + query);
        try (DBCStatement dbStat = session.prepareStatement(DBCStatementType.QUERY, query, false, false, false)) {
            dbStat.executeStatement();
            try (DBCResultSet dbResult = dbStat.openResultSet()) {
                while (dbResult.nextRow()) {
                    result.add(CommonUtils.toString(dbResult.getAttributeValue("name")));
                }
            }
        }
        UIUtils.asyncExec(() -> {
            String oldText = combo.getText();
            if (!result.contains(oldText)) {
                result.add(0, oldText);
            }
            if (!result.contains("")) {
                result.add(0, "");
            }
            combo.setItems(result.toArray(new String[0]));
            combo.setText(oldText);
        });
    }

    @Override
    public IDialogPage[] getSubPages()
    {
        return new IDialogPage[] {
            new DriverPropertiesDialogPage(this)
        };
    }

}
