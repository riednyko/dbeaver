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
package org.jkiss.dbeaver.ui.editors.sql.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.navigator.NavigatorHandlerObjectOpen;
import org.jkiss.dbeaver.ui.resources.ScriptsHandlerImpl;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenSQLEditorHandler extends BaseSQLEditorHandler {

    @Nullable
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        DBPDataSourceContainer dataSourceContainer = getDataSourceContainer(event, false);
        IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);

        IProject project = dataSourceContainer != null ?
            dataSourceContainer.getRegistry().getProject() :
            DBeaverCore.getInstance().getProjectRegistry().getActiveProject();
        try {
            final IFolder rootFolder = ScriptsHandlerImpl.getScriptsFolder(project, false);
            List<IFile> scriptFiles = new ArrayList<>();
            ScriptsHandlerImpl.findScriptsByDataSource(rootFolder, dataSourceContainer, scriptFiles);
            if (scriptFiles.isEmpty()) {
                // Create new script
                final IFile newScript = ScriptsHandlerImpl.createNewScript(project, rootFolder, dataSourceContainer);
                NavigatorHandlerObjectOpen.openResource(newScript, workbenchWindow);
            } else {
                // Show script chooser
                ScriptSelectorShell selector = new ScriptSelectorShell(workbenchWindow, dataSourceContainer, rootFolder, scriptFiles);
                selector.show();
            }
/*
            scriptFile = ScriptsHandlerImpl.findRecentScript(project, dataSourceContainer);
            if (scriptFile == null) {
                scriptFile = ScriptsHandlerImpl.createNewScript(project, scriptFolder, dataSourceContainer);
            }
            NavigatorHandlerObjectOpen.openResource(scriptFile, workbenchWindow);
*/
        }
        catch (CoreException e) {
            log.error(e);
        }


        return null;
    }

    private static class ScriptSelectorShell {

        private final IWorkbenchWindow workbenchWindow;
        private final Shell popup;
        private final List<IFile> scriptFiles;
        private final Text patternText;
        private final Tree scriptTable;
        private final Button newButton;

        public ScriptSelectorShell(final IWorkbenchWindow workbenchWindow, final DBPDataSourceContainer dataSourceContainer, final IFolder rootFolder, List<IFile> scriptFiles) {
            this.workbenchWindow = workbenchWindow;
            this.scriptFiles = new ArrayList<>(scriptFiles);
            Collections.sort(this.scriptFiles, new Comparator<IFile>() {
                @Override
                public int compare(IFile o1, IFile o2) {
                    return (int)(o1.getLocation().toFile().lastModified() - o2.getLocation().toFile().lastModified());
                }
            });
            Shell parent = this.workbenchWindow.getShell();

            final Color bg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

            popup = new Shell(parent, SWT.RESIZE | SWT.NO_TRIM);
            popup.setLayout(new FillLayout());
            popup.setBounds(100, 100, 400, 200);

            Composite composite = new Composite(popup, SWT.BORDER);

            final GridLayout gl = new GridLayout(2, false);
            //gl.marginHeight = 0;
            //gl.marginWidth = 0;
            composite.setLayout(gl);
            composite.setBackground(bg);

            patternText = new Text(composite, SWT.NONE);
            patternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            patternText.setBackground(bg);

            newButton = new Button(composite, SWT.PUSH | SWT.FLAT);
            newButton.setText("&New Script");
            newButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    popup.dispose();
                    IFile scriptFile;
                    try {
                        scriptFile = ScriptsHandlerImpl.createNewScript(rootFolder.getProject(), rootFolder, dataSourceContainer);
                        NavigatorHandlerObjectOpen.openResource(scriptFile, workbenchWindow);
                    }
                    catch (CoreException ex) {
                        log.error(ex);
                    }
                }
            });

            ((GridData)UIUtils.createHorizontalLine(composite).getLayoutData()).horizontalSpan = 2;

            scriptTable = new Tree(composite, SWT.MULTI | SWT.FULL_SELECTION);
            final GridData gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = 2;
            scriptTable.setLayoutData(gd);
            scriptTable.setBackground(bg);
            scriptTable.setLinesVisible(true);
            //scriptTable.setHeaderVisible(true);
            UIUtils.createTreeColumn(scriptTable, SWT.LEFT, "Script");
            UIUtils.createTreeColumn(scriptTable, SWT.LEFT, "Info");

            for (IFile scriptFile : this.scriptFiles) {
                final TreeItem item = new TreeItem(scriptTable, SWT.NONE);
                item.setData(scriptFile);
                item.setImage(DBeaverIcons.getImage(UIIcon.SQL_SCRIPT));
                item.setText(0, scriptFile.getName() + "  ");

                String desc = SQLUtils.getScriptDescription(scriptFile);
                if (CommonUtils.isEmptyTrimmed(desc)) {
                    desc = "<empty>";
                }
                item.setText(1, desc);
            }

            scriptTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    List<IFile> files = new ArrayList<>();
                    for (TreeItem item : scriptTable.getSelection()) {
                        files.add((IFile) item.getData());
                    }
                    popup.dispose();
                    for (IFile file : files) {
                        NavigatorHandlerObjectOpen.openResource(
                            file,
                            ScriptSelectorShell.this.workbenchWindow);
                    }
                }
            });
            this.patternText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.keyCode == SWT.ARROW_DOWN) {
                        scriptTable.select(scriptTable.getItem(0));
                        scriptTable.setFocus();
                    } else if (e.keyCode == SWT.ARROW_UP) {
                        scriptTable.select(scriptTable.getItem(scriptTable.getItemCount() - 1));
                        scriptTable.setFocus();
                    }
                }
            });

            final Listener focusFilter = new Listener() {
                public void handleEvent(Event event)
                {
                    if (event.widget != scriptTable && event.widget != patternText && event.widget != newButton) {
                        popup.dispose();
                    }
                }
            };

            popup.getDisplay().addFilter(SWT.FocusIn, focusFilter);
            popup.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    popup.getDisplay().removeFilter(SWT.FocusIn, focusFilter);
                }
            });
        }

        void show() {
            // Fill script list
            popup.layout();
            popup.setVisible(true);
            final int totalWidth = scriptTable.getSize().x;
            final TreeColumn column0 = scriptTable.getColumn(0);
            final TreeColumn column1 = scriptTable.getColumn(1);
            column0.pack();
            column1.pack();
            if (column0.getWidth() + column1.getWidth() < totalWidth) {
                column1.setWidth(totalWidth - column0.getWidth());
            }
            //int nameWidth = Math.max(100, totalWidth / 2);
            //scriptTable.getColumn(1).setWidth(totalWidth - column0.x);
            //UIUtils.packColumns(scriptTable, true, new float[] {0.7f, 0.3f});

            patternText.setFocus();
        }
    }

}