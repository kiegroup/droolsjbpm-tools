package org.drools.ide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Handles the images used in this plugin.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsPluginImages {

    public static final String IMG_LOGICAL = "ImageLogical";
    public static final String IMG_LOGICAL_DISABLED = "ImageLogicalDisabled";
    public static final String REFRESH_LOG = "RefreshLog";
    public static final String REFRESH_LOG_DISABLED = "RefreshLogDisabled";
    public static final String OPEN_LOG = "OpenLog";
    public static final String DELETE_LOG = "ClearLog";
    public static final String DELETE_LOG_DISABLED = "ClearLogDisabled";
    public static final String ASSERT_OBJECT = "AssertObject";
    public static final String MODIFY_OBJECT = "ModifyObject";
    public static final String RETRACT_OBJECT = "RetractObject";
    public static final String CREATE_ACTIVATION = "CreateActivation";
    public static final String CANCEL_ACTIVATION = "CancelActivation";
    public static final String EXECUTE_ACTIVATION = "ExecuteActivation";
    private static ImageRegistry imageRegistry;
    private static Map imageDescriptors;
    private static URL ICON_BASE_URL= null;
    static {
        String pathSuffix = "icons/";
        ICON_BASE_URL= DroolsIDEPlugin.getDefault().getBundle().getEntry(pathSuffix);
    }
    
    private static void declareImages() {
        declareRegistryImage(IMG_LOGICAL, "logical_structure.gif");
        declareRegistryImage(IMG_LOGICAL_DISABLED, "logical_structure_disabled.gif");
        declareRegistryImage(REFRESH_LOG, "refresh.gif");
        declareRegistryImage(REFRESH_LOG_DISABLED, "refresh_disabled.gif");
        declareRegistryImage(OPEN_LOG, "open.gif");
        declareRegistryImage(DELETE_LOG, "clear.gif");
        declareRegistryImage(DELETE_LOG_DISABLED, "clear_disabled.gif");
        declareRegistryImage(ASSERT_OBJECT, "greensquare.gif");
        declareRegistryImage(MODIFY_OBJECT, "yellowsquare.gif");
        declareRegistryImage(RETRACT_OBJECT, "redsquare.gif");
        declareRegistryImage(CREATE_ACTIVATION, "arrowright.gif");
        declareRegistryImage(CANCEL_ACTIVATION, "arrowleft.gif");
        declareRegistryImage(EXECUTE_ACTIVATION, "bluediamond.gif");
    }

    /**
     * Declare an Image in the registry table.
     * @param key   The key to use when registering the image
     * @param path  The path where the image can be found. This path is relative to where
     *              this plugin class is found (i.e. typically the packages directory)
     */
    private final static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
        try {
            desc= ImageDescriptor.createFromURL(makeIconFileURL(path));
        } catch (MalformedURLException e) {
            DroolsIDEPlugin.log(e);
        }
        imageRegistry.put(key, desc);
        imageDescriptors.put(key, desc);
    }
    
    /**
     * Returns the ImageRegistry.
     */
    public static ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            initializeImageRegistry();
        }
        return imageRegistry;
    }

    public static ImageRegistry initializeImageRegistry() {
        imageRegistry= new ImageRegistry();
        imageDescriptors = new HashMap(30);
        declareImages();
        return imageRegistry;
    }

    /**
     * Returns the <code>Image</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static Image getImage(String key) {
        return getImageRegistry().get(key);
    }
    
    /**
     * Returns the <code>ImageDescriptor</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static ImageDescriptor getImageDescriptor(String key) {
        if (imageDescriptors == null) {
            initializeImageRegistry();
        }
        return (ImageDescriptor)imageDescriptors.get(key);
    }
    
    private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
        if (ICON_BASE_URL == null) {
            throw new MalformedURLException();
        }
            
        return new URL(ICON_BASE_URL, iconPath);
    }
}