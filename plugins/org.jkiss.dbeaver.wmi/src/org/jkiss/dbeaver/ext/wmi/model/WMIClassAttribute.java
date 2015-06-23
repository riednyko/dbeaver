/*
 * DBeaver - Universal Database Manager
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
package org.jkiss.dbeaver.ext.wmi.model;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.utils.CommonUtils;
import org.jkiss.wmi.service.WMIConstants;
import org.jkiss.wmi.service.WMIException;
import org.jkiss.wmi.service.WMIObjectAttribute;

/**
 * Class property
 */
public class WMIClassAttribute extends WMIClassElement<WMIObjectAttribute> implements DBSEntityAttribute, DBPImageProvider
{
    protected WMIClassAttribute(WMIClass wmiClass, WMIObjectAttribute attribute)
    {
        super(wmiClass, attribute);
    }

    @Override
    @Property(viewable = true, order = 10)
    public String getTypeName()
    {
        return element.getTypeName();
    }

    @Override
    public int getTypeID()
    {
        return element.getType();
    }

    @Override
    public DBPDataKind getDataKind()
    {
        return getDataKindById(element.getType());
    }

    @Override
    public int getScale()
    {
        return 0;
    }

    @Override
    public int getPrecision()
    {
        return 0;
    }

    @Override
    public long getMaxLength()
    {
        try {
            Object maxLengthQ = getQualifiedObject().getQualifier(WMIConstants.Q_MaxLen);
            if (maxLengthQ instanceof Number) {
                return ((Number) maxLengthQ).longValue();
            }
        } catch (WMIException e) {
            log.warn(e);
        }
        return 0;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public boolean isAutoGenerated()
    {
        return false;
    }

    @Override
    public boolean isPseudoAttribute() {
        return false;
    }

    @Override
    public int getOrdinalPosition()
    {
        return 0;
    }

    public boolean isKey() throws DBException
    {
        return getFlagQualifier(WMIConstants.Q_Key) || getFlagQualifier(WMIConstants.Q_CIM_Key);
    }

    @Override
    @Property(viewable = true, order = 20)
    public String getDefaultValue()
    {
        return CommonUtils.toString(element.getValue());
    }

    @Nullable
    @Override
    public DBPImage getObjectImage()
    {
        return getPropertyImage(element.getType());
    }

    public static DBPImage getPropertyImage(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBIcon.TYPE_NUMBER;
            case WMIConstants.CIM_BOOLEAN:
                return DBIcon.TYPE_BOOLEAN;
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBIcon.TYPE_STRING;
            case WMIConstants.CIM_DATETIME:
                return DBIcon.TYPE_DATETIME;
            default:
                return DBIcon.TYPE_UNKNOWN;
        }
    }

    public static DBPDataKind getDataKindById(int type)
    {
        switch (type) {
            case WMIConstants.CIM_SINT8:
            case WMIConstants.CIM_UINT8:
            case WMIConstants.CIM_SINT16:
            case WMIConstants.CIM_UINT16:
            case WMIConstants.CIM_SINT32:
            case WMIConstants.CIM_UINT32:
            case WMIConstants.CIM_SINT64:
            case WMIConstants.CIM_UINT64:
            case WMIConstants.CIM_REAL32:
            case WMIConstants.CIM_REAL64:
                return DBPDataKind.NUMERIC;
            case WMIConstants.CIM_BOOLEAN:
                return DBPDataKind.BOOLEAN;
            case WMIConstants.CIM_STRING:
            case WMIConstants.CIM_CHAR16:
                return DBPDataKind.STRING;
            case WMIConstants.CIM_DATETIME:
                return DBPDataKind.DATETIME;
            default:
                return DBPDataKind.OBJECT;
        }
    }

}
