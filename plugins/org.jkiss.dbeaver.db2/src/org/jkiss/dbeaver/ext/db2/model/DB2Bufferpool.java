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
package org.jkiss.dbeaver.ext.db2.model;

import java.sql.ResultSet;

import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

/**
 * DB2 Bufferpool
 * 
 * @author Denis Forveille
 * 
 */
public class DB2Bufferpool extends DB2GlobalObject {

   private String  name;
   private Integer id;
   private String  dbpgName;
   private Integer nPages;
   private Integer pageSize;
   private Integer estore;
   private Integer numBlockPages;
   private Integer blockSize;

   // -----------------
   // Constructors
   // -----------------

   public DB2Bufferpool(DB2DataSource db2DataSource, ResultSet dbResult) {
      super(db2DataSource, dbResult != null);

      this.name = JDBCUtils.safeGetString(dbResult, "BPNAME");
      this.id = JDBCUtils.safeGetInteger(dbResult, "BUFFERPOOLID");
      this.dbpgName = JDBCUtils.safeGetString(dbResult, "DBPGNAME");
      this.nPages = JDBCUtils.safeGetInteger(dbResult, "NPAGES");
      this.pageSize = JDBCUtils.safeGetInteger(dbResult, "PAGESIZE");
      this.estore = JDBCUtils.safeGetInteger(dbResult, "ESTORE");
      this.numBlockPages = JDBCUtils.safeGetInteger(dbResult, "NUMBLOCKPAGES");
      this.blockSize = JDBCUtils.safeGetInteger(dbResult, "BLOCKSIZE");
   }

   // -----------------
   // Properties
   // -----------------

   @Override
   @Property(viewable = true, editable = false, order = 1)
   public String getName() {
      return name;
   }

   @Property(viewable = true, editable = false, order = 2)
   public Integer getId() {
      return id;
   }

   @Property(viewable = true, editable = false, order = 3)
   public String getDbpgName() {
      return dbpgName;
   }

   @Property(viewable = true, editable = false, order = 4)
   public Integer getnPages() {
      return nPages;
   }

   @Property(viewable = true, editable = false, order = 5)
   public Integer getPageSize() {
      return pageSize;
   }

   @Property(viewable = true, editable = false, order = 6)
   public Integer getEstore() {
      return estore;
   }

   @Property(viewable = true, editable = false, order = 7)
   public Integer getNumBlockPages() {
      return numBlockPages;
   }

   @Property(viewable = true, editable = false, order = 8)
   public Integer getBlockSize() {
      return blockSize;
   }
}
