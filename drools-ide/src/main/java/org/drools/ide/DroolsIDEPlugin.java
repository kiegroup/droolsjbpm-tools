package org.drools.ide;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsIDEPlugin extends AbstractUIPlugin {
    
    public static final int INTERNAL_ERROR = 120;
    public static final String PLUGIN_ID = "org.drools.ide";
    
	//The shared instance.
	private static DroolsIDEPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private Map colors = new HashMap();
	
	/**
	 * The constructor.
	 */
	public DroolsIDEPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
    }

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
		Iterator iterator = colors.values().iterator();
		while (iterator.hasNext()) {
			((Color) iterator.next()).dispose();
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static DroolsIDEPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = DroolsIDEPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("droolsIDE.DroolsIDEPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("DroolsIDE", path);
	}
    
    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
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
        log(new Status(IStatus.ERROR, getUniqueIdentifier(),
            INTERNAL_ERROR, "Internal error in Drools Plugin: ", top));        
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

	public Color getColor(String type) {
		return (Color) colors.get(type);
	}
	
	public void setColor(String type, Color color) {
		colors.put(type, color);
	}
}
