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

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.DB2Constants;
import org.jkiss.dbeaver.ext.db2.DB2Utils;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2TableStatus;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2TableType;
import org.jkiss.dbeaver.ext.db2.model.source.DB2StatefulObject;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCStructCache;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObjectState;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKey;
import org.jkiss.utils.CommonUtils;

/**
 * DB2 Table
 * 
 * @author Denis Forveille
 * 
 */
public class DB2Table extends DB2TableBase implements DBPNamedObject2, DBPRefreshableObject, DB2StatefulObject {

   private static final Log log = LogFactory.getLog(DB2Table.class);

   private DB2TableStatus   status;
   private DB2TableType     type;
   private Timestamp        createTime;
   private Timestamp        alterTime;
   private Timestamp        invalidateTime;

   private String           tablespace;                             // TODO DF: replace with real tablespace
   private String           indexTablespace;
   private String           longTablespace;

   private Timestamp        statsTime;
   private Long             card;
   private Long             nPages;
   private Long             fPages;
   private Long             overFLow;

   // -----------------
   // Constructors
   // -----------------
   public DB2Table(DBRProgressMonitor monitor, DB2Schema schema, ResultSet dbResult) {
      super(monitor, schema, dbResult);

      setName(JDBCUtils.safeGetString(dbResult, "TABNAME"));

      this.status = CommonUtils.valueOf(DB2TableStatus.class, JDBCUtils.safeGetString(dbResult, "STATUS"));
      this.type = CommonUtils.valueOf(DB2TableType.class, JDBCUtils.safeGetString(dbResult, "TYPE"));
      this.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
      this.alterTime = JDBCUtils.safeGetTimestamp(dbResult, "ALTER_TIME");
      this.invalidateTime = JDBCUtils.safeGetTimestamp(dbResult, "INVALIDATE_TIME");
      this.statsTime = JDBCUtils.safeGetTimestamp(dbResult, "STATS_TIME");

      this.tablespace = JDBCUtils.safeGetString(dbResult, "TBSPACE");
      this.indexTablespace = JDBCUtils.safeGetString(dbResult, "INDEX_TBSPACE");
      this.longTablespace = JDBCUtils.safeGetString(dbResult, "LONG_TBSPACE");

      this.card = JDBCUtils.safeGetLongNullable(dbResult, "CARD");
      this.nPages = JDBCUtils.safeGetLongNullable(dbResult, "NPAGES");
      this.fPages = JDBCUtils.safeGetLongNullable(dbResult, "FPAGES");
      this.overFLow = JDBCUtils.safeGetLongNullable(dbResult, "OVERFLOW");
   }

   @Override
   public boolean isView() {
      return false;
   }

   @Override
   public DBSObjectState getObjectState() {
      return status.getState();
   }

   @Override
   public JDBCStructCache<DB2Schema, DB2Table, DB2TableColumn> getCache() {
      return getContainer().getTableCache();
   }

   @Override
   public boolean refreshObject(DBRProgressMonitor monitor) throws DBException {
      getContainer().getTableCache().clearChildrenCache(this);

      getContainer().getIndexCache().clearObjectCache(this);
      getContainer().getConstraintCache().clearObjectCache(this);
      getContainer().getAssociationCache().clearObjectCache(this);
      getContainer().getReferenceCache().clearObjectCache(this);
      getContainer().getTriggerCache().clearObjectCache(this);

      return true;
   }

   @Override
   public void refreshObjectState(DBRProgressMonitor monitor) throws DBCException {
      // TODO DF: What to do here?
   }

   public String getDDL(DBRProgressMonitor monitor) throws DBException {
      // TODO DF: How to get line separator ?
      return DB2Utils.generateDDLforTable(monitor, ";", getDataSource(), this);
   }

   // -----------------
   // Columns
   // -----------------

   @Override
   public Collection<DB2TableColumn> getAttributes(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getTableCache().getChildren(monitor, getContainer(), this);
   }

   @Override
   public DB2TableColumn getAttribute(DBRProgressMonitor monitor, String attributeName) throws DBException {
      return getContainer().getTableCache().getChild(monitor, getContainer(), this, attributeName);
   }

   // -----------------
   // Associations
   // -----------------

   @Override
   @Association
   public Collection<DB2Index> getIndexes(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getIndexCache().getObjects(monitor, getContainer(), this);
   }

   @Override
   @Association
   public Collection<DB2TableUniqueKey> getConstraints(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getConstraintCache().getObjects(monitor, getContainer(), this);
   }

   public DB2TableUniqueKey getConstraint(DBRProgressMonitor monitor, String ukName) throws DBException {
      return getContainer().getConstraintCache().getObject(monitor, getContainer(), this, ukName);
   }

   @Override
   @Association
   public Collection<DB2TableForeignKey> getAssociations(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getAssociationCache().getObjects(monitor, getContainer(), this);
   }

   public DBSTableForeignKey getAssociation(DBRProgressMonitor monitor, String ukName) throws DBException {
      return getContainer().getAssociationCache().getObject(monitor, getContainer(), this, ukName);
   }

   @Override
   @Association
   public Collection<DB2TableReference> getReferences(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getReferenceCache().getObjects(monitor, getContainer(), this);
   }

   public DBSTableForeignKey getReference(DBRProgressMonitor monitor, String ukName) throws DBException {
      return getContainer().getReferenceCache().getObject(monitor, getContainer(), this, ukName);
   }

   @Association
   public Collection<DB2Trigger> getTriggers(DBRProgressMonitor monitor) throws DBException {
      return getContainer().getTriggerCache().getObjects(monitor, getContainer(), this);
   }

   // -----------------
   // Properties
   // -----------------

   @Override
   @Property(viewable = true, editable = false, valueTransformer = DBObjectNameCaseTransformer.class, order = 1)
   public String getName() {
      return super.getName();
   }

   @Override
   @Property(viewable = true, editable = false, order = 2)
   public DB2Schema getSchema() {
      return super.getContainer();
   }

   @Property(viewable = true, editable = false, order = 3)
   public DB2TableStatus getStatus() {
      return status;
   }

   @Property(viewable = true, editable = false, order = 4)
   public String getTypeDescription() {
      return type.getDescription();
   }

   @Property(viewable = true, editable = false, category = DB2Constants.CAT_TABLESPACE)
   public String getTablespace() {
      return tablespace;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_TABLESPACE)
   public String getIndexTablespace() {
      return indexTablespace;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_TABLESPACE)
   public String getLongTablespace() {
      return longTablespace;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_DATETIME)
   public Timestamp getCreateTime() {
      return createTime;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_DATETIME)
   public Timestamp getAlterTime() {
      return alterTime;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_DATETIME)
   public Timestamp getInvalidateTime() {
      return invalidateTime;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_STATS)
   public Timestamp getStatsTime() {
      return statsTime;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_STATS)
   public Long getCard() {
      return card;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_STATS)
   public Long getnPages() {
      return nPages;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_STATS)
   public Long getfPages() {
      return fPages;
   }

   @Property(viewable = false, editable = false, category = DB2Constants.CAT_STATS)
   public Long getOverFLow() {
      return overFLow;
   }

   // --------------
   // Static Helpers
   // --------------
   public static DB2Table findTable(DBRProgressMonitor monitor, DB2Schema schema, String ownerName, String tableName) throws DBException {

      if (schema == null) {
         log.debug("Referenced schema '" + ownerName + "' not found");
         return null;
      }

      DB2Table table = schema.getTableCache().getObject(monitor, schema, tableName);
      if (table == null) {
         log.debug("Table '" + tableName + "' not found in schema '" + ownerName + "'");
      }
      return table;
   }

   public static DB2TableColumn findTableColumn(DBRProgressMonitor monitor, DB2Table parent, String columnName) throws DBException {
      DB2TableColumn tableColumn = parent.getAttribute(monitor, columnName);
      if (tableColumn == null) {
         log.debug("Column '" + columnName + "' not found in table '" + parent.getName() + "'");
      }
      return tableColumn;
   }

}
