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
package org.jkiss.dbeaver.ui.controls.resultset;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.exec.DBCEntityIdentifier;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.virtual.DBVEntity;
import org.jkiss.dbeaver.model.virtual.DBVEntityConstraint;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Confirm virtual key usage dialog
 */
class ValidateUniqueKeyUsageDialog extends MessageDialogWithToggle {

    private ResultSetViewer viewer;

    protected ValidateUniqueKeyUsageDialog(ResultSetViewer viewer)
    {
        super(
            viewer.getControl().getShell(),
            "Possible multiple rows modification",
            null,
            "There is no physical unique key defined for  '" + DBUtils.getObjectFullName(viewer.getVirtualEntityIdentifier().getReferrer().getParentObject()) +
                "'.\nDBeaver will use all columns as unique key. Possible multiple rows modification. \nAre you sure you want to proceed?",
            WARNING,
            new String[]{"Use All Columns", "Custom Unique Key", IDialogConstants.CANCEL_LABEL},
            0,
            "Do not ask again for '" + viewer.getDataSource().getContainer().getName() + "'",
            false);
        this.viewer = viewer;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
    }

    @Override
    protected void buttonPressed(int buttonId)
    {
        switch (buttonId)
        {
            case IDialogConstants.CANCEL_ID:
                // Save toggle state
//                DBVEntity entity = (DBVEntity) viewer.getVirtualEntityIdentifier().getReferrer().getParentObject();
//                entity.setProperty(DBVConstants.PROPERTY_USE_VIRTUAL_KEY_QUIET, String.valueOf(getToggleState()));

                super.buttonPressed(buttonId);
                break;
            case IDialogConstants.INTERNAL_ID:
                useAllColumns();

                break;
            case IDialogConstants.INTERNAL_ID + 1:
                editCustomKey();

                break;
        }
        viewer.getDataSource().getContainer().persistConfiguration();
    }

    private void useAllColumns()
    {
        // Use all columns
        final DBCEntityIdentifier identifier = viewer.getVirtualEntityIdentifier();
        DBVEntityConstraint constraint = (DBVEntityConstraint) viewer.getVirtualEntityIdentifier().getReferrer();
        List<DBSEntityAttribute> uniqueColumns = new ArrayList<DBSEntityAttribute>();
        for (DBDAttributeBinding binding : viewer.getModel().getColumns()) {
            if (binding.getEntityAttribute() != null) {
                uniqueColumns.add(binding.getEntityAttribute());
            }
        }
        if (uniqueColumns.isEmpty()) {
            UIUtils.showErrorDialog(getShell(), "Use All Columns", "No valid columns found for unique key");
            return;
        }
        constraint.setAttributes(uniqueColumns);

        try {
            identifier.reloadAttributes(VoidProgressMonitor.INSTANCE, viewer.getModel().getVisibleColumn(0).getMetaAttribute().getEntity());
        } catch (DBException e) {
            UIUtils.showErrorDialog(getShell(), "Use All Columns", "Can't reload unique columns", e);
            return;
        }

        super.buttonPressed(IDialogConstants.OK_ID);
    }

    private void editCustomKey()
    {
        // Edit custom key
        try {
            if (viewer.editEntityIdentifier(VoidProgressMonitor.INSTANCE)) {
                super.buttonPressed(IDialogConstants.OK_ID);
            }
        } catch (DBException e) {
            UIUtils.showErrorDialog(getShell(), "Virtual key edit", "Error editing virtual key", e);
        }
    }

    public static boolean validateUniqueKey(ResultSetViewer viewer)
    {
        final DBCEntityIdentifier identifier = viewer.getVirtualEntityIdentifier();
        if (identifier == null) {
            // No key
            return false;
        }
        if (!CommonUtils.isEmpty(identifier.getAttributes())) {
            // Key already defined
            return true;
        }

        DBSEntity ownerEntity = identifier.getReferrer().getParentObject();
        if (ownerEntity instanceof DBVEntity) {
//            if (CommonUtils.getBoolean(((DBVEntity)ownerEntity).getProperty(DBVConstants.PROPERTY_USE_VIRTUAL_KEY_QUIET))) {
//                return true;
//            }
        }

        ValidateUniqueKeyUsageDialog dialog = new ValidateUniqueKeyUsageDialog(viewer);
        int result = dialog.open();
        return result == IDialogConstants.OK_ID;
    }

}
