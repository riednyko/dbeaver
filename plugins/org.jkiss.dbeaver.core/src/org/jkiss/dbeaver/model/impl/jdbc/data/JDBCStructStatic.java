/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDStructure;
import org.jkiss.dbeaver.model.data.DBDValueCloneable;
import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static struct holder.
 * Attributes described by static data type.
 */
public class JDBCStructStatic implements JDBCStruct, DBDValueCloneable {

    static final Log log = LogFactory.getLog(JDBCStructStatic.class);

    @NotNull
    private DBSDataType type;
    @Nullable
    private Struct contents;
    @NotNull
    private Map<DBSAttributeBase, Object> values;

    private JDBCStructStatic()
    {
    }

    public JDBCStructStatic(DBCSession session, @NotNull DBSDataType type, @Nullable Struct contents) throws DBCException
    {
        this.type = type;
        this.contents = contents;

        // Extract structure data
        values = new LinkedHashMap<DBSAttributeBase, Object>();
        try {
            Object[] attrValues = contents == null ? null : contents.getAttributes();
            if (type instanceof DBSEntity) {
                DBSEntity entity = (DBSEntity)type;
                Collection<? extends DBSEntityAttribute> entityAttributes = CommonUtils.safeCollection(entity.getAttributes(session.getProgressMonitor()));
                int valueCount = attrValues == null ? 0 : attrValues.length;
                if (attrValues != null && entityAttributes.size() != valueCount) {
                    log.warn("Number of entity attributes (" + entityAttributes.size() + ") differs from real values (" + valueCount + ")");
                }
                for (DBSEntityAttribute attr : entityAttributes) {
                    int ordinalPosition = attr.getOrdinalPosition() - 1;
                    if (ordinalPosition < 0 || attrValues != null && ordinalPosition >= valueCount) {
                        log.warn("Attribute '" + attr.getName() + "' ordinal position (" + ordinalPosition + ") is out of range (" + valueCount + ")");
                        continue;
                    }
                    Object value = attrValues != null ? attrValues[ordinalPosition] : null;
                    DBDValueHandler valueHandler = DBUtils.findValueHandler(session, attr);
                    value = valueHandler.getValueFromObject(session, attr, value, false);
                    values.put(attr, value);
                }
            }
        } catch (DBException e) {
            throw new DBCException("Can't obtain attributes meta information", e);
        } catch (SQLException e) {
            throw new DBCException(e, session.getDataSource());
        }
    }

    @Nullable
    public Struct getValue() throws DBCException
    {
        return contents;
    }

    @Override
    public boolean isNull()
    {
        return contents == null;
    }

    @Override
    public void release()
    {
        contents = null;
        values.clear();
    }

    @NotNull
    public String getTypeName()
    {
        return type.getTypeName();
    }

    public String getStringRepresentation()
    {
        return getTypeName();
    }

    @Override
    public DBSDataType getObjectDataType()
    {
        return type;
    }

    @NotNull
    @Override
    public Collection<DBSAttributeBase> getAttributes()
    {
        return values.keySet();
    }

    @Nullable
    @Override
    public Object getAttributeValue(@NotNull DBSAttributeBase attribute) throws DBCException
    {
        return values.get(attribute);
    }

    @Override
    public void setAttributeValue(@NotNull DBSAttributeBase attribute, @Nullable Object value) throws DBCException
    {
        values.put(attribute, value);
    }

    @Override
    public JDBCStructStatic cloneValue(DBRProgressMonitor monitor) throws DBCException
    {
        JDBCStructStatic copyStruct = new JDBCStructStatic();
        copyStruct.type = this.type;
        copyStruct.contents = null;
        copyStruct.values = new LinkedHashMap<DBSAttributeBase, Object>(this.values);
        return copyStruct;
    }

}
