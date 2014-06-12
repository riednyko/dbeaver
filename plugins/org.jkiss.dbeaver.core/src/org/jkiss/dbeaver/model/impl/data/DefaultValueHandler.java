/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.model.impl.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.dialogs.data.TextViewDialog;

/**
 * Default value handler
 */
public class DefaultValueHandler extends BaseValueHandler {

    public static final DefaultValueHandler INSTANCE = new DefaultValueHandler();

    @Override
    public int getFeatures()
    {
        return FEATURE_EDITOR;
    }

    @Override
    public Class getValueObjectType()
    {
        return Object.class;
    }

    @Override
    public Object fetchValueObject(
        @NotNull DBCSession session,
        @NotNull DBCResultSet resultSet,
        @NotNull DBSTypedObject type,
        int index) throws DBCException
    {
        return resultSet.getColumnValue(index + 1);
    }

    @Override
    public void bindValueObject(
        @NotNull DBCSession session,
        @NotNull DBCStatement statement,
        @NotNull DBSTypedObject type,
        int index,
        Object value) throws DBCException
    {
        
    }

    @Override
    public Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, Object object, boolean copy) throws DBCException
    {
        return object;
    }

    @Override
    public DBDValueEditor createEditor(@NotNull final DBDValueController controller) throws DBException {
        switch (controller.getEditType()) {
            case INLINE:
            case PANEL:
                return new DBDValueEditor() {
                    private Text control;

                    @Override
                    public void createControl() {
                        control = new Text(controller.getEditPlaceholder(), SWT.BORDER);
                    }

                    @Override
                    public Control getControl() {
                        return control;
                    }

                    @Override
                    public Object extractEditorValue() throws DBException {
                        return control.getText();
                    }

                    @Override
                    public void primeEditorValue(@Nullable Object value) throws DBException {
                        control.setText(
                            DBUtils.getDefaultValueDisplayString(value, DBDDisplayFormat.EDIT));
                    }
                };
            case EDITOR:
                return new TextViewDialog(controller);
            default:
                return null;
        }
    }

}
