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
package org.jkiss.dbeaver.ui.dialogs.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBPConnectionEventType;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.model.DBPDataSourceProvider;
import org.jkiss.dbeaver.model.net.DBWHandlerConfiguration;
import org.jkiss.dbeaver.model.struct.DBSObjectFilter;
import org.jkiss.dbeaver.model.struct.rdb.DBSCatalog;
import org.jkiss.dbeaver.model.struct.rdb.DBSSchema;
import org.jkiss.dbeaver.model.struct.rdb.DBSTable;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.ui.IHelpContextIds;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.ActiveWizardPage;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

class ConnectionPageFinal extends ActiveWizardPage {
    static final Log log = LogFactory.getLog(ConnectionPageFinal.class);

    private ConnectionWizard wizard;
    private DataSourceDescriptor dataSourceDescriptor;
    private Text connectionNameText;
    private Button savePasswordCheck;
    private Button showSystemObjects;
    private Button readOnlyConnection;
    private Font boldFont;

    private boolean connectionNameChanged = false;
    private Button tunnelButton;
    private Button eventsButton;
    private java.util.List<FilterInfo> filters = new ArrayList<FilterInfo>();
    private Group filtersGroup;

    private static class FilterInfo {
        final Class<?> type;
        final String title;
        Link link;
        DBSObjectFilter filter;

        private FilterInfo(Class<?> type, String title)
        {
            this.type = type;
            this.title = title;
        }
    }
    
    ConnectionPageFinal(ConnectionWizard wizard)
    {
        super("newConnectionFinal"); //$NON-NLS-1$
        this.wizard = wizard;
        setTitle(CoreMessages.dialog_connection_wizard_final_header);
        setDescription(CoreMessages.dialog_connection_wizard_final_description);

        filters.add(new FilterInfo(DBSCatalog.class, CoreMessages.dialog_connection_wizard_final_filter_catalogs));
        filters.add(new FilterInfo(DBSSchema.class, CoreMessages.dialog_connection_wizard_final_filter_schemas_users));
        filters.add(new FilterInfo(DBSTable.class, CoreMessages.dialog_connection_wizard_final_filter_tables));
    }

    ConnectionPageFinal(ConnectionWizard wizard, DataSourceDescriptor dataSourceDescriptor)
    {
        this(wizard);
        this.dataSourceDescriptor = dataSourceDescriptor;

        for (FilterInfo filterInfo : filters) {
            filterInfo.filter = dataSourceDescriptor.getObjectFilter(filterInfo.type, null, false);
        }
    }

    @Override
    public void dispose()
    {
        UIUtils.dispose(boldFont);
        super.dispose();
    }

    @Override
    public void activatePage()
    {
        if (connectionNameText != null) {
            ConnectionPageSettings settings = wizard.getPageSettings();
            if (settings != null && connectionNameText != null && (CommonUtils.isEmpty(connectionNameText.getText()) || !connectionNameChanged)) {
                DBPConnectionInfo connectionInfo = settings.getConnectionInfo();
                String newName = dataSourceDescriptor == null ? "" : dataSourceDescriptor.getName(); //$NON-NLS-1$
                if (CommonUtils.isEmpty(newName)) {
                    newName = connectionInfo.getDatabaseName();
                    if (CommonUtils.isEmpty(newName)) {
                        newName = connectionInfo.getHostName();
                    }
                    if (CommonUtils.isEmpty(newName)) {
                        newName = connectionInfo.getUrl();
                    }
                    if (CommonUtils.isEmpty(newName)) {
                        newName = CoreMessages.dialog_connection_wizard_final_default_new_connection_name;
                    }
                    StringTokenizer st = new StringTokenizer(newName, "/\\:,?=%$#@!^&*()"); //$NON-NLS-1$
                    while (st.hasMoreTokens()) {
                        newName = st.nextToken();
                    }
                    if (!CommonUtils.isEmpty(settings.getDriver().getCategory())) {
                        newName = settings.getDriver().getCategory() + " - " + newName; //$NON-NLS-1$
                    } else {
                        newName = settings.getDriver().getName() + " - " + newName; //$NON-NLS-1$
                    }
                    newName = CommonUtils.truncateString(newName, 50);
                }
                connectionNameText.setText(newName);
                connectionNameChanged = false;

                tunnelButton.setFont(getFont());
                for (DBWHandlerConfiguration config : connectionInfo.getDeclaredHandlers()) {
                    if (config.isEnabled()) {
                        tunnelButton.setFont(boldFont);
                        break;
                    }
                }
                eventsButton.setFont(getFont());
                for (DBPConnectionEventType eventType : connectionInfo.getDeclaredEvents()) {
                    if (connectionInfo.getEvent(eventType).isEnabled()) {
                        eventsButton.setFont(boldFont);
                        break;
                    }
                }
            }
        }
        if (dataSourceDescriptor != null) {
            savePasswordCheck.setSelection(dataSourceDescriptor.isSavePassword());
            showSystemObjects.setSelection(dataSourceDescriptor.isShowSystemObjects());
            readOnlyConnection.setSelection(dataSourceDescriptor.isConnectionReadOnly());
        }
        long features = 0;
        try {
            features = wizard.getPageSettings().getDriver().getDataSourceProvider().getFeatures();
        } catch (DBException e) {
            log.error("Can't obtain data source provider instance", e); //$NON-NLS-1$
        }

        for (FilterInfo filterInfo : filters) {
            if (DBSCatalog.class.isAssignableFrom(filterInfo.type)) {
                enableFilter(filterInfo, (features & DBPDataSourceProvider.FEATURE_CATALOGS) != 0);
            } else if (DBSSchema.class.isAssignableFrom(filterInfo.type)) {
                enableFilter(filterInfo, (features & DBPDataSourceProvider.FEATURE_SCHEMAS) != 0);
            } else {
                enableFilter(filterInfo, true);
            }
        }
        filtersGroup.layout();
    }

    private void enableFilter(FilterInfo filterInfo, boolean enable)
    {
        filterInfo.link.setEnabled(enable);
        if (enable) {
            filterInfo.link.setText("<a>" + filterInfo.title + "</a>");
            filterInfo.link.setToolTipText(NLS.bind(CoreMessages.dialog_connection_wizard_final_filter_link_tooltip, filterInfo.title));
            if (filterInfo.filter != null && !filterInfo.filter.isEmpty()) {
                filterInfo.link.setFont(boldFont);
            } else {
                filterInfo.link.setFont(getFont());
            }
        } else {
            filterInfo.link.setText(NLS.bind(CoreMessages.dialog_connection_wizard_final_filter_link_not_supported_text, filterInfo.title));
            filterInfo.link.setToolTipText(NLS.bind(CoreMessages.dialog_connection_wizard_final_filter_link_not_supported_tooltip, filterInfo.title, wizard.getPageSettings().getDriver().getName()));
        }
    }

    @Override
    public void deactivatePage()
    {
    }

    @Override
    public void createControl(Composite parent)
    {
        boldFont = UIUtils.makeBoldFont(parent.getFont());
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        //gl.marginHeight = 20;
        //gl.marginWidth = 20;
        //gl.verticalSpacing = 10;
        group.setLayout(gl);
        GridData gd;// = new GridData(GridData.FILL_HORIZONTAL);
        //gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
        //gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        //group.setLayoutData(gd);

        String connectionName = dataSourceDescriptor == null ? "" : dataSourceDescriptor.getName(); //$NON-NLS-1$
        connectionNameText = UIUtils.createLabelText(group, CoreMessages.dialog_connection_wizard_final_label_connection_name, CommonUtils.toString(connectionName));
        connectionNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                connectionNameChanged = true;
                ConnectionPageFinal.this.getContainer().updateButtons();
            }
        });

        {
            Group securityGroup = UIUtils.createControlGroup(group, CoreMessages.dialog_connection_wizard_final_group_security, 1, GridData.FILL_HORIZONTAL, 0);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            gd.widthHint = 400;
            securityGroup.setLayoutData(gd);

            savePasswordCheck = UIUtils.createCheckbox(securityGroup, CoreMessages.dialog_connection_wizard_final_checkbox_save_password_locally, dataSourceDescriptor == null || dataSourceDescriptor.isSavePassword());
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            //gd.horizontalSpan = 2;
            savePasswordCheck.setLayoutData(gd);
        }

        Composite optionsGroup = new Composite(group, SWT.NONE);
        gl = new GridLayout(2, true);
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 5;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        optionsGroup.setLayout(gl);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        optionsGroup.setLayoutData(gd);

        {
            Group miscGroup = UIUtils.createControlGroup(
                optionsGroup,
                CoreMessages.dialog_connection_wizard_final_group_misc,
                1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL, 0);

            showSystemObjects = UIUtils.createCheckbox(
                miscGroup,
                CoreMessages.dialog_connection_wizard_final_checkbox_show_system_objects,
                dataSourceDescriptor == null || dataSourceDescriptor.isShowSystemObjects());
            showSystemObjects.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

            readOnlyConnection = UIUtils.createCheckbox(
                miscGroup,
                CoreMessages.dialog_connection_wizard_final_checkbox_connection_readonly,
                dataSourceDescriptor != null && dataSourceDescriptor.isConnectionReadOnly());
            gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            //gd.horizontalSpan = 2;
            readOnlyConnection.setLayoutData(gd);
        }
        {
            // Filters
            filtersGroup = UIUtils.createControlGroup(
                optionsGroup,
                CoreMessages.dialog_connection_wizard_final_group_filters,
                1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL, 0);

            for (int i = 0; i < filters.size(); i++) {
                final FilterInfo filterInfo = filters.get(i);
                filterInfo.link = new Link(filtersGroup,SWT.NONE);
                filterInfo.link.setText("<a>" + filterInfo.title + "</a>");
                filterInfo.link.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        //DBSObjectFilter filter = 
                        EditObjectFilterDialog dialog = new EditObjectFilterDialog(
                            getShell(),
                            filterInfo.title,
                            filterInfo.filter != null ? filterInfo.filter : new DBSObjectFilter(),
                            true);
                        if (dialog.open() == IDialogConstants.OK_ID) {
                            filterInfo.filter = dialog.getFilter();
                            if (filterInfo.filter != null && !filterInfo.filter.isEmpty()) {
                                filterInfo.link.setFont(boldFont);
                            } else {
                                filterInfo.link.setFont(getFont());
                            }
                        }
                    }
                });
            }
        }

        {
            //Composite buttonsGroup = UIUtils.createPlaceholder(group, 3);
            Composite buttonsGroup = new Composite(group, SWT.NONE);
            gl = new GridLayout(3, false);
            gl.verticalSpacing = 0;
            gl.horizontalSpacing = 10;
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            buttonsGroup.setLayout(gl);

            //buttonsGroup.setLayout(new GridLayout(2, true));
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            buttonsGroup.setLayoutData(gd);

//            String catFilter = dataSourceDescriptor == null ? null : dataSourceDescriptor.getCatalogFilter();
//            catFilterText = UIUtils.createCheckText(buttonsGroup, CoreMessages.dialog_connection_wizard_final_checkbox_filter_catalogs, CommonUtils.getString(catFilter), !CommonUtils.isEmpty(catFilter), 200);
//
//            String schFilter = dataSourceDescriptor == null ? "" : dataSourceDescriptor.getSchemaFilter(); //$NON-NLS-1$
//            schemaFilterText = UIUtils.createCheckText(buttonsGroup, CoreMessages.dialog_connection_wizard_final_checkbox_filter_schemas, CommonUtils.getString(schFilter), !CommonUtils.isEmpty(schFilter), 200);

            {
                tunnelButton = new Button(buttonsGroup, SWT.PUSH);
                tunnelButton.setText(CoreMessages.dialog_connection_wizard_final_button_tunneling);
                gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
                gd.grabExcessVerticalSpace = true;
                tunnelButton.setLayoutData(gd);
                tunnelButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        configureTunnels();
                    }
                });
            }

            {
                eventsButton = new Button(buttonsGroup, SWT.PUSH);
                eventsButton.setText(CoreMessages.dialog_connection_wizard_final_button_events);
                gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
                gd.grabExcessHorizontalSpace = true;
                eventsButton.setLayoutData(gd);
                eventsButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        configureEvents();
                    }
                });
            }
        }

        setControl(group);

        UIUtils.setHelp(group, IHelpContextIds.CTX_CON_WIZARD_FINAL);
    }

    @Override
    public boolean isPageComplete()
    {
        return connectionNameText != null &&
            !CommonUtils.isEmpty(connectionNameText.getText());
    }

    void saveSettings(DataSourceDescriptor dataSource)
    {
        dataSource.setName(connectionNameText.getText());
        dataSource.setSavePassword(savePasswordCheck.getSelection());
        dataSource.setShowSystemObjects(showSystemObjects.getSelection());
        dataSource.setConnectionReadOnly(readOnlyConnection.getSelection());
        if (!dataSource.isSavePassword()) {
            dataSource.resetPassword();
        }
        for (FilterInfo filterInfo : filters) {
            if (filterInfo.filter != null) {
                dataSource.setObjectFilter(filterInfo.type, null, filterInfo.filter);
            }
        }
    }

    private void configureEvents()
    {
        DBPConnectionInfo connectionInfo = wizard.getPageSettings().getConnectionInfo();
        EditEventsDialog dialog = new EditEventsDialog(
            getShell(),
            connectionInfo);
        if (dialog.open() == IDialogConstants.OK_ID) {
            eventsButton.setFont(getFont());
            for (DBPConnectionEventType eventType : connectionInfo.getDeclaredEvents()) {
                if (connectionInfo.getEvent(eventType).isEnabled()) {
                    eventsButton.setFont(boldFont);
                    break;
                }
            }
        }
    }

    private void configureTunnels()
    {
        EditTunnelDialog dialog = new EditTunnelDialog(
            getShell(),
            wizard.getPageSettings().getDriver(),
            wizard.getPageSettings().getConnectionInfo());
        if (dialog.open() == IDialogConstants.OK_ID) {
            tunnelButton.setFont(getFont());
            for (DBWHandlerConfiguration config : wizard.getPageSettings().getConnectionInfo().getDeclaredHandlers()) {
                if (config.isEnabled()) {
                    tunnelButton.setFont(boldFont);
                    break;
                }
            }
       }
    }

}