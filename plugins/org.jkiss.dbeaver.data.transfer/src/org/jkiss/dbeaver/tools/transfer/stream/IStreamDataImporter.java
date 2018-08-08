/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2018 Serge Rider (serge@jkiss.org)
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

package org.jkiss.dbeaver.tools.transfer.stream;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.tools.transfer.IDataTransferConsumer;
import org.jkiss.dbeaver.tools.transfer.IDataTransferProcessor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * IStreamDataImporter
 */
public interface IStreamDataImporter extends IDataTransferProcessor {

    void init(IStreamDataImporterSite site) throws DBException;

    List<StreamDataImporterColumnInfo> readColumnsInfo(InputStream inputStream, StreamProducerSettings settings, Map<Object, Object> processorProperties) throws DBException;

    void runImport(DBRProgressMonitor monitor, InputStream inputStream, StreamProducerSettings.EntityMapping mapping, Map<Object, Object> properties, int rowCount, IDataTransferConsumer consumer) throws DBException;

    void dispose();

}
