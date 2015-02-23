/*
 * Copyright (C) 2010-2015 Serge Rieder
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
package org.jkiss.dbeaver.tools.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;


class ProjectImportWizardPageFile extends WizardPage {

    private ProjectImportData importData;
    private String curFolder;
    private Table projectsTable;

    protected ProjectImportWizardPageFile(ProjectImportData importData)
    {
        super(CoreMessages.dialog_project_import_wizard_file_name);
        this.importData = importData;

        setTitle(CoreMessages.dialog_project_import_wizard_file_title);
        setDescription(CoreMessages.dialog_project_import_wizard_file_description);
    }

    @Override
    public boolean isPageComplete()
    {
        return importData.isProjectsSelected(this);
    }

    @Override
    public void createControl(Composite parent)
    {
        Composite placeholder = UIUtils.createPlaceholder(parent, 1);
        Composite configGroup = UIUtils.createControlGroup(placeholder, CoreMessages.dialog_project_import_wizard_file_group_input, 3, GridData.FILL_HORIZONTAL, 0);

        final Text fileNameText = UIUtils.createLabelText(configGroup, CoreMessages.dialog_project_import_wizard_file_label_file, null); //$NON-NLS-2$
        fileNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                String fileName = fileNameText.getText();
                if (CommonUtils.isEmpty(fileName)) {
                    importData.setImportFile(null);
                    clearArchive();
                } else {
                    importData.setImportFile(new File(fileName));
                    loadArchive();
                }
                updateState();
            }
        });
        Button openFolder = new Button(configGroup, SWT.PUSH);
        openFolder.setImage(DBIcon.TREE_FOLDER.getImage());
        openFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
                fd.setText(CoreMessages.dialog_project_import_wizard_file_dialog_export_archive_text);
                fd.setFilterPath(curFolder);
                String[] filterExt = {"*.dbp", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String selected = fd.open();
                if (selected != null) {
                    curFolder = fd.getFilterPath();
                    fileNameText.setText(selected);
                }
            }
        });
        final Button importDriverCheck = UIUtils.createCheckbox(configGroup, CoreMessages.dialog_project_import_wizard_file_checkbox_import_libraries, true);
        importDriverCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                importData.setImportDriverLibraries(importDriverCheck.getSelection());
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 3;
        importDriverCheck.setLayoutData(gd);

        Group projectsGroup = UIUtils.createControlGroup(placeholder, CoreMessages.dialog_project_import_wizard_file_group_projects, 1, GridData.FILL_BOTH, 0);

        // Project list
        projectsTable = new Table(projectsGroup, SWT.MULTI | SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        projectsTable.setHeaderVisible(true);
        projectsTable.setLinesVisible(true);
        gd = new GridData(GridData.FILL_BOTH);
        projectsTable.setLayoutData(gd);
        projectsTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                updateProjectsSelection();
                updateState();
            }
        });
        UIUtils.createTableColumn(projectsTable, SWT.LEFT, CoreMessages.dialog_project_import_wizard_file_column_source_name);
        UIUtils.createTableColumn(projectsTable, SWT.LEFT, CoreMessages.dialog_project_import_wizard_file_column_target_name);

        final TableEditor tableEditor = new TableEditor(projectsTable);
        tableEditor.horizontalAlignment = SWT.LEFT;
        tableEditor.verticalAlignment = SWT.TOP;
        tableEditor.grabHorizontal = true;

        projectsTable.addMouseListener(new MouseAdapter() {
            private void disposeOldEditor()
            {
                Control oldEditor = tableEditor.getEditor();
                if (oldEditor != null) oldEditor.dispose();
            }
            @Override
            public void mouseUp(MouseEvent e)
            {
                // Clean up any previous editor control
                disposeOldEditor();
                TableItem item = projectsTable.getItem(new Point(e.x, e.y));
                if (item == null) {
                    return;
                }
                showEditor(item);
            }
            private void showEditor(final TableItem item) {
                // Identify the selected row
                Text text = new Text(projectsTable, SWT.BORDER);
                text.setText(item.getText(1));
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        Text text = (Text) tableEditor.getEditor();
                        item.setText(1, text.getText());
                        updateProjectsSelection();
                        updateState();
                    }
                });
                text.selectAll();
                text.setFocus();
                tableEditor.setEditor(text, item, 1);
            }
        });

        UIUtils.packColumns(projectsTable);

        setControl(placeholder);
    }

    private boolean updateProjectsSelection()
    {
        importData.clearProjectNameMap();
        boolean failed = false;
        for (TableItem item : projectsTable.getItems()) {
            boolean validItem = checkProjectItem(item);
            if (!validItem && item.getChecked()) {
                failed = true;
            }
        }

        if (!failed) {
            boolean hasChecked = false;
            for (TableItem item : projectsTable.getItems()) {
                if (item.getChecked()) {
                    importData.addProjectName(item.getText(0), item.getText(1));
                    hasChecked = true;
                }
            }
            if (hasChecked) {
                setMessage(CoreMessages.dialog_project_import_wizard_file_message_ready, IMessageProvider.INFORMATION);
            } else {
                setMessage(CoreMessages.dialog_project_import_wizard_file_message_choose_project, IMessageProvider.INFORMATION);
            }
        }
        return !failed;
    }

    private boolean checkProjectItem(TableItem item)
    {
        String projectName = item.getText(1);
        IProject project = DBeaverCore.getInstance().getWorkspace().getRoot().getProject(projectName);
        if (!project.isAccessible()) {
            item.setForeground(1, null);
            return true;
        } else {
            if (item.getChecked()) {
                setMessage(NLS.bind(CoreMessages.dialog_project_import_wizard_file_message_project_exists, projectName), IMessageProvider.ERROR);
            }
            item.setForeground(1, projectsTable.getDisplay().getSystemColor(SWT.COLOR_RED));
            return false;
        }
    }

    private void updateState()
    {
        getContainer().updateButtons();
    }

    private boolean loadArchive()
    {
        if (importData.isFileSpecified(this)) {
            if (importData.getMetaTree() != null) {
                return true;
            }
            clearArchive();
            if (importData.loadArchiveMeta(this)) {
                Document metaTree = importData.getMetaTree();
                Element projectsElement = XMLUtils.getChildElement(metaTree.getDocumentElement(), ExportConstants.TAG_PROJECTS);
                if (projectsElement == null) {
                    setMessage(CoreMessages.dialog_project_import_wizard_file_message_cannt_find_projects, IMessageProvider.ERROR);
                } else {
                    projectsTable.removeAll();
                    for (Element projectElement : XMLUtils.getChildElementList(projectsElement, ExportConstants.TAG_PROJECT)) {
                        String projectName = projectElement.getAttribute(ExportConstants.ATTR_NAME);
                        TableItem item = new TableItem(projectsTable, SWT.NONE);
                        item.setImage(DBIcon.PROJECT.getImage());
                        item.setText(0, projectName);
                        item.setText(1, projectName);
                    }
                    UIUtils.packColumns(projectsTable);
                    updateProjectsSelection();
                    return true;
                }
            }
            return false;
        } else {
            clearArchive();
            return false;
        }
    }

    private void clearArchive()
    {
        projectsTable.removeAll();
    }

}
