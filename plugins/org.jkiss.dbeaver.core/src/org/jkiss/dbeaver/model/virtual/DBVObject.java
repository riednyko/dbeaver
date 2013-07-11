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
package org.jkiss.dbeaver.model.virtual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.xml.XMLBuilder;

import java.io.IOException;

/**
 * Virtual model object
 */
public abstract class DBVObject implements DBSObject {

    static final Log log = LogFactory.getLog(DBVObject.class);

    @Override
    public abstract DBVContainer getParentObject();

    @Override
    public boolean isPersisted() {
        return true;
    }

    public abstract void persist(XMLBuilder xml) throws IOException;

    abstract public boolean hasValuableData();

}
