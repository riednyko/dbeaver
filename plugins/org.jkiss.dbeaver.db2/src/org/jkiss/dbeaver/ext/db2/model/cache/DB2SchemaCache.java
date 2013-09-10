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
package org.jkiss.dbeaver.ext.db2.model.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * 
 * Cache for DB2 Schemas
 * 
 * @author Denis Forveille
 * 
 */
public final class DB2SchemaCache extends JDBCObjectCache<DB2DataSource, DB2Schema> {

   private static final String SQL = "SELECT * FROM SYSCAT.SCHEMATA ORDER BY SCHEMANAME WITH UR";

   public DB2SchemaCache() {
      init();
   }

   @Override
   protected JDBCStatement prepareObjectsStatement(JDBCExecutionContext context, DB2DataSource db2DataSource) throws SQLException {
      return context.prepareStatement(SQL);
   }

   @Override
   protected DB2Schema fetchObject(JDBCExecutionContext context, DB2DataSource db2DataSource, ResultSet resultSet) throws SQLException,
                                                                                                                  DBException {
      return new DB2Schema(db2DataSource, resultSet);
   }

   @Override
   protected void invalidateObjects(DBRProgressMonitor monitor, DB2DataSource db2DataSource, Iterator<DB2Schema> objectIter) {
      init();
   }

   // -------
   // Helpers
   // -------

   private void init() {
      setListOrderComparator(DBUtils.<DB2Schema> nameComparator());
   }
}