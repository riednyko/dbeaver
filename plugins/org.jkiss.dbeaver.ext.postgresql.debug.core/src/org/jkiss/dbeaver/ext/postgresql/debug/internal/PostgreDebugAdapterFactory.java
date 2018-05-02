/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2018 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
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
package org.jkiss.dbeaver.ext.postgresql.debug.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.jkiss.dbeaver.debug.DBGController;
import org.jkiss.dbeaver.debug.DBGControllerFactory;
import org.jkiss.dbeaver.debug.DBGResolver;
import org.jkiss.dbeaver.ext.postgresql.PostgreDataSourceProvider;
import org.jkiss.dbeaver.ext.postgresql.debug.internal.impl.PostgreDebugController;
import org.jkiss.dbeaver.ext.postgresql.debug.internal.impl.PostgreDebugControllerFactory;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPDataSourceProvider;
import org.jkiss.dbeaver.model.connection.DBPDriver;

public class PostgreDebugAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = new Class[] { DBGController.class, DBGResolver.class };

    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType == DBGControllerFactory.class) {
            if (adaptableObject instanceof DBPDataSourceContainer) {
                DBPDataSourceContainer ds = (DBPDataSourceContainer) adaptableObject;
                if (ds.getDriver().getDataSourceProvider() instanceof PostgreDataSourceProvider) {
                    return adapterType.cast(new PostgreDebugControllerFactory());
                }
            }
        } else if (adapterType == DBGResolver.class) {
            if (adaptableObject instanceof DBPDataSourceContainer) {
                DBPDataSourceContainer ds = (DBPDataSourceContainer) adaptableObject;
                if (ds.getDataSource() instanceof PostgreDataSource) {
                    return adapterType.cast(
                        new PostgreResolver((PostgreDataSource) ds.getDataSource()));
                }
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return CLASSES;
    }

}
