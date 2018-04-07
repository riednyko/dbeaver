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

package org.jkiss.dbeaver.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPMessageType;
import org.jkiss.dbeaver.ui.notifications.NotificationPopupMessage;
import org.jkiss.dbeaver.ui.notifications.NotificationUtils;

/**
 * DBeaverIcons
 */
public class DBeaverNotifications
{

    public static void showNotification(DBPDataSource dataSource, String id, String text) {
        NotificationUtils.sendNotification(dataSource, id, text, null, null);
    }

    public static void showNotification(DBPDataSource dataSource, String id, String text, DBPMessageType messageType) {
        NotificationUtils.sendNotification(dataSource, id, text, messageType, null);
    }

    public static void showNotification(DBPDataSource dataSource, String id, String text, DBPMessageType messageType, Runnable feedback) {
        NotificationUtils.sendNotification(dataSource, id, text, messageType, feedback);
    }

}
