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
package org.jkiss.dbeaver.ui.data.editors;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.AbstractLoadService;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.navigator.actions.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.controls.ProgressLoaderVisualizer;
import org.jkiss.dbeaver.ui.data.IAttributeController;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.IValueEditor;
import org.jkiss.dbeaver.ui.editors.data.DatabaseDataEditor;
import org.jkiss.dbeaver.ui.editors.object.struct.EditDictionaryPage;
import org.jkiss.dbeaver.ui.navigator.NavigatorUtils;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ReferenceValueEditor
 *
 * @author Serge Rider
 */
public class ReferenceValueEditor {

    private static final Log log = Log.getLog(ReferenceValueEditor.class);

    private IValueController valueController;
    private IValueEditor valueEditor;
    private DBSEntityReferrer refConstraint;
    private Table editorSelector;
    private volatile boolean sortByValue = true;
    private volatile boolean sortAsc = true;
    private volatile boolean dictLoaded = false;
    private Object lastPattern;

    public ReferenceValueEditor(IValueController valueController, IValueEditor valueEditor) {
        this.valueController = valueController;
        this.valueEditor = valueEditor;
    }

    public boolean isReferenceValue()
    {
        return getEnumerableConstraint() != null;
    }

    @Nullable
    private DBSEntityReferrer getEnumerableConstraint()
    {
        if (valueController instanceof IAttributeController) {
            return getEnumerableConstraint(((IAttributeController) valueController).getBinding());
        }
        return null;
    }

    public static DBSEntityReferrer getEnumerableConstraint(DBDAttributeBinding binding) {
        try {
            DBSEntityAttribute entityAttribute = binding.getEntityAttribute();
            if (entityAttribute != null) {
                List<DBSEntityReferrer> refs = DBUtils.getAttributeReferrers(new VoidProgressMonitor(), entityAttribute);
                DBSEntityReferrer constraint = refs.isEmpty() ? null : refs.get(0);
                if (constraint instanceof DBSEntityAssociation &&
                    ((DBSEntityAssociation)constraint).getReferencedConstraint() instanceof DBSConstraintEnumerable)
                {
                    final DBSConstraintEnumerable refConstraint = (DBSConstraintEnumerable) ((DBSEntityAssociation) constraint).getReferencedConstraint();
                    if (refConstraint != null && refConstraint.supportsEnumeration()) {
                        return constraint;
                    }
                }
            }
        } catch (DBException e) {
            log.error(e);
        }
        return null;
    }

    public boolean createEditorSelector(final Composite parent)
    {
        if (!(valueController instanceof IAttributeController) || valueController.isReadOnly()) {
            return false;
        }
        refConstraint = getEnumerableConstraint();
        if (refConstraint == null) {
            return false;
        }

        if (refConstraint instanceof DBSEntityAssociation) {
            final DBSEntityAssociation association = (DBSEntityAssociation)refConstraint;
            if (association.getReferencedConstraint() != null) {
                final DBSEntity refTable = association.getReferencedConstraint().getParentObject();
                Composite labelGroup = UIUtils.createPlaceholder(parent, 2);
                labelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                Link dictLabel = UIUtils.createLink(
                    labelGroup,
                    NLS.bind(CoreMessages.dialog_value_view_label_dictionary, refTable.getName()), new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            // Open
                            final IWorkbenchWindow window = valueController.getValueSite().getWorkbenchWindow();
                            UIUtils.runInUI(window, monitor -> {
                                DBNDatabaseNode tableNode = NavigatorUtils.getNodeByObject(
                                    monitor,
                                    refTable,
                                    true
                                );
                                if (tableNode != null) {
                                    NavigatorHandlerObjectOpen.openEntityEditor(tableNode, DatabaseDataEditor.class.getName(), window);
                                }
                            });
                        }
                    });
                dictLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

                Link hintLabel = UIUtils.createLink(labelGroup, "(<a>Define Description</a>)", new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        EditDictionaryPage editDictionaryPage = new EditDictionaryPage(refTable);
                        if (editDictionaryPage.edit(parent.getShell())) {
                            reloadSelectorValues(null);
                        }
                    }
                });
                hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            }
        }

        editorSelector = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        editorSelector.setLinesVisible(true);
        editorSelector.setHeaderVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        //gd.widthHint = 300;
        //gd.grabExcessVerticalSpace = true;
        //gd.grabExcessHorizontalSpace = true;
        editorSelector.setLayoutData(gd);

        TableColumn valueColumn = UIUtils.createTableColumn(editorSelector, SWT.LEFT, CoreMessages.dialog_value_view_column_value);
        valueColumn.setData(Boolean.TRUE);
        TableColumn descColumn = UIUtils.createTableColumn(editorSelector, SWT.LEFT, CoreMessages.dialog_value_view_column_description);
        descColumn.setData(Boolean.FALSE);

        SortListener sortListener = new SortListener();
        valueColumn.addListener(SWT.Selection, sortListener);
        descColumn.addListener(SWT.Selection, sortListener);

        editorSelector.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                TableItem[] selection = editorSelector.getSelection();
                if (selection != null && selection.length > 0) {
                    Object value = selection[0].getData();
                    //editorControl.setText(selection[0].getText());
                    try {
                        valueEditor.primeEditorValue(value);
                    } catch (DBException e1) {
                        log.error(e1);
                    }
                }
            }
        });

        Control control = valueEditor.getControl();
        ModifyListener modifyListener = e -> {
            Object curEditorValue;
            try {
                curEditorValue = valueEditor.extractEditorValue();
            } catch (DBException e1) {
                log.error(e1);
                return;
            }
            // Try to select current value in the table
            final String curTextValue = valueController.getValueHandler().getValueDisplayString(
                ((IAttributeController) valueController).getBinding(),
                curEditorValue,
                DBDDisplayFormat.UI);
            boolean valueFound = false;
            if (curTextValue != null) {
                for (TableItem item : editorSelector.getItems()) {
                    if (curTextValue.equalsIgnoreCase(item.getText(0)) || curTextValue.equalsIgnoreCase(item.getText(1))) {
                        editorSelector.select(editorSelector.indexOf(item));
                        editorSelector.showItem(item);
                        valueFound = true;
                        break;
                    }
                }
            }

            if (!valueFound) {
                // Read dictionary
                reloadSelectorValues(curEditorValue);
            }
        };
        if (control instanceof Text) {
            ((Text)control).addModifyListener(modifyListener);
        } else if (control instanceof StyledText) {
            ((StyledText)control).addModifyListener(modifyListener);
        }

        if (refConstraint instanceof DBSEntityAssociation) {
            final Text valueFilterText = new Text(parent, SWT.BORDER);
            valueFilterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            valueFilterText.addModifyListener(e -> {
                String filterPattern = valueFilterText.getText();
                reloadSelectorValues(filterPattern);
            });
            valueFilterText.addPaintListener(e -> {
                if (valueFilterText.isEnabled() && valueFilterText.getCharCount() == 0) {
                    e.gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    e.gc.drawText("Type part of dictionary value to search",
                        2, 0, true);
                    e.gc.setFont(null);
                }
            });
        }
        UIUtils.asyncExec(() -> {
            UIUtils.packColumns(editorSelector, true);
        });
        final Object curValue = valueController.getValue();

        reloadSelectorValues(curValue instanceof Number ? curValue : null);

        return true;
    }

    private void reloadSelectorValues(Object pattern) {
        if (dictLoaded && CommonUtils.equalObjects(String.valueOf(lastPattern), String.valueOf(pattern))) {
            selectCurrentValue();
            return;
        }
        lastPattern = pattern;
        dictLoaded = true;
        SelectorLoaderService loadingService = new SelectorLoaderService();
        if (pattern != null) {
            loadingService.setPattern(pattern);
        }
        LoadingJob.createService(
            loadingService,
            new SelectorLoaderVisualizer(loadingService))
            .schedule();
    }

    private void updateDictionarySelector(EnumValuesData valuesData) {
        if (editorSelector == null || editorSelector.isDisposed()) {
            return;
        }
        editorSelector.setRedraw(false);
        try {
            editorSelector.removeAll();
            for (DBDLabelValuePair entry : valuesData.keyValues) {
                TableItem discItem = new TableItem(editorSelector, SWT.NONE);
                discItem.setText(0,
                    valuesData.keyHandler.getValueDisplayString(
                        valuesData.keyColumn.getAttribute(),
                        entry.getValue(),
                        DBDDisplayFormat.UI));
                discItem.setText(1, entry.getLabel());
                discItem.setData(entry.getValue());
            }

            selectCurrentValue();

            UIUtils.maxTableColumnsWidth(editorSelector);
        } finally {
            editorSelector.setRedraw(true);
        }
    }

    private void selectCurrentValue() {
        Control editorControl = valueEditor.getControl();
        if (editorControl != null && !editorControl.isDisposed()) {
            try {
                Object curValue = valueEditor.extractEditorValue();
                final String curTextValue = valueController.getValueHandler().getValueDisplayString(
                        ((IAttributeController) valueController).getBinding(),
                        curValue,
                        DBDDisplayFormat.UI);

                TableItem curItem = null;
                for (TableItem item : editorSelector.getItems()) {
                    if (item.getText(0).equals(curTextValue)) {
                        curItem = item;
                        break;
                    }
                }
                if (curItem != null) {
                    editorSelector.setSelection(curItem);
                    editorSelector.showItem(curItem);
                } else {
                    editorSelector.deselectAll();
                }
            } catch (DBException e) {
                log.error(e);
            }
        }
    }

    private class SortListener implements Listener {
        private TableColumn prevColumn = null;
        private int sortDirection = SWT.DOWN;

        public SortListener() {
        }

        @Override
        public void handleEvent(Event event) {
            TableColumn column = (TableColumn) event.widget;
            if (prevColumn == column) {
                // Set reverse order
                sortDirection = (sortDirection == SWT.UP ? SWT.DOWN : SWT.UP);
            }
            prevColumn = column;
            sortByValue = (Boolean)column.getData();
            sortAsc = sortDirection == SWT.DOWN;
            editorSelector.setSortColumn(column);
            editorSelector.setSortDirection(sortDirection);
            reloadSelectorValues(lastPattern);
        }
    }

    private static class EnumValuesData {
        List<DBDLabelValuePair> keyValues;
        DBSEntityAttributeRef keyColumn;
        DBDValueHandler keyHandler;

        public EnumValuesData(Collection<DBDLabelValuePair> keyValues, DBSEntityAttributeRef keyColumn, DBDValueHandler keyHandler) {
            this.keyValues = new ArrayList<>(keyValues);
            this.keyColumn = keyColumn;
            this.keyHandler = keyHandler;
        }
    }

    class SelectorLoaderService extends AbstractLoadService<EnumValuesData> {

        private Object pattern;

        private SelectorLoaderService() {
            super(CoreMessages.dialog_value_view_job_selector_name + valueController.getValueName() + " possible values");
        }

        void setPattern(@Nullable Object pattern)
        {
            this.pattern = pattern;
        }

        @Override
        public EnumValuesData evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            if (editorSelector.isDisposed()) {
                return null;
            }
/*
            final Map<Object, String> keyValues = new TreeMap<>((o1, o2) -> {
                if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                if (o1 == o2) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    return o1.toString().compareTo(o2.toString());
                }
            });
*/
            try {
                IAttributeController attributeController = (IAttributeController)valueController;
                final DBSEntityAttribute tableColumn = attributeController.getBinding().getEntityAttribute();
                if (tableColumn == null) {
                    return null;
                }
                final DBSEntityAttributeRef fkColumn = DBUtils.getConstraintAttribute(monitor, refConstraint, tableColumn);
                if (fkColumn == null) {
                    return null;
                }
                DBSEntityAssociation association;
                if (refConstraint instanceof DBSEntityAssociation) {
                    association = (DBSEntityAssociation)refConstraint;
                } else {
                    return null;
                }
                final DBSEntityAttribute refColumn = DBUtils.getReferenceAttribute(monitor, association, tableColumn, false);
                if (refColumn == null) {
                    return null;
                }
                List<DBDAttributeValue> precedingKeys = null;
                List<? extends DBSEntityAttributeRef> allColumns = CommonUtils.safeList(refConstraint.getAttributeReferences(monitor));
                if (allColumns.size() > 1 && allColumns.get(0) != fkColumn) {
                    // Our column is not a first on in foreign key.
                    // So, fill uo preceeding keys
                    List<DBDAttributeBinding> rowAttributes = attributeController.getRowController().getRowAttributes();
                    precedingKeys = new ArrayList<>();
                    for (DBSEntityAttributeRef precColumn : allColumns) {
                        if (precColumn == fkColumn) {
                            // Enough
                            break;
                        }
                        DBSEntityAttribute precAttribute = precColumn.getAttribute();
                        if (precAttribute != null) {
                            DBDAttributeBinding rowAttr = DBUtils.findBinding(rowAttributes, precAttribute);
                            if (rowAttr != null) {
                                Object precValue = attributeController.getRowController().getAttributeValue(rowAttr);
                                precedingKeys.add(new DBDAttributeValue(precAttribute, precValue));
                            }
                        }
                    }
                }
                final DBSEntityAttribute fkAttribute = fkColumn.getAttribute();
                final DBSEntityConstraint refConstraint = association.getReferencedConstraint();
                final DBSConstraintEnumerable enumConstraint = (DBSConstraintEnumerable) refConstraint;
                if (fkAttribute != null && enumConstraint != null) {
                    try (DBCSession session = valueController.getExecutionContext().openSession(
                        monitor,
                        DBCExecutionPurpose.UTIL,
                        NLS.bind(CoreMessages.dialog_value_view_context_name, fkAttribute.getName()))) {
                        Collection<DBDLabelValuePair> enumValues = enumConstraint.getKeyEnumeration(
                            session,
                            refColumn,
                            pattern,
                            precedingKeys,
                            sortByValue,
                            sortAsc,
                            200);
//                        for (DBDLabelValuePair pair : enumValues) {
//                            keyValues.put(pair.getValue(), pair.getLabel());
//                        }
                        if (monitor.isCanceled()) {
                            return null;
                        }
                        final DBDValueHandler colHandler = DBUtils.findValueHandler(session, fkAttribute);
                        return new EnumValuesData(enumValues, fkColumn, colHandler);
                    }
                }

            } catch (DBException e) {
                // error
                // just ignore
                log.warn(e);
            }
            return null;
        }

        @Override
        public Object getFamily() {
            return valueController.getExecutionContext();
        }

    }

    private class SelectorLoaderVisualizer extends ProgressLoaderVisualizer<EnumValuesData> {
        public SelectorLoaderVisualizer(SelectorLoaderService loadingService) {
            super(loadingService, editorSelector);
        }

        @Override
        public void visualizeLoading() {
            super.visualizeLoading();
        }

        @Override
        public void completeLoading(EnumValuesData result) {
            super.completeLoading(result);
            super.visualizeLoading();
            if (result != null) {
                updateDictionarySelector(result);
            }
        }
    }

}
