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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectType;
import org.jkiss.dbeaver.ui.DBIcon;

/**
 * DB2 Object type
 * 
 * @author Denis Forveille
 * 
 */
public enum DB2ObjectType implements DBSObjectType {

   // TODO DF: real usage of this class?

   CONSTRAINT("CONSTRAINT", DBIcon.TREE_CONSTRAINT.getImage(), DB2TableUniqueKey.class, null), // fake object

   FOREIGN_KEY("FOREIGN KEY", DBIcon.TREE_FOREIGN_KEY.getImage(), DB2TableForeignKey.class, null), // fake object

   FUNCTION("FUNCTION", DBIcon.TREE_PROCEDURE.getImage(), DB2Routine.class, new ObjectFinder() {
      @Override
      public DB2Routine findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getProcedureCache().getObject(monitor, schema, objectName);
      }
   }),

   INDEX("INDEX", DBIcon.TREE_INDEX.getImage(), DB2Index.class, new ObjectFinder() {
      @Override
      public DB2Index findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getIndexCache().getObject(monitor, schema, objectName);
      }
   }),

   MATERIALIZED_VIEW("MATERIALIZED VIEW", null, DBSObject.class, null),

   PROCEDURE("PROCEDURE", DBIcon.TREE_PROCEDURE.getImage(), DB2Routine.class, new ObjectFinder() {
      @Override
      public DB2Routine findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getProcedureCache().getObject(monitor, schema, objectName);
      }
   }),

   TABLE("TABLE", DBIcon.TREE_TABLE.getImage(), DB2Table.class, new ObjectFinder() {
      @Override
      public DB2Table findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getTableCache().getObject(monitor, schema, objectName);
      }
   }),

   TRIGGER("TRIGGER", DBIcon.TREE_TRIGGER.getImage(), DB2Trigger.class, new ObjectFinder() {
      @Override
      public DB2Trigger findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getTriggerCache().getObject(monitor, schema, objectName);
      }
   }),

   VIEW("VIEW", DBIcon.TREE_VIEW.getImage(), DB2View.class, new ObjectFinder() {
      @Override
      public DB2View findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
         return schema.getTableCache().getObject(monitor, schema, objectName, DB2View.class);
      }
   });

   static final Log                          log     = LogFactory.getLog(DB2ObjectType.class);

   private static Map<String, DB2ObjectType> typeMap = new HashMap<String, DB2ObjectType>();

   static {
      for (DB2ObjectType type : values()) {
         typeMap.put(type.getTypeName(), type);
      }
   }

   public static DB2ObjectType getByType(String typeName) {
      return typeMap.get(typeName);
   }

   private static interface ObjectFinder {
      DBSObject findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException;
   }

   private final String                     objectType;
   private final Image                      image;
   private final Class<? extends DBSObject> typeClass;
   private final ObjectFinder               finder;

   <OBJECT_TYPE extends DBSObject> DB2ObjectType(String objectType, Image image, Class<OBJECT_TYPE> typeClass, ObjectFinder finder) {
      this.objectType = objectType;
      this.image = image;
      this.typeClass = typeClass;
      this.finder = finder;
   }

   public boolean isBrowsable() {
      return finder != null;
   }

   @Override
   public String getTypeName() {
      return objectType;
   }

   @Override
   public String getDescription() {
      return null;
   }

   @Override
   public Image getImage() {
      return image;
   }

   @Override
   public Class<? extends DBSObject> getTypeClass() {
      return typeClass;
   }

   public DBSObject findObject(DBRProgressMonitor monitor, DB2Schema schema, String objectName) throws DBException {
      if (finder != null) {
         return finder.findObject(monitor, schema, objectName);
      } else {
         return null;
      }
   }

   public static Object resolveObject(DBRProgressMonitor monitor,
                                      DB2DataSource dataSource,
                                      String objectTypeName,
                                      String objectOwner,
                                      String objectName) throws DBException {
      DB2ObjectType objectType = DB2ObjectType.getByType(objectTypeName);
      if (objectType == null) {
         log.debug("Unrecognized object type: " + objectTypeName);
         return objectName;
      }
      if (!objectType.isBrowsable()) {
         log.debug("Unsupported object type: " + objectTypeName);
         return objectName;
      }
      final DB2Schema schema = dataSource.getSchema(monitor, objectOwner);
      if (schema == null) {
         log.debug("Schema '" + objectOwner + "' not found");
         return objectName;
      }
      final DBSObject object = objectType.findObject(monitor, schema, objectName);
      if (object == null) {
         log.debug(objectTypeName + " '" + objectName + "' not found in '" + schema.getName() + "'");
         return objectName;
      }
      return object;
   }

   @Override
   public String toString() {
      return objectType;
   }

}
