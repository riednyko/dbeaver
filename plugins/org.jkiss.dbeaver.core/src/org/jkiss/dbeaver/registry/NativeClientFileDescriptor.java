/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.dbeaver.model.impl.AbstractDescriptor;

import java.io.File;

/**
 * NativeClientDistributionDescriptor
 */
public class NativeClientFileDescriptor {
    private String type;
    private String name;

    public NativeClientFileDescriptor(IConfigurationElement config) {
        this.type = config.getAttribute(RegistryConstants.ATTR_TYPE);
        this.name = config.getAttribute(RegistryConstants.ATTR_NAME);
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
