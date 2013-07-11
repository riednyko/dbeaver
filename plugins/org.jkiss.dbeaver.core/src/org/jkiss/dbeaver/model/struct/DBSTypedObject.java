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
package org.jkiss.dbeaver.model.struct;

import org.jkiss.dbeaver.model.DBPObject;

/**
 * DBSTypedObject
 */
public interface DBSTypedObject extends DBPObject
{
    /**
     * Database specific type name
     * @return type name
     */
    String getTypeName();

    /**
     * Type numeric ID.
     * (may refer on java.sql.Types or other constant depending on implementer)
     * @return value type
     */
    int getTypeID();

    /**
     * Determines kind of data for this typed object.
     * @return data kind
     */
    DBSDataKind getDataKind();

    /**
     * Value scale
     * @return scale
     */
    int getScale();

    /**
     * Value precision
     * @return precision
     */
    int getPrecision();

    /**
     * Maximum length
     * @return max length
     */
    long getMaxLength();
}