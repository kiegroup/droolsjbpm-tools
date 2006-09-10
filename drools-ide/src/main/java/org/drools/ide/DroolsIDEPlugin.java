package org.drools.ide;
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

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.drools.compiler.DrlParser;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.builder.Util;
import org.drools.ide.editors.DSLAdapter;
import org.drools.ide.preferences.IDroolsConstants;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
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
    public static final String BUILD_RESULT_PACKAGE = "Package";
    public static final String BUILD_RESULT_PACKAGE_DESCR = "PackageDescr";
    
	//The shared instance.
	private static DroolsIDEPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private Map colors = new HashMap();
	private Map parsedRules = new HashMap();
	private Map ruleInfoByRuleNameMap = new HashMap();
	private Map ruleInfoByClassNameMap = new HashMap();
	
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
		parsedRules = null;
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
     * Uses the plug ins image registry to "cache" it.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
        
        DroolsIDEPlugin plugin = getDefault();
        ImageRegistry reg = plugin.getImageRegistry();
        ImageDescriptor des = reg.getDescriptor( path );
        if (des == null) {
            des = AbstractUIPlugin.imageDescriptorFromPlugin("org.drools.ide", path);
            reg.put( path, des );
        }
		return des;
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
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(IDroolsConstants.BUILD_ALL, false);
		store.setDefault(IDroolsConstants.EDITOR_FOLDING, true);
	}
	
	public PackageDescr parseResource(IResource resource, boolean compile) {
		// TODO cache result and clear cache if necessary, taking properties into account
		PackageDescr result = (PackageDescr) parsedRules.get(resource);
		if (result != null) {
			return result;
		}
		result = generateParsedResource(resource, compile);
		if (result != null) {
			parsedRules.put(resource, result);
		}
		return result;
	}
	
	private PackageDescr generateParsedResource(IResource resource, boolean compile) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
	        DrlParser parser = new DrlParser();
	        try {
	        	String content = new String(Util.getResourceContentsAsCharArray(file));
	            Reader dslReader = DSLAdapter.getDSLContent(content, file);
	            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
	            ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
	            PackageBuilderConfiguration builder_configuration = new PackageBuilderConfiguration();
	            if (file.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
	                IJavaProject project = JavaCore.create(file.getProject());
	                newLoader = ProjectClassLoader.getProjectClassLoader(project);
	                String level = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
	            	builder_configuration.setJavaLanguageLevel(level);
	            }
	            try {
	            	builder_configuration.setClassLoader(newLoader);
	                Thread.currentThread().setContextClassLoader(newLoader);
	                
	                //First we parse the source
	                PackageDescr packageDescr = null;
                	if (dslReader != null) { 
                		packageDescr = parser.parse(content, dslReader);
                	} else {
                		packageDescr = parser.parse(content);
                	}
                	if (compile && !parser.hasErrors()) {
                        PackageBuilder builder = new PackageBuilder(builder_configuration);
                        builder.addPackage(packageDescr);
                        Iterator rules = packageDescr.getRules().iterator();
                    	while (rules.hasNext()) {
                    		RuleDescr descr = (RuleDescr) rules.next();
                    		String className = packageDescr.getName() + "." + descr.getClassName();
                    		RuleInfo ruleInfo = new RuleInfo(descr.getName(),
                				packageDescr.getName(), resource.getName(),
                				resource.getFullPath().toString(), className,
                				descr.getConsequenceLine(),
                				builder.getPackage().getPackageCompilationData()
                					.getLineMappings(className).getOffset());
                    		ruleInfoByRuleNameMap.put(packageDescr.getName() + "." + descr.getName(), ruleInfo);
                    		ruleInfoByClassNameMap.put(className, ruleInfo);
                    	}
                    }
                	return packageDescr;
	            } finally {
	                Thread.currentThread().setContextClassLoader(oldLoader);
	            }
	        } catch (Throwable t) {
	        	log(t);
	        }
		}
		return null;
	}
	
	public RuleInfo getRuleInfoByClass(String ruleClassName) {
		return (RuleInfo) ruleInfoByClassNameMap.get(ruleClassName);
	}

	public RuleInfo getRuleInfoByRule(String ruleName) {
		return (RuleInfo) ruleInfoByClassNameMap.get(ruleName);
	}

}
