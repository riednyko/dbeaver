/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.data.managers.stream;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.data.DBDContent;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.impl.StringContentStorage;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.data.IStreamValueEditor;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
* TextPanelEditor
*/
public class TextPanelEditor implements IStreamValueEditor<Text> {

    @Override
    public Text createControl(IValueController valueController)
    {
        Text text = new Text(valueController.getEditPlaceholder(), SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        text.setEditable(!valueController.isReadOnly());
        text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        return text;
    }

    @Override
    public void primeEditorValue(@NotNull DBRProgressMonitor monitor, @NotNull Text control, @NotNull DBDContent value) throws DBException
    {
        monitor.subTask("Read text value");
        DBDContentStorage data = value.getContents(monitor);
        StringWriter buffer = new StringWriter();
        if (data != null) {
            try (Reader contentReader = data.getContentReader()) {
                ContentUtils.copyStreams(contentReader, -1, buffer, monitor);
            } catch (IOException e) {
                throw new DBException("Error reading text from stream", e);
            }
        }
        control.setText(buffer.toString());
    }

    @Override
    public void extractEditorValue(@NotNull DBRProgressMonitor monitor, @NotNull Text control, @NotNull DBDContent value) throws DBException
    {
        monitor.subTask("Read text value");
        value.updateContents(
            monitor,
            new StringContentStorage(control.getText()));
    }

    @Override
    public void contributeActions(@NotNull IContributionManager manager, @NotNull final Text control) throws DBCException {
    }

}
