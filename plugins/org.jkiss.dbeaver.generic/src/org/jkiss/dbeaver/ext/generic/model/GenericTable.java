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
package org.jkiss.dbeaver.ext.generic.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTable;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyDefferability;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.utils.CommonUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * GenericTable
 */
public class GenericTable extends JDBCTable<GenericDataSource, GenericStructContainer> implements DBPRefreshableObject, DBPSystemObject
{
    static final Log log = LogFactory.getLog(GenericTable.class);

    private String tableType;
    private boolean isView;
    private boolean isSystem;
    private String description;
    private Long rowCount;

    public GenericTable(
        GenericStructContainer container)
    {
        this(container, null, null, null, false);
    }

    public GenericTable(
        GenericStructContainer container,
        String tableName,
        String tableType,
        String remarks,
        boolean persisted)
    {
        super(container, tableName, persisted);
        this.tableType = tableType;
        this.description = remarks;
        if (!CommonUtils.isEmpty(this.getTableType())) {
            String type = this.getTableType().toUpperCase();
            this.isView = (type.contains("VIEW"));
            this.isSystem =
                (type.contains("SYSTEM")) || // general rule
                tableName.contains("RDB$");    // [Firebird]
        }
    }

    @Override
    public TableCache getCache()
    {
        return getContainer().getTableCache();
    }

    @Override
    public DBSObject getParentObject()
    {
        return getContainer().getObject();
    }

    @Override
    public String getFullQualifiedName()
    {
/*
        String ownerName = null, catalogName = null;
        if (getSchema() != null) {
            ownerName = getSchema().getName();
        } else if (getCatalog() != null) {
            ownerName = getCatalog().getName();
        }
        if (getSchema() != null && getCatalog() != null && getDataSource().getCatalogs().size() > 1) {
            // Use catalog name only if there are multiple catalogs
            catalogName = getCatalog().getName();
        }
*/
        return DBUtils.getFullQualifiedName(getDataSource(), getCatalog(), getSchema(), this);
    }

    @Override
    public boolean isView()
    {
        return this.isView;
    }

    @Override
    public boolean isSystem()
    {
        return this.isSystem;
    }

    @Property(viewable = true, order = 2)
    public String getTableType()
    {
        return tableType;
    }

    @Property(viewable = true, order = 3)
    public GenericCatalog getCatalog()
    {
        return getContainer().getCatalog();
    }

    @Property(viewable = true, order = 4)
    public GenericSchema getSchema()
    {
        return getContainer().getSchema();
    }

    @Override
    public synchronized Collection<GenericTableColumn> getAttributes(DBRProgressMonitor monitor)
        throws DBException
    {
        return this.getContainer().getTableCache().getChildren(monitor, getContainer(), this);
    }

    @Override
    public GenericTableColumn getAttribute(DBRProgressMonitor monitor, String attributeName)
        throws DBException
    {
        return this.getContainer().getTableCache().getChild(monitor, getContainer(), this, attributeName);
    }

    @Override
    public synchronized Collection<GenericTableIndex> getIndexes(DBRProgressMonitor monitor)
        throws DBException
    {
        // Read indexes using cache
        return this.getContainer().getIndexCache().getObjects(monitor, getContainer(), this);
    }

    @Override
    public synchronized Collection<GenericPrimaryKey> getConstraints(DBRProgressMonitor monitor)
        throws DBException
    {
        // ensure all columns are already cached
        getAttributes(monitor);
        return getContainer().getPrimaryKeysCache().getObjects(monitor, getContainer(), this);
    }

    synchronized void addUniqueKey(GenericPrimaryKey constraint) {
        getContainer().getPrimaryKeysCache().cacheObject(constraint);
    }

    @Override
    public Collection<GenericTableForeignKey> getReferences(DBRProgressMonitor monitor)
        throws DBException
    {
        return loadReferences(monitor);
    }

    @Override
    public synchronized Collection<GenericTableForeignKey> getAssociations(DBRProgressMonitor monitor)
        throws DBException
    {
        if (getDataSource().getInfo().supportsReferentialIntegrity()) {
            return getContainer().getForeignKeysCache().getObjects(monitor, getContainer(), this);
        }
        return null;
    }

    public Collection<GenericTable> getSubTables()
    {
        return null;
    }

    @Override
    @Property(viewable = true, order = 100)
    public String getDescription()
    {
        return description;
    }

    @Override
    public synchronized boolean refreshObject(DBRProgressMonitor monitor) throws DBException
    {
        this.getContainer().getTableCache().clearChildrenCache(this);
        this.getContainer().getIndexCache().clearObjectCache(this);
        this.getContainer().getPrimaryKeysCache().clearObjectCache(this);
        this.getContainer().getForeignKeysCache().clearObjectCache(this);
        rowCount = null;
        return true;
    }

    // Comment row count calculation - it works too long and takes a lot of resources without serious reason
    @Property(viewable = true, expensive = true, order = 5)
    public synchronized Long getRowCount(DBRProgressMonitor monitor)
    {
        if (rowCount != null) {
            return rowCount;
        }
        if (isView() || !isPersisted()) {
            // Do not count rows for views
            return null;
        }

//        if (indexes != null) {
//            rowCount = getRowCountFromIndexes(monitor);
//        }

        if (rowCount == null) {
            // Query row count
            JDBCExecutionContext context = getDataSource().openContext(monitor, DBCExecutionPurpose.META, "Read row count");
            try {
                JDBCPreparedStatement dbStat = context.prepareStatement(
                    "SELECT COUNT(*) FROM " + getFullQualifiedName());
                try {
    //                dbStat.setQueryTimeout(3);
                    JDBCResultSet resultSet = dbStat.executeQuery();
                    try {
                        resultSet.next();
                        rowCount = resultSet.getLong(1);
                    }
                    finally {
                        resultSet.close();
                    }
                }
                finally {
                    dbStat.close();
                }
            }
            catch (SQLException e) {
                //throw new DBCException(e);
                // do not throw this error - row count is optional info and some providers may fail
                log.debug("Can't fetch row count: " + e.getMessage());
//                if (indexes != null) {
//                    rowCount = getRowCountFromIndexes(monitor);
//                }
            }
            finally {
                context.close();
            }
        }
        if (rowCount == null) {
            rowCount = -1L;
        }

        return rowCount;
    }

    public Long getRowCountFromIndexes(DBRProgressMonitor monitor)
    {
        try {
            // Try to get cardinality from some unique index
            // Cardinality
            final Collection<GenericTableIndex> indexList = getIndexes(monitor);
            if (!CommonUtils.isEmpty(indexList)) {
                for (GenericTableIndex index : indexList) {
                    if (index.isUnique() || index.getIndexType() == DBSIndexType.STATISTIC) {
                        final long cardinality = index.getCardinality();
                        if (cardinality > 0) {
                            return cardinality;
                        }
                    }
                }
            }
        } catch (DBException e) {
            log.error(e);
        }
        return null;
    }

    private static class ForeignKeyInfo {
        String pkColumnName;
        String fkTableCatalog;
        String fkTableSchema;
        String fkTableName;
        String fkColumnName;
        int keySeq;
        int updateRuleNum;
        int deleteRuleNum;
        String fkName;
        String pkName;
        int defferabilityNum;
    }

    private synchronized List<GenericTableForeignKey> loadReferences(DBRProgressMonitor monitor)
        throws DBException
    {
        if (!isPersisted() || !getDataSource().getInfo().supportsReferentialIntegrity()) {
            return new ArrayList<GenericTableForeignKey>();
        }
        JDBCExecutionContext context = getDataSource().openContext(monitor, DBCExecutionPurpose.META, "Load table relations");
        try {
            // Read foreign keys in two passes
            // First read entire resultset to prevent recursive metadata requests
            // some drivers don't like it
            List<ForeignKeyInfo> fkInfos = new ArrayList<ForeignKeyInfo>();
            JDBCDatabaseMetaData metaData = context.getMetaData();
            // Load indexes
            JDBCResultSet dbResult = metaData.getExportedKeys(
                    getCatalog() == null ? null : getCatalog().getName(),
                    getSchema() == null ? null : getSchema().getName(),
                    getName());
            try {
                while (dbResult.next()) {
                    ForeignKeyInfo fkInfo = new ForeignKeyInfo();
                    fkInfo.pkColumnName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKCOLUMN_NAME);
                    fkInfo.fkTableCatalog = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FKTABLE_CAT);
                    fkInfo.fkTableSchema = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FKTABLE_SCHEM);
                    fkInfo.fkTableName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FKTABLE_NAME);
                    fkInfo.fkColumnName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FKCOLUMN_NAME);
                    fkInfo.keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);
                    fkInfo.updateRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.UPDATE_RULE);
                    fkInfo.deleteRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DELETE_RULE);
                    fkInfo.fkName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FK_NAME);
                    fkInfo.pkName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PK_NAME);
                    fkInfo.defferabilityNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DEFERRABILITY);
                    fkInfos.add(fkInfo);
                }
            }
            finally {
                dbResult.close();
            }

            List<GenericTableForeignKey> fkList = new ArrayList<GenericTableForeignKey>();
            Map<String, GenericTableForeignKey> fkMap = new HashMap<String, GenericTableForeignKey>();
            for (ForeignKeyInfo info : fkInfos) {
                DBSForeignKeyModifyRule deleteRule = JDBCUtils.getCascadeFromNum(info.deleteRuleNum);
                DBSForeignKeyModifyRule updateRule = JDBCUtils.getCascadeFromNum(info.updateRuleNum);
                DBSForeignKeyDefferability defferability;
                switch (info.defferabilityNum) {
                    case DatabaseMetaData.importedKeyInitiallyDeferred: defferability = DBSForeignKeyDefferability.INITIALLY_DEFERRED; break;
                    case DatabaseMetaData.importedKeyInitiallyImmediate: defferability = DBSForeignKeyDefferability.INITIALLY_IMMEDIATE; break;
                    case DatabaseMetaData.importedKeyNotDeferrable: defferability = DBSForeignKeyDefferability.NOT_DEFERRABLE; break;
                    default: defferability = DBSForeignKeyDefferability.UNKNOWN; break;
                }

                if (info.fkTableName == null) {
                    log.debug("Null FK table name");
                    continue;
                }
                //String fkTableFullName = DBUtils.getFullQualifiedName(getDataSource(), info.fkTableCatalog, info.fkTableSchema, info.fkTableName);
                GenericTable fkTable = getDataSource().findTable(monitor, info.fkTableCatalog, info.fkTableSchema, info.fkTableName);
                if (fkTable == null) {
                    log.warn("Can't find FK table " + info.fkTableName);
                    continue;
                }
                GenericTableColumn pkColumn = this.getAttribute(monitor, info.pkColumnName);
                if (pkColumn == null) {
                    log.warn("Can't find PK column " + info.pkColumnName);
                    continue;
                }
                GenericTableColumn fkColumn = fkTable.getAttribute(monitor, info.fkColumnName);
                if (fkColumn == null) {
                    log.warn("Can't find FK table " + fkTable.getFullQualifiedName() + " column " + info.fkColumnName);
                    continue;
                }

                // Find PK
                GenericPrimaryKey pk = null;
                if (info.pkName != null) {
                    pk = DBUtils.findObject(this.getConstraints(monitor), info.pkName);
                    if (pk == null) {
                        log.warn("Unique key '" + info.pkName + "' not found in table " + this.getFullQualifiedName());
                    }
                }
                if (pk == null) {
                    Collection<GenericPrimaryKey> uniqueKeys = this.getConstraints(monitor);
                    if (uniqueKeys != null) {
                        for (GenericPrimaryKey pkConstraint : uniqueKeys) {
                            if (pkConstraint.getConstraintType().isUnique() && DBUtils.getConstraintColumn(monitor, pkConstraint, pkColumn) != null) {
                                pk = pkConstraint;
                                break;
                            }
                        }
                    }
                }
                if (pk == null) {
                    log.warn("Could not find unique key for table " + this.getFullQualifiedName() + " column " + pkColumn.getName());
                    // Too bad. But we have to create new fake PK for this FK
                    //String pkFullName = getFullQualifiedName() + "." + info.pkName;
                    pk = new GenericPrimaryKey(this, info.pkName, null, DBSEntityConstraintType.PRIMARY_KEY, true);
                    pk.addColumn(new GenericTableConstraintColumn(pk, pkColumn, info.keySeq));
                    // Add this fake constraint to it's owner
                    this.addUniqueKey(pk);
                }

                // Find (or create) FK
                GenericTableForeignKey fk = DBUtils.findObject(fkTable.getAssociations(monitor), info.fkName);
                if (fk == null) {
                    log.warn("Could not find foreign key '" + info.fkName + "' for table " + fkTable.getFullQualifiedName());
                    // No choice, we have to create fake foreign key :(
                } else {
                    if (!fkList.contains(fk)) {
                        fkList.add(fk);
                    }
                }

                if (fk == null) {
                    fk = fkMap.get(info.fkName);
                    if (fk == null) {
                        fk = new GenericTableForeignKey(fkTable, info.fkName, null, pk, deleteRule, updateRule, defferability, true);
                        fkMap.put(info.fkName, fk);
                        fkList.add(fk);
                    }
                    GenericTableForeignKeyColumnTable fkColumnInfo = new GenericTableForeignKeyColumnTable(fk, fkColumn, info.keySeq, pkColumn);
                    fk.addColumn(fkColumnInfo);
                }
            }

            return fkList;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
        finally {
            context.close();
        }
    }

}
