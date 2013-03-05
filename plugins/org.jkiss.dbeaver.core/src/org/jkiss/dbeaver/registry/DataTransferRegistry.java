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

package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * EntityEditorsRegistry
 */
public class DataTransferRegistry {

    private List<DataTransferNodeDescriptor> nodes = new ArrayList<DataTransferNodeDescriptor>();

    public DataTransferRegistry(IExtensionRegistry registry)
    {
        // Load datasource providers from external plugins
        IConfigurationElement[] extElements = registry.getConfigurationElementsFor(DataTransferProcessorDescriptor.EXTENSION_ID);
        for (IConfigurationElement ext : extElements) {
            if (RegistryConstants.TAG_NODE.equals(ext.getName())) {
                nodes.add(new DataTransferNodeDescriptor(ext));
            }
        }
    }

    public List<DataTransferNodeDescriptor> getAvailableProducers(Collection<Class<?>> objectTypes)
    {
        return getAvailableNodes(DataTransferNodeDescriptor.NodeType.PRODUCER, objectTypes);
    }

    public List<DataTransferNodeDescriptor> getAvailableConsumers(Collection<Class<?>> objectTypes)
    {
        return getAvailableNodes(DataTransferNodeDescriptor.NodeType.CONSUMER, objectTypes);
    }

    List<DataTransferNodeDescriptor> getAvailableNodes(DataTransferNodeDescriptor.NodeType nodeType, Collection<Class<?>> objectTypes)
    {
        List<DataTransferNodeDescriptor> result = new ArrayList<DataTransferNodeDescriptor>();
        for (DataTransferNodeDescriptor node : nodes) {
            if (node.getNodeType() == nodeType) {
                for (Class objectType : objectTypes) {
                    if (node.appliesToType(objectType)) {
                        result.add(node);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void dispose()
    {
        nodes.clear();
    }
}
