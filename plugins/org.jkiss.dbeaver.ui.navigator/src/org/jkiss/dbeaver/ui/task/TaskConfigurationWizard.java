/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ui.task;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.app.DBPProject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.task.DBTTask;
import org.jkiss.dbeaver.model.task.DBTTaskType;
import org.jkiss.dbeaver.registry.task.TaskRegistry;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseWizard;
import org.jkiss.dbeaver.ui.navigator.NavigatorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class TaskConfigurationWizard extends BaseWizard implements IWorkbenchWizard {

    private DBTTask currentTask;
    private IStructuredSelection currentSelection;
    private Button saveAsTaskButton;

    protected TaskConfigurationWizard() {
    }

    protected TaskConfigurationWizard(@Nullable DBTTask task) {
        this.currentTask = task;
    }

    protected abstract String getDefaultWindowTitle();

    public boolean isTaskEditor() {
        return currentTask != null;
    }

    public abstract String getTaskTypeId();

    public abstract void saveTaskState(DBRProgressMonitor monitor, Map<String, Object> state);

    public IStructuredSelection getCurrentSelection() {
        return currentSelection;
    }

    public DBTTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(DBTTask currentTask) {
        this.currentTask = currentTask;
        updateWizardTitle();
        getContainer().updateButtons();
    }

    public DBPProject getProject() {
        return currentTask != null ? currentTask.getProject() : NavigatorUtils.getSelectedProject();
    }

    protected void updateWizardTitle() {
        String wizTitle = getDefaultWindowTitle();
        if (isTaskEditor()) {
            TaskConfigurationWizardPageTask taskPage = getContainer() == null ? null : getContainer().getTaskPage();
            wizTitle += " - [" + (taskPage == null ? currentTask.getName() : taskPage.getTaskName()) + "]";
        }
        setWindowTitle(wizTitle);
    }

    @Override
    public TaskConfigurationWizardDialog getContainer() {
        return (TaskConfigurationWizardDialog) super.getContainer();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        updateWizardTitle();
        setNeedsProgressMonitor(true);
        this.currentSelection = currentSelection;
        getContainer().addPageChangedListener(event -> updateWizardTitle());
    }

    @Override
    public void addPages() {
        super.addPages();
        // If we are in task edit mode then add special first page.
        // Do not add it if this is an ew task wizard (because this page is added separately)
        if (isCurrentTaskSaved()) {
            // Task editor. Add first page
            addPage(new TaskConfigurationWizardPageTask(getCurrentTask()));
        }
    }

    public boolean isCurrentTaskSaved() {
        return getCurrentTask() != null && getCurrentTask().getProject().getTaskManager().getTaskById(getCurrentTask().getId()) != null;
    }

    @Override
    public boolean canFinish() {
        for (IWizardPage page : getPages()) {
            if (isPageValid(page) && !page.isPageComplete()) {
                return false;
            }
        }
        TaskConfigurationWizardPageTask taskPage = getContainer().getTaskPage();
        if (taskPage != null && !taskPage.isPageComplete()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean performFinish() {
        if (currentTask != null) {
            saveTask();
        }
        return true;
    }

    protected boolean isPageValid(IWizardPage page) {
        return true;
    }

    public void saveTask() {
        DBTTask currentTask = getCurrentTask();
        if (currentTask == null) {
            // Create new task
            DBTTaskType taskType = TaskRegistry.getInstance().getTaskType(getTaskTypeId());
            if (taskType == null) {
                DBWorkbench.getPlatformUI().showError("No task type", "Can't find task type " + getTaskTypeId());
                return;
            }
            EditTaskConfigurationDialog dialog = new EditTaskConfigurationDialog(getContainer().getShell(), getProject(), taskType);
            if (dialog.open() == IDialogConstants.OK_ID) {
                setCurrentTask(currentTask = dialog.getTask());
            } else {
                return;
            }
        } else {
            TaskConfigurationWizardPageTask taskPage = getContainer().getTaskPage();
            if (taskPage != null) {
                taskPage.saveSettings();
            }
        }
        try {
            DBTTask theTask = currentTask;
            getRunnableContext().run(true, true, monitor -> {
                try {
                    saveTaskState(monitor, theTask.getProperties());
                    theTask.getProject().getTaskManager().updateTaskConfiguration(theTask);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            });
        } catch (InvocationTargetException e) {
            DBWorkbench.getPlatformUI().showError("Tsk save error", "Error saving task configuration", e.getTargetException());
        } catch (InterruptedException e) {
            // ignore
        }
    }


    public void createTaskSaveButtons(Composite parent, int hSpan) {
        Composite panel = new Composite(parent, SWT.NONE);
        if (parent.getLayout() instanceof GridLayout) {
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = hSpan;
            panel.setLayoutData(gd);
            //((GridLayout) parent.getLayout()).numColumns++;
        }
        panel.setLayout(new GridLayout(3, false));
        saveAsTaskButton = UIUtils.createDialogButton(panel, isTaskEditor() ? "Update configuration in task" : "Save configuration as task", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveTask();
            }
        });
        //((GridData)UIUtils.createEmptyLabel(panel, 1, 1).getLayoutData()).grabExcessHorizontalSpace = true;
        Link tasksLink = UIUtils.createLink(panel, "<a>Open Tasks view</a>", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    UIUtils.getActiveWorkbenchWindow().getActivePage().showView(DatabaseTasksView.VIEW_ID);
                } catch (PartInitException e1) {
                    DBWorkbench.getPlatformUI().showError("Show view", "Error opening database tasks view", e1);
                }
            }
        });
        tasksLink.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    }

    public void updateSaveTaskButtons() {
        if (saveAsTaskButton != null) {
            // TODO: we should be able to save/run task immediately if it was saved before.
            // TODO: There is a bug in DT wizard which doesn't let to do it (producers/consumers are initialized only on the last page).
            // TODO: init transfer for all deserialized producers/consumers
            saveAsTaskButton.setEnabled(/*(getTaskWizard() != null && getTaskWizard().isCurrentTaskSaved()) || */canFinish());
        }
    }

}