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
package org.jkiss.dbeaver.model.impl.data;

import org.jkiss.dbeaver.model.data.DBDBinaryPresentation;
import org.jkiss.dbeaver.utils.ContentUtils;

/**
 * String presentation
 */
public class BinaryPresentationString implements DBDBinaryPresentation {

    @Override
    public String getId()
    {
        return "string";
    }

    @Override
    public String getTitle()
    {
        return "String";
    }

    @Override
    public String toString(byte[] bytes, int offset, int length)
    {
        return ContentUtils.convertToString(bytes, offset, length);
    }

    @Override
    public byte[] toBytes(String string)
    {
        return ContentUtils.convertToBytes(string);
    }

}
