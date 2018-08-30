/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-log.2018 Serge Rider (serge@jkiss.org)
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

package org.jkiss.dbeaver.ext.format.sqlworkbenchj;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.sql.format.SQLFormatter;
import org.jkiss.dbeaver.model.sql.format.SQLFormatterConfiguration;
import org.jkiss.dbeaver.runtime.ui.DBUserInterface;
import org.jkiss.utils.CommonUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * External SQL formatter
 */
public class SQLWorkbenchJFormatter implements SQLFormatter {

    private static final Log log = Log.getLog(SQLWorkbenchJFormatter.class);

    @Override
    public String format(String source, SQLFormatterConfiguration configuration) {

        String workbenchPath = CommonUtils.toString(configuration.getPreferenceStore().getString(SQLWorkbenchJConstants.PROP_WORKBENCH_PATH));
        if (CommonUtils.isEmpty(workbenchPath)) {
            workbenchPath = "C:\\Java\\SQLWorkbenchJ\\";
            //DBUserInterface.getInstance().showError("Workbench not configure", "Empty SQL Workbench/J path");
            //return source;
        }

        try {
            File wbJar = new File(workbenchPath, "sqlworkbench.jar");

            URLClassLoader cl = new URLClassLoader(new URL[] { wbJar.toURI().toURL() });
            Class<?> wbFormatterClass = cl.loadClass("workbench.sql.formatter.WbSqlFormatter");
            Object wbFormatterInstance = wbFormatterClass.getConstructor(CharSequence.class, String.class).newInstance(source, "mysql");
            Object formatResult = wbFormatterClass.getMethod("getFormattedSql").invoke(wbFormatterInstance);
            if (formatResult != null) {
                return CommonUtils.toString(formatResult);
            }

            return source;
        } catch (Exception e) {
            DBUserInterface.getInstance().showError("Workbench format error", "Error formatting with SQL Workbench/J", e);
            return source;
        }
    }

}