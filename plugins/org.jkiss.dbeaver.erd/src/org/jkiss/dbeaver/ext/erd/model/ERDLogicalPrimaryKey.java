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
package org.jkiss.dbeaver.ext.erd.model;

import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableConstraintColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Logical primary key
 */
public class ERDLogicalPrimaryKey implements DBSEntityConstraint,DBSEntityReferrer {

    private DBSEntity entity;
    private String name;
    private String description;
    private List<? extends DBSTableConstraintColumn> columns = new ArrayList<DBSTableConstraintColumn>();

    public ERDLogicalPrimaryKey(ERDEntity entity, String name, String description)
    {
        this.entity = entity.getObject();
        this.name = name;
        this.description = description;
    }

    @Override
    public DBPDataSource getDataSource()
    {
        return entity.getDataSource();
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public DBSEntity getParentObject()
    {
        return entity;
    }

    @Override
    public DBSEntityConstraintType getConstraintType()
    {
        return DBSEntityConstraintType.PRIMARY_KEY;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isPersisted()
    {
        return false;
    }

    @Override
    public Collection<? extends DBSEntityAttributeRef> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }
}
