/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
package org.jkiss.dbeaver.ui.controls.resultset.valuefilter;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.data.DBDLabelValuePair;
import org.jkiss.dbeaver.model.data.DBDLabelValuePairExt;
import org.jkiss.dbeaver.model.exec.DBCLogicalOperator;
import org.jkiss.dbeaver.model.struct.DBSEntityAssociation;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ViewerColumnController;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetRow;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetUtils;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetViewer;
import org.jkiss.dbeaver.ui.controls.resultset.internal.ResultSetMessages;
import org.jkiss.dbeaver.ui.dialogs.AbstractPopupPanel;
import org.jkiss.dbeaver.ui.editors.object.struct.EditDictionaryPage;

public class FilterValueEditPopup extends AbstractPopupPanel {

    private static final String DIALOG_ID = "DBeaver.FilterValueEditMenu";//$NON-NLS-1$

    private Object value;
    private GenericFilterValueEdit filter;
    private Point location;

    public FilterValueEditPopup(Shell parentShell, @NotNull ResultSetViewer viewer, @NotNull DBDAttributeBinding attr, @NotNull ResultSetRow[] rows) {
        super(parentShell, NLS.bind(ResultSetMessages.dialog_filter_value_edit_title, attr.getFullyQualifiedName(DBPEvaluationContext.UI)));
        setShellStyle(SWT.SHELL_TRIM);
        filter = new GenericFilterValueEdit(viewer, attr, rows, DBCLogicalOperator.IN);
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings()
    {
        return UIUtils.getDialogSettings(DIALOG_ID);
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        if (location != null) {
            return location;
        }
        return super.getInitialLocation(initialSize);
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        DBSEntityReferrer descReferrer = ResultSetUtils.getEnumerableConstraint(filter.getAttribute());

        Composite group = (Composite) super.createDialogArea(parent);
        {
            Composite labelComposite = UIUtils.createComposite(group, 2);
            labelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Label controlLabel = UIUtils.createControlLabel(labelComposite, ResultSetMessages.dialog_filter_value_edit_label_choose_values);
            controlLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (descReferrer instanceof DBSEntityAssociation) {
                Link hintLabel = UIUtils.createLink(labelComposite, ResultSetMessages.dialog_filter_value_edit_label_define_description, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        EditDictionaryPage editDictionaryPage = new EditDictionaryPage(((DBSEntityAssociation) descReferrer).getAssociatedEntity());
                        if (editDictionaryPage.edit(parent.getShell())) {
                            filter.loadValues(null);
                        }
                    }
                });
                hintLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            } else {
                UIUtils.createEmptyLabel(labelComposite, 1, 1);
            }
        }

        Text filterTextbox = filter.addFilterTextbox(group);
        filterTextbox.setFocus();
        filterTextbox.addTraverseListener(e -> {
            Table table = filter.getTableViewer().getTable();
            if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS || e.detail == SWT.TRAVERSE_ARROW_NEXT) {
                if (table.getSelectionIndex() < 0 && table.getItemCount() > 0) {
                    table.setSelection(0);
                }
                table.setFocus();
            } else if (e.detail == SWT.TRAVERSE_RETURN) {
                applyFilterValue();
            }
        });
        UIUtils.addEmptyTextHint(filterTextbox, text -> ResultSetMessages.dialog_filter_value_edit_text_hint);

        Composite tableComposite = UIUtils.createComposite(group, 1);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        gd.heightHint = 300;
        tableComposite.setLayoutData(gd);

        filter.setupTable(
            tableComposite,
            SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL |
                (filter.getOperator() == DBCLogicalOperator.IN ? SWT.CHECK : SWT.NONE),
            true,
            true,
            new GridData(GridData.FILL_BOTH));

        Table table = filter.getTableViewer().getTable();

        ViewerColumnController<?, ?> columnController = new ViewerColumnController<>("sqlFilterValueEditPopup", filter.getTableViewer());
        columnController.addColumn(ResultSetMessages.dialog_filter_value_edit_table_value_label, ResultSetMessages.dialog_filter_value_edit_table_value_description, SWT.LEFT, true, true, new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return filter.getAttribute().getValueHandler().getValueDisplayString(filter.getAttribute(), ((DBDLabelValuePair) element).getValue(), DBDDisplayFormat.UI);
            }
        });
        if (descReferrer != null) {
            columnController.addColumn(ResultSetMessages.dialog_filter_value_edit_table_description_label, ResultSetMessages.dialog_filter_value_edit_table_description_description, SWT.LEFT, true, true, new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((DBDLabelValuePair) element).getLabel();
                }
            });
        }
        if (descReferrer == null) {
            columnController.addColumn(ResultSetMessages.dialog_filter_value_edit_table_count_label, ResultSetMessages.dialog_filter_value_edit_table_count_description, SWT.LEFT, true, true, new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof DBDLabelValuePairExt) {
                        return String.valueOf(((DBDLabelValuePairExt) element).getCount());
                    }
                    return "";
                }
            });
        }
        columnController.createColumns(true);

        filter.getTableViewer().addSelectionChangedListener(event -> {
            value = filter.getFilterValue();
            //okPressed();
        });
        filter.getTableViewer().addDoubleClickListener(event -> applyFilterValue());

        filter.setFilterPattern(null);
        filter.loadValues(() ->
            UIUtils.asyncExec(() -> UIUtils.packColumns(table, false)));

        filter.createFilterButton(ResultSetMessages.sql_editor_resultset_filter_panel_btn_apply, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                applyFilterValue();
            }
        });

        closeOnFocusLost(filterTextbox, table);

        return tableComposite;
    }

    private void applyFilterValue() {
        value = filter.getFilterValue();
        okPressed();
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return UIUtils.createPlaceholder(parent, 1);
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Object getValue() {
        return value;
    }
}
