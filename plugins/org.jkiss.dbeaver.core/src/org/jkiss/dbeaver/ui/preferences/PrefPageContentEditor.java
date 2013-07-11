/*
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.utils.AbstractPreferenceStore;

/**
 * PrefPageSQL
 */
public class PrefPageContentEditor extends TargetPrefPage
{
    public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.main.contenteditor"; //$NON-NLS-1$

    private Spinner maxTextContentSize;
    private Button editLongAsLobCheck;
    private Button commitOnEditApplyCheck;
    private Button commitOnContentApplyCheck;
    private Combo encodingCombo;

    public PrefPageContentEditor()
    {
        super();
    }

    @Override
    protected boolean hasDataSourceSpecificOptions(DataSourceDescriptor dataSourceDescriptor)
    {
        AbstractPreferenceStore store = dataSourceDescriptor.getPreferenceStore();
        return
                store.contains(PrefConstants.RS_EDIT_MAX_TEXT_SIZE) ||
                store.contains(PrefConstants.RS_EDIT_LONG_AS_LOB) ||
                store.contains(PrefConstants.RS_COMMIT_ON_EDIT_APPLY) ||
                store.contains(PrefConstants.RS_COMMIT_ON_CONTENT_APPLY)
            ;
    }

    @Override
    protected boolean supportsDataSourceSpecificOptions()
    {
        return true;
    }

    @Override
    protected Control createPreferenceContent(Composite parent)
    {
        Composite composite = UIUtils.createPlaceholder(parent, 1);

        // Content
        {
            Group contentGroup = new Group(composite, SWT.NONE);
            contentGroup.setText(CoreMessages.pref_page_content_editor_group_content);
            contentGroup.setLayout(new GridLayout(2, false));

            UIUtils.createControlLabel(contentGroup, CoreMessages.pref_page_content_editor_label_max_text_length);

            maxTextContentSize = new Spinner(contentGroup, SWT.BORDER);
            maxTextContentSize.setSelection(0);
            maxTextContentSize.setDigits(0);
            maxTextContentSize.setIncrement(1000000);
            maxTextContentSize.setMinimum(0);
            maxTextContentSize.setMaximum(Integer.MAX_VALUE);

            editLongAsLobCheck = UIUtils.createLabelCheckbox(contentGroup, CoreMessages.pref_page_content_editor_checkbox_edit_long_as_lobs, false);
            commitOnEditApplyCheck = UIUtils.createLabelCheckbox(contentGroup, CoreMessages.pref_page_content_editor_checkbox_commit_on_value_apply, false);
            commitOnContentApplyCheck = UIUtils.createLabelCheckbox(contentGroup, CoreMessages.pref_page_content_editor_checkbox_commit_on_content_apply, false);
        }

        // Hex
        {
            Group contentGroup = new Group(composite, SWT.NONE);
            contentGroup.setText(CoreMessages.pref_page_content_editor_group_hex);
            contentGroup.setLayout(new GridLayout(2, false));

            UIUtils.createControlLabel(contentGroup, CoreMessages.pref_page_content_editor_hex_encoding);
            encodingCombo = UIUtils.createEncodingCombo(contentGroup, null);
        }
        return composite;
    }

    @Override
    protected void loadPreferences(IPreferenceStore store)
    {
        try {
            maxTextContentSize.setSelection(store.getInt(PrefConstants.RS_EDIT_MAX_TEXT_SIZE));
            editLongAsLobCheck.setSelection(store.getBoolean(PrefConstants.RS_EDIT_LONG_AS_LOB));
            commitOnEditApplyCheck.setSelection(store.getBoolean(PrefConstants.RS_COMMIT_ON_EDIT_APPLY));
            commitOnContentApplyCheck.setSelection(store.getBoolean(PrefConstants.RS_COMMIT_ON_CONTENT_APPLY));
            UIUtils.setComboSelection(encodingCombo, store.getString(PrefConstants.CONTENT_HEX_ENCODING));
        } catch (Exception e) {
            log.warn(e);
        }
    }

    @Override
    protected void savePreferences(IPreferenceStore store)
    {
        try {
            store.setValue(PrefConstants.RS_EDIT_MAX_TEXT_SIZE, maxTextContentSize.getSelection());
            store.setValue(PrefConstants.RS_EDIT_LONG_AS_LOB, editLongAsLobCheck.getSelection());
            store.setValue(PrefConstants.RS_COMMIT_ON_EDIT_APPLY, commitOnEditApplyCheck.getSelection());
            store.setValue(PrefConstants.RS_COMMIT_ON_CONTENT_APPLY, commitOnContentApplyCheck.getSelection());
            store.setValue(PrefConstants.CONTENT_HEX_ENCODING, UIUtils.getComboSelection(encodingCombo));
        } catch (Exception e) {
            log.warn(e);
        }
        RuntimeUtils.savePreferenceStore(store);
    }

    @Override
    protected void clearPreferences(IPreferenceStore store)
    {
        store.setToDefault(PrefConstants.RS_EDIT_MAX_TEXT_SIZE);
        store.setToDefault(PrefConstants.RS_EDIT_LONG_AS_LOB);
        store.setToDefault(PrefConstants.RS_COMMIT_ON_EDIT_APPLY);
        store.setToDefault(PrefConstants.RS_COMMIT_ON_CONTENT_APPLY);
        store.setToDefault(PrefConstants.CONTENT_HEX_ENCODING);
    }

    @Override
    public void applyData(Object data)
    {
        super.applyData(data);
    }

    @Override
    protected String getPropertyPageID()
    {
        return PAGE_ID;
    }

}