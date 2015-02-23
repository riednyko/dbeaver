/*
 * Copyright (C) 2010-2015 Serge Rieder
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

package org.jkiss.wmi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Qualified object
 */
public abstract class WMIQualifiedObject {

    private volatile List<WMIQualifier> qualifiers;

    public Collection<WMIQualifier> getQualifiers()
        throws WMIException
    {
        if (qualifiers == null) {
            synchronized (this) {
                if (qualifiers == null) {
                    qualifiers = new ArrayList<WMIQualifier>();
                    readObjectQualifiers(qualifiers);
                }
            }
        }
        return qualifiers;
    }

    public Object getQualifier(String name)
        throws WMIException
    {
        for (WMIQualifier q : getQualifiers()) {
            if (q.getName().equalsIgnoreCase(name)) {
                return q.getValue();
            }
        }
        return null;
    }

    protected abstract void readObjectQualifiers(List<WMIQualifier> qualifiers) throws WMIException;

}
