/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.model.security;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.ext.db2.model.DB2TableBase;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;

/**
 * DB2 Authorisations on Tables
 * 
 * @author Denis Forveille
 */
public class DB2AuthTable extends DB2AuthTableBase {

    // -----------------------
    // Constructors
    // -----------------------

    public DB2AuthTable(DBRProgressMonitor monitor, DB2Grantee db2Grantee, DB2TableBase db2TableBase, ResultSet resultSet)
        throws DBException
    {
        super(monitor, db2Grantee, db2TableBase, resultSet);
    }

    // -----------------
    // Properties
    // -----------------

    @Property(viewable = true, order = 1)
    public DB2Schema getObjectSchema()
    {
        return super.getObjectSchema();
    }

    @Property(viewable = true, order = 2)
    public DBSObject getObject()
    {
        return super.getObject();
    }
}
