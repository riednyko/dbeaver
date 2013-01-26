/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.ui.dialogs.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Collection;

/**
 * Structure object editor
 */
public class ComplexObjectEditor extends TreeViewer {

    static final Log log = LogFactory.getLog(ComplexObjectEditor.class);

    public ComplexObjectEditor(Composite parent, int style)
    {
        super(parent, style | SWT.SINGLE | SWT.FULL_SELECTION);

        final Tree treeControl = super.getTree();
        treeControl.setHeaderVisible(true);
        treeControl.setLinesVisible(true);

        treeControl.addControlListener(new ControlAdapter() {
            private boolean packing = false;

            @Override
            public void controlResized(ControlEvent e)
            {
                if (!packing) {
                    packing = true;
                    UIUtils.packColumns(treeControl, true, new float[]{0.2f, 0.8f});
                    treeControl.removeControlListener(this);
                }
            }
        });

        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);

        TreeViewerColumn column = new TreeViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setMoveable(true);
        column.getColumn().setText(CoreMessages.ui_properties_name);
        column.setLabelProvider(new PropsLabelProvider(true));


        column = new TreeViewerColumn(this, SWT.NONE);
        column.getColumn().setWidth(120);
        column.getColumn().setMoveable(true);
        column.getColumn().setText(CoreMessages.ui_properties_value);
        column.setLabelProvider(new PropsLabelProvider(false));

        super.setContentProvider(new StructContentProvider());
    }

    public void setModel(final DBDComplexType value)
    {
        getTree().setRedraw(false);
        try {
            setInput(value);
            expandAll();
        } finally {
            getTree().setRedraw(true);
        }
    }

    private static class FieldInfo {
        final DBSAttributeBase attribute;
        final Object value;
        DBDValueHandler valueHandler;

        private FieldInfo(DBPDataSource dataSource, DBSAttributeBase attribute, Object value)
        {
            this.attribute = attribute;
            this.value = value;
            this.valueHandler = DBUtils.findValueHandler(dataSource, attribute);
        }
    }

    private static class ArrayItem {
        final DBDArray array;
        final int index;
        final Object value;
        DBDValueHandler valueHandler;

        private ArrayItem(DBDArray array, int index, Object value)
        {
            this.array = array;
            this.index = index;
            this.value = value;
            this.valueHandler = DBUtils.findValueHandler(array.getElementType().getDataSource(), array.getElementType());
        }
    }

    class StructContentProvider implements IStructuredContentProvider, ITreeContentProvider
    {
        public StructContentProvider()
        {
        }

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
        }

        @Override
        public void dispose()
        {
        }

        @Override
        public Object[] getElements(Object parent)
        {
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child)
        {
            return null;
        }

        @Override
        public Object[] getChildren(Object parent)
        {
            if (parent instanceof DBDStructure) {
                DBDStructure structure = (DBDStructure)parent;
                try {
                    Collection<? extends DBSAttributeBase> attributes = structure.getAttributes();
                    Object[] children = new Object[attributes.size()];
                    int index = 0;
                    for (DBSAttributeBase attr : attributes) {
                        Object value = structure.getAttributeValue(attr);
                        children[index++] = new FieldInfo(structure.getStructType().getDataSource(), attr, value);
                    }
                    return children;
                } catch (DBException e) {
                    log.error("Error getting structure meta data", e);
                }
            } else if (parent instanceof DBDArray) {
                DBDArray array = (DBDArray)parent;
                try {
                    Object[] contents = array.getContents();
                    ArrayItem[] items = new ArrayItem[contents.length];
                    for (int i = 0; i < contents.length; i++) {
                        items[i] = new ArrayItem(array, i, contents[i]);
                    }
                    return items;
                } catch (DBCException e) {
                    log.error("Error getting array content", e);
                }
            } else if (parent instanceof FieldInfo) {
                Object value = ((FieldInfo) parent).value;
                if (value instanceof DBDStructure || value instanceof DBDArray) {
                    return getChildren(value);
                }
            }
            return new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent)
        {
            return getChildren(parent).length > 0;
        }
    }

    private class PropsLabelProvider extends CellLabelProvider
    {
        private final boolean isName;
        public PropsLabelProvider(boolean isName)
        {
            this.isName = isName;
        }

        public String getText(Object obj, int columnIndex)
        {
            if (obj instanceof FieldInfo) {
                FieldInfo field = (FieldInfo) obj;
                if (isName) {
                    return field.attribute.getName();
                }
                if (field.value instanceof DBDStructure) {
                    return "";
                }
                return field.valueHandler.getValueDisplayString(field.attribute, field.value, DBDDisplayFormat.UI);
            } else if (obj instanceof ArrayItem) {
                ArrayItem item = (ArrayItem) obj;
                if (isName) {
                    return String.valueOf(item.index);
                }
                return item.valueHandler.getValueDisplayString(item.array.getElementType(), item.value, DBDDisplayFormat.UI);
            }
            return String.valueOf(columnIndex);
        }

        @Override
        public String getToolTipText(Object obj)
        {
            if (obj instanceof FieldInfo) {
                return ((FieldInfo) obj).attribute.getName() + " " + ((FieldInfo) obj).attribute.getTypeName();
            }
            return "";
        }

        @Override
        public void update(ViewerCell cell)
        {
            Object element = cell.getElement();
            cell.setText(getText(element, cell.getColumnIndex()));
        }

    }

}
