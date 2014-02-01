/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2014 Serge Rieder serge@jkiss.org
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
import org.jkiss.dbeaver.ext.db2.DB2Constants;
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;

/**
 * DB2 Authorisations on Schemas
 * 
 * @author Denis Forveille
 */
public class DB2AuthSchema extends DB2AuthBase {

    private DB2AuthHeldType alterIn;
    private DB2AuthHeldType createIn;
    private DB2AuthHeldType dropIn;

    // -----------------------
    // Constructors
    // -----------------------
    public DB2AuthSchema(DBRProgressMonitor monitor, DB2Grantee db2Grantee, DB2Schema db2Schema, ResultSet resultSet)
        throws DBException
    {
        super(monitor, db2Grantee, db2Schema, resultSet);

        this.alterIn = CommonUtils.valueOf(DB2AuthHeldType.class, JDBCUtils.safeGetString(resultSet, "ALTERINAUTH"));
        this.createIn = CommonUtils.valueOf(DB2AuthHeldType.class, JDBCUtils.safeGetString(resultSet, "CREATEINAUTH"));
        this.dropIn = CommonUtils.valueOf(DB2AuthHeldType.class, JDBCUtils.safeGetString(resultSet, "DROPINAUTH"));
    }

    // -----------------
    // Properties
    // -----------------
    @Property(viewable = true, order = 2)
    public DBSObject getObject()
    {
        return super.getObject();
    }

    @Override
    @Property(hidden = true)
    public DB2Schema getObjectSchema()
    {
        return super.getObjectSchema();
    }

    @Property(viewable = true, order = 20, category = DB2Constants.CAT_AUTH)
    public DB2AuthHeldType getAlterIn()
    {
        return alterIn;
    }

    @Property(viewable = true, order = 21, category = DB2Constants.CAT_AUTH)
    public DB2AuthHeldType getCreateIn()
    {
        return createIn;
    }

    @Property(viewable = true, order = 22, category = DB2Constants.CAT_AUTH)
    public DB2AuthHeldType getDropIn()
    {
        return dropIn;
    }

}
