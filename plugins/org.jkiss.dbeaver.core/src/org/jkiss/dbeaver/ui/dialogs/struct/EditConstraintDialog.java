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

package org.jkiss.dbeaver.ui.dialogs.struct;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;

/**
 * EditConstraintDialog
 *
 * @author Serge Rider
 */
public class EditConstraintDialog extends AttributesSelectorDialog {

    private DBSEntityConstraintType[] constraintTypes;
    private DBSEntityConstraintType selectedConstraintType;
    private DBSEntityReferrer constraint;
    private Collection<? extends DBSEntityAttributeRef> attributes;

    public EditConstraintDialog(
        Shell shell,
        String title,
        DBSEntity entity,
        DBSEntityConstraintType[] constraintTypes)
    {
        super(shell, title, entity);
        this.constraintTypes = constraintTypes;
        Assert.isTrue(!CommonUtils.isEmpty(this.constraintTypes));
    }

    public EditConstraintDialog(
        Shell shell,
        String title,
        DBSEntityReferrer constraint)
    {
        super(shell, title, constraint.getParentObject());
        this.constraint = constraint;
        this.constraintTypes = new DBSEntityConstraintType[] {constraint.getConstraintType()};
        try {
            this.attributes = constraint.getAttributeReferences(VoidProgressMonitor.INSTANCE);
        } catch (DBException e) {
            UIUtils.showErrorDialog(shell, "Can't get attributes", "Error obtaining entity attributes", e);
        }
    }

    @Override
    protected void createContentsBeforeColumns(Composite panel)
    {
        UIUtils.createControlLabel(panel, CoreMessages.dialog_struct_edit_constrain_label_type);
        final Combo typeCombo = new Combo(panel, SWT.DROP_DOWN | SWT.READ_ONLY);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        for (DBSEntityConstraintType constraintType : constraintTypes) {
            typeCombo.add(constraintType.getName());
            if (selectedConstraintType == null) {
                selectedConstraintType = constraintType;
            }
        }
        typeCombo.select(0);
        typeCombo.setEnabled(constraintTypes.length > 1);
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                selectedConstraintType = constraintTypes[typeCombo.getSelectionIndex()];
            }
        });
    }

    public DBSEntityConstraintType getConstraintType()
    {
        return selectedConstraintType;
    }

    @Override
    public boolean isColumnSelected(DBSEntityAttribute attribute)
    {
        if (!CommonUtils.isEmpty(attributes)) {
            for (DBSEntityAttributeRef ref : attributes) {
                if (ref.getAttribute() == attribute) {
                    return true;
                }
            }
        }
        return false;
    }

}
