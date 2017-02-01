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
package org.jkiss.dbeaver.ui.data.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.impl.AbstractDescriptor;
import org.jkiss.dbeaver.registry.RegistryConstants;
import org.jkiss.dbeaver.ui.data.IStreamValueManager;
import org.jkiss.utils.CommonUtils;

/**
 * StreamValueManagerDescriptor
 */
public class StreamValueManagerDescriptor extends AbstractDescriptor
{
    public static final String TAG_STREAM_MANAGER = "streamManager"; //$NON-NLS-1$
    private static final String ATTR_PRIMARY_MIME = "primaryMime";
    private static final String ATTR_SUPPORTED_MIME = "supportedMime";

    private String id;
    private ObjectType implType;
    private final String label;
    private final String description;
    private final DBPImage icon;
    private final String primaryMime;
    private final String[] supportedMime;

    private IStreamValueManager instance;

    public StreamValueManagerDescriptor(IConfigurationElement config)
    {
        super(config);

        this.id = config.getAttribute(RegistryConstants.ATTR_ID);
        this.implType = new ObjectType(config.getAttribute(RegistryConstants.ATTR_CLASS));
        this.label = config.getAttribute(RegistryConstants.ATTR_LABEL);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
        this.icon = iconToImage(config.getAttribute(RegistryConstants.ATTR_ICON));

        this.primaryMime = config.getAttribute(ATTR_PRIMARY_MIME);
        this.supportedMime = CommonUtils.notEmpty(config.getAttribute(ATTR_SUPPORTED_MIME)).split(",");
    }

    public String getId()
    {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public DBPImage getIcon() {
        return icon;
    }

    public String[] getSupportedMime() {
        return supportedMime;
    }

    public String getPrimaryMime() {
        return primaryMime;
    }

    @NotNull
    public IStreamValueManager getInstance()
    {
        if (instance == null) {
            try {
                this.instance = implType.createInstance(IStreamValueManager.class);
            }
            catch (Exception e) {
                throw new IllegalStateException("Can't instantiate content value manager '" + this.id + "'", e); //$NON-NLS-1$
            }
        }
        return instance;
    }

    @Override
    public String toString() {
        return id + " (" + label + ")";
    }
}