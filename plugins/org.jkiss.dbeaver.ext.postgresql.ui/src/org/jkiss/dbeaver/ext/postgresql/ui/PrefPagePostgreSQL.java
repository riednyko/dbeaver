/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
 * Copyright (C) 2011-2012 Eugene Fradkin (eugene.fradkin@gmail.com)
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
package org.jkiss.dbeaver.ext.postgresql.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jkiss.dbeaver.ext.postgresql.PostgreConstants;
import org.jkiss.dbeaver.ext.postgresql.PostgreMessages;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.preferences.AbstractPrefPage;
import org.jkiss.dbeaver.ui.preferences.PreferenceStoreDelegate;

/**
 * PrefPagePostgreSQL
 */
public class PrefPagePostgreSQL extends AbstractPrefPage implements IWorkbenchPreferencePage
{
    public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.postgresql.general"; //$NON-NLS-1$

    private Button showNonDefault;
    private Button showTemplates;
    private Combo ddPlainBehaviorCombo;
    private Combo ddTagBehaviorCombo;

    public PrefPagePostgreSQL()
    {
        super();
        setPreferenceStore(new PreferenceStoreDelegate(DBWorkbench.getPlatform().getPreferenceStore()));
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite cfgGroup = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        cfgGroup.setLayout(gl);
        cfgGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        DBPPreferenceStore globalPrefs = DBWorkbench.getPlatform().getPreferenceStore();

        {
            Group secureGroup = new Group(cfgGroup, SWT.NONE);
            secureGroup.setText(PostgreMessages.dialog_setting_connection_settings);
            secureGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            secureGroup.setLayout(new GridLayout(2, false));

            showNonDefault = UIUtils.createCheckbox(secureGroup,
                PostgreMessages.dialog_setting_connection_nondefaultDatabase,
                PostgreMessages.dialog_setting_connection_nondefaultDatabase_tip,
                globalPrefs.getBoolean(PostgreConstants.PROP_SHOW_NON_DEFAULT_DB),
                2);
            showNonDefault.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    showTemplates.setEnabled(showNonDefault.getSelection());
                }
            });
            showTemplates = UIUtils.createCheckbox(secureGroup,
                PostgreMessages.dialog_setting_connection_show_templates,
                PostgreMessages.dialog_setting_connection_show_templates_tip,
                globalPrefs.getBoolean(PostgreConstants.PROP_SHOW_TEMPLATES_DB),
                2);
        }

        {
            Group secureGroup = new Group(cfgGroup, SWT.NONE);
            secureGroup.setText(PostgreMessages.dialog_setting_group_sql);
            secureGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            secureGroup.setLayout(new GridLayout(2, false));

            ddPlainBehaviorCombo = UIUtils.createLabelCombo(secureGroup, PostgreMessages.dialog_setting_sql_dd_plain_label, PostgreMessages.dialog_setting_sql_dd_plain_tip, SWT.DROP_DOWN | SWT.READ_ONLY);
            ddPlainBehaviorCombo.add(PostgreMessages.dialog_setting_sql_dd_string);
            ddPlainBehaviorCombo.add(PostgreMessages.dialog_setting_sql_dd_code_block);
            ddTagBehaviorCombo = UIUtils.createLabelCombo(secureGroup, PostgreMessages.dialog_setting_sql_dd_tag_label, PostgreMessages.dialog_setting_sql_dd_tag_tip, SWT.DROP_DOWN | SWT.READ_ONLY);
            ddTagBehaviorCombo.add(PostgreMessages.dialog_setting_sql_dd_string);
            ddTagBehaviorCombo.add(PostgreMessages.dialog_setting_sql_dd_code_block);

            ddPlainBehaviorCombo.select(globalPrefs.getBoolean(PostgreConstants.PROP_DD_PLAIN_STRING) ? 0 : 1);
            ddTagBehaviorCombo.select(globalPrefs.getBoolean(PostgreConstants.PROP_DD_TAG_STRING) ? 0 : 1);
        }

        return cfgGroup;
    }

    @Override
    public boolean performOk() {
        DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
        preferenceStore.setValue(PostgreConstants.PROP_SHOW_NON_DEFAULT_DB, String.valueOf(showNonDefault.getSelection()));
        preferenceStore.setValue(PostgreConstants.PROP_SHOW_TEMPLATES_DB, String.valueOf(showTemplates.getSelection()));

        preferenceStore.setValue(PostgreConstants.PROP_DD_PLAIN_STRING, ddPlainBehaviorCombo.getSelectionIndex() == 0);
        preferenceStore.setValue(PostgreConstants.PROP_DD_TAG_STRING, ddTagBehaviorCombo.getSelectionIndex() == 0);

        return super.performOk();
    }

    @Override
    public void init(IWorkbench workbench) {

    }
}
