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

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.data.DBDContent;
import org.jkiss.dbeaver.model.data.DBDContentStorage;
import org.jkiss.dbeaver.model.data.DBDValueCloneable;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * JDBCContentLOB
 *
 * @author Serge Rider
 */
public abstract class JDBCContentLOB extends JDBCContentAbstract implements DBDContent {

    private DBDContentStorage originalStorage;
    protected DBDContentStorage storage;

    protected JDBCContentLOB(DBPDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public long getContentLength() throws DBCException {
        if (storage != null) {
            return storage.getContentLength();
        }
        return getLOBLength();
    }

    protected abstract long getLOBLength() throws DBCException;

    @Override
    public boolean updateContents(
        DBRProgressMonitor monitor,
        DBDContentStorage storage)
        throws DBException
    {
        if (this.storage != null) {
            if (this.originalStorage != null) {
                this.originalStorage.release();
            }
            this.originalStorage = this.storage;
        }
        this.storage = storage;
        return true;
    }

    @Override
    public void release()
    {
        if (this.storage != null) {
            this.storage.release();
            this.storage = null;
        }
        if (this.originalStorage != null) {
            this.originalStorage.release();
            this.originalStorage = null;
        }
    }

    @Override
    public void resetContents()
    {
        if (this.originalStorage != null) {
            if (this.storage != null) {
                this.storage.release();
            }
            this.storage = this.originalStorage;
        }
    }

    @Override
    public DBDValueCloneable cloneValue(DBRProgressMonitor monitor)
        throws DBCException
    {
        JDBCContentLOB copy = createNewContent();
        DBDContentStorage storage = getContents(monitor);
        try {
            copy.updateContents(monitor, storage.cloneStorage(monitor));
        }
        catch (Exception e) {
            throw new DBCException(e);
        }
        return copy;
    }

    protected abstract JDBCContentLOB createNewContent();

}
