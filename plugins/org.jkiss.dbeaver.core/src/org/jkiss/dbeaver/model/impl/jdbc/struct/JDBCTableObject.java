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
package org.jkiss.dbeaver.model.impl.jdbc.struct;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * JDBC abstract table object
 */
public abstract class JDBCTableObject<TABLE extends JDBCTable> implements DBSObject, DBPSaveableObject
{
    private final TABLE table;
    private String name;
    private boolean persisted;

    protected JDBCTableObject(TABLE table, String name, boolean persisted) {
        this.table = table;
        this.name = name;
        this.persisted = persisted;
    }

    protected JDBCTableObject(JDBCTableObject<TABLE> source)
    {
        this.table = source.table;
        this.name = source.name;
        this.persisted = source.persisted;
    }

    @Override
    public TABLE getParentObject()
    {
        return table;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String indexName)
    {
        this.name = indexName;
    }

    @Property(viewable = true, order = 2)
    public TABLE getTable()
    {
        return table;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public boolean isPersisted()
    {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted)
    {
        this.persisted = persisted;
    }

}
