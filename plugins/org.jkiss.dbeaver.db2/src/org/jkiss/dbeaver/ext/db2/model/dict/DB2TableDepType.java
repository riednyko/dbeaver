/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2013-2015 Denis Forveille (titou10.titou10@gmail.com)
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ext.db2.model.dict;

import org.jkiss.dbeaver.ext.db2.editors.DB2ObjectType;
import org.jkiss.dbeaver.model.DBPNamedObject;

/**
 * DB2 Type of Table Dependency
 * 
 * @author Denis Forveille
 */
public enum DB2TableDepType implements DBPNamedObject {
    A("Table alias", DB2ObjectType.ALIAS),

    F("Routine", DB2ObjectType.ROUTINE),

    I("Index", DB2ObjectType.INDEX),

    G("Global temporary table", DB2ObjectType.TABLE),

    N("Nickname", DB2ObjectType.NICKNAME),

    O("Privilege dependency on all subtables or subviews in a table or view hierarchy"),

    R("UDT", DB2ObjectType.UDT),

    S("MQT", DB2ObjectType.MQT),

    T("Table", DB2ObjectType.TABLE),

    U("Typed table", DB2ObjectType.TABLE),

    V("View", DB2ObjectType.VIEW),

    W("Typed view", DB2ObjectType.VIEW),

    Z("XSR object", DB2ObjectType.XML_SCHEMA),

    u("Module alias", DB2ObjectType.ALIAS),

    v("Global variable", DB2ObjectType.VARIABLE);

    private String name;
    private DB2ObjectType db2ObjectType;

    // -----------
    // Constructor
    // -----------

    private DB2TableDepType(String name, DB2ObjectType db2ObjectType)
    {
        this.name = name;
        this.db2ObjectType = db2ObjectType;
    }

    private DB2TableDepType(String description)
    {
        this(description, null);
    }

    // -----------------------
    // Display @Property Value
    // -----------------------
    @Override
    public String toString()
    {
        return name;
    }

    // ----------------
    // Standard Getters
    // ----------------

    @Override
    public String getName()
    {
        return name;
    }

    public DB2ObjectType getDb2ObjectType()
    {
        return db2ObjectType;
    }

}