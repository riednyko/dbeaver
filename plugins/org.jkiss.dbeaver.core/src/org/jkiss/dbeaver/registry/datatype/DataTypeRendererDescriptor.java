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
package org.jkiss.dbeaver.registry.datatype;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.dbeaver.model.data.DBDValueRenderer;
import org.jkiss.dbeaver.registry.RegistryConstants;

/**
 * DataTypeRendererDescriptor
 */
public class DataTypeRendererDescriptor extends DataTypeAbstractDescriptor<DBDValueRenderer>
{

    private String name;
    private String description;

    public DataTypeRendererDescriptor(IConfigurationElement config)
    {
        super(config, DBDValueRenderer.class);

        this.name = config.getAttribute(RegistryConstants.ATTR_NAME);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}