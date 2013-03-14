/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 * eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.tools.transfer.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.AbstractJob;
import org.jkiss.dbeaver.tools.transfer.IDataTransferConsumer;
import org.jkiss.dbeaver.tools.transfer.IDataTransferProducer;
import org.jkiss.dbeaver.tools.transfer.IDataTransferSettings;

/**
 * Data transfer job
 */
public class DataTransferJob extends AbstractJob {

    private DataTransferSettings settings;

    public DataTransferJob(DataTransferSettings settings)
    {
        super(CoreMessages.data_transfer_wizard_job_name);
        this.settings = settings;

        setUser(true);
    }

    @Override
    public boolean belongsTo(Object family)
    {
        return family == settings;
    }

    @Override
    protected IStatus run(DBRProgressMonitor monitor)
    {

        for (; ;) {
            DataTransferPipe transferPipe = settings.acquireDataPipe();
            if (transferPipe == null) {
                break;
            }
            transferData(monitor, transferPipe);
        }

        return Status.OK_STATUS;
    }

    private void transferData(DBRProgressMonitor monitor, DataTransferPipe transferPipe)
    {
        IDataTransferProducer producer = transferPipe.getProducer();
        IDataTransferConsumer consumer = transferPipe.getConsumer();

        IDataTransferSettings consumerSettings = settings.getNodeSettings(consumer);

        setName(NLS.bind(CoreMessages.data_transfer_wizard_job_container_name,
            producer.getSourceObject().getName()));

        IDataTransferSettings nodeSettings = settings.getNodeSettings(producer);
        try {
            //consumer.initTransfer(producer.getSourceObject(), consumerSettings, );

            producer.transferData(
                monitor,
                consumer,
                nodeSettings);
            consumer.finishTransfer(false);
        } catch (Exception e) {
            new DataTransferErrorJob(e).schedule();
        }

    }

}
