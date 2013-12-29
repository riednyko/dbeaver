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
import org.jkiss.dbeaver.ext.db2.model.dict.DB2YesNo;
import org.jkiss.dbeaver.model.DBPNamedObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * DB2 Storage Group
 * 
 * @author Denis Forveille
 */
public class DB2StorageGroup extends DB2GlobalObject implements DBPNamedObject {

    private String name;
    private Integer id;
    private String owner;
    private Timestamp createTime;
    private Boolean defautSG;
    private Double overhead;
    private Double deviceReadRate;
    private Double writeOverhead;
    private Double deviceWriteRate;
    private Integer dataTag;
    private String cachingTier;
    private String remarks;

    // -----------------------
    // Constructors
    // -----------------------

    public DB2StorageGroup(DB2DataSource db2DataSource, ResultSet dbResult) throws DBException
    {
        super(db2DataSource, dbResult != null);
        this.name = JDBCUtils.safeGetString(dbResult, "SGNAME");
        this.id = JDBCUtils.safeGetInteger(dbResult, "SGID");
        this.owner = JDBCUtils.safeGetString(dbResult, "OWNER");
        this.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
        this.defautSG = JDBCUtils.safeGetBoolean(dbResult, "DEFAULTSG", DB2YesNo.Y.name());
        this.overhead = JDBCUtils.safeGetDouble(dbResult, "OVERHEAD");
        this.deviceReadRate = JDBCUtils.safeGetDouble(dbResult, "DEVICEREADRATE");
        this.writeOverhead = JDBCUtils.safeGetDouble(dbResult, "WRITEOVERHEAD");
        this.deviceWriteRate = JDBCUtils.safeGetDouble(dbResult, "DEVICEWRITERATE");
        this.dataTag = JDBCUtils.safeGetInteger(dbResult, "DATATAG");
        this.remarks = JDBCUtils.safeGetString(dbResult, "REMARKS");

        // DF: it is declared "Integer" in infocenter but Varchar in the catalog...
        if (db2DataSource.isAtLeastV10_5()) {
            this.cachingTier = JDBCUtils.safeGetString(dbResult, "CACHINGTIER");
        }

    }

    // -----------------
    // Properties
    // -----------------

    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return name;
    }

    @Property(viewable = true, order = 2)
    public Integer getId()
    {
        return id;
    }

    @Property(viewable = true, order = 3)
    public Boolean getDefautSG()
    {
        return defautSG;
    }

    @Property(viewable = false, category = DB2Constants.CAT_OWNER)
    public String getOwner()
    {
        return owner;
    }

    @Property(viewable = false, category = DB2Constants.CAT_DATETIME)
    public Timestamp getCreateTime()
    {
        return createTime;
    }

    @Property(viewable = false)
    public String getCachingTier()
    {
        return cachingTier;
    }

    @Property(viewable = false)
    public Integer getDataTag()
    {
        return dataTag;
    }

    @Override
    @Property(viewable = false)
    public String getDescription()
    {
        return remarks;
    }

    @Property(viewable = false, order = 100, category = DB2Constants.CAT_PERFORMANCE)
    public Double getOverhead()
    {
        return overhead;
    }

    @Property(viewable = false, order = 101, category = DB2Constants.CAT_PERFORMANCE)
    public Double getDeviceReadRate()
    {
        return deviceReadRate;
    }

    @Property(viewable = false, order = 102, category = DB2Constants.CAT_PERFORMANCE)
    public Double getWriteOverhead()
    {
        return writeOverhead;
    }

    @Property(viewable = false, order = 103, category = DB2Constants.CAT_PERFORMANCE)
    public Double getDeviceWriteRate()
    {
        return deviceWriteRate;
    }

}
