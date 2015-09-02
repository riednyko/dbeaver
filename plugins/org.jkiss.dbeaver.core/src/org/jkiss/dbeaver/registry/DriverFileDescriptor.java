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
package org.jkiss.dbeaver.registry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPDriverFile;
import org.jkiss.dbeaver.model.DBPDriverFileType;
import org.jkiss.dbeaver.model.DBPPreferenceStore;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.OSDescriptor;
import org.jkiss.dbeaver.registry.maven.MavenArtifact;
import org.jkiss.dbeaver.registry.maven.MavenLocalVersion;
import org.jkiss.dbeaver.registry.maven.MavenRegistry;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.NumberFormat;

/**
 * DriverFileDescriptor
 */
public class DriverFileDescriptor implements DBPDriverFile
{
    static final Log log = Log.getLog(DriverFileDescriptor.class);

    public static final String FILE_SOURCE_MAVEN = "maven:/";
    public static final String FILE_SOURCE_REPO = "repo:/";
    public static final String FILE_SOURCE_PLATFORM = "platform:/";
    private static final String DEFAULT_MAVEN_VERSION = "release";

    private final DriverDescriptor driver;
    private final DBPDriverFileType type;
    private final OSDescriptor system;
    private String path;
    private String fileExtension;
    private String description;
    private boolean custom;
    private boolean disabled;

    public DriverFileDescriptor(DriverDescriptor driver, DBPDriverFileType type, String path)
    {
        this.driver = driver;
        this.type = type;
        this.system = DBeaverCore.getInstance().getLocalSystem();
        this.path = path;
        this.custom = true;
    }

    DriverFileDescriptor(DriverDescriptor driver, IConfigurationElement config)
    {
        this.driver = driver;
        this.type = DBPDriverFileType.valueOf(config.getAttribute(RegistryConstants.ATTR_TYPE));

        String osName = config.getAttribute(RegistryConstants.ATTR_OS);
        this.system = osName == null ? null : new OSDescriptor(
            osName,
            config.getAttribute(RegistryConstants.ATTR_ARCH));
        this.path = config.getAttribute(RegistryConstants.ATTR_PATH);
        this.fileExtension = config.getAttribute(RegistryConstants.ATTR_EXTENSION);
        this.description = config.getAttribute(RegistryConstants.ATTR_DESCRIPTION);
        this.custom = false;
    }

    public DriverDescriptor getDriver()
    {
        return driver;
    }

    @Override
    public DBPDriverFileType getType()
    {
        return type;
    }

    @Override
    public OSDescriptor getSystem()
    {
        return system;
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public String getFileType() {
        return null;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isCustom()
    {
        return custom;
    }

    public void setCustom(boolean custom)
    {
        this.custom = custom;
    }

    @Override
    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    @Override
    public boolean isLocal()
    {
        return path.startsWith(DriverDescriptor.DRIVERS_FOLDER) || isMavenArtifact();
    }

    private boolean isMavenArtifact() {
        return path.startsWith(FILE_SOURCE_MAVEN);
    }

    @Nullable
    private MavenArtifact getMavenArtifact() {
        String[] info = getMavenArtifactInfo();
        if (info == null) {
            return null;
        } else {
            return MavenRegistry.getInstance().findArtifact(info[0], info[1]);
        }
    }

    private String[] getMavenArtifactInfo() {
        String mavenUri = path;
        int divPos = mavenUri.indexOf('/');
        if (divPos < 0) {
            log.warn("Bad maven uri: " + mavenUri);
            return null;
        }
        mavenUri = mavenUri.substring(divPos + 1);
        divPos = mavenUri.indexOf(':');
        if (divPos < 0) {
            log.warn("Bad maven uri, no group id: " + mavenUri);
            return null;
        }
        String groupId = mavenUri.substring(0, divPos);
        int divPos2 = mavenUri.indexOf(':', divPos + 1);
        if (divPos2 < 0) {
            log.warn("Bad maven uri, no artifact id: " + mavenUri);
            return null;
        }
        String artifactId = mavenUri.substring(divPos + 1, divPos2);
        String version = null;
        int divPos3 = mavenUri.indexOf(':', divPos2 + 1);
        if (divPos3 < 0) {
            version = DEFAULT_MAVEN_VERSION;
        } else {
            version = mavenUri.substring(divPos2 + 1, divPos2);;
        }
        return new String[] {groupId, artifactId, version};
    }

    private File detectLocalFile()
    {
        if (isMavenArtifact()) {
            MavenArtifact artifact = getMavenArtifact();
            if (artifact != null) {
                MavenLocalVersion localVersion = artifact.getActiveLocalVersion();
                if (localVersion != null) {
                    return localVersion.getCacheFile();
                }
            }
            return null;
        }
        // Try to use relative path from installation dir
        File file = new File(new File(Platform.getInstallLocation().getURL().getFile()), path);
        if (!file.exists()) {
            // Use custom drivers path
            file = new File(DriverDescriptor.getCustomDriversHome(), path);
        }
        return file;
    }

    @Nullable
    public String getExternalURL() {
        if (path.startsWith(FILE_SOURCE_PLATFORM)) {
            return path;
        } else if (isMavenArtifact()) {
            MavenArtifact artifact = getMavenArtifact();
            if (artifact != null) {
                MavenLocalVersion localVersion = artifact.getActiveLocalVersion();
                if (localVersion != null) {
                    return localVersion.getExternalURL();
                }
            }
            return null;
        } else {
            String primarySource = DriverDescriptor.getDriversPrimarySource();
            if (!primarySource.endsWith("/") && !path.startsWith("/")) {
                primarySource += '/';
            }
            return primarySource + path;
        }
    }


    @Nullable
    @Override
    public File getLocalFile()
    {
        if (path.startsWith(FILE_SOURCE_PLATFORM)) {
            try {
                return RuntimeUtils.getPlatformFile(path);
            } catch (IOException e) {
                log.warn("Bad file URL: " + path, e);
            }
        }
        // Try to use direct path
        File libraryFile = new File(path);
        if (libraryFile.exists()) {
            return libraryFile;
        }
        // Try to get local file
        File platformFile = detectLocalFile();
        if (platformFile != null && platformFile.exists()) {
            // Relative file do not exists - use plain one
            return platformFile;
        }

        // Try to get from plugin's bundle/from external resources
        {
            URL url = driver.getProviderDescriptor().getContributorBundle().getEntry(path);
            if (url == null) {
                // Find in external resources
                url = driver.getProviderDescriptor().getRegistry().findResourceURL(path);
            }
            if (url != null) {
                try {
                    url = FileLocator.toFileURL(url);
                }
                catch (IOException ex) {
                    log.warn(ex);
                }
            }
            if (url != null) {
                return new File(url.getFile());
            }
        }

        // Nothing fits - just return plain url
        return platformFile;
    }

    @Override
    public boolean matchesCurrentPlatform()
    {
        return system == null || system.matches(DBeaverCore.getInstance().getLocalSystem());
    }

    private String getMavenArtifactFileName() {
        String artifactName = path.substring(FILE_SOURCE_MAVEN.length());
        String ext = fileExtension;
        String[] artifactNameParts = artifactName.split(":");
        if (artifactNameParts.length != 3) {
            log.warn("Bad Maven artifact reference: " + artifactName);
            return artifactName.replace(':', '.');
        }
        String artifactFileName = DriverDescriptor.DRIVERS_FOLDER + "/" + artifactNameParts[1] + "/" +
            artifactNameParts[0] + "." + artifactNameParts[1] + "." + artifactNameParts[2];
        if (CommonUtils.isEmpty(ext)) {
            switch (type) {
                case lib:
                    return System.mapLibraryName(artifactFileName);
                case executable:
                    if (RuntimeUtils.isPlatformWindows()) {
                        ext = "exe";
                    } else {
                        return artifactFileName;
                    }
                    break;
                case jar:
                    ext = "jar";
                    break;
                case license:
                    ext = "txt";
                    break;
            }
        }
        return artifactFileName + '.' + ext;
    }

    void downloadLibraryFile(DBRProgressMonitor monitor) throws IOException, InterruptedException
    {
        if (isMavenArtifact()) {
            downloadMavenArtifact(monitor);
        }
        DBPPreferenceStore prefs = DBeaverCore.getGlobalPreferenceStore();
        String proxyHost = prefs.getString(DBeaverPreferences.UI_PROXY_HOST);
        Proxy proxy = null;
        if (!CommonUtils.isEmpty(proxyHost)) {
            int proxyPort = prefs.getInt(DBeaverPreferences.UI_PROXY_PORT);
            if (proxyPort <= 0) {
                log.warn("Invalid proxy port: " + proxyPort);
            }
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }
        String externalURL = getExternalURL();
        if (externalURL == null) {
            throw new IOException("Unresolved file reference: " + getPath());
        }

        URL url = new URL(externalURL);
        monitor.beginTask("Check file " + url.toString() + "...", 1);
        monitor.subTask("Connecting to the server");
        final HttpURLConnection connection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);
        connection.setRequestMethod("GET"); //$NON-NLS-1$
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty(
            "User-Agent",  //$NON-NLS-1$
            DBeaverCore.getProductTitle());
        connection.connect();
        if (connection.getResponseCode() != 200) {
            throw new IOException("Can't find driver file '" + url + "': " + connection.getResponseMessage());
        }
        monitor.worked(1);
        monitor.done();

        final int contentLength = connection.getContentLength();
        monitor.beginTask("Download " + externalURL, contentLength);
        boolean success = false;
        final File localFile = getLocalFile();
        if (localFile == null) {
            throw new IOException("No target file for '" + getPath() + "'");
        }
        final File localDir = localFile.getParentFile();
        if (!localDir.exists()) {
            if (!localDir.mkdirs()) {
                log.warn("Can't create directory for local driver file '" + localDir.getAbsolutePath() + "'");
            }
        }
        final OutputStream outputStream = new FileOutputStream(localFile);
        try {
            final InputStream inputStream = connection.getInputStream();
            try {
                final NumberFormat numberFormat = NumberFormat.getNumberInstance();
                byte[] buffer = new byte[10000];
                int totalRead = 0;
                for (;;) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    monitor.subTask(numberFormat.format(totalRead) + "/" + numberFormat.format(contentLength));
                    final int count = inputStream.read(buffer);
                    if (count <= 0) {
                        success = true;
                        break;
                    }
                    outputStream.write(buffer, 0, count);
                    monitor.worked(count);
                    totalRead += count;
                }
            }
            finally {
                ContentUtils.close(inputStream);
            }
        } finally {
            ContentUtils.close(outputStream);
            if (!success) {
                if (!localFile.delete()) {
                    log.warn("Can't delete local driver file '" + localFile.getAbsolutePath() + "'");
                }
            }
        }
        monitor.done();
    }

    private void downloadMavenArtifact(DBRProgressMonitor monitor) throws IOException {
        String[] artifactInfo = getMavenArtifactInfo();
        if (artifactInfo == null) {
            throw new IOException("Bad Maven artifact path '" + path + "'");
        }
        MavenArtifact artifact = getMavenArtifact();
        if (artifact == null) {
            throw new IOException("Maven artifact '" + path + "' not found");
        }
        MavenLocalVersion localVersion = artifact.getActiveLocalVersion();
        if (localVersion != null && localVersion.getCacheFile().exists()) {
            // Already cached
            return;
        }
        monitor.beginTask("Download Maven artifact '" + artifact + "'", 3);
        try {
            monitor.subTask("Download metadata from " + artifact.getRepository().getUrl());
            artifact.loadMetadata();
            monitor.worked(1);

            String versionInfo = artifactInfo[2];
            if (versionInfo.equals("release")) {
                versionInfo = artifact.getReleaseVersion();
            } else if (versionInfo.equals("latest")) {
                versionInfo = artifact.getLatestVersion();
            }
            monitor.subTask("Download binaries for version " + versionInfo);
            if (localVersion == null) {
                artifact.makeLocalVersion(versionInfo, true);
            }
            monitor.worked(1);
            monitor.subTask("Save repository cache");
            artifact.getRepository().flushCache();
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    @Override
    public String toString() {
        return path;
    }

}
