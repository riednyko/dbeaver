/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2016 Serge Rieder (serge@jkiss.org)
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

package org.jkiss.dbeaver.utils;

import org.eclipse.core.runtime.Platform;

import java.io.File;
import java.net.URL;

/**
 * SystemVariablesResolver
 */
public class SystemVariablesResolver implements GeneralUtils.IVariableResolver {

    @Override
    public String get(String name) {
        if (name.equalsIgnoreCase("home")) {
            return getUserHome();
        } else if (name.equalsIgnoreCase("workspace")) {
            return getWorkspacePath();
        } else if (name.equalsIgnoreCase("dbeaver_home")) {
            return getInstallPath();
        }
        return null;
    }

    public static String getInstallPath() {
        return getPlainPath(Platform.getInstallLocation().getURL());
    }

    public static String getWorkspacePath() {
        return getPlainPath(Platform.getInstanceLocation().getURL());
    }

    public static String getUserHome() {
        return System.getenv("user.home");
    }

    private static String getPlainPath(URL url) {
        String path = url.toString();
        if (path.startsWith("file:/")) {
            path = path.substring(6);
        }
        path = path.replace("/", File.separator);
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
