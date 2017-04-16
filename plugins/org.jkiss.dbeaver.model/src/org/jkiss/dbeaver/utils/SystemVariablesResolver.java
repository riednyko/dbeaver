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

package org.jkiss.dbeaver.utils;

import org.eclipse.core.runtime.Platform;
import org.jkiss.utils.StandardConstants;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * SystemVariablesResolver
 */
public class SystemVariablesResolver implements GeneralUtils.IVariableResolver {

    public static final String VAR_WORKSPACE = "workspace";
    public static final String VAR_HOME = "home";
    public static final String VAR_DBEAVER_HOME = "dbeaver_home";

    @Override
    public String get(String name) {
        if (name.equalsIgnoreCase(VAR_HOME)) {
            return getUserHome();
        } else if (name.equalsIgnoreCase(VAR_WORKSPACE)) {
            return getWorkspacePath();
        } else if (name.equalsIgnoreCase(VAR_DBEAVER_HOME)) {
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
        return System.getProperty(StandardConstants.ENV_USER_HOME);
    }

    private static String getPlainPath(URL url) {
        try {
            File file = new File(url.toURI());
            return file.getAbsolutePath();
        } catch (URISyntaxException e) {
            return url.toString();
        }
    }

}
