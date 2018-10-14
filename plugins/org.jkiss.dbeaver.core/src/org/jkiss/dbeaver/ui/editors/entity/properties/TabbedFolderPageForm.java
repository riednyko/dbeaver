/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2018 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.editors.entity.properties;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.prop.DBECommandProperty;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAdapter;
import org.jkiss.dbeaver.model.meta.IPropertyValueListProvider;
import org.jkiss.dbeaver.model.preferences.DBPPropertyDescriptor;
import org.jkiss.dbeaver.model.preferences.DBPPropertySource;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.registry.editor.EntityEditorsRegistry;
import org.jkiss.dbeaver.runtime.properties.ObjectPropertyDescriptor;
import org.jkiss.dbeaver.ui.*;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.controls.folders.TabbedFolderPage;
import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
import org.jkiss.dbeaver.ui.editors.entity.EntityEditor;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.BeanUtils;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TabbedFolderPageProperties
 */
public class TabbedFolderPageForm extends TabbedFolderPage implements IRefreshablePart, DBPEventListener {

    private static final Log log = Log.getLog(TabbedFolderPageForm.class);

    protected IWorkbenchPart part;
    protected ObjectEditorPageControl ownerControl;
    protected IDatabaseEditorInput input;
    private Font boldFont;
    private Composite propertiesGroup;
    private DBPPropertySource curPropertySource;
    private final Map<DBPPropertyDescriptor, Control> editorMap = new HashMap<>();
    private boolean activated;
    private Button saveButton;
    private Button scriptButton;
    private Button revertButton;

    private transient volatile boolean isLoading;

    public TabbedFolderPageForm(IWorkbenchPart part, ObjectEditorPageControl ownerControl, IDatabaseEditorInput input) {
        this.part = part;
        this.ownerControl = ownerControl;
        this.input = input;
    }

    @Override
    public void createControl(Composite parent)
    {
        this.boldFont = UIUtils.makeBoldFont(parent.getFont());

        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setLayout(new GridLayout(1, false));

        propertiesGroup = new Composite(scrolled, SWT.NONE);
        propertiesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolled.setContent(propertiesGroup);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);

        scrolled.addListener( SWT.Resize, event -> {
            int width = scrolled.getClientArea().width;
            scrolled.setMinSize( propertiesGroup.computeSize( width, SWT.DEFAULT ) );
        } );


        curPropertySource = input.getPropertySource();
        refreshProperties();
        input.getCommandContext().addCommandListener(new DBECommandAdapter() {
            @Override
            public void onCommandChange(DBECommand command) {
                UIUtils.asyncExec(() -> {
                    updateEditButtonsState();
                    if (command instanceof DBECommandProperty) {
                        Object propId = ((DBECommandProperty) command).getHandler().getId();
                        updateOtherPropertyValues(propId);
                    }
                });
            }

            @Override
            public void onSave() {
                UIUtils.asyncExec(() -> updateEditButtonsState());
            }

            @Override
            public void onReset() {
                UIUtils.asyncExec(() -> {
                    refreshProperties();
                    updateEditButtonsState();
                });
            }
        });

        if (input.getDatabaseObject() != null) {
            DBUtils.getObjectRegistry((DBSObject) curPropertySource.getEditableValue()).addDataSourceListener(TabbedFolderPageForm.this);
        }

        propertiesGroup.addDisposeListener(e -> dispose());
	}

    private void updateEditButtonsState() {
        boolean isDirty = input.getCommandContext().isDirty();
        saveButton.setEnabled(isDirty);
        revertButton.setEnabled(isDirty);
        scriptButton.setEnabled(isDirty);
    }

    @Override
    public void setFocus() {
        propertiesGroup.setFocus();
    }

    @Override
    public void dispose() {
        if (curPropertySource != null && curPropertySource.getEditableValue() instanceof DBSObject) {
            DBUtils.getObjectRegistry((DBSObject) curPropertySource.getEditableValue()).removeDataSourceListener(this);
            curPropertySource = null;
        }
        UIUtils.dispose(boldFont);
		super.dispose();
	}

    private void refreshProperties() {
        if (curPropertySource == null) {
            return;
        }
        List<DBPPropertyDescriptor> allProps = filterProperties(curPropertySource.getPropertyDescriptors2());

        boolean firstInit = editorMap.isEmpty();
        if (firstInit) {
            // Prepare property lists
            List<DBPPropertyDescriptor> primaryProps = new ArrayList<>();
            List<DBPPropertyDescriptor> secondaryProps = new ArrayList<>();
            for (DBPPropertyDescriptor prop : allProps) {
                if (prop.getId().equals(DBConstants.PROP_ID_NAME) ||
                    prop.getId().equals(DBConstants.PROP_ID_DESCRIPTION) ||
                    prop.isEditable(curPropertySource.getEditableValue())) {
                    primaryProps.add(prop);
                } else {
                    secondaryProps.add(prop);
                }
            }
            if (primaryProps.isEmpty()) {
                primaryProps.addAll(secondaryProps);
                secondaryProps.clear();
            }

            boolean hasSecondaryProps = !secondaryProps.isEmpty();
            GridLayout layout = new GridLayout(hasSecondaryProps ? 3 : 2, false);
            propertiesGroup.setLayout(layout);

            int groupWidth = UIUtils.getFontHeight(propertiesGroup) * 40;

            Composite primaryGroup = new Composite(propertiesGroup, SWT.BORDER);
            primaryGroup.setLayout(new GridLayout(2, false));
            GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            gd.widthHint = groupWidth;
            primaryGroup.setLayoutData(gd);


            try {
                isLoading = true;

                for (DBPPropertyDescriptor primaryProp : primaryProps) {
                    createPropertyEditor(primaryGroup, primaryProp);
                }
                if (hasSecondaryProps) {
                    Composite secondaryGroup = new Composite(propertiesGroup, SWT.BORDER);
                    secondaryGroup.setLayout(new GridLayout(2, false));
                    gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
                    gd.widthHint = groupWidth;
                    secondaryGroup.setLayoutData(gd);


                    for (DBPPropertyDescriptor secondaryProp : secondaryProps) {
                        createPropertyEditor(secondaryGroup, secondaryProp);
                    }
                }
            } finally {
                isLoading = false;
            }
            if (isEditableObject()) {
                Composite buttonsGroup = new Composite(propertiesGroup, SWT.NONE);
                buttonsGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
                RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
                rowLayout.pack = true;
                rowLayout.fill = true;
                buttonsGroup.setLayout(rowLayout);
                saveButton = UIUtils.createPushButton(buttonsGroup, "Save", DBeaverIcons.getImage(UIIcon.SAVE), new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        ActionUtils.runCommand(IWorkbenchCommandConstants.FILE_SAVE, part.getSite());
                    }
                });
                scriptButton = UIUtils.createPushButton(buttonsGroup, "View script", DBeaverIcons.getImage(UIIcon.SQL_SCRIPT), new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        showAlterScript();
                    }
                });
                revertButton = UIUtils.createPushButton(buttonsGroup, "Revert", DBeaverIcons.getImage(UIIcon.REVERT), new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        ActionUtils.runCommand(IWorkbenchCommandConstants.FILE_REVERT, part.getSite());
                    }
                });
                saveButton.setEnabled(false);
                scriptButton.setEnabled(false);
                revertButton.setEnabled(false);
            }
        }

        refreshPropertyValues(allProps, firstInit);
    }

    private void showAlterScript() {
        IWorkbenchPartSite site = part.getSite();
        if (site instanceof MultiPageEditorSite) {
            MultiPageEditorPart mainEditor = ((MultiPageEditorSite) site).getMultiPageEditor();
            if (mainEditor instanceof EntityEditor) {
                ((EntityEditor) mainEditor).showChanges(false);
            }
        }
    }

    private void refreshPropertyValues(List<DBPPropertyDescriptor> allProps, boolean disableControls) {
        ControlEnableState blockEnableState = disableControls ? ControlEnableState.disable(propertiesGroup) : null;

        ownerControl.runService(
            LoadingJob.createService(
                new DatabaseLoadService<Map<DBPPropertyDescriptor, Object>>("Load main properties", input.getExecutionContext()) {
                    @Override
                    public Map<DBPPropertyDescriptor, Object> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask("Load '" + DBValueFormatting.getDefaultValueDisplayString(curPropertySource.getEditableValue(), DBDDisplayFormat.UI) + "' properties", allProps.size());
                        Map<DBPPropertyDescriptor, Object> propValues = new HashMap<>();
                        for (DBPPropertyDescriptor prop : allProps) {
                            if (monitor.isCanceled()) {
                                break;
                            }
                            Object value = curPropertySource.getPropertyValue(monitor, prop.getId());
                            propValues.put(prop, value);
                            monitor.worked(1);
                        }
                        monitor.done();
                        return propValues;
                    }
                },
                ownerControl.createDefaultLoadVisualizer(editorValues -> {
                    loadEditorValues(editorValues);
                    if (blockEnableState != null) {
                        blockEnableState.restore();
                    }
                })
            )
        );
    }

    private void updateOtherPropertyValues(Object excludePropId) {
        List<DBPPropertyDescriptor> allProps = filterProperties(curPropertySource.getPropertyDescriptors2());

        Map<DBPPropertyDescriptor, Object> propValues = new HashMap<>();
        for (DBPPropertyDescriptor prop : allProps) {
            if (excludePropId != null && excludePropId.equals(prop.getId())) {
                continue;
            }
            Object value = curPropertySource.getPropertyValue(null, prop.getId());
            propValues.put(prop, value);
        }
        loadEditorValues(propValues);
    }

    private boolean isEditableObject() {
        for (DBPPropertyDescriptor prop : curPropertySource.getPropertyDescriptors2()) {
            if (prop.isEditable(curPropertySource.getEditableValue()) ||
                (prop.getId().equals(DBConstants.PROP_ID_NAME) && supportsObjectRename()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean supportsObjectRename() {
        return EntityEditorsRegistry.getInstance().getObjectManager(
            curPropertySource.getEditableValue().getClass(), DBEObjectRenamer.class) != null;
    }

    private void createPropertyEditor(Composite group, DBPPropertyDescriptor prop) {
        if (prop == null) {
            UIUtils.createEmptyLabel(group, 2, 1);
        } else {
            Label label = UIUtils.createControlLabel(group, prop.getDisplayName());
            label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            boolean editable = prop.isEditable(curPropertySource.getEditableValue()) ||
                (prop.getId().equals(DBConstants.PROP_ID_NAME) && supportsObjectRename());
            Class<?> propType = prop.getDataType();
            Object propertyValue = curPropertySource.getPropertyValue(null, prop.getId());
            Control editControl = null;
            if (editable || isTextPropertyType(propType) || BeanUtils.isBooleanType(propType)) {
                editControl = createEditorControl(
                    group,
                    curPropertySource.getEditableValue(),
                    prop,
                    propertyValue,
                    !editable);
            }
            if (editControl == null) {
                editControl = new Text(group, SWT.BORDER | SWT.READ_ONLY);
                ((Text)editControl).setText(objectValueToString(propertyValue));
            }
            boolean plainText = (CharSequence.class.isAssignableFrom(propType));
            GridData gd = (GridData) editControl.getLayoutData();
            if (gd == null ) {
                gd = new GridData(
                    (plainText ? GridData.FILL_HORIZONTAL : GridData.HORIZONTAL_ALIGN_BEGINNING) | GridData.VERTICAL_ALIGN_BEGINNING);
                editControl.setLayoutData(gd);
            }
            if (editControl instanceof Text || editControl instanceof Combo) {
                gd.widthHint = Math.max(
                    UIUtils.getFontHeight(group) * 10,
                    editControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
            }

            editorMap.put(prop, editControl);

            Control finalEditControl = editControl;

            if (finalEditControl instanceof Combo) {
                ((Combo) finalEditControl).addModifyListener(e -> updatePropertyValue(prop, ((Combo) finalEditControl).getText()));
            } else if (finalEditControl instanceof Text) {
                ((Text) finalEditControl).addModifyListener(e -> updatePropertyValue(prop, ((Text) finalEditControl).getText()));
            } else if (finalEditControl instanceof Button) {
                ((Button) finalEditControl).addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        updatePropertyValue(prop, ((Button) finalEditControl).getSelection());
                    }
                });
            }
        }
    }

    private void updatePropertyValue(DBPPropertyDescriptor prop, Object value) {
        if (!isLoading) {
            if (prop.getId().equals(DBConstants.PROP_ID_NAME)) {
                DBEObjectRenamer renamer = EntityEditorsRegistry.getInstance().getObjectManager(curPropertySource.getEditableValue().getClass(), DBEObjectRenamer.class);
                if (renamer != null) {
                    try {
                        renamer.renameObject(input.getCommandContext(), input.getDatabaseObject(), CommonUtils.toString(value));
                    } catch (Throwable e) {
                        log.error("Error renaming object", e);
                    }
                }
            } else {
                Class<?> dataType = prop.getDataType();
                if (value instanceof String) {
                    value = GeneralUtils.convertString((String) value, dataType);
                }
                curPropertySource.setPropertyValue(null, prop.getId(), value);
            }
        }
    }

    /**
     * Supported editors:
     * Combo (lists)
     * Text (strings, numbers, dates)
     * Button (booleans)
     */
    public Control createEditorControl(Composite parent, Object object, DBPPropertyDescriptor property, Object value, boolean readOnly)
    {
        // List
        if (property instanceof IPropertyValueListProvider) {
            final IPropertyValueListProvider listProvider = (IPropertyValueListProvider) property;
            Object[] items = listProvider.getPossibleValues(object);
            if (items == null && property instanceof ObjectPropertyDescriptor && ((ObjectPropertyDescriptor) property).hasListValueProvider()) {
                // It is a list provider but it seems to be lazy and not yet initialized
                items = new Object[0];
            }
            if (items != null) {
                final String[] strings = new String[items.length];
                for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
                    strings[i] = objectValueToString(items[i]);
                }
                Combo combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | (listProvider.allowCustomValue() ? SWT.NONE : SWT.READ_ONLY) | (readOnly ? SWT.READ_ONLY : SWT.NONE));
                combo.setItems(strings);
                combo.setText(objectValueToString(value));
                return combo;
            }
        }
        Class<?> propertyType = property.getDataType();
        if (isTextPropertyType(propertyType)) {
            if (property instanceof ObjectPropertyDescriptor && ((ObjectPropertyDescriptor) property).isMultiLine()) {
                Text editor = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | (readOnly ? SWT.READ_ONLY : SWT.NONE));
                editor.setText(objectValueToString(value));
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.heightHint = UIUtils.getFontHeight(editor) * 4;
                editor.setLayoutData(gd);
                return editor;
            } else {
                Text editor = new Text(parent, SWT.BORDER | (readOnly ? SWT.READ_ONLY : SWT.NONE));
                editor.setText(objectValueToString(value));
                return editor;
            }
        } else if (BeanUtils.isBooleanType(propertyType)) {
            Button editor = new Button(parent, SWT.CHECK);
            if (readOnly) {
                editor.setEnabled(false);
            }
            editor.setSelection(CommonUtils.toBoolean(value));
            return editor;
        } else if (propertyType.isEnum()) {
            final Object[] enumConstants = propertyType.getEnumConstants();
            final String[] strings = new String[enumConstants.length];
            for (int i = 0, itemsLength = enumConstants.length; i < itemsLength; i++) {
                strings[i] = ((Enum)enumConstants[i]).name();
            }
            Combo combo = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | (readOnly ? SWT.READ_ONLY : SWT.NONE));
            combo.setItems(strings);
            combo.setText(objectValueToString(value));
            return combo;
        } else if (DBSObject.class.isAssignableFrom(propertyType)) {
            if (value == null) {
                if (readOnly) {
                    return UIUtils.createLabel(parent, "<none>");
                } else {
                    Text editor = new Text(parent, SWT.BORDER);
                    editor.setText("");
                    return editor;
                }
            } else {
                Link link = new Link(parent, SWT.NONE);
                link.setText("<a>" + objectValueToString(value) + "</a>");
                return link;
            }
        } else {
            log.warn("Unsupported property type: " + propertyType.getName());
            return null;
        }
    }

    private void loadEditorValues(Map<DBPPropertyDescriptor, Object> editorValues) {
        try {
            isLoading = true;

            Object object = curPropertySource.getEditableValue();
            for (Map.Entry<DBPPropertyDescriptor, Object> prop : editorValues.entrySet()) {
                setEditorValue(object, prop.getKey(), prop.getValue());
            }

        } finally {
            isLoading = false;
        }
    }

    public void setEditorValue(Object object, DBPPropertyDescriptor property, Object value)
    {
        Control editorControl = editorMap.get(property);
        Class<?> propertyType = property.getDataType();
        // List
        if (editorControl instanceof Combo) {
            Combo combo = (Combo) editorControl;
            if (property instanceof IPropertyValueListProvider) {
                final IPropertyValueListProvider listProvider = (IPropertyValueListProvider) property;
                final Object[] items = listProvider.getPossibleValues(object);
                if (items != null) {
                    final String[] strings = new String[items.length];
                    for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
                        strings[i] = items[i] instanceof DBPNamedObject ? ((DBPNamedObject) items[i]).getName() : CommonUtils.toString(items[i]);
                    }
                    combo.setItems(strings);
                    combo.setText(objectValueToString(value));
                }
            } else if (propertyType.isEnum()) {
                final Object[] enumConstants = propertyType.getEnumConstants();
                final String[] strings = new String[enumConstants.length];
                for (int i = 0, itemsLength = enumConstants.length; i < itemsLength; i++) {
                    strings[i] = ((Enum) enumConstants[i]).name();
                }
                combo.setItems(strings);
                combo.setText(objectValueToString(value));
            }
        } else {
            if (editorControl instanceof Text) {
                ((Text) editorControl).setText(objectValueToString(value));
            } else if (editorControl instanceof Button) {
                ((Button) editorControl).setSelection(CommonUtils.toBoolean(value));
            } else if (editorControl instanceof Link) {
                Link link = (Link)editorControl;
                link.setText("<a>" + objectValueToString(value) + "</a>");
            }
        }
    }

    private static String objectValueToString(Object value) {
        if (value instanceof DBPNamedObject) {
            return ((DBPNamedObject) value).getName();
        } else if (value instanceof Enum) {
            return ((Enum) value).name();
        } else {
            return DBValueFormatting.getDefaultValueDisplayString(value, DBDDisplayFormat.EDIT);
        }
    }

    private static boolean isTextPropertyType(Class<?> propertyType) {
        return propertyType == null || CharSequence.class.isAssignableFrom(propertyType) || BeanUtils.isNumericType(propertyType);
    }

    private List<DBPPropertyDescriptor> filterProperties(DBPPropertyDescriptor[] props) {
        List<DBPPropertyDescriptor> result = new ArrayList<>();
        for (DBPPropertyDescriptor prop : props) {
            String category = prop.getCategory();
            if (!CommonUtils.isEmpty(category)) {
                // Keep only basic properties
                continue;
            }
            result.add(prop);
        }
        return result;
    }

    @Override
    public void handleDataSourceEvent(DBPEvent event)
    {
        if (input.getDatabaseObject() == event.getObject() && !Boolean.FALSE.equals(event.getEnabled()) && !propertiesGroup.isDisposed()) {
            //UIUtils.asyncExec(this::refreshProperties);
        }
    }

    @Override
    public void refreshPart(Object source, boolean force) {
        // Do not do any refresh. Everything is done in context listener
/*
        if (force) {
            curPropertySource = input.getPropertySource();
            if (propertiesGroup != null) {
                refreshProperties();
            }
        }
*/
    }

    @Override
    public void aboutToBeShown() {
        if (!activated) {
            activated = true;
        }
    }

}