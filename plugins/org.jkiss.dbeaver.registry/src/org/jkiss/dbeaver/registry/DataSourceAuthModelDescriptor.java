/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
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
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.auth.DBAAuthCredentials;
import org.jkiss.dbeaver.model.auth.DBAAuthModel;
import org.jkiss.dbeaver.model.connection.DBPAuthModelDescriptor;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.preferences.DBPPropertySource;
import org.jkiss.dbeaver.runtime.properties.PropertyCollector;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Auth model descriptor
 */
public class DataSourceAuthModelDescriptor extends DataSourceBindingDescriptor implements DBPAuthModelDescriptor {

    public static final String EXTENSION_ID = "org.jkiss.dbeaver.dataSourceAuth"; //$NON-NLS-1$

    private final String id;
    private final ObjectType implType;
    private final String name;
    private final String description;
    private DBPImage icon;
    private boolean defaultModel;
    private final List<String> replaces = new ArrayList<>();

    private DBAAuthModel instance;

    DataSourceAuthModelDescriptor(IConfigurationElement config) {
        super(config);

        this.id = config.getAttribute(RegistryConstants.ATTR_ID);
        this.implType = new ObjectType(config.getAttribute(RegistryConstants.ATTR_CLASS));
        this.name = config.getAttribute(RegistryConstants.ATTR_LABEL);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
        this.icon = iconToImage(config.getAttribute(RegistryConstants.ATTR_ICON));
        if (this.icon == null) {
            this.icon = DBIcon.TREE_PACKAGE;
        }
        this.defaultModel = CommonUtils.toBoolean(config.getAttribute(RegistryConstants.ATTR_DEFAULT));

        for (IConfigurationElement dsConfig : config.getChildren("replace")) {
            this.replaces.add(dsConfig.getAttribute("model"));
        }
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public DBPImage getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public String getImplClassName() {
        return implType.getImplName();
    }

    @Override
    public boolean isDefaultModel() {
        return defaultModel;
    }

    @Override
    public boolean isApplicableTo(DBPDriver driver) {
        return appliesTo(driver);
    }

    @Nullable
    @Override
    public DBPAuthModelDescriptor getReplacedBy() {
        for (DataSourceAuthModelDescriptor amd : DataSourceProviderRegistry.getInstance().getAllAuthModels()) {
            if (amd.getReplaces().contains(id)) {
                return amd;
            }
        }
        return null;
    }

    @NotNull
    public DBAAuthModel getInstance() {
        if (instance == null) {
            try {
                // locate class
                this.instance = implType.createInstance(DBAAuthModel.class);
            } catch (Throwable ex) {
                this.instance = null;
                throw new IllegalStateException("Can't initialize data source auth model '" + implType.getImplName() + "'", ex);
            }
        }
        return instance;
    }

    @NotNull
    @Override
    public DBPPropertySource createCredentialsSource(DBPDataSourceContainer dataSource, DBPConnectionConfiguration configuration) {
        DBAAuthModel instance = getInstance();
        DBAAuthCredentials credentials = dataSource == null || configuration == null ?
            instance.createCredentials() :
            instance.loadCredentials(dataSource, configuration);
        PropertyCollector propertyCollector = new PropertyCollector(credentials, false);
        propertyCollector.collectProperties();
        return propertyCollector;
    }

    boolean appliesTo(DBPDriver driver) {
        return isDriverApplicable(driver);
    }

    public List<String> getReplaces() {
        return replaces;
    }

    @Override
    public String toString() {
        return id;
    }

}
