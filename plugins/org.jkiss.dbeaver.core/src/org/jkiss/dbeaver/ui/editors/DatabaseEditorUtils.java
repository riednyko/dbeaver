/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.editors;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.jkiss.dbeaver.model.IDataSourceProvider;
import org.jkiss.dbeaver.model.DBPDataSource;

/**
 * DB editor utils
 */
public class DatabaseEditorUtils {

    public static void setPartBackground(IEditorPart editor, Composite composite)
    {
        Composite rootComposite = null;
        for (Composite c = composite; c != null; c = c.getParent()) {
            if (c.getParent() instanceof CTabFolder) {
                ((CTabFolder) c.getParent()).setBorderVisible(false);
                rootComposite = c;
                break;
            }
        }
        if (rootComposite == null) {
            return;
        }

        DBPDataSource dataSource = null;
        if (editor instanceof IDataSourceProvider) {
            dataSource = ((IDataSourceProvider) editor).getDataSource();
        }
        if (dataSource == null) {
            rootComposite.setBackground(null);
            return;
        }
        rootComposite.setBackground(
            dataSource.getContainer().getConnectionInfo().getColor());
    }

}
