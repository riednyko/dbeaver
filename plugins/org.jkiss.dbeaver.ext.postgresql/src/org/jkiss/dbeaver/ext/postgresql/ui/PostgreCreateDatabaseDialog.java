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
package org.jkiss.dbeaver.ext.postgresql.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.ext.postgresql.model.*;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.jkiss.dbeaver.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreCreateDatabaseDialog
 */
public class PostgreCreateDatabaseDialog extends BaseDialog
{
    private final PostgreDataSource dataSource;
    private List<PostgreRole> allUsers;
    private List<PostgreCharset> allEncodings;
    private List<PostgreTablespace> allTablespaces;

    private String name;
    private PostgreRole owner;
    private PostgreCharset encoding;
    private PostgreTablespace tablespace;

    public PostgreCreateDatabaseDialog(Shell parentShell, PostgreDataSource dataSource) {
        super(parentShell, "Create database", null);
        this.dataSource = dataSource;
    }

    @Override
    protected Composite createDialogArea(Composite parent) {
        final Composite composite = super.createDialogArea(parent);

        final Composite groupGeneral = UIUtils.createControlGroup(composite, "General", 2, GridData.FILL_HORIZONTAL, SWT.NONE);

        final Text nameText = UIUtils.createLabelText(groupGeneral, "Database name", "");
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameText.getText();
                getButton(IDialogConstants.OK_ID).setEnabled(!name.isEmpty());
            }
        });

        final Combo userCombo = UIUtils.createLabelCombo(groupGeneral, "Owner", SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        userCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                owner = allUsers.get(userCombo.getSelectionIndex());
            }
        });

        final Composite groupDefinition = UIUtils.createControlGroup(composite, "Definition", 2, GridData.FILL_HORIZONTAL, SWT.NONE);
        final Combo encodingCombo = UIUtils.createLabelCombo(groupDefinition, "Encoding", SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        encodingCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                encoding = allEncodings.get(encodingCombo.getSelectionIndex());
            }
        });
        final Combo tablespaceCombo = UIUtils.createLabelCombo(groupDefinition, "Tablespace", SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        tablespaceCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tablespace = allTablespaces.get(tablespaceCombo.getSelectionIndex());
            }
        });


        new AbstractJob("Load users") {

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                try {
                    PostgreDatabase database = dataSource.getDefaultInstance();
                    allUsers = new ArrayList<>(database.getUsers(monitor));
                    allEncodings = new ArrayList<>(database.getEncodings(monitor));
                    allTablespaces = new ArrayList<>(database.getTablespaces(monitor));

                    final PostgreRole dba = database.getDBA(monitor);
                    final String defUserName = dba == null ? "" : dba.getName();
                    final PostgreCharset defCharset = database.getDefaultEncoding(monitor);
                    final PostgreTablespace defTablespace = database.getDefaultTablespace(monitor);

                    DBeaverUI.syncExec(new Runnable() {
                        @Override
                        public void run() {
                            for (PostgreRole authId : allUsers) {
                                String name = authId.getName();
                                userCombo.add(name);
                                if (name.equals(defUserName)) {
                                    owner = authId;
                                }
                            }
                            userCombo.setText(defUserName);

                            for (PostgreCharset charset : allEncodings) {
                                encodingCombo.add(charset.getName());
                                if (charset == defCharset) {
                                    encoding = defCharset;
                                }
                            }
                            encodingCombo.setText(defCharset.getName());

                            for (PostgreTablespace ts : allTablespaces) {
                                tablespaceCombo.add(ts.getName());
                                if (ts == defTablespace) {
                                    tablespace = ts;
                                }
                            }
                            tablespaceCombo.setText(defTablespace.getName());
                        }
                    });
                } catch (DBException e) {
                    return GeneralUtils.makeExceptionStatus(e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();

        return composite;
    }

    public String getName() {
        return name;
    }

    public PostgreRole getOwner() {
        return owner;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
}
