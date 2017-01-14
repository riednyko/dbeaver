/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
 * Copyright (C) 2011-2012 Eugene Fradkin (eugene.fradkin@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.preferences;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.registry.driver.DriverDescriptor;
import org.jkiss.dbeaver.registry.encode.EncryptionException;
import org.jkiss.dbeaver.registry.encode.SecuredPasswordEncrypter;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.DialogUtils;
import org.jkiss.dbeaver.ui.dialogs.EnterNameDialog;
import org.jkiss.dbeaver.utils.PrefUtils;
import org.jkiss.utils.CommonUtils;

/**
 * PrefPageDrivers
 */
public class PrefPageDrivers extends AbstractPrefPage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage
{
    private static final Log log = Log.getLog(PrefPageDrivers.class);

    public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.drivers"; //$NON-NLS-1$

    private Button versionUpdateCheck;

    private List sourceList;

    private Text proxyHostText;
    private Spinner proxyPortSpinner;
    private Text proxyUserText;
    private Text proxyPasswordText;
    private SecuredPasswordEncrypter encrypter;

    private Text customDriversHome;

    @Override
    public void init(IWorkbench workbench)
    {
        try {
            encrypter = new SecuredPasswordEncrypter();
        } catch (EncryptionException e) {
            // ignore
            log.warn(e);
        }
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite composite = UIUtils.createPlaceholder(parent, 1, 5);

        {
            Group settings = UIUtils.createControlGroup(composite, "Settings", 2, GridData.FILL_HORIZONTAL, 300);
            versionUpdateCheck = UIUtils.createCheckbox(settings, "Check for new driver versions", false);
        }

        {
            Group proxyObjects = UIUtils.createControlGroup(composite, CoreMessages.pref_page_ui_general_group_http_proxy, 4, GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING, 300);
            proxyHostText = UIUtils.createLabelText(proxyObjects, CoreMessages.pref_page_ui_general_label_proxy_host, null); //$NON-NLS-2$
            proxyPortSpinner = UIUtils.createLabelSpinner(proxyObjects, CoreMessages.pref_page_ui_general_spinner_proxy_port, 0, 0, 65535);
            proxyUserText = UIUtils.createLabelText(proxyObjects, CoreMessages.pref_page_ui_general_label_proxy_user, null); //$NON-NLS-2$
            proxyPasswordText = UIUtils.createLabelText(proxyObjects, CoreMessages.pref_page_ui_general_label_proxy_password, null, SWT.PASSWORD | SWT.BORDER); //$NON-NLS-2$
        }

        {
            Group drivers = UIUtils.createControlGroup(composite, CoreMessages.pref_page_drivers_group_location, 2, GridData.FILL_HORIZONTAL, 300);
            customDriversHome = DialogUtils.createOutputFolderChooser(drivers, "Local folder", null);
        }

        {
            Group repoGroup = UIUtils.createControlGroup(composite, "File repositories", 2, GridData.FILL_HORIZONTAL, 300);
            sourceList = new List(repoGroup, SWT.BORDER | SWT.SINGLE);
            sourceList.setLayoutData(new GridData(GridData.FILL_BOTH));
            Composite buttonsPH = UIUtils.createPlaceholder(repoGroup, 1);
            UIUtils.createToolButton(buttonsPH, "Add", new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    String url = EnterNameDialog.chooseName(getShell(), "Enter drivers location URL", "http://");
                    if (url != null) {
                        sourceList.add(url);
                    }
                }
            });
            final Button removeButton = UIUtils.createToolButton(buttonsPH, "Remove", new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    sourceList.remove(sourceList.getSelectionIndices());
                    sourceList.notifyListeners(SWT.Selection, new Event());
                }
            });
            removeButton.setEnabled(false);

            sourceList.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if (sourceList.getSelectionIndex() >= 0) {
                        removeButton.setEnabled(sourceList.getItemCount() > 1);
                    } else {
                        removeButton.setEnabled(false);
                    }
                }
            });
        }

        performDefaults();

        return composite;
    }

    @Override
    protected void performDefaults()
    {
        DBPPreferenceStore store = DBeaverCore.getGlobalPreferenceStore();

        versionUpdateCheck.setSelection(store.getBoolean(DBeaverPreferences.UI_DRIVERS_VERSION_UPDATE));

        proxyHostText.setText(store.getString(DBeaverPreferences.UI_PROXY_HOST));
        proxyPortSpinner.setSelection(store.getInt(DBeaverPreferences.UI_PROXY_PORT));
        proxyUserText.setText(store.getString(DBeaverPreferences.UI_PROXY_USER));
        // Load and decrypt password
        String passwordString = store.getString(DBeaverPreferences.UI_PROXY_PASSWORD);
        if (!CommonUtils.isEmpty(passwordString) && encrypter != null) {
            try {
                passwordString = encrypter.decrypt(passwordString);
            } catch (EncryptionException e) {
                log.warn(e);
            }
        }
        proxyPasswordText.setText(passwordString);
        customDriversHome.setText(DriverDescriptor.getCustomDriversHome().getAbsolutePath());

        for (String source : DriverDescriptor.getDriversSources()) {
            sourceList.add(source);
        }
        super.performDefaults();
    }

    @Override
    public boolean performOk()
    {
        DBPPreferenceStore store = DBeaverCore.getGlobalPreferenceStore();
        store.setValue(DBeaverPreferences.UI_DRIVERS_VERSION_UPDATE, versionUpdateCheck.getSelection());

        store.setValue(DBeaverPreferences.UI_PROXY_HOST, proxyHostText.getText());
        store.setValue(DBeaverPreferences.UI_PROXY_PORT, proxyPortSpinner.getSelection());
        store.setValue(DBeaverPreferences.UI_PROXY_USER, proxyUserText.getText());
        String password = proxyPasswordText.getText();
        if (!CommonUtils.isEmpty(password) && encrypter != null) {
            // Encrypt password
            try {
                password = encrypter.encrypt(password);
            } catch (EncryptionException e) {
                log.warn(e);
            }
        }
        store.setValue(DBeaverPreferences.UI_PROXY_PASSWORD, password);
        store.setValue(DBeaverPreferences.UI_DRIVERS_HOME, customDriversHome.getText());

        {
            StringBuilder sources = new StringBuilder();
            for (String item : sourceList.getItems()) {
                if (sources.length() > 0) sources.append('|');
                sources.append(item);
            }
            store.setValue(DBeaverPreferences.UI_DRIVERS_SOURCES, sources.toString());
        }

        PrefUtils.savePreferenceStore(store);

        return super.performOk();
    }

    @Override
    public IAdaptable getElement()
    {
        return null;
    }

    @Override
    public void setElement(IAdaptable element)
    {

    }

}