/*
 * Copyright (C) 2010-2013 Serge Rieder
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
package org.jkiss.dbeaver.tools.transfer.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBPNamedObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.data.DBDContent;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.tools.transfer.IDataTransferConsumer;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.Base64;
import org.jkiss.utils.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
* Stream transfer consumer
*/
public class StreamTransferConsumer implements IDataTransferConsumer<StreamConsumerSettings, IStreamDataExporter> {

    static final Log log = LogFactory.getLog(StreamTransferConsumer.class);

    private static final String LOB_DIRECTORY_NAME = "files"; //$NON-NLS-1$

    private IStreamDataExporter processor;
    private StreamConsumerSettings settings;
    private DBSObject sourceObject;
    private OutputStream outputStream;
    private ZipOutputStream zipStream;
    private PrintWriter writer;
    private List<DBDAttributeBinding> metaColumns;
    private Object[] row;
    private File lobDirectory;
    private long lobCount;
    private File outputFile;
    private StreamExportSite exportSite;
    private Map<Object, Object> processorProperties;

    public StreamTransferConsumer()
    {
    }

    @Override
    public void fetchStart(DBCSession session, DBCResultSet resultSet) throws DBCException
    {
        initExporter(session);

        // Prepare columns
        metaColumns = new ArrayList<DBDAttributeBinding>();
        List<DBCAttributeMetaData> attributes = resultSet.getResultSetMetaData().getAttributes();
        for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++) {
            DBCAttributeMetaData attribute = attributes.get(i);
            DBDAttributeBinding columnBinding = DBUtils.getColumnBinding(session, attribute, i);
            metaColumns.add(columnBinding);
        }
        row = new Object[metaColumns.size()];

        try {
            processor.exportHeader(session.getProgressMonitor());
        } catch (DBException e) {
            log.warn("Error while exporting table header", e);
        } catch (IOException e) {
            throw new DBCException("IO error", e);
        }
    }

    @Override
    public void fetchRow(DBCSession session, DBCResultSet resultSet) throws DBCException
    {
        try {
            // Get values
            for (int i = 0; i < metaColumns.size(); i++) {
                DBDAttributeBinding column = metaColumns.get(i);
                Object value = column.getValueHandler().fetchValueObject(session, resultSet, column.getMetaAttribute(), column.getAttributeIndex());
                if (value instanceof DBDContent) {
                    // Check for binary type export
                    if (!ContentUtils.isTextContent((DBDContent)value)) {
                        switch (settings.getLobExtractType()) {
                            case SKIP:
                                // Set it it null
                                value = null;
                                break;
                            case INLINE:
                                // Just pass content to exporter
                                break;
                            case FILES:
                                // Save content to file and pass file reference to exporter
                                value = saveContentToFile(session.getProgressMonitor(), (DBDContent)value);
                                break;
                        }
                    }
                }
                row[i] = value;
            }
            // Export row
            processor.exportRow(session.getProgressMonitor(), row);
        } catch (DBException e) {
            throw new DBCException("Error while exporting table row", e);
        } catch (IOException e) {
            throw new DBCException("IO error", e);
        }
    }

    @Override
    public void fetchEnd(DBCSession session) throws DBCException
    {
        try {
            processor.exportFooter(session.getProgressMonitor());
        } catch (DBException e) {
            log.warn("Error while exporting table footer", e);
        } catch (IOException e) {
            throw new DBCException("IO error", e);
        }

        closeExporter();
    }

    @Override
    public void close()
    {
        metaColumns = null;
        row = null;
    }

    private File saveContentToFile(DBRProgressMonitor monitor, DBDContent content)
        throws IOException, DBCException
    {
        DBDContentStorage contents = content.getContents(monitor);
        if (contents == null) {
            log.warn("Null value content");
            return null;
        }
        if (lobDirectory == null) {
            lobDirectory = new File(settings.getOutputFolder(), LOB_DIRECTORY_NAME);
            if (!lobDirectory.exists()) {
                if (!lobDirectory.mkdir()) {
                    throw new IOException("Could not create directory for LOB files: " + lobDirectory.getAbsolutePath());
                }
            }
        }
        lobCount++;
        Boolean extractImages = (Boolean) processorProperties.get(StreamConsumerSettings.PROP_EXTRACT_IMAGES);
        String fileExt = (extractImages != null && extractImages) ? ".jpg" : ".data";
        File lobFile = new File(lobDirectory, outputFile.getName() + "-" + lobCount + fileExt); //$NON-NLS-1$ //$NON-NLS-2$
        ContentUtils.saveContentToFile(contents.getContentStream(), lobFile, monitor);
        return lobFile;
    }

    private void initExporter(DBCSession session) throws DBCException
    {
        if (settings.getFormatterProfile() != null) {
            session.setDataFormatterProfile(settings.getFormatterProfile());
        }

        exportSite = new StreamExportSite();

        // Open output streams
        outputFile = makeOutputFile();
        try {
            this.outputStream = new BufferedOutputStream(
                new FileOutputStream(outputFile),
                10000);

            if (settings.isCompressResults()) {
                zipStream = new ZipOutputStream(this.outputStream);
                zipStream.putNextEntry(new ZipEntry(getOutputFileName()));
                StreamTransferConsumer.this.outputStream = zipStream;
            }
            this.writer = new PrintWriter(new OutputStreamWriter(this.outputStream, settings.getOutputEncoding()), true);

            // Check for BOM
            if (settings.isOutputEncodingBOM()) {
                byte[] bom = ContentUtils.getCharsetBOM(settings.getOutputEncoding());
                if (bom != null) {
                    outputStream.write(bom);
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            closeExporter();
            throw new DBCException("Data transfer IO error", e);
        }

        try {
            // init exporter
            processor.init(exportSite);
        } catch (DBException e) {
            throw new DBCException("Could not initialize data exporter", e);
        }
    }

    private void closeExporter()
    {
        if (exportSite != null) {
            try {
                exportSite.flush();
            } catch (IOException e) {
                log.debug(e);
            }
        }

        if (processor != null) {
            // Dispose exporter
            processor.dispose();
            processor = null;
        }

        // Finish zip stream
        if (zipStream != null) {
            try {
                zipStream.closeEntry();
            } catch (IOException e) {
                log.debug(e);
            }
            try {
                zipStream.finish();
            } catch (IOException e) {
                log.debug(e);
            }
        }
        if (this.writer != null) {
            ContentUtils.close(this.writer);
            this.writer = null;
        }

        if (outputStream != null) {
            ContentUtils.close(outputStream);
            outputStream = null;
        }
    }

    @Override
    public void initTransfer(DBSObject sourceObject, StreamConsumerSettings settings, IStreamDataExporter processor, Map<Object, Object> processorProperties)
    {
        this.sourceObject = sourceObject;
        this.processor = processor;
        this.settings = settings;
        this.processorProperties = processorProperties;
    }

    @Override
    public void startTransfer(DBRProgressMonitor monitor)
    {
        // do nothing
    }

    @Override
    public void finishTransfer(boolean last)
    {
        if (last && settings.isOpenFolderOnFinish()) {
            // Last one
            DBeaverUI.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    RuntimeUtils.launchProgram(settings.getOutputFolder());
                }
            });
        }
    }

    @Override
    public String getTargetName()
    {
        return makeOutputFile().getAbsolutePath();
    }

    public String getOutputFileName()
    {
        Object extension = processorProperties.get(StreamConsumerSettings.PROP_FILE_EXTENSION);
        String fileName = processTemplate(stripObjectName(sourceObject.getName()));
        if (extension != null) {
            return fileName + "." + extension;
        } else {
            return fileName;
        }
    }

    public File makeOutputFile()
    {
        File dir = new File(settings.getOutputFolder());
        String fileName = getOutputFileName();
        if (settings.isCompressResults()) {
            fileName += ".zip";
        }
        return new File(dir, fileName);
    }

    private String processTemplate(String tableName)
    {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        return settings.getOutputFilePattern()
            .replaceAll("\\{table\\}", tableName)
            .replaceAll("\\{timestamp\\}", timeStamp);
    }

    private static String stripObjectName(String name)
    {
        StringBuilder result = new StringBuilder();
        boolean lastUnd = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
                lastUnd = false;
            } else if (!lastUnd) {
                result.append('_');
                lastUnd = true;
            }
            if (result.length() >= 64) {
                break;
            }
        }
        return result.toString();
    }

    private class StreamExportSite implements IStreamDataExporterSite {
        @Override
        public DBPNamedObject getSource()
        {
            return sourceObject;
        }

        @Override
        public DBDDisplayFormat getExportFormat()
        {
            DBDDisplayFormat format = DBDDisplayFormat.UI;
            Object formatProp = processorProperties.get(StreamConsumerSettings.PROP_FORMAT);
            if (formatProp != null) {
                format = DBDDisplayFormat.valueOf(formatProp.toString().toUpperCase());
            }
            return format;
        }

        @Override
        public Map<Object, Object> getProperties()
        {
            return processorProperties;
        }

        @Override
        public List<DBDAttributeBinding> getAttributes()
        {
            return metaColumns;
        }

        @Override
        public OutputStream getOutputStream()
        {
            return outputStream;
        }

        @Override
        public PrintWriter getWriter()
        {
            return writer;
        }

        @Override
        public void flush() throws IOException
        {
            writer.flush();
            outputStream.flush();
        }

        @Override
        public void writeBinaryData(InputStream stream, long streamLength) throws IOException
        {
            exportSite.flush();
            switch (settings.getLobEncoding()) {
                case BASE64:
                {
                    Base64.encode(stream, streamLength, writer);
                    break;
                }
                case HEX:
                {
                    writer.write("0x"); //$NON-NLS-1$
                    byte[] buffer = new byte[5000];
                    for (;;) {
                        int count = stream.read(buffer);
                        if (count <= 0) {
                            break;
                        }
                        ContentUtils.writeBytesAsHex(writer, buffer, 0, count);
                    }
                    break;
                }
                default:
                    // Binary stream
                    IOUtils.copyStream(stream, outputStream, 5000);
                    break;
            }
        }
    }
}
