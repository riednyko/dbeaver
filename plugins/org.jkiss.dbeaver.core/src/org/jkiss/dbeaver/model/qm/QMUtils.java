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

package org.jkiss.dbeaver.model.qm;

import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.runtime.qm.QMMetaEvent;
import org.jkiss.dbeaver.runtime.qm.QMMetaListener;

import java.util.List;

/**
 * Query Manager utils
 */
public class QMUtils {

    private static QMExecutionHandler defaultHandler; 

    public static QMExecutionHandler getDefaultHandler()
    {
        if (defaultHandler == null) {
            defaultHandler = DBeaverCore.getInstance().getQueryManager().getDefaultHandler();
        }
        return defaultHandler;
    }

    public static void registerHandler(QMExecutionHandler handler)
    {
        DBeaverCore.getInstance().getQueryManager().registerHandler(handler);
    }

    public static void unregisterHandler(QMExecutionHandler handler)
    {
        DBeaverCore.getInstance().getQueryManager().unregisterHandler(handler);
    }

    public static void registerMetaListener(QMMetaListener metaListener)
    {
        DBeaverCore.getInstance().getQueryManager().registerMetaListener(metaListener);
    }

    public static void unregisterMetaListener(QMMetaListener metaListener)
    {
        DBeaverCore.getInstance().getQueryManager().unregisterMetaListener(metaListener);
    }

    public static List<QMMetaEvent> getPastMetaEvents()
    {
        return DBeaverCore.getInstance().getQueryManager().getPastMetaEvents();
    }
}
