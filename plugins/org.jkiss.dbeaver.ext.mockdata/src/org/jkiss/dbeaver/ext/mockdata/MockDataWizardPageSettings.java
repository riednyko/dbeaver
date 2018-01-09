/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2010-2017 Eugene Fradkin (eugene.fradkin@gmail.com)
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
package org.jkiss.dbeaver.ext.mockdata;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.tools.AbstractToolWizardPage;

public class MockDataWizardPageSettings extends AbstractToolWizardPage<MockDataExecuteWizard>
{
    private Button removeOldDataCheck;

    protected MockDataWizardPageSettings(MockDataExecuteWizard wizard)
    {
        super(wizard, MockDataMessages.tools_mockdata_wizard_page_settings_page_name);
        setTitle(MockDataMessages.tools_mockdata_wizard_page_settings_page_name);
        setDescription((MockDataMessages.tools_mockdata_wizard_page_settings_page_description));
    }

    public void createControl(Composite parent)
    {
        Composite composite = UIUtils.createPlaceholder(parent, 1);

        SelectionListener changeListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateState();
            }
        };

        Group settingsGroup = UIUtils.createControlGroup(composite, MockDataMessages.tools_mockdata_wizard_page_settings_group_settings, 3, GridData.FILL_HORIZONTAL, 0);
        removeOldDataCheck = UIUtils.createCheckbox(settingsGroup, MockDataMessages.tools_mockdata_wizard_page_settings_checkbox_remove_old_data, wizard.removeOldData);
        removeOldDataCheck.addSelectionListener(changeListener);

        setControl(composite);

    }

    private void updateState()
    {
        wizard.removeOldData = removeOldDataCheck.getSelection();

        getContainer().updateButtons();
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }
}
