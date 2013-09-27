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
package org.jkiss.dbeaver.ext.db2.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.ext.db2.model.DB2Table;
import org.jkiss.dbeaver.ext.db2.model.DB2View;
import org.jkiss.dbeaver.ext.db2.model.dict.DB2TableType;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractObjectReference;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectReference;
import org.jkiss.dbeaver.model.struct.DBSObjectType;
import org.jkiss.dbeaver.model.struct.DBSStructureAssistant;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DB2 Structure Assistant
 * 
 * @author Denis Forveille
 */
public class DB2StructureAssistant implements DBSStructureAssistant {
    private static final Log LOG = LogFactory.getLog(DB2StructureAssistant.class);

    // TODO DF: Work in progess
    // For now only support Search/Autocomplete on Aliases, Tables, Views and Nicknames

    private static final DBSObjectType[] SUPP_OBJ_TYPES = { DB2ObjectType.ALIAS, DB2ObjectType.TABLE, DB2ObjectType.VIEW,
        DB2ObjectType.NICKNAME, DB2ObjectType.COLUMN, };

    private static final DBSObjectType[] HYPER_LINKS_TYPES = { DB2ObjectType.ALIAS, DB2ObjectType.TABLE, DB2ObjectType.VIEW,
        DB2ObjectType.NICKNAME, };
    private static final DBSObjectType[] AUTOC_OBJ_TYPES = { DB2ObjectType.ALIAS, DB2ObjectType.TABLE, DB2ObjectType.VIEW,
        DB2ObjectType.NICKNAME, };

    private static String SQL_TABLES_ALL;
    private static String SQL_TABLES_SCHEMA;
    private static String SQL_COLS_ALL;
    private static String SQL_COLS_SCHEMA;

    private final DB2DataSource dataSource;

    // -----------------
    // Constructors
    // -----------------
    public DB2StructureAssistant(DB2DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    // -----------------
    // Method Interface
    // -----------------

    @Override
    public DBSObjectType[] getSupportedObjectTypes()
    {
        return SUPP_OBJ_TYPES;
    }

    @Override
    public DBSObjectType[] getHyperlinkObjectTypes()
    {
        return HYPER_LINKS_TYPES;
    }

    @Override
    public DBSObjectType[] getAutoCompleteObjectTypes()
    {
        return AUTOC_OBJ_TYPES;
    }

    @Override
    public Collection<DBSObjectReference> findObjectsByMask(DBRProgressMonitor monitor, DBSObject parentObject,
        DBSObjectType[] objectTypes, String objectNameMask, boolean caseSensitive, int maxResults) throws DBException
    {

        List<DB2ObjectType> db2ObjectTypes = new ArrayList<DB2ObjectType>(objectTypes.length);
        for (DBSObjectType dbsObjectType : objectTypes) {
            db2ObjectTypes.add((DB2ObjectType) dbsObjectType);
        }

        DB2Schema schema = parentObject instanceof DB2Schema ? (DB2Schema) parentObject : null;

        JDBCExecutionContext context = dataSource.openContext(monitor, DBCExecutionPurpose.META, "Find objects by name");
        try {
            return searchAllObjects(context, schema, objectNameMask, db2ObjectTypes, caseSensitive, maxResults);
        } catch (SQLException ex) {
            throw new DBException(ex);
        } finally {
            context.close();
        }
    }

    // -----------------
    // Helpers
    // -----------------

    private List<DBSObjectReference> searchAllObjects(final JDBCExecutionContext context, final DB2Schema schema,
        String objectNameMask, List<DB2ObjectType> db2ObjectTypes, boolean caseSensitive, int maxResults) throws SQLException,
        DBException
    {

        List<DBSObjectReference> objects = new ArrayList<DBSObjectReference>();

        String searchObjectNameMask = objectNameMask;
        if (!caseSensitive) {
            searchObjectNameMask = searchObjectNameMask.toUpperCase();
        }

        int nbResults = 0;

        // Tables, Alias, Views, NicsearchObjectNameMaskknames
        if ((db2ObjectTypes.contains(DB2ObjectType.ALIAS)) || (db2ObjectTypes.contains(DB2ObjectType.TABLE))
            || (db2ObjectTypes.contains(DB2ObjectType.NICKNAME)) || (db2ObjectTypes.contains(DB2ObjectType.VIEW))) {
            searchTables(context, schema, searchObjectNameMask, db2ObjectTypes, maxResults, objects, nbResults);

            if (nbResults >= maxResults) {
                return objects;
            }
        }

        // Columns
        if (db2ObjectTypes.contains(DB2ObjectType.COLUMN)) {
            searchColumns(context, schema, searchObjectNameMask, db2ObjectTypes, maxResults, objects, nbResults);
        }

        return objects;
    }

    // --------------
    // Helper Classes
    // --------------

    private void searchTables(JDBCExecutionContext context, DB2Schema schema, String searchObjectNameMask,
        List<DB2ObjectType> db2ObjectTypes, int maxResults, List<DBSObjectReference> objects, int nbResults) throws SQLException,
        DBException
    {
        String baseSQL;
        if (schema != null) {
            baseSQL = SQL_TABLES_SCHEMA;
        } else {
            baseSQL = SQL_TABLES_ALL;
        }

        String sql = buildTableSQL(baseSQL, db2ObjectTypes);

        int n = 1;
        JDBCPreparedStatement dbStat = context.prepareStatement(sql);
        try {
            if (schema != null) {
                dbStat.setString(n++, schema.getName());
            }
            dbStat.setString(n++, searchObjectNameMask);

            dbStat.setFetchSize(DBConstants.METADATA_FETCH_SIZE);

            String schemaName;
            String objectName;
            DB2Schema db2Schema;
            DB2TableType tableType;
            DB2ObjectType objectType;

            JDBCResultSet dbResult = dbStat.executeQuery();
            try {
                while (dbResult.next()) {
                    if (context.getProgressMonitor().isCanceled()) {
                        break;
                    }

                    if (nbResults++ >= maxResults) {
                        return;
                    }

                    schemaName = JDBCUtils.safeGetStringTrimmed(dbResult, "TABSCHEMA");
                    objectName = JDBCUtils.safeGetString(dbResult, "TABNAME");
                    tableType = CommonUtils.valueOf(DB2TableType.class, JDBCUtils.safeGetString(dbResult, "TYPE"));

                    db2Schema = dataSource.getSchema(context.getProgressMonitor(), schemaName);
                    if (db2Schema == null) {
                        LOG.debug("Schema '" + schemaName + "' not found. Probably was filtered");
                        continue;
                    }

                    objectType = tableType.getDb2ObjectType();
                    objects.add(new DB2ObjectReference(objectName, db2Schema, objectType));
                }
            } finally {
                dbResult.close();
            }
        } finally {
            dbStat.close();
        }
    }

    private void searchColumns(JDBCExecutionContext context, DB2Schema schema, String searchObjectNameMask,
        List<DB2ObjectType> objectTypes, int maxResults, List<DBSObjectReference> objects, int nbResults) throws SQLException,
        DBException
    {
        String sql;
        if (schema != null) {
            sql = SQL_COLS_SCHEMA;
        } else {
            sql = SQL_COLS_ALL;
        }

        int n = 1;
        JDBCPreparedStatement dbStat = context.prepareStatement(sql);
        try {
            if (schema != null) {
                dbStat.setString(n++, schema.getName());
            }
            dbStat.setString(n++, searchObjectNameMask);

            dbStat.setFetchSize(DBConstants.METADATA_FETCH_SIZE);

            String tableSchemaName;
            String tableOrViewName;
            String columnName;
            DB2Schema db2Schema;
            DB2Table db2Table;
            DB2View db2View;

            JDBCResultSet dbResult = dbStat.executeQuery();
            try {
                while (dbResult.next()) {
                    if (context.getProgressMonitor().isCanceled()) {
                        break;
                    }

                    if (nbResults++ >= maxResults) {
                        return;
                    }

                    tableSchemaName = JDBCUtils.safeGetStringTrimmed(dbResult, "TABSCHEMA");
                    tableOrViewName = JDBCUtils.safeGetString(dbResult, "TABNAME");
                    columnName = JDBCUtils.safeGetString(dbResult, "COLNAME");

                    db2Schema = dataSource.getSchema(context.getProgressMonitor(), tableSchemaName);
                    if (db2Schema == null) {
                        LOG.debug("Schema '" + tableSchemaName + "' not found. Probably was filtered");
                        continue;
                    }
                    // Try with table, then view
                    db2Table = db2Schema.getTable(context.getProgressMonitor(), tableOrViewName);
                    if (db2Table != null) {
                        objects.add(new DB2ObjectReference(columnName, db2Table, DB2ObjectType.COLUMN));
                    } else {
                        db2View = db2Schema.getView(context.getProgressMonitor(), tableOrViewName);
                        if (db2View != null) {
                            objects.add(new DB2ObjectReference(columnName, db2View, DB2ObjectType.COLUMN));
                        }
                    }

                }
            } finally {
                dbResult.close();
            }
        } finally {
            dbStat.close();
        }
    }

    private class DB2ObjectReference extends AbstractObjectReference {

        private DB2ObjectReference(String objectName, DB2Schema db2Schema, DB2ObjectType objectType)
        {
            super(objectName, db2Schema, null, objectType);
        }

        private DB2ObjectReference(String objectName, DB2Table db2Table, DB2ObjectType objectType)
        {
            super(objectName, db2Table, null, objectType);
        }

        private DB2ObjectReference(String objectName, DB2View db2View, DB2ObjectType objectType)
        {
            super(objectName, db2View, null, objectType);
        }

        @Override
        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException
        {

            DB2ObjectType db2ObjectType = (DB2ObjectType) getObjectType();

            if (getContainer() instanceof DB2Schema) {
                DB2Schema db2Schema = (DB2Schema) getContainer();

                DBSObject object = db2ObjectType.findObject(monitor, db2Schema, getName());
                if (object == null) {
                    throw new DBException(db2ObjectType + " '" + getName() + "' not found in schema '" + db2Schema.getName() + "'");
                }
                return object;
            }
            if (getContainer() instanceof DB2Table) {
                DB2Table db2Table = (DB2Table) getContainer();

                DBSObject object = db2ObjectType.findObject(monitor, db2Table, getName());
                if (object == null) {
                    throw new DBException(db2ObjectType + " '" + getName() + "' not found in table '" + db2Table.getName() + "'");
                }
                return object;
            }
            if (getContainer() instanceof DB2View) {
                DB2View db2View = (DB2View) getContainer();

                DBSObject object = db2ObjectType.findObject(monitor, db2View, getName());
                if (object == null) {
                    throw new DBException(db2ObjectType + " '" + getName() + "' not found in view '" + db2View.getName() + "'");
                }
                return object;
            }
            return null;
        }

    }

    private String buildTableSQL(String baseStatement, List<DB2ObjectType> objectTypes)
    {
        List<Character> listChars = new ArrayList<Character>(objectTypes.size());
        for (DB2ObjectType objectType : objectTypes) {
            if (objectType.equals(DB2ObjectType.ALIAS)) {
                listChars.add('A');
            }
            if (objectType.equals(DB2ObjectType.TABLE)) {
                listChars.add('G');
                listChars.add('H');
                listChars.add('L');
                listChars.add('S');
                listChars.add('T');
                listChars.add('U');
            }
            if (objectType.equals(DB2ObjectType.VIEW)) {
                listChars.add('V');
                listChars.add('W');
            }
            if (objectType.equals(DB2ObjectType.NICKNAME)) {
                listChars.add('N');
            }

        }
        Boolean notFirst = false;
        StringBuilder sb = new StringBuilder(64);
        for (Character letter : listChars) {
            if (notFirst) {
                sb.append(",");
            } else {
                notFirst = true;
            }
            sb.append("'");
            sb.append(letter);
            sb.append("'");
        }
        return String.format(baseStatement, sb.toString());
    }

    static {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("SELECT TABSCHEMA,TABNAME,TYPE");
        sb.append("  FROM SYSCAT.TABLES");
        sb.append(" WHERE TABSCHEMA = ?");
        sb.append("   AND TABNAME LIKE ?");
        sb.append("   AND TYPE IN (%s)");
        sb.append(" WITH UR");
        SQL_TABLES_SCHEMA = sb.toString();

        sb.setLength(0);

        sb.append("SELECT TABSCHEMA,TABNAME,TYPE");
        sb.append("  FROM SYSCAT.TABLES");
        sb.append(" WHERE TABNAME LIKE ?");
        sb.append("   AND TYPE IN (%s)");
        sb.append(" WITH UR");
        SQL_TABLES_ALL = sb.toString();

        sb.setLength(0);

        sb.append("SELECT TABSCHEMA,TABNAME,COLNAME");
        sb.append("  FROM SYSCAT.COLUMNS");
        sb.append(" WHERE TABSCHEMA = ?");
        sb.append("   AND COLNAME LIKE ?");
        sb.append(" WITH UR");
        SQL_COLS_ALL = sb.toString();

        sb.setLength(0);

        sb.append("SELECT TABSCHEMA,TABNAME,COLNAME");
        sb.append("  FROM SYSCAT.COLUMNS");
        sb.append(" WHERE COLNAME LIKE ?");
        sb.append(" WITH UR");
        SQL_COLS_ALL = sb.toString();

    }

}
