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
package org.jkiss.dbeaver.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.runtime.RunnableContextDelegate;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.AbstractUIJob;
import org.jkiss.dbeaver.ui.SharedTextColors;
import org.jkiss.dbeaver.ui.TrayIconHandler;
import org.osgi.framework.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DBeaver UI core
 */
public class DBeaverUI {

    static final Log log = Log.getLog(DBeaverUI.class);

    private static DBeaverUI instance;

    private SharedTextColors sharedTextColors;
    private TrayIconHandler trayItem;
    private final List<IDisposable> globalDisposables = new ArrayList<IDisposable>();

    public static DBeaverUI getInstance()
    {
        if (instance == null) {
            instance = new DBeaverUI();
            instance.initialize();
        }
        return instance;
    }

    static void disposeUI()
    {
        if (instance != null) {
            instance.dispose();
        }
    }

    public static ISharedTextColors getSharedTextColors()
    {
        return getInstance().sharedTextColors;
    }

    private void dispose()
    {
        //this.trayItem.dispose();
        this.sharedTextColors.dispose();

        if (trayItem != null) {
            trayItem.hide();
        }

        List<IDisposable> dispList = new ArrayList<IDisposable>(globalDisposables);
        Collections.reverse(dispList);
        for (IDisposable disp : dispList) {
            try {
                disp.dispose();
            } catch (Exception e) {
                log.error(e);
            }
            globalDisposables.remove(disp);
        }
    }

    private void initialize()
    {
        Bundle coreBundle = DBeaverActivator.getInstance().getBundle();
        DBeaverIcons.initRegistry(coreBundle);

        this.sharedTextColors = new SharedTextColors();

        this.trayItem = new TrayIconHandler();
    }

    public static AbstractUIJob runUIJob(String jobName, final DBRRunnableWithProgress runnableWithProgress)
    {
        return runUIJob(jobName, 0, runnableWithProgress);
    }

    public static AbstractUIJob runUIJob(String jobName, int timeout, final DBRRunnableWithProgress runnableWithProgress)
    {
        AbstractUIJob job = new AbstractUIJob(jobName) {
            @Override
            public IStatus runInUIThread(DBRProgressMonitor monitor)
            {
                try {
                    runnableWithProgress.run(monitor);
                } catch (InvocationTargetException e) {
                    return RuntimeUtils.makeExceptionStatus(e);
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule(timeout);
        return job;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window != null) {
            return window;
        }
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        if (windows.length > 0) {
            return windows[0];
        }
        return null;
    }

    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    public static Display getDisplay()
    {
        Shell shell = getActiveWorkbenchShell();
        if (shell != null)
            return shell.getDisplay();
        else
            return Display.getDefault();
    }

/*
    public static void runWithProgress(IWorkbenchPartSite site, final DBRRunnableWithProgress runnable)
        throws InvocationTargetException, InterruptedException
    {
        IActionBars actionBars = null;
        if (site instanceof IViewSite) {
            actionBars = ((IViewSite) site).getActionBars();
        } else if (site instanceof IEditorSite) {
            actionBars = ((IEditorSite) site).getActionBars();
        }
        IStatusLineManager statusLineManager = null;
        if (actionBars != null) {
            statusLineManager = actionBars.getStatusLineManager();
        }
        if (statusLineManager == null) {
            runInProgressService(runnable);
        } else {
            IProgressMonitor progressMonitor = statusLineManager.getProgressMonitor();
            runnable.run(new DefaultProgressMonitor(progressMonitor));
        }
    }
*/

    public static DBRRunnableContext getDefaultRunnableContext()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null && workbench.getActiveWorkbenchWindow() != null) {
            return new RunnableContextDelegate(workbench.getActiveWorkbenchWindow());
        } else {
            return new DBRRunnableContext() {
                @Override
                public void run(boolean fork, boolean cancelable, DBRRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException
                {
                    runnable.run(VoidProgressMonitor.INSTANCE);
                }
            };
        }
    }

    public static void runInProgressService(final DBRRunnableWithProgress runnable)
        throws InvocationTargetException, InterruptedException
    {
        getDefaultRunnableContext().run(true, true, new DBRRunnableWithProgress() {
                @Override
                public void run(DBRProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    runnable.run(monitor);
                }
            });
    }

    public static void runInProgressDialog(final DBRRunnableWithProgress runnable) throws InvocationTargetException
    {
        try {
            IRunnableContext runnableContext;
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
            if (workbenchWindow != null) {
                runnableContext = new ProgressMonitorDialog(workbench.getActiveWorkbenchWindow().getShell());
            } else {
                runnableContext = workbench.getProgressService();
            }
            runnableContext.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    runnable.run(RuntimeUtils.makeMonitor(monitor));
                }
            });
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static void runInUI(IRunnableContext context, final DBRRunnableWithProgress runnable)
    {
        try {
            PlatformUI.getWorkbench().getProgressService().runInUI(context, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    runnable.run(RuntimeUtils.makeMonitor(monitor));
                }
            }, DBeaverActivator.getWorkspace().getRoot());
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException());
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static void notifyAgent(String message, int status)
    {
        if (!DBeaverCore.getGlobalPreferenceStore().getBoolean(DBeaverPreferences.AGENT_LONG_OPERATION_NOTIFY)) {
            // Notifications disabled
            return;
        }
        getInstance().trayItem.notify(message, status);
    }

    public void addDisposeListener(IDisposable disposable) {
        globalDisposables.add(disposable);
    }
}
