/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ui.actions.datasource;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.runtime.jobs.InvalidateJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.DataSourceHandler;
import org.jkiss.dbeaver.ui.dialogs.StandardErrorDialog;
import org.jkiss.utils.CommonUtils;

public class DataSourceInvalidateHandler extends DataSourceHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        final DataSourceDescriptor dataSourceContainer = (DataSourceDescriptor) getDataSourceContainer(event, false);
        if (dataSourceContainer != null) {
            execute(dataSourceContainer);
        }
        return null;
    }

    public static void execute(DBSDataSourceContainer dataSourceContainer) {
        if (dataSourceContainer instanceof DataSourceDescriptor && dataSourceContainer.isConnected()) {
            final DataSourceDescriptor dataSourceDescriptor = (DataSourceDescriptor)dataSourceContainer;
            if (!CommonUtils.isEmpty(Job.getJobManager().find(dataSourceDescriptor))) {
                // Already connecting/disconnecting - just return
                return;
            }
            InvalidateJob invalidateJob = new InvalidateJob(dataSourceContainer.getDataSource());
            invalidateJob.schedule();
        }
    }

    public static void showConnectionLostDialog(final Shell shell, final String message, final DBException error)
    {
        //log.debug(message);
        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {
                // Display the dialog
                DBPDataSource dataSource = error.getDataSource();
                if (dataSource == null) {
                    throw new IllegalStateException("No data source in error");
                }
                String title = "Connection with [" + dataSource.getContainer().getName() + "] lost";
                ConnectionRecoverDialog dialog = new ConnectionRecoverDialog(shell, title, message == null ? title : message, error);
                dialog.open();
            }
        };
        UIUtils.runInUI(shell, runnable);
    }

    private static class ConnectionRecoverDialog extends StandardErrorDialog {

        private final DBPDataSource dataSource;

        public ConnectionRecoverDialog(Shell shell, String title, String message, DBException error)
        {
            super(
                shell == null ? DBeaverUI.getActiveWorkbenchShell() : shell,
                title,
                message,
                RuntimeUtils.makeExceptionStatus(error),
                IStatus.ERROR);
            dataSource = error.getDataSource();
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent)
        {
            createButton(parent, IDialogConstants.RETRY_ID, "Reconnect", true);
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
            createDetailsButton(parent);
        }

        @Override
        protected void buttonPressed(int id)
        {
            if (id == IDialogConstants.RETRY_ID) {
                execute(dataSource.getContainer());
                super.buttonPressed(IDialogConstants.OK_ID);
            }
            super.buttonPressed(id);
        }
    }

}