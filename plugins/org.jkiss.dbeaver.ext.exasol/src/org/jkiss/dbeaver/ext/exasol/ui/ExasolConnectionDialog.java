/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2017 Karl Griesser (fullref@gmail.com)
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
package org.jkiss.dbeaver.ext.exasol.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.ext.exasol.model.ExasolDataSource;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.jkiss.utils.CommonUtils;


public class ExasolConnectionDialog extends BaseDialog {

    private String name = "";
    private String user = "";
    private String password = "";
    private String url = "";
    private String comment = "";

    
    public ExasolConnectionDialog(Shell parentShell, ExasolDataSource datasource)
    {
        super(parentShell,"Create Connection",null);
    }
    
    

    @Override
    protected Composite createDialogArea(Composite parent)
    {
        final Composite composite = super.createDialogArea(parent);
        
        final Composite group = new Composite(composite, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        final Text nameText = UIUtils.createLabelText(group, "Schema Name", "");
        
        final Text urlText = UIUtils.createLabelText(group,"Connection URL", "");


        
        final Group configGroup  = UIUtils.createControlGroup(group, "Credentials", 1, GridData.FILL_HORIZONTAL, 0);
        Button saveCred = UIUtils.createCheckbox(configGroup, "Provide Credentials", false);
        
        Text userText = UIUtils.createLabelText(configGroup, "User", "");
        userText.setEnabled(false);
        Text passwordText = UIUtils.createLabelText(configGroup, "Password", "", SWT.PASSWORD);
        passwordText.setEnabled(false);

        final Text commentText = UIUtils.createLabelText(group,"Comment", "");
        
        ModifyListener mod = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameText.getText();
                user = userText.getText();
                password = passwordText.getText();
                comment = commentText.getText();
                getButton(IDialogConstants.OK_ID).setEnabled(!name.isEmpty());
                //enable/disable OK button   
                if (
                        (
                            saveCred.getSelection() &
                            (
                                CommonUtils.isEmpty(user) |
                                CommonUtils.isEmpty(password)
                            )
                        ) 
                        | name.isEmpty() 
                        | url.isEmpty()
                    )
                {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        };
        
        nameText.addModifyListener(mod);
        userText.addModifyListener(mod);
        passwordText.addModifyListener(mod);
        saveCred.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                userText.setEnabled(saveCred.getSelection());
                passwordText.setEnabled(saveCred.getSelection());
            }
            
        });
        
        return composite;
    }
    
    public String getName()
    {
        return name;
    }


    public String getUser()
    {
        return user;
    }


    public String getPassword()
    {
        return password;
    }
    

    public String getUrl()
    {
        return url;
    }

    public String getComment()
    {
        return comment;
    }

    
    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

}
