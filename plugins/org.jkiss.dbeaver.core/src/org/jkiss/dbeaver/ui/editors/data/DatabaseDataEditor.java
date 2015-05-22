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
package org.jkiss.dbeaver.ui.editors.data;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
import org.jkiss.dbeaver.model.data.DBDDataFilter;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.struct.DBSDataContainer;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetContainer;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetListener;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetViewer;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.utils.CommonUtils;

/**
 * DatabaseDataEditor
 */
public class DatabaseDataEditor extends AbstractDatabaseObjectEditor<DBSDataContainer>
    implements IResultSetContainer,IResultSetListener
{
    public static final String ATTR_SUSPEND_QUERY = "suspendQuery";
    public static final String ATTR_DATA_FILTER = "dataFilter";

    private ResultSetViewer resultSetView;
    private boolean loaded = false;
    //private boolean running = false;
    private Composite parent;
    private FindReplaceAction findReplaceAction;

    @Override
    public void createPartControl(Composite parent)
    {
        this.parent = parent;

        // Register find/replace action
        // We do it in setFocus because each entity editor registers it's own action - and they
        // share a single action bars instance. To avoid mess just update handler every time editor activated
        findReplaceAction = new FindReplaceAction(DBeaverActivator.getResourceBundle(), "Editor.FindReplace.", this);
        findReplaceAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
    }

    @Override
    public void activatePart()
    {
        createResultSetView();

        IDatabaseEditorInput editorInput = getEditorInput();
        boolean suspendQuery = CommonUtils.toBoolean(editorInput.getAttribute(ATTR_SUSPEND_QUERY));
        DBDDataFilter dataFilter = (DBDDataFilter) editorInput.getAttribute(ATTR_DATA_FILTER);
        if (!loaded && !suspendQuery) {
            if (getDatabaseObject() != null && getDatabaseObject().isPersisted()) {
                resultSetView.setStatus("Query data from '" + editorInput.getDatabaseObject().getName() + "'...");
                if (dataFilter == null) {
                    resultSetView.refresh();
                } else {
                    resultSetView.refreshWithFilter(dataFilter);
                }
                loaded = true;
            }
        }
        //resultSetView.setSelection(resultSetView.getSelection());
    }

    private void createResultSetView()
    {
        if (resultSetView == null) {
            resultSetView = new ResultSetViewer(parent, getSite(), this);
            resultSetView.addListener(this);
            parent.layout();
            resultSetView.getControl().setFocus();

            // Set selection provider from resultset
            getSite().setSelectionProvider(resultSetView);
        }
    }

    @Override
    public void deactivatePart()
    {
    }

    @Override
    public void dispose() {
        if (resultSetView != null) {
            resultSetView.removeListener(this);
            resultSetView = null;
        }
        super.dispose();
    }

    /**
     * Uses data source as main execution context
     * @return data source reference. never null.
     */
    @Override
    public DBCExecutionContext getExecutionContext() {
        return getEditorInput().getDataSource();
    }

    @Nullable
    @Override
    public ResultSetViewer getResultSetViewer()
    {
        return resultSetView;
    }

    @Nullable
    @Override
    public DBSDataContainer getDataContainer()
    {
        return (DBSDataContainer)getEditorInput().getDatabaseObject();
    }

    @Override
    public boolean isReadyToRun()
    {
        return true;
    }

    @Override
    public void setFocus()
    {
        createResultSetView();
        if (resultSetView != null) {
            resultSetView.getActivePresentation().getControl().setFocus();
        }

        refreshActions();
    }

    private void refreshActions() {
        findReplaceAction.update();
        IActionBars actionBars = getEditorSite().getActionBars();
        actionBars.setGlobalActionHandler("dde_findReplace", findReplaceAction);
        actionBars.updateActionBars();
    }

    @Override
    public void refreshPart(Object source, boolean force)
    {
        if (loaded) {
            //resultSetView.refresh();
        }
    }

    @Override
    public boolean isDirty()
    {
        return resultSetView != null && !resultSetView.getControl().isDisposed() && resultSetView.isDirty();
    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        if (resultSetView != null && resultSetView.isDirty()) {
            resultSetView.applyChanges(RuntimeUtils.makeMonitor(monitor));
        }
    }

    @Override
    public void handleResultSetLoad() {
        refreshActions();
    }

    @Override
    public void handleResultSetChange()
    {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public Object getAdapter(Class required)
    {
        if (resultSetView != null) {
            Object adapter = resultSetView.getAdapter(required);
            if (adapter != null) {
                return adapter;
            }
        }
        return super.getAdapter(required);
    }

}
