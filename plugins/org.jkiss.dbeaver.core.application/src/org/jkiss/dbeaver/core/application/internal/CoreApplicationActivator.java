/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2019 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.core.application.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPMessageType;
import org.jkiss.dbeaver.runtime.DBeaverNotifications;
import org.jkiss.dbeaver.ui.notifications.NotificationUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.hooks.bundle.EventHook;

public class CoreApplicationActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.jkiss.dbeaver.core.application";
    private static final Log log = Log.getLog(CoreApplicationActivator.class);

    // The shared instance
    private static CoreApplicationActivator plugin;

    public CoreApplicationActivator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        // Set notifications handler
        DBeaverNotifications.setHandler(new DBeaverNotifications.NotificationHandler() {
            @Override
            public void sendNotification(DBPDataSource dataSource, String id, String text, DBPMessageType messageType, Runnable feedback) {
                NotificationUtils.sendNotification(dataSource, id, text, messageType, feedback);
            }

            @Override
            public void sendNotification(String id, String title, String text, DBPMessageType messageType, Runnable feedback) {
                NotificationUtils.sendNotification(id, title, text, messageType, feedback);
            }
        });

        // Add bundle load logger
        {
            context.registerService(EventHook.class, (event, contexts) -> {
                String message = null;
                if (event.getType() == BundleEvent.STARTED) {
                    if (event.getBundle().getState() == Bundle.ACTIVE) {
                        message = "> Start " + event.getBundle().getSymbolicName() + " [" + event.getBundle().getVersion() + "]";
                    }
                } else if (event.getType() == BundleEvent.STOPPING) {
                    message = "< Stop " + event.getBundle().getSymbolicName() + " [" + event.getBundle().getVersion() + "]";
                }
                if (message != null) {
                    log.debug(message);
                }
            }, null);
            //context.addBundleListener(new BundleLoadListener());
        }

        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static CoreApplicationActivator getDefault() {
        return plugin;
    }

}
