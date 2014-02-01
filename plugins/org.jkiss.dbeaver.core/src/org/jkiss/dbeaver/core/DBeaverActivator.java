/*
 * Copyright (C) 2010-2014 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.core;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jkiss.dbeaver.DBeaverConstants;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The activator class controls the plug-in life cycle
 */
public class DBeaverActivator extends AbstractUIPlugin
{

    // The shared instance
    private static DBeaverActivator instance;
    private ResourceBundle resourceBundle;
    private PrintStream debugWriter;

    /**
     * The constructor
     */
    public DBeaverActivator()
    {
    }

    public static DBeaverActivator getInstance()
    {
        return instance;
    }

    /*
      * (non-Javadoc)
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
      */
    @Override
    public void start(BundleContext context)
        throws Exception
    {
        super.start(context);

        instance = this;

        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", DBeaverLogger.class.getName()); //$NON-NLS-1$
        try {
            resourceBundle = ResourceBundle.getBundle(CoreMessages.BUNDLE_NAME);
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /*
      * (non-Javadoc)
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
      */
    @Override
    public void stop(BundleContext context)
        throws Exception
    {
        this.shutdownCore();

        if (debugWriter != null) {
            debugWriter.close();
            debugWriter = null;
        }
        instance = null;

        super.stop(context);
    }

    public synchronized PrintStream getDebugWriter()
    {
        if (debugWriter == null) {
            File logPath = Platform.getLogFileLocation().toFile().getParentFile();
            File debugLogFile = new File(logPath, "dbeaver-debug.log"); //$NON-NLS-1$
            if (debugLogFile.exists()) {
                if (!debugLogFile.delete()) {
                    System.err.println("Could not delete debug log file"); //$NON-NLS-1$
                }
            }
            try {
                debugWriter = new PrintStream(debugLogFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace(System.err);
            }
        }
        return debugWriter;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return imageDescriptorFromPlugin(DBeaverConstants.PLUGIN_ID, path);
    }

    /**
     * Returns the plugin's resource bundle,
     * @return core resource bundle
     */
    public static ResourceBundle getResourceBundle()
    {
        return getInstance().resourceBundle;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = getResourceBundle();
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    private void shutdownCore()
    {
        try {
// Dispose core
            if (DBeaverCore.instance != null) {
                DBeaverCore.instance.dispose();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logMessage("Internal error after shutdown process:" + e.getMessage()); //$NON-NLS-1$
        }
    }

    private void logMessage(String message)
    {
        getDebugWriter().print(message);
    }

}
