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
package org.jkiss.dbeaver.ui.views.process;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.runtime.DBRProcessController;
import org.jkiss.dbeaver.model.runtime.DBRProcessDescriptor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.AbstractJob;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.help.IHelpContextIds;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShellProcessView extends ViewPart implements DBRProcessController
{
    public static final String VIEW_ID = "org.jkiss.dbeaver.core.shellProcess";

    private StyledText processLogText;
    private static int viewId = 0;
    private DBRProcessDescriptor processDescriptor;

    @Override
    public void createPartControl(Composite parent)
    {
        Composite group = UIUtils.createPlaceholder(parent, 1);
        group.setLayout(new FillLayout());

        processLogText = new StyledText(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        UIUtils.setHelp(group, IHelpContextIds.CTX_QUERY_MANAGER);
    }

    @Override
    public DBRProcessDescriptor getProcessDescriptor()
    {
        return processDescriptor;
    }

    @Override
    public void terminateProcess()
    {
        if (processDescriptor != null) {
            if (processDescriptor.isRunning()) {
                processDescriptor.terminate();
                DBeaverCore.getActiveWorkbenchShell().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run()
                    {
                        setPartName(processDescriptor.getName() + " (destroyed: " + processDescriptor.getExitValue() + ")");
                    }
                });

            }
        }
    }

    @Override
    public void dispose()
    {
        terminateProcess();
        super.dispose();
    }

    @Override
    public void setFocus()
    {
        if (processLogText != null && !processLogText.isDisposed()) {
            processLogText.setFocus();
        }
    }

    public synchronized static String getNextId()
    {
        viewId++;
        return String.valueOf(viewId);
    }

    public void initProcess(DBRProcessDescriptor processDescriptor)
    {
        this.processDescriptor = processDescriptor;
        setPartName(processDescriptor.getName());

        new ProcessLogger().schedule();
    }

    private class ProcessLogger extends AbstractJob {

        protected ProcessLogger()
        {
            super(processDescriptor.getName());
        }

        @Override
        protected IStatus run(DBRProgressMonitor monitor)
        {
            try {
                processDescriptor.execute();
                try {
                    final InputStream execOut = processDescriptor.getProcess().getInputStream();
                    final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(execOut)
                    );

                    for (;;) {
                        final String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        writeProcessLog(line);
                    }
                } finally {
                    processDescriptor.terminate();
                }

            } catch (Exception e) {
                return RuntimeUtils.makeExceptionStatus(e);
            }
            return Status.OK_STATUS;
        }
    }

    private void writeProcessLog(final String line)
    {
        if (line.isEmpty()) {
            return;
        }
        final Shell shell = DBeaverCore.getActiveWorkbenchShell();
        if (shell == null) {
            return;
        }
        final String logLine = line + ContentUtils.getDefaultLineSeparator();
        shell.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run()
            {
                if (processLogText == null || processLogText.isDisposed()) {
                    return;
                }
                processLogText.append(logLine);
            }
        });
    }

}
