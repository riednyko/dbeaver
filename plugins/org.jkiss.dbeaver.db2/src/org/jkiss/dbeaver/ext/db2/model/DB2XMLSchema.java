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
package org.jkiss.dbeaver.ext.db2.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.DB2Constants;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2OwnerType;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2XSRDecomposition;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2XSRStatus;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2XSRType;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLXML;
import java.sql.Timestamp;

/**
 * DB2 XML Schema (XSR)
 * 
 * @author Denis Forveille
 */
public class DB2XMLSchema extends DB2SchemaObject implements DBPRefreshableObject {

    private Long id;
    private String targetNameSpace;
    private String schemaLocation;
    private SQLXML objectInfo;
    private DB2XSRType objectType;
    private String owner;
    private DB2OwnerType ownerType;
    private Timestamp createTime;
    private Timestamp alterTime;
    private DB2XSRStatus status;
    private DB2XSRDecomposition decomposition;
    private String remarks;

    // -----------------------
    // Constructors
    // -----------------------
    public DB2XMLSchema(DB2Schema schema, ResultSet dbResult)
    {
        super(schema, JDBCUtils.safeGetString(dbResult, "OBJECTNAME"), true);

        this.id = JDBCUtils.safeGetLong(dbResult, "OBJECTID");
        this.targetNameSpace = JDBCUtils.safeGetString(dbResult, "TARGETNAMESPACE");
        this.schemaLocation = JDBCUtils.safeGetString(dbResult, "SCHEMALOCATION");
        this.objectInfo = JDBCUtils.safeGetXML(dbResult, "OBJECTINFO");
        this.objectType = CommonUtils.valueOf(DB2XSRType.class, JDBCUtils.safeGetString(dbResult, "OBJECTTYPE"));
        this.owner = JDBCUtils.safeGetString(dbResult, "OWNER");
        this.ownerType = CommonUtils.valueOf(DB2OwnerType.class, JDBCUtils.safeGetString(dbResult, "OWNERTYPE"));
        this.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
        this.alterTime = JDBCUtils.safeGetTimestamp(dbResult, "ALTER_TIME");
        this.status = CommonUtils.valueOf(DB2XSRStatus.class, JDBCUtils.safeGetString(dbResult, "STATUS"));
        this.decomposition = CommonUtils.valueOf(DB2XSRDecomposition.class, JDBCUtils.safeGetString(dbResult, "DECOMPOSITION"));
        this.remarks = JDBCUtils.safeGetString(dbResult, "REMARKS");
    }

    // -----------------
    // Business contract
    // -----------------
    @Override
    public boolean refreshObject(DBRProgressMonitor monitor) throws DBException
    {
        return true;
    }

    // -----------------
    // Properties
    // -----------------

    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return super.getName();
    }

    @Property(viewable = true, order = 2)
    public DB2Schema getSchema()
    {
        return super.getSchema();
    }

    @Property(viewable = true, order = 3)
    public DB2XSRType getObjectType()
    {
        return objectType;
    }

    @Property(viewable = true, order = 4)
    public DB2XSRStatus getStatus()
    {
        return status;
    }

    @Property(viewable = true, order = 5)
    public DB2XSRDecomposition getDecomposition()
    {
        return decomposition;
    }

    @Property(viewable = false, order = 10)
    public Long getId()
    {
        return id;
    }

    @Property(viewable = false, order = 11)
    public String getTargetNameSpace()
    {
        return targetNameSpace;
    }

    @Property(viewable = false, order = 12)
    public String getSchemaLocation()
    {
        return schemaLocation;
    }

    @Property(viewable = false, order = 13)
    public SQLXML getObjectInfo()
    {
        return objectInfo;
    }

    @Override
    @Property(viewable = false, order = 20, updatable = true)
    public String getDescription()
    {
        return remarks;
    }

    @Property(viewable = false, order = 21, category = DB2Constants.CAT_OWNER)
    public String getOwner()
    {
        return owner;
    }

    @Property(viewable = false, order = 22, category = DB2Constants.CAT_OWNER)
    public DB2OwnerType getOwnerType()
    {
        return ownerType;
    }

    @Property(viewable = false, order = 23, category = DB2Constants.CAT_DATETIME)
    public Timestamp getCreateTime()
    {
        return createTime;
    }

    @Property(viewable = false, order = 24, category = DB2Constants.CAT_DATETIME)
    public Timestamp getAlterTime()
    {
        return alterTime;
    }

}
