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
package org.jkiss.dbeaver.model.virtual;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Virtual attribute
 */
public class DBVEntityAttribute implements DBSEntityAttribute
{
    private final DBVEntity entity;
    private String name;
    private Map<String, String> valueColors = new LinkedHashMap<>();
    private String defaultValue;
    private String description;
    private String presentationManager;
    private Map<String, String> presentationProperties;

    public DBVEntityAttribute(DBVEntity entity, String name) {
        this.entity = entity;
        this.name = name;
    }

    public DBVEntityAttribute(DBVEntity entity, DBVEntityAttribute copy) {
        this.entity = entity;
        this.name = copy.name;
    }

    @Nullable
    public DBSEntityAttribute getRealAttribute(DBRProgressMonitor monitor) throws DBException
    {
        final DBSEntity realEntity = entity.getRealEntity(monitor);
        return realEntity == null ? null : realEntity.getAttribute(monitor, getName());
    }

    @NotNull
    @Override
    public DBSEntity getParentObject() {
        return entity;
    }

    @NotNull
    @Override
    public DBPDataSource getDataSource() {
        return entity.getDataSource();
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return "void";
    }

    @Override
    public int getTypeID() {
        return -1;
    }

    @Override
    public DBPDataKind getDataKind() {
        return DBPDataKind.UNKNOWN;
    }

    @Override
    public int getScale() {
        return -1;
    }

    @Override
    public int getPrecision() {
        return -1;
    }

    @Override
    public long getMaxLength() {
        return -1;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @Override
    public int getOrdinalPosition() {
        return 0;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isAutoGenerated() {
        return false;
    }

    @Override
    public boolean isPseudoAttribute() {
        return false;
    }

    @Nullable
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public Map<String, String> getValueColors() {
        return valueColors;
    }

    public void setValueColors(@NotNull Map<String, String> valueColors) {
        this.valueColors = new LinkedHashMap<>(valueColors);
    }

}
