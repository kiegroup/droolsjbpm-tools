package org.drools.eclipse;
/*
 * Copyright 2005 JBoss Inc
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

import java.net.MalformedURLException;
import java.net.URL;

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
    public static final String INSERT = "Insert";
    public static final String UPDATE = "Update";
    public static final String RETRACT = "RetractO";
    public static final String CREATE_ACTIVATION = "CreateActivation";
    public static final String CANCEL_ACTIVATION = "CancelActivation";
    public static final String EXECUTE_ACTIVATION = "ExecuteActivation";
    public static final String CLASS = "Class";
    public static final String PACKAGE = "Package";
    public static final String METHOD = "Method";
    public static final String VARIABLE = "Variable";
    public static final String DROOLS = "Drools";
    public static final String RULE = "DroolsRule";
    public static final String QUERY = "DroolsQuery";
    public static final String DSL_EXPRESSION = "DslExpression";
    public static final String IMPORT = "Import";
    public static final String DSL = "DSL";
    public static final String GLOBAL = "Global";
    public static final String RULEFLOW = "RuleFlow";
    public static final String RULEFLOW_GROUP = "RuleFlowGroup";
    public static final String RULEFLOW_NODE_TRIGGERED = "RuleFlowNodeTriggered";
    public static final String RULEGROUP = "RuleGroup";
    public static final String DEFAULTRULEGROUP = "DefaultRuleGroup";
    
    // TODO : Change Image
    public static final String GROUPS = "RuleFlowGroup";
    
    private static ImageRegistry imageRegistry;
    private static final String PATH_SUFFIX = "/icons/";
    private static final URL ICON_BASE_URL =
    	DroolsEclipsePlugin.getDefault().getBundle().getEntry(PATH_SUFFIX);
    
    private static void declareImages() {
        declareRegistryImage(IMG_LOGICAL, "logical_structure.gif");
        declareRegistryImage(IMG_LOGICAL_DISABLED, "logical_structure_disabled.gif");
        declareRegistryImage(REFRESH_LOG, "refresh.gif");
        declareRegistryImage(REFRESH_LOG_DISABLED, "refresh_disabled.gif");
        declareRegistryImage(OPEN_LOG, "open.gif");
        declareRegistryImage(DELETE_LOG, "clear.gif");
        declareRegistryImage(DELETE_LOG_DISABLED, "clear_disabled.gif");
        declareRegistryImage(INSERT, "greensquare.GIF");
        declareRegistryImage(UPDATE, "yellowsquare.GIF");
        declareRegistryImage(RETRACT, "redsquare.GIF");
        declareRegistryImage(CREATE_ACTIVATION, "arrowright.GIF");
        declareRegistryImage(CANCEL_ACTIVATION, "arrowleft.GIF");
        declareRegistryImage(EXECUTE_ACTIVATION, "bluediamond.GIF");
        declareRegistryImage(CLASS, "class_obj.gif");
        declareRegistryImage(PACKAGE, "package_obj.gif");
        declareRegistryImage(METHOD, "methpub_obj.gif");
        declareRegistryImage(VARIABLE, "field_private_obj.gif");
        declareRegistryImage(DROOLS, "drools.gif");
        declareRegistryImage(RULE, "drools-rule.GIF");
        declareRegistryImage(QUERY, "drools-query.GIF");
        declareRegistryImage(DSL_EXPRESSION, "dsl_expression.gif");
        declareRegistryImage(IMPORT, "import.gif");
        declareRegistryImage(DSL, "dsl.GIF");
        declareRegistryImage(GLOBAL, "field_public_obj.gif");
        declareRegistryImage(RULEFLOW, "process.gif");
        declareRegistryImage(RULEFLOW_GROUP, "activity.gif");
        declareRegistryImage(RULEFLOW_NODE_TRIGGERED, "node-triggered.gif");
        declareRegistryImage(RULEGROUP, "rulegroup.gif");
        declareRegistryImage(DEFAULTRULEGROUP, "defaultrulegroup.gif");
    }

    /**
     * Declare an Image in the registry table.
     * @param key   The key to use when registering the image
     * @param path  The path where the image can be found. This path is relative to where
     *              this plugin class is found (i.e. typically the packages directory)
     */
    public final static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
        try {
            desc= ImageDescriptor.createFromURL(makeIconFileURL(path));
        } catch (MalformedURLException e) {
            DroolsEclipsePlugin.log(e);
        }
        imageRegistry.put(key, desc);
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
        imageRegistry = new ImageRegistry();
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
    
    public static void putImage(String key, Image image) {
        getImageRegistry().put(key, image);
    }
    
    /**
     * Returns the <code>ImageDescriptor</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
    }
    
    private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
        if (ICON_BASE_URL == null) {
            throw new MalformedURLException();
        }
            
        return new URL(ICON_BASE_URL, iconPath);
    }
}