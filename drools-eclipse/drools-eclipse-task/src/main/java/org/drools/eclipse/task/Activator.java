package org.drools.eclipse.task;

import org.drools.eclipse.task.preferences.DroolsTaskConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.drools.eclipse.task";

	// The shared instance
	private static Activator plugin;
	
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
    protected void initializeDefaultPreferences(IPreferenceStore store) {
        store.setDefault( DroolsTaskConstants.SERVER_IP_ADDRESS, "127.0.0.1");
        store.setDefault( DroolsTaskConstants.SERVER_PORT, 9123 );
        store.setDefault( DroolsTaskConstants.LANGUAGE, "en-UK" );
    }

}
