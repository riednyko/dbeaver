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
package org.jkiss.dbeaver.registry.driver;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.connection.DBPDriverContext;
import org.jkiss.dbeaver.model.connection.DBPDriverLibrary;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.registry.maven.*;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DriverLibraryDescriptor
 */
public class DriverLibraryMavenArtifact extends DriverLibraryAbstract
{
    static final Log log = Log.getLog(DriverLibraryMavenArtifact.class);

    public static final String PATH_PREFIX = "maven:/";

    private final MavenArtifactReference reference;
    protected MavenArtifactVersion localVersion;

    public DriverLibraryMavenArtifact(DriverDescriptor driver, FileType type, String path) {
        super(driver, type, path);
        reference = new MavenArtifactReference(path);
    }

    public DriverLibraryMavenArtifact(DriverDescriptor driver, IConfigurationElement config) {
        super(driver, config);
        reference = new MavenArtifactReference(path);
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public boolean isDownloadable()
    {
        return true;
    }

    @Override
    public boolean isResolved() {
        return getCachedArtifactVersion() != null;
    }

    @Override
    public void resolve(DBPDriverContext context) throws IOException {
        if (getArtifactVersion(context) == null) {
            throw new IOException("Can't resolve artifact " + this + " version");
        }
    }

    @Nullable
    protected MavenArtifactVersion getCachedArtifactVersion() {
        if (localVersion == null) {
            try (DBPDriverContext context = new DBPDriverContext(VoidProgressMonitor.INSTANCE)) {
                localVersion = MavenRegistry.getInstance().findCachedArtifact(context, reference);
            }
        }
        return localVersion;
    }

    @Nullable
    protected MavenArtifactVersion getArtifactVersion(DBPDriverContext context) {
        if (localVersion == null) {
            localVersion = MavenRegistry.getInstance().findArtifact(context, reference);
        }
        return localVersion;
    }

    @Nullable
    @Override
    public String getExternalURL(DBPDriverContext context) {
        MavenArtifactVersion localVersion = getArtifactVersion(context);
        if (localVersion != null) {
            return localVersion.getExternalURL(MavenArtifact.FILE_JAR);
        }
        return null;
    }


    @Nullable
    @Override
    public File getLocalFile()
    {
        // Try to get local file
        File platformFile = detectLocalFile();
        if (platformFile != null && platformFile.exists()) {
            // Relative file do not exists - use plain one
            return platformFile;
        }
        // Nothing fits - just return plain url
        return platformFile;
    }

    private File detectLocalFile()
    {
        MavenArtifactVersion localVersion = getCachedArtifactVersion();
        if (localVersion != null) {
            return localVersion.getCacheFile();
        }
        return null;
    }

    @Nullable
    @Override
    public Collection<? extends DBPDriverLibrary> getDependencies(@NotNull DBPDriverContext context) throws IOException {
        List<DriverLibraryMavenDependency> dependencies = new ArrayList<>();
        MavenArtifactVersion localVersion = resolveLocalVersion(context, false);
        if (localVersion != null) {
            List<MavenArtifactDependency> artifactDeps = localVersion.getDependencies(context);
            if (!CommonUtils.isEmpty(artifactDeps)) {
                for (MavenArtifactDependency dependency : artifactDeps) {
                    if (isDependencyExcluded(context, dependency)) {
                        continue;
                    }

                    MavenArtifactVersion depArtifact = MavenRegistry.getInstance().findArtifact(context, dependency);
                    if (depArtifact != null) {
                        dependencies.add(
                            new DriverLibraryMavenDependency(
                                this,
                                depArtifact,
                                dependency));
                    } else {
                        dependency.setBroken(true);
                    }
                }
            }
        }

        return dependencies;
    }

    protected boolean isDependencyExcluded(DBPDriverContext context, MavenArtifactDependency dependency) {
        return false;
    }

    @NotNull
    public String getDisplayName() {
        return getId();
    }

    @Override
    public String getId() {
        return reference.getGroupId() + ":" + reference.getArtifactId();
    }

    @Override
    public String getVersion() {
        MavenArtifactVersion version = getCachedArtifactVersion();
        if (version != null) {
            return version.getVersion();
        }

        return path;
    }

    @NotNull
    @Override
    public DBIcon getIcon() {
        return UIIcon.APACHE;
    }

    public void downloadLibraryFile(@NotNull DBPDriverContext context, boolean forceUpdate, String taskName) throws IOException, InterruptedException {
        //monitor.beginTask(taskName + " - update localVersion information", 1);
        try {
            MavenArtifactVersion localVersion = resolveLocalVersion(context, forceUpdate);
            if (localVersion.getArtifact().getRepository().isLocal()) {
                // No need to download local artifacts
                return;
            }
        } finally {
            //monitor.done();
        }
        super.downloadLibraryFile(context, forceUpdate, taskName);
    }

    protected MavenArtifactVersion resolveLocalVersion(DBPDriverContext context, boolean forceUpdate) throws IOException {
        if (forceUpdate) {
            MavenRegistry.getInstance().resetArtifactInfo(reference);
        }
        MavenArtifactVersion version = getArtifactVersion(context);
        if (version == null) {
            throw new IOException("Maven artifact '" + path + "' not found");
        }
        return version;
    }

}
