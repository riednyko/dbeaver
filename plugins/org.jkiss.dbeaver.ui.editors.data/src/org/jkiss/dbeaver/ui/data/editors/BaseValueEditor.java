/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
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

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.themes.ITheme;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetPreferences;
import org.jkiss.dbeaver.ui.controls.resultset.ThemeConstants;
import org.jkiss.dbeaver.ui.data.IMultiController;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.IValueEditor;
import org.jkiss.dbeaver.ui.editors.EditorUtils;
import org.jkiss.dbeaver.ui.editors.TextEditorUtils;

/**
* BaseValueEditor
*/
public abstract class BaseValueEditor<T extends Control> implements IValueEditor {
    private static final String RESULTS_EDIT_CONTEXT_ID = "org.jkiss.dbeaver.ui.context.resultset.edit";

    private static final Log log = Log.getLog(BaseValueEditor.class);

    protected final IValueController valueController;
    protected T control;
    protected boolean dirty;
    protected boolean autoSaveEnabled;

    protected BaseValueEditor(final IValueController valueController)
    {
        this.valueController = valueController;
    }

    public IValueController getValueController() {
        return valueController;
    }

    public void createControl() {
        T control = createControl(valueController.getEditPlaceholder());
        setControl(control);
    }

    @Override
    public Control getControl()
    {
        return control;
    }

    @Override
    public boolean isReadOnly() {
        return valueController.isReadOnly();
    }

    public void setControl(T control) {
        this.control = control;
        if (this.control != null && control != valueController.getEditPlaceholder()) {
            initInlineControl(this.control);
        }
    }

    @Override
    public void contributeActions(@NotNull IContributionManager manager, @NotNull IValueController controller) throws DBCException {

    }

    protected abstract T createControl(Composite editPlaceholder);

    protected void initInlineControl(final Control inlineControl)
    {
        boolean isInline = (valueController.getEditType() == IValueController.EditType.INLINE);
        if (isInline && UIUtils.isInDialog(inlineControl)) {
            //isInline = false;
        }
        TextEditorUtils.enableHostEditorKeyBindingsSupport(valueController.getValueSite(), inlineControl);

//            if (!isInline) {
//                inlineControl.setBackground(valueController.getEditPlaceholder().getBackground());
//            }


        if (isInline) {
            EditorUtils.trackControlContext(valueController.getValueSite(), inlineControl, RESULTS_EDIT_CONTEXT_ID);

            //inlineControl.setFont(valueController.getEditPlaceholder().getFont());
            //inlineControl.setFocus();

            if (valueController instanceof IMultiController) { // In dialog it also should handle all standard stuff because we have params dialog
                 inlineControl.addTraverseListener(e -> {
                     if (e.detail == SWT.TRAVERSE_RETURN) {
                         if (!valueController.isReadOnly()) {
                             saveValue();
                         }
                         ((IMultiController) valueController).closeInlineEditor();
                         e.doit = false;
                         e.detail = SWT.TRAVERSE_NONE;
                     } else if (e.detail == SWT.TRAVERSE_ESCAPE) {
                         ((IMultiController) valueController).closeInlineEditor();
                         e.doit = false;
                         e.detail = SWT.TRAVERSE_NONE;
                     } else if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                         saveValue();
                         ((IMultiController) valueController).nextInlineEditor(e.detail == SWT.TRAVERSE_TAB_NEXT);
                         e.doit = false;
                         e.detail = SWT.TRAVERSE_NONE;
                     }
                   });
                 if (!UIUtils.isInDialog(inlineControl)) {
                     addAutoSaveSupport(inlineControl);
                 } else {
                     ((IMultiController) valueController).closeInlineEditor();
                 }
            }

            if (!UIUtils.isInDialog(inlineControl)) {
                // Set control font (the same as for results viewer)
                ITheme currentTheme = valueController.getValueSite().getWorkbenchWindow().getWorkbench().getThemeManager().getCurrentTheme();
                if (currentTheme != null) {
                    Font rsFont = currentTheme.getFontRegistry().get(ThemeConstants.FONT_SQL_RESULT_SET);
                    if (rsFont != null) {
                        inlineControl.setFont(rsFont);
                    }
                }
            }
        }
        final ControlModifyListener modifyListener = new ControlModifyListener();
        inlineControl.addListener(SWT.Modify, modifyListener);
        inlineControl.addListener(SWT.Selection, modifyListener);
    }

    private void addAutoSaveSupport(final Control inlineControl) {
        BaseValueEditor<?> editor = this;
        // Do not use focus listener in dialogs (because dialog has controls like Ok/Cancel buttons)
        inlineControl.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                // It feels like on Linux editor's control is 'invisible' for GTK and mouse clicks
                // 'go through' the control and reach underlying spreadsheet. Workaround:
                // check that in reality we clicked on editor by checking that cursor is in control's
                // bounds. See [dbeaver#10561].
                Rectangle controlBounds = editor.control.getBounds();
                Point relativeCursorLocation = editor.control.toControl(e.display.getCursorLocation());
                if (controlBounds.contains(relativeCursorLocation)) {
                    return;
                }

                // Check new focus control in async mode
                // (because right now focus is still on edit control)
                if (!valueController.isReadOnly()) {
                    saveValue(false);
                }
                if (valueController instanceof IMultiController) {
                    ((IMultiController) valueController).closeInlineEditor();
                }
            }
        });
    }

    protected void saveValue() {
        saveValue(true);
    }

    protected void saveValue(boolean showError) {
        try {
            Object newValue = extractEditorValue();
            if (dirty || control instanceof Combo || control instanceof CCombo || control instanceof List) {
                // Combos are always dirty (because drop-down menu sets a selection)
                valueController.updateSelectionValue(newValue);
            }
        } catch (DBException e) {
            if (valueController instanceof IMultiController) {
                ((IMultiController) valueController).closeInlineEditor();
            }
            if (showError) {
                DBWorkbench.getPlatformUI().showError("Value save", "Can't save edited value", e);
            } else {
                log.debug("Error saving value: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    private class ControlModifyListener implements Listener {
        @Override
        public void handleEvent(Event event) {
            if (event.type == SWT.Selection) {
                if (!isListControl(event.widget)) {
                    // Just a text selection
                    return;
                }
            }
            setDirty(true);
            if (autoSaveEnabled && DBWorkbench.getPlatform().getPreferenceStore().getBoolean(ResultSetPreferences.RS_EDIT_AUTO_UPDATE_VALUE)) {
                saveValue(false);
            }
        }

        private boolean isListControl(Widget control) {
            return !(control instanceof StyledText || control instanceof Text || control instanceof Table || control instanceof Tree);
        }
    }
}
