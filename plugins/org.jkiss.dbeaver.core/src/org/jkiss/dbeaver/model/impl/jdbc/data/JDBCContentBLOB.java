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
package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.impl.BytesContentStorage;
import org.jkiss.dbeaver.model.impl.TemporaryContentStorage;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.preferences.PrefConstants;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.dbeaver.utils.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * JDBCContentBLOB
 *
 * @author Serge Rider
 */
public class JDBCContentBLOB extends JDBCContentLOB {

    static final Log log = LogFactory.getLog(JDBCContentBLOB.class);

    private Blob blob;
    private InputStream tmpStream;

    public JDBCContentBLOB(Blob blob) {
        this.blob = blob;
    }

    @Override
    public long getLOBLength() throws DBCException {
        if (blob != null) {
            try {
                return blob.length();
            } catch (SQLException e) {
                throw new DBCException(e);
            }
        }
        return 0;
    }

    @Override
    public String getContentType()
    {
        return MimeTypes.OCTET_STREAM;
    }

    @Override
    public DBDContentStorage getContents(DBRProgressMonitor monitor)
        throws DBCException
    {
        if (storage == null && blob != null) {
            long contentLength = getContentLength();
            if (contentLength < DBeaverCore.getInstance().getGlobalPreferenceStore().getInt(PrefConstants.MEMORY_CONTENT_MAX_SIZE)) {
                try {
                    storage = BytesContentStorage.createFromStream(
                        blob.getBinaryStream(),
                        contentLength,
                        DBeaverCore.getInstance().getGlobalPreferenceStore().getString(PrefConstants.CONTENT_HEX_ENCODING));
                }
                catch (Exception e) {
                    throw new DBCException(e);
                }
            } else {
                // Create new local storage
                IFile tempFile;
                try {
                    tempFile = ContentUtils.createTempContentFile(monitor, "blob" + blob.hashCode());
                }
                catch (IOException e) {
                    throw new DBCException(e);
                }
                try {
                    ContentUtils.copyStreamToFile(monitor, blob.getBinaryStream(), contentLength, tempFile);
                } catch (Exception e) {
                    ContentUtils.deleteTempFile(monitor, tempFile);
                    throw new DBCException(e);
                }
                this.storage = new TemporaryContentStorage(tempFile);
            }
            // Free blob - we don't need it anymore
            try {
                blob.free();
            } catch (Throwable e) {
                log.debug(e);
            } finally {
                blob = null;
            }
        }
        return storage;
    }

    @Override
    public void release()
    {
        if (tmpStream != null) {
            ContentUtils.close(tmpStream);
            tmpStream = null;
        }
//        if (blob != null) {
//            try {
//                blob.free();
//            } catch (Exception e) {
//                log.warn(e);
//            }
//            blob = null;
//        }
        super.release();
    }

    @Override
    public void bindParameter(JDBCExecutionContext context, JDBCPreparedStatement preparedStatement, DBSTypedObject columnType, int paramIndex)
        throws DBCException
    {
        try {
            if (storage != null) {
                // Write new blob value
                tmpStream = storage.getContentStream();
                try {
                    preparedStatement.setBinaryStream(paramIndex, tmpStream);
                }
                catch (Throwable e) {
                    if (e instanceof SQLException) {
                        throw (SQLException)e;
                    } else {
                        try {
                            preparedStatement.setBinaryStream(paramIndex, tmpStream, storage.getContentLength());
                        }
                        catch (Throwable e1) {
                            if (e1 instanceof SQLException) {
                                throw (SQLException)e1;
                            } else {
                                preparedStatement.setBinaryStream(paramIndex, tmpStream, (int)storage.getContentLength());
                            }
                        }
                    }
                }
            } else {
                preparedStatement.setNull(paramIndex, java.sql.Types.BLOB);
            }
        }
        catch (SQLException e) {
            throw new DBCException(e);
        }
        catch (IOException e) {
            throw new DBCException(e);
        }
    }

    @Override
    public boolean isNull()
    {
        return blob == null && storage == null;
    }

    @Override
    protected JDBCContentLOB createNewContent()
    {
        return new JDBCContentBLOB(null);
    }

    @Override
    public String toString() {
        return blob == null && storage == null ? null : "[BLOB]";
    }

}
