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

package org.jkiss.dbeaver.tools.scripts;

import org.eclipse.core.resources.IResource;

import java.io.File;
import java.util.Collection;


class ScriptsExportData {

    private final Collection<IResource> scripts;
    private final boolean overwriteFiles;
    private final File outputFolder;

    ScriptsExportData(Collection<IResource> scripts, boolean overwriteFiles, File outputFolder)
    {
        this.scripts = scripts;
        this.overwriteFiles = overwriteFiles;
        this.outputFolder = outputFolder;
    }

    public Collection<IResource> getScripts()
    {
        return scripts;
    }

    public boolean isOverwriteFiles()
    {
        return overwriteFiles;
    }

    public File getOutputFolder()
    {
        return outputFolder;
    }

}
