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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.PackageRegistry;
import org.drools.compiler.ProcessBuilder;
import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.builder.Util;
import org.drools.eclipse.dsl.editor.DSLAdapter;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.io.ResourceFactory;
import org.drools.lang.descr.PackageDescr;
import org.drools.process.core.Process;
import org.drools.rule.Package;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.drools.util.StringUtils;
import org.drools.xml.XmlProcessReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsEclipsePlugin extends AbstractUIPlugin {

    public static final int             INTERNAL_ERROR             = 120;
    public static final String          PLUGIN_ID                  = "org.drools.eclipse";
    public static final String          BUILD_RESULT_PACKAGE       = "Package";
    public static final String          BUILD_RESULT_PACKAGE_DESCR = "PackageDescr";

    //The shared instance.
    private static DroolsEclipsePlugin  plugin;
    //Resource bundle.
    private ResourceBundle              resourceBundle;
    private Map<String, Color>          colors                     = new HashMap<String, Color>();
    private Map<IResource, DRLInfo>     parsedRules                = new HashMap<IResource, DRLInfo>();
    private Map<IResource, DRLInfo>     compiledRules              = new HashMap<IResource, DRLInfo>();
    private Map<String, RuleInfo>       ruleInfoByClassNameMap     = new HashMap<String, RuleInfo>();
    private Map<String, FunctionInfo>   functionInfoByClassNameMap = new HashMap<String, FunctionInfo>();
    private Map<IResource, ProcessInfo> processInfos               = new HashMap<IResource, ProcessInfo>();
    private Map<String, ProcessInfo>    processInfosById           = new HashMap<String, ProcessInfo>();
    private boolean                     useCachePreference;

    private FormColors                  ruleBuilderFormColors;

    /**
     * The constructor.
     */
    public DroolsEclipsePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start( context );
        IPreferenceStore preferenceStore = getPreferenceStore();
        useCachePreference = preferenceStore.getBoolean( IDroolsConstants.CACHE_PARSED_RULES );
        preferenceStore.addPropertyChangeListener( new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ( IDroolsConstants.CACHE_PARSED_RULES.equals( event.getProperty() ) ) {
                    useCachePreference = ((Boolean) event.getNewValue()).booleanValue();
                    if ( !useCachePreference ) {
                        clearCache();
                    }
                }
            }
        } );

    }

    public void clearCache() {
        parsedRules.clear();
        compiledRules.clear();
        ruleInfoByClassNameMap.clear();
        functionInfoByClassNameMap.clear();
        processInfos.clear();
        processInfosById = null;
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop( context );
        plugin = null;
        resourceBundle = null;
        parsedRules = null;
        compiledRules = null;
        processInfos = null;
        processInfosById = null;
        for (Color color: colors.values()) {
            color.dispose();
        }
    }

    /**
     * Returns the shared instance.
     */
    public static DroolsEclipsePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = DroolsEclipsePlugin.getDefault().getResourceBundle();
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
        try {
            if ( resourceBundle == null ) resourceBundle = ResourceBundle.getBundle( "droolsIDE.DroolsIDEPluginResources" );
        } catch ( MissingResourceException x ) {
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

        DroolsEclipsePlugin plugin = getDefault();
        ImageRegistry reg = plugin.getImageRegistry();
        ImageDescriptor des = reg.getDescriptor( path );
        if ( des == null ) {
            des = AbstractUIPlugin.imageDescriptorFromPlugin( "org.drools.eclipse",
                                                              path );
            reg.put( path,
                     des );
        }
        return des;
    }

    public static String getUniqueIdentifier() {
        if ( getDefault() == null ) {
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
    }

    public static void log(Throwable t) {
        Throwable top = t;
        if ( t instanceof DebugException ) {
            DebugException de = (DebugException) t;
            IStatus status = de.getStatus();
            if ( status.getException() != null ) {
                top = status.getException();
            }
        }
        log( new Status( IStatus.ERROR,
                         getUniqueIdentifier(),
                         INTERNAL_ERROR,
                         "Internal error in Drools Plugin: ",
                         top ) );
    }

    public static void log(IStatus status) {
        getDefault().getLog().log( status );
    }

    public Color getColor(String type) {
        return (Color) colors.get( type );
    }

    public void setColor(String type,
                         Color color) {
        colors.put( type,
                    color );
    }

    protected void initializeDefaultPreferences(IPreferenceStore store) {
        store.setDefault( IDroolsConstants.BUILD_ALL,
                          false );
        store.setDefault( IDroolsConstants.EDITOR_FOLDING,
                          true );
        store.setDefault( IDroolsConstants.CACHE_PARSED_RULES,
                          true );
        store.setDefault( IDroolsConstants.DSL_RULE_EDITOR_COMPLETION_FULL_SENTENCES,
                          true );
        store.setDefault( IDroolsConstants.SKIN,
                          "BPMN2" );
        store.setDefault( IDroolsConstants.ALLOW_NODE_CUSTOMIZATION,
        				  false );
        store.setDefault( IDroolsConstants.INTERNAL_API,
        				  2 );
        store.setDefault( IDroolsConstants.FLOW_NODES, 
        		          "1111111111111" );
    }

    public DRLInfo parseResource(IResource resource,
                                 boolean compile) throws DroolsParserException {
        DRLInfo result = (DRLInfo) compiledRules.get( resource );
        if ( result == null && !compile ) {
            result = (DRLInfo) parsedRules.get( resource );
        }
        if ( result != null ) {
            return result;
        }
        return generateParsedResource( resource,
                                       compile );
    }

    public DRLInfo parseResource(AbstractRuleEditor editor,
                                 boolean useUnsavedContent,
                                 boolean compile) throws DroolsParserException {
        IResource resource = editor.getResource();
        if ( !editor.isDirty() || !useUnsavedContent ) {
            DRLInfo result = (DRLInfo) compiledRules.get( resource );
            if ( result == null && !compile ) {
                result = (DRLInfo) parsedRules.get( resource );
            }
            if ( result != null ) {
                return result;
            }
        }
        if ( !editor.isDirty() ) {
            return generateParsedResource( editor.getContent(),
                                           resource,
                                           true,
                                           compile );
        }
        // TODO: can we cache result when using unsaved content as well? 
        return generateParsedResource( editor.getContent(),
                                       resource,
                                       !useUnsavedContent,
                                       compile );
    }

    public DRLInfo parseXLSResource(String content,
                                    IResource resource) throws DroolsParserException {
        DRLInfo result = (DRLInfo) compiledRules.get( resource );
        if ( result != null ) {
            return result;
        }
        return generateParsedResource( content,
                                       resource,
                                       false,
                                       true );
    }

    public DRLInfo parseBRLResource(String content,
                                    IResource resource) throws DroolsParserException {
        DRLInfo result = (DRLInfo) compiledRules.get( resource );
        if ( result != null ) {
            return result;
        }
        return generateParsedResource( content,
                                       resource,
                                       false,
                                       true );
    }

    public DRLInfo parseGDSTResource(String content, IResource resource) throws DroolsParserException {
		DRLInfo result = (DRLInfo) compiledRules.get(resource);
		if (result != null) {
			return result;
		}
		return generateParsedResource(content, resource, false, true);
	}

    public void invalidateResource(IResource resource) {
        DRLInfo cached = (DRLInfo) compiledRules.remove( resource );
        if ( cached != null ) {
            RuleInfo[] ruleInfos = cached.getRuleInfos();
            for ( int i = 0; i < ruleInfos.length; i++ ) {
                ruleInfoByClassNameMap.remove( ruleInfos[i].getClassName() );
            }
            FunctionInfo[] functionInfos = cached.getFunctionInfos();
            for ( int i = 0; i < functionInfos.length; i++ ) {
                functionInfoByClassNameMap.remove( functionInfos[i].getClassName() );
            }
        }
        parsedRules.remove( resource );
        ProcessInfo processInfo = processInfos.remove( resource );
        if ( processInfo != null ) {
            processInfosById.remove( processInfo.getProcessId() );
        }
    }

    private DRLInfo generateParsedResource(IResource resource,
                                           boolean compile) throws DroolsParserException {
        if ( resource instanceof IFile ) {
            IFile file = (IFile) resource;
            try {
                String content = new String( Util.getResourceContentsAsCharArray( file ) );
                return generateParsedResource( content,
                                               file,
                                               true,
                                               compile );
            } catch ( CoreException e ) {
                log( e );
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	public DRLInfo generateParsedResource(String content,
                                          IResource resource,
                                          boolean useCache,
                                          boolean compile) throws DroolsParserException {
        useCache = useCache && useCachePreference;
        DrlParser parser = new DrlParser();
        try {
            Reader dslReader = DSLAdapter.getDSLContent( content,
                                                         resource );
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
            String level = null;
            // resource could be null when opening a read-only remote file 
            if ( resource != null && resource.getProject().getNature( "org.eclipse.jdt.core.javanature" ) != null ) {
                IJavaProject project = JavaCore.create( resource.getProject() );
                newLoader = ProjectClassLoader.getProjectClassLoader( project );
                level = project.getOption( JavaCore.COMPILER_COMPLIANCE,
                                           true );
            }
            try {
                Thread.currentThread().setContextClassLoader( newLoader );
                PackageBuilderConfiguration builder_configuration = new PackageBuilderConfiguration();
                if ( level != null ) {
                    JavaDialectConfiguration javaConf = (JavaDialectConfiguration) builder_configuration.getDialectConfiguration( "java" );
                    javaConf.setJavaLanguageLevel( level );
                }
                builder_configuration.setClassLoader( newLoader );

                // first parse the source
                PackageDescr packageDescr = null;
                List<DroolsError> parserErrors = null;
                if ( useCache && resource != null) {
                    DRLInfo cachedDrlInfo = (DRLInfo) parsedRules.get( resource );
                    if ( cachedDrlInfo != null ) {
                        packageDescr = cachedDrlInfo.getPackageDescr();
                        parserErrors = cachedDrlInfo.getParserErrors();
                    }
                }

                if ( packageDescr == null ) {
                    if ( dslReader != null ) {
                        packageDescr = parser.parse( true, content, dslReader );
                    } else {
                        packageDescr = parser.parse( true, content );
                    }
                    parserErrors = parser.getErrors();
                }
                PackageBuilder builder = new PackageBuilder( builder_configuration );
                DRLInfo result = null;
                // compile parsed rules if necessary
                if ( packageDescr != null && compile && !parser.hasErrors()) {
                    // check whether a .package file exists and add it
                    if ( resource != null && resource.getParent() != null ) {
                        MyResourceVisitor visitor = new MyResourceVisitor();
                        resource.getParent().accept( visitor,
                                                     IResource.DEPTH_ONE,
                                                     IResource.NONE );
                        IResource packageDef = visitor.getPackageDef();
                        if ( packageDef != null ) {
                            PackageDescr desc = parseResource( packageDef,
                                                               false ).getPackageDescr();
                            if (desc != null){
                                builder.addPackage( desc);
                            }
                        }
                    }

                    builder.addPackage( packageDescr );
                                        
                    // make sure the namespace is set, use default if necessary, as this is used to build the DRLInfo
                    if ( StringUtils.isEmpty( packageDescr.getNamespace() ) ) {
                        packageDescr.setNamespace( builder.getPackageBuilderConfiguration().getDefaultPackageName() );
                    }
                    
                    result = new DRLInfo( resource == null ? "" : resource.getProjectRelativePath().toString(),
                                          packageDescr,
                                          parserErrors,
                                          builder.getPackage(),
                                          builder.getErrors().getErrors(),                                          
                                          builder.getPackageRegistry( packageDescr.getNamespace() ).getDialectCompiletimeRegistry() );
                } else {
                    result = new DRLInfo( resource == null ? "" : resource.getProjectRelativePath().toString(),
                                          packageDescr,
                                          parserErrors,
                                          new PackageRegistry(builder, new Package("")).getDialectCompiletimeRegistry() );
                }

                // cache result
                if ( useCache && resource != null) {
                    if ( compile && !parser.hasErrors() ) {
                    	parsedRules.remove( resource );
                        compiledRules.put( resource,
                                           result );
                        RuleInfo[] ruleInfos = result.getRuleInfos();
                        for ( int i = 0; i < ruleInfos.length; i++ ) {
                            ruleInfoByClassNameMap.put( ruleInfos[i].getClassName(),
                                                        ruleInfos[i] );
                        }
                        FunctionInfo[] functionInfos = result.getFunctionInfos();
                        for ( int i = 0; i < functionInfos.length; i++ ) {
                            functionInfoByClassNameMap.put( functionInfos[i].getClassName(),
                                                            functionInfos[i] );
                        }
                    } else {
                        parsedRules.put( resource,
                                         result );
                    }
                }
                return result;
            } finally {
                Thread.currentThread().setContextClassLoader( oldLoader );
            }
        } catch ( CoreException e ) {
            log( e );
        }
        return null;
    }

    public RuleInfo getRuleInfoByClass(String ruleClassName) {
        return (RuleInfo) ruleInfoByClassNameMap.get( ruleClassName );
    }

    public FunctionInfo getFunctionInfoByClass(String functionClassName) {
        return (FunctionInfo) functionInfoByClassNameMap.get( functionClassName );
    }

    public ProcessInfo parseProcess(String input,
                                    IResource resource) {
        try {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newLoader = this.getClass().getClassLoader();
            String level = null;
            if ( resource.getProject().getNature( "org.eclipse.jdt.core.javanature" ) != null ) {
                IJavaProject project = JavaCore.create( resource.getProject() );
                newLoader = ProjectClassLoader.getProjectClassLoader( project );
                level = project.getOption( JavaCore.COMPILER_COMPLIANCE,
                                           true );
            }
            try {
                Thread.currentThread().setContextClassLoader( newLoader );
                PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
                if ( level != null ) {
                    JavaDialectConfiguration javaConf = (JavaDialectConfiguration) configuration.getDialectConfiguration( "java" );
                    javaConf.setJavaLanguageLevel( level );
                }
                configuration.setClassLoader( newLoader );
                
                XmlProcessReader xmlReader = new XmlProcessReader( configuration.getSemanticModules() );
                Process process = (Process) xmlReader.read( new StringReader( input ) );
                if ( process != null ) {
                    return parseProcess( process,
                                         resource,
                                         configuration );
                } else {
                    throw new IllegalArgumentException( "Could not parse process " + resource );
                }
            } finally {
                Thread.currentThread().setContextClassLoader( oldLoader );
            }
        } catch ( Exception e ) {
            log( e );
        }
        return null;
    }

    public ProcessInfo getProcessInfo(String processId) {
        return processInfosById.get( processId );
    }

    public ProcessInfo parseProcess(Process process,
                                    IResource resource,
                                    PackageBuilderConfiguration config) {
        PackageBuilder packageBuilder = new PackageBuilder( config );
        ProcessBuilder processBuilder = new ProcessBuilder( packageBuilder );
        processBuilder.buildProcess( process, ResourceFactory.newUrlResource(
    		"file://" + resource.getLocation().toString() ) );
        ProcessInfo processInfo = new ProcessInfo( process.getId(),
                                                   process );
        List<DroolsError> errors = new ArrayList<DroolsError>();
        errors.addAll( processBuilder.getErrors() );
        errors.addAll( Arrays.asList( packageBuilder.getErrors().getErrors() ) );
        processInfo.setErrors( errors );
        if ( useCachePreference ) {
            processInfos.put( resource,
                              processInfo );
            processInfosById.put( process.getId(),
                                  processInfo );
        }
        return processInfo;
    }

    /**
     * Form Colors, default colors for now.
     * 
     * @param display
     * @return
     */
    public FormColors getRuleBuilderFormColors(Display display) {
        if ( ruleBuilderFormColors == null ) {
            ruleBuilderFormColors = new FormColors( display );
            ruleBuilderFormColors.markShared();
        }
        return ruleBuilderFormColors;
    }

    private class MyResourceVisitor
        implements
        IResourceVisitor {
        private IResource packageDef;

        public boolean visit(IResource resource) throws CoreException {
            if ( "package".equals( resource.getFileExtension() ) ) {
                packageDef = resource;
            }
            return true;
        }

        public IResource getPackageDef() {
            return packageDef;
        }
    }
}
