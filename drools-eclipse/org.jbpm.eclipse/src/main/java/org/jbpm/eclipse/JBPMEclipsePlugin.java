/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.eclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class JBPMEclipsePlugin extends AbstractUIPlugin {

    public static final String          PLUGIN_ID                  = "org.jbpm.eclipse";

    // The shared instance.
    private static JBPMEclipsePlugin    plugin;

    /**
     * The constructor.
     */
    public JBPMEclipsePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start( context );
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop( context );
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static JBPMEclipsePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = JBPMEclipsePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString( key ) : key;
        } catch ( MissingResourceException e ) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return null;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     * Uses the plug ins image registry to "cache" it.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        JBPMEclipsePlugin plugin = getDefault();
        ImageRegistry reg = plugin.getImageRegistry();
        ImageDescriptor des = reg.getDescriptor(path);
        if (des == null) {
            des = AbstractUIPlugin.imageDescriptorFromPlugin("org.jbpm.eclipse", path);
            reg.put(path, des);
        }
        return des;
    }

    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
    }

    protected void initializeDefaultPreferences(IPreferenceStore store) {
    }

    public static void log(Throwable t) {
        Throwable top = t;
        if (t instanceof DebugException) {
            DebugException de = (DebugException) t;
            IStatus status = de.getStatus();
            if (status.getException() != null) {
                top = status.getException();
            }
        }
        log(new Status(IStatus.ERROR,
                       getUniqueIdentifier(),
                       1033,
                       "Internal error in jBPM Plugin: ",
                       top ) );
    }

    public static void log(IStatus status) {
        getDefault().getLog().log( status );
    }
}
