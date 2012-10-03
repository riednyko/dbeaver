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
package org.jkiss.dbeaver.runtime.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.progress.IProgressConstants;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPConnectionEventType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;

/**
 * ConnectJob
 */
public class ConnectJob extends EventProcessorJob
{
    static final Log log = LogFactory.getLog(ConnectJob.class);

    private volatile Thread connectThread;
    private boolean reflect = true;

    public ConnectJob(
        DataSourceDescriptor container)
    {
        super(NLS.bind(CoreMessages.runtime_jobs_connect_name, container.getName()), container);
        setUser(true);
        setProperty(IProgressConstants.ICON_PROPERTY, ImageDescriptor.createFromImage(container.getDriver().getIcon()));
    }

    @Override
    protected IStatus run(DBRProgressMonitor monitor)
    {
        try {
            connectThread = getThread();
            String oldName = connectThread.getName();
            if (connectThread != null && reflect) {
                connectThread.setName(NLS.bind(CoreMessages.runtime_jobs_connect_thread_name, container.getName()));
            }

            processEvents(DBPConnectionEventType.BEFORE_CONNECT);

            try {
                container.connect(monitor, reflect);
            } finally {
                if (connectThread != null) {
                    connectThread.setName(oldName);
                    connectThread = null;
                }
            }

            processEvents(DBPConnectionEventType.AFTER_CONNECT);

            return new Status(
                Status.OK,
                DBeaverCore.getInstance().getPluginID(),
                CoreMessages.runtime_jobs_connect_status_connected);
        }
        catch (Throwable ex) {
            log.debug(ex);
            return RuntimeUtils.makeExceptionStatus(
                NLS.bind(CoreMessages.runtime_jobs_connect_status_error, container.getName()),
                ex);
        }
    }

    public IStatus runSync(DBRProgressMonitor monitor)
    {
        setThread(Thread.currentThread());
        reflect = false;
        return run(monitor);
    }

    @Override
    public boolean belongsTo(Object family)
    {
        return container == family;
    }

    @Override
    protected void canceling()
    {
        if (connectThread != null) {
            connectThread.interrupt();
        }
    }

}