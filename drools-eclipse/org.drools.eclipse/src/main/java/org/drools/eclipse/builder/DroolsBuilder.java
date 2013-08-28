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

package org.drools.eclipse.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.drools.compiler.commons.jci.problems.CompilationProblem;
import org.drools.compiler.compiler.DescrBuildError;
import org.drools.compiler.compiler.DroolsError;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.FactTemplateError;
import org.drools.compiler.compiler.FieldTemplateError;
import org.drools.compiler.compiler.FunctionError;
import org.drools.compiler.compiler.GlobalError;
import org.drools.compiler.compiler.ImportError;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.compiler.RuleBuildError;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.drools.eclipse.wizard.project.NewDroolsProjectWizard;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.compiler.lang.ExpanderException;
import org.drools.template.parser.DecisionTableParseException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilderResult;
import org.kie.internal.io.ResourceFactory;

/**
 * Automatically syntax checks .drl files and adds possible errors or warnings
 * to the problem list. Nominally is triggerd on save.
 */
public class DroolsBuilder extends IncrementalProjectBuilder {

    public static final String       BUILDER_ID = "org.drools.eclipse.droolsbuilder";

    private boolean isKieProject = false;

    protected IProject[] build(int kind,
                               Map args,
                               IProgressMonitor monitor)
                                                        throws CoreException {
        IProject currentProject = getProject();
        if ( currentProject == null || !currentProject.isAccessible() ) {
            return new IProject[0];
        }

        try {
            if ( monitor != null && monitor.isCanceled() ) throw new OperationCanceledException();

            if ( kind == IncrementalProjectBuilder.FULL_BUILD ) {
                fullBuild( monitor );
            } else {
                IResourceDelta delta = getDelta( getProject() );
                if ( delta == null ) {
                    fullBuild( monitor );
                } else {
                    incrementalBuild( delta,
                                      monitor );
                }
            }
        } catch ( CoreException e ) {
            IMarker marker = currentProject.createMarker( IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER );
            marker.setAttribute( IMarker.MESSAGE,
                                 "Error when trying to build Drools project: " + e.getLocalizedMessage() );
            marker.setAttribute( IMarker.SEVERITY,
                                 IMarker.SEVERITY_ERROR );
        }

        return getRequiredProjects( currentProject );
    }

    protected void fullBuild(IProgressMonitor monitor) throws CoreException {
        removeProblemsFor( getProject() );
        IJavaProject project = JavaCore.create( getProject() );
        IClasspathEntry[] classpathEntries = project.getRawClasspath();
        for ( int i = 0; i < classpathEntries.length; i++ ) {
            if ( NewDroolsProjectWizard.DROOLS_CLASSPATH_CONTAINER_PATH.equals( classpathEntries[i].getPath().toString() ) ) {
                String[] jars = DroolsRuntimeManager.getDroolsRuntimeJars( getProject() );
                if ( jars == null || jars.length == 0 ) {
                    String runtime = DroolsRuntimeManager.getDroolsRuntime( getProject() );
                    IMarker marker = getProject().createMarker( IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER );
                    if ( runtime == null ) {
                        marker.setAttribute( IMarker.MESSAGE,
                                             "Could not find default Drools runtime" );
                    } else {
                        marker.setAttribute( IMarker.MESSAGE,
                                             "Could not find Drools runtime " + runtime );
                    }
                    marker.setAttribute( IMarker.SEVERITY,
                                         IMarker.SEVERITY_ERROR );
                    return;
                }
            }
        }

        isKieProject = false;
        DroolsBuilderVisitor droolsBuilderVisitor = new DroolsBuilderVisitor();
        getProject().accept( droolsBuilderVisitor );
        droolsBuilderVisitor.build();
    }

    protected void incrementalBuild(IResourceDelta delta,
                                    IProgressMonitor monitor) throws CoreException {
        IPreferenceStore store = DroolsEclipsePlugin.getDefault().getPreferenceStore();
        boolean fullBuild = store.getBoolean( IDroolsConstants.CROSS_BUILD ) || store.getBoolean( IDroolsConstants.BUILD_ALL ) || isKieProject;

        if ( !fullBuild ) {
            fullBuild = DroolsEclipsePlugin.getDefault().resetForceFullBuild();
        }

        if ( fullBuild ) {
            // to make sure that all rules are checked when a java file is changed
            fullBuild( monitor );
        } else {
            delta.accept( new DroolsBuildDeltaVisitor() );
        }
    }

    private class DroolsBuilderVisitor
		    implements
		    IResourceVisitor {
    	
        private final List<IResource> resources = new ArrayList<IResource>();
        
        public boolean visit(IResource resource) throws CoreException {
            if ( isInOutputDirectory( resource ) || !exists( resource ) ) {
                return false;
            }
            if ( resource instanceof IFile) {
            	isKieProject |= resource.getProjectRelativePath().toString().endsWith(KieModuleModelImpl.KMODULE_JAR_PATH);
            	resources.add(resource);
            }
            return true;
        }
        
        public void build() throws CoreException {
        	if (isKieProject) {
        		doBuildKieProject();
        	} else if ( DroolsEclipsePlugin.getDefault().getPreferenceStore().getBoolean( IDroolsConstants.CROSS_BUILD ) ) {
            	doBatchBuild();
            } else {
            	doBuild();
            }
        }

        private void doBuildKieProject() throws CoreException {
        	KieServices ks = KieServices.Factory.get();
        	KieFileSystem kfs = ks.newKieFileSystem();
        	
        	Map<String, IResource> resourcesMap = new HashMap<String, IResource>(); 
        	for (IResource resource : resources) {
        		String resourcePath = resource.getProjectRelativePath().toString();
                if ( ResourceType.determineResourceType( resource.getName() ) != null ) {
                    removeProblemsFor( resource );
                    kfs.write(resourcePath, ResourceFactory.newInputStreamResource( ((IFile) resource).getContents() ));
                    resourcesMap.put(resourcePath, resource);
                } else if ( resourcePath.endsWith(KieModuleModelImpl.KMODULE_JAR_PATH) ) {
                	kfs.writeKModuleXML(new String( Util.getResourceContentsAsCharArray( (IFile)resource ) ));
                }
        	}
        	
        	KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        	List<Message> messages = kieBuilder.buildAll().getResults().getMessages();
        	for (Message message : messages) {
        		IResource resource = resourcesMap.get(message.getPath());
        		if (resource == null) {
        			resource = resourcesMap.get("src/main/resources/" + message.getPath());
        		}
        		if (resource != null) {
        			createMarker(resource, message.getText(), message.getLine());
        		}
        	}
    	}
        
        private void doBuild() {
        	for (IResource resource : resources) {
        		parseResource( resource, true );
        	}
    	}

        private void doBatchBuild() {
        	List<ResourceDescr> resourceDescrs = new ArrayList<ResourceDescr>();
        	
        	for (IResource resource : resources) {
                ResourceDescr resourceDescr = ResourceDescr.createResourceDescr( resource );
                if ( resourceDescr != null ) {
                    removeProblemsFor( resource );
                    DroolsEclipsePlugin.getDefault().invalidateResource( resource );
                    resourceDescrs.add( resourceDescr );
                }
        	}
        	
            List<DRLInfo> drlInfos = DroolsEclipsePlugin.getDefault().parseResources( resourceDescrs );
            for ( DRLInfo drlInfo : drlInfos ) {
                appendMarkers( drlInfo );
            }
    	}
    }

    private class DroolsBuildDeltaVisitor
            implements
            IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) throws CoreException {
            return parseResource( delta.getResource(),
                                  false );
        }
    }

    private boolean isInOutputDirectory(IResource res) throws JavaModelException {
        IJavaProject project = JavaCore.create( res.getProject() );
        // exclude files that are located in the output directory,
        // unless the output directory is the same as the project location
        return !project.getOutputLocation().equals( project.getPath() )
               && project.getOutputLocation().isPrefixOf( res.getFullPath() );
    }

	private boolean exists(IResource res) {
        if ( !res.exists() ) {
            removeProblemsFor( res );
            DroolsEclipsePlugin.getDefault().invalidateResource( res );
            return false;
        }
        return true;
    }

    protected boolean parseResource(IResource res,
                                    boolean clean) {
        try {
            // exclude .guvnorinfo files
            if ( ".guvnorinfo".equals( res.getName() ) ) {
                return false;
            }
            if ( isInOutputDirectory( res ) ) {
                return false;
            }
        } catch ( JavaModelException e ) {
            // do nothing
        }

        if ( !exists( res ) ) {
            return false;
        }

        if ( res instanceof IFile) {
        	String fileExtension = res.getFileExtension();
	        if ( "drl".equals( fileExtension )
                 || "gdrl".equals( fileExtension )
                 || "rdrl".equals( fileExtension )
                 || "dslr".equals( fileExtension )
                 || "rdslr".equals( fileExtension )
                 || ".package".equals( res.getName() ) ) {
	            removeProblemsFor( res );
	            try {
	                if ( clean ) {
	                    DroolsEclipsePlugin.getDefault().invalidateResource( res );
	                }
	                appendMarkers( res, parseDRLFile( (IFile) res ) );
	            } catch ( Throwable t ) {
	                DroolsEclipsePlugin.log( t );
	                createMarker( res,
	                              t.getMessage(),
	                              -1 );
	            }
	            return false;
	        } else if ( "xls".equals( fileExtension ) ) {
	            removeProblemsFor( res );
	            try {
	                if ( clean ) {
	                    DroolsEclipsePlugin.getDefault().invalidateResource( res );
	                }
	                appendMarkers( res, parseXLSFile( (IFile) res ) );
	            } catch ( Throwable t ) {
	                createMarker( res,
	                              t.getMessage(),
	                              -1 );
	            }
	            return false;
	        } else if ( "csv".equals( fileExtension ) ) {
	            removeProblemsFor( res );
	            try {
	                if ( clean ) {
	                    DroolsEclipsePlugin.getDefault().invalidateResource( res );
	                }
	                appendMarkers( res, parseCSVFile( (IFile) res ) );
	            } catch ( Throwable t ) {
	                createMarker( res,
	                              t.getMessage(),
	                              -1 );
	            }
	            return false;
	        } else if ( "rf".equals( fileExtension ) ) {
	            removeProblemsFor( res );
	            try {
	                if ( clean ) {
	                    DroolsEclipsePlugin.getDefault().invalidateResource( res );
	                }
	                appendMarkers( res, parseRuleFlowFile( (IFile) res ) );
	            } catch ( Throwable t ) {
	                createMarker( res,
	                              t.getMessage(),
	                              -1 );
	            }
	            return false;
	        } else if ( "bpmn".equals( fileExtension )
	                    || "bpmn2".equals( fileExtension ) ) {
	            removeProblemsFor( res );
	            try {
	                if ( clean ) {
	                    DroolsEclipsePlugin.getDefault().invalidateResource( res );
	                }
	                appendMarkers( res, parseRuleFlowFile( (IFile) res ) );
	            } catch ( Throwable t ) {
	                createMarker( res,
	                              t.getMessage(),
	                              -1 );
	            }
	            return false;
	        }
        }
        
        return true;
    }

    private void appendMarkers(DRLInfo drlInfo) {
        List<DroolsBuildMarker> markers = new ArrayList<DroolsBuildMarker>();
        markParseErrors( markers, drlInfo.getParserErrors() );
        markOtherErrors( markers, drlInfo.getBuilderErrors() );
        appendMarkers( drlInfo.getResource(), markers );
    }

    private void appendMarkers(IResource res,
                               List<DroolsBuildMarker> markers) {
        for ( DroolsBuildMarker marker : markers ) {
            createMarker( res, marker.getText(), marker.getLine() );
        }
    }

    private interface ResourceParser {
        DRLInfo parseResource() throws DroolsParserException,
                               DecisionTableParseException,
                               CoreException,
                               IOException;
    }

    private List<DroolsBuildMarker> parseResource(ResourceParser resourceParser) {
        List<DroolsBuildMarker> markers = new ArrayList<DroolsBuildMarker>();
        try {
            DRLInfo drlInfo = resourceParser.parseResource();

            //parser errors
            markParseErrors( markers, drlInfo.getParserErrors() );
            markOtherErrors( markers, drlInfo.getBuilderErrors() );
        } catch ( DroolsParserException e ) {
            // we have an error thrown from DrlParser
            Throwable cause = e.getCause();
            if ( cause instanceof RecognitionException ) {
                RecognitionException recogErr = (RecognitionException) cause;
                markers.add( new DroolsBuildMarker( recogErr.getMessage(),
                                                    recogErr.line ) ); //flick back the line number
            }
        } catch ( DecisionTableParseException e ) {
            if ( !"No RuleTable's were found in spreadsheet.".equals( e.getMessage() ) ) {
                throw e;
            }
        } catch ( Exception t ) {
            String message = t.getMessage();
            if ( message == null || message.trim().equals( "" ) ) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add( new DroolsBuildMarker( message ) );
        }
        return markers;
    }

    private List<DroolsBuildMarker> parseDRLFile(final IFile file) {
        return parseResource( new ResourceParser() {
            public DRLInfo parseResource() throws DroolsParserException {
                return DroolsEclipsePlugin.getDefault().parseResource( file, true );
            }
        } );
    }

    private List<DroolsBuildMarker> parseXLSFile(final IFile file) {
        return parseResource( new ResourceParser() {
            public DRLInfo parseResource() throws DroolsParserException,
                                          DecisionTableParseException,
                                          CoreException {
                SpreadsheetCompiler converter = new SpreadsheetCompiler();
                String drl = converter.compile( file.getContents(), InputType.XLS );
                return DroolsEclipsePlugin.getDefault().parseXLSResource( drl, file );
            }
        } );
    }

    private List<DroolsBuildMarker> parseCSVFile(final IFile file) {
        return parseResource( new ResourceParser() {
            public DRLInfo parseResource() throws DroolsParserException,
                                          CoreException {
                SpreadsheetCompiler converter = new SpreadsheetCompiler();
                String drl = converter.compile( file.getContents(), InputType.CSV );
                return DroolsEclipsePlugin.getDefault().parseXLSResource( drl, file );
            }
        } );
    }

    protected List<DroolsBuildMarker> parseRuleFlowFile(IFile file) {
        List<DroolsBuildMarker> markers = new ArrayList<DroolsBuildMarker>();
        if ( !file.exists() ) {
            return markers;
        }
        try {
            String input = convertToString( file.getContents() );
            ProcessInfo processInfo =
                    DroolsEclipsePlugin.getDefault().parseProcess( input,
                                                                   file );
            if ( processInfo != null ) {
                markParseErrors( markers,
                                 processInfo.getErrors() );
            }
        } catch ( Exception t ) {
            t.printStackTrace();
            String message = t.getMessage();
            if ( message == null || message.trim().equals( "" ) ) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add( new DroolsBuildMarker( message ) );
        }
        return markers;
    }

    protected static String convertToString(final InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader( inputStream );
        final StringBuffer text = new StringBuffer();
        final char[] buf = new char[1024];
        int len = 0;
        while ( (len = reader.read( buf )) >= 0 ) {
            text.append( buf,
                         0,
                         len );
        }
        return text.toString();
    }

    /**
     * This will create markers for parse errors. Parse errors mean that antlr
     * has picked up some major typos in the input source.
     */
    protected void markParseErrors(List<DroolsBuildMarker> markers,
                                   List<DroolsError> parserErrors) {
        for ( Iterator<DroolsError> iter = parserErrors.iterator(); iter.hasNext(); ) {
            Object error = iter.next();
            if ( error instanceof ParserError ) {
                ParserError err = (ParserError) error;
                markers.add( new DroolsBuildMarker( err.getMessage(),
                                                    err.getRow() ) );
            } else if ( error instanceof KnowledgeBuilderResult) {
                KnowledgeBuilderResult res = (KnowledgeBuilderResult) error;
                int[] errorLines = res.getLines();
                markers.add( new DroolsBuildMarker( res.getMessage(),
                                                    errorLines != null && errorLines.length > 0 ? errorLines[0] : -1 ) );
            } else if ( error instanceof ExpanderException ) {
                ExpanderException exc = (ExpanderException) error;
                // TODO line mapping is incorrect
                markers.add( new DroolsBuildMarker( exc.getMessage(),
                                                    -1 ) );
            } else {
                markers.add( new DroolsBuildMarker( error.toString() ) );
            }
        }
    }

    /**
     * This will create markers for build errors that happen AFTER parsing.
     */
    private void markOtherErrors(List<DroolsBuildMarker> markers,
                                 DroolsError[] buildErrors) {
        // TODO are there warnings too?
        for ( int i = 0; i < buildErrors.length; i++ ) {
            DroolsError error = buildErrors[i];
            if ( error instanceof GlobalError ) {
                GlobalError globalError = (GlobalError) error;
                markers.add( new DroolsBuildMarker( "Global error: " + globalError.getGlobal(),
                                                    -1 ) );
            } else if ( error instanceof RuleBuildError ) {
                RuleBuildError ruleError = (RuleBuildError) error;
                // TODO try to retrieve line number (or even character start-end)
                // disabled for now because line number are those of the rule class,
                // not the rule file itself
                if ( ruleError.getObject() instanceof CompilationProblem[] ) {
                    CompilationProblem[] problems = (CompilationProblem[]) ruleError.getObject();
                    for ( int j = 0; j < problems.length; j++ ) {
                        markers.add( new DroolsBuildMarker( problems[j].getMessage(),
                                                            ruleError.getLine() ) );
                    }
                } else {
                    markers.add( new DroolsBuildMarker( ruleError.getRule().getName() + ":" + ruleError.getMessage(),
                                                        ruleError.getLine() ) );
                }
            } else if ( error instanceof ParserError ) {
                ParserError parserError = (ParserError) error;
                // TODO try to retrieve character start-end
                markers.add( new DroolsBuildMarker( parserError.getMessage(),
                                                    parserError.getRow() ) );
            } else if ( error instanceof FunctionError ) {
                FunctionError functionError = (FunctionError) error;
                // TODO add line to function error
                // TODO try to retrieve character start-end
                if ( functionError.getObject() instanceof CompilationProblem[] ) {
                    CompilationProblem[] problems = (CompilationProblem[]) functionError.getObject();
                    for ( int j = 0; j < problems.length; j++ ) {
                        markers.add( new DroolsBuildMarker( problems[j].getMessage(),
                                                            functionError.getLines()[j] ) );
                    }
                } else {
                    markers.add( new DroolsBuildMarker( functionError.getFunctionDescr().getName() + ":" + functionError.getMessage(),
                                                        -1 ) );
                }
            } else if ( error instanceof FieldTemplateError ) {
                markers.add( new DroolsBuildMarker( error.getMessage(),
                                                    ((FieldTemplateError) error).getLine() ) );
            } else if ( error instanceof FactTemplateError ) {
                markers.add( new DroolsBuildMarker( error.getMessage(),
                                                    ((FactTemplateError) error).getLine() ) );
            } else if ( error instanceof ImportError ) {
                markers.add( new DroolsBuildMarker( "ImportError: " + error.getMessage() ) );
            } else if ( error instanceof DescrBuildError ) {
                markers.add( new DroolsBuildMarker( "BuildError: " + error.getMessage(),
                                                    ((DescrBuildError) error).getLine() ) );
            } else {
                markers.add( new DroolsBuildMarker( "Unknown DroolsError " + error.getClass() + ": " + error ) );
            }
        }
    }

    protected void createMarker(final IResource res,
                                final String message,
                                final int lineNumber) {
        try {
            IWorkspaceRunnable r = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    IMarker marker = res
                            .createMarker( IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER );
                    marker.setAttribute( IMarker.MESSAGE,
                                         message );
                    marker.setAttribute( IMarker.SEVERITY,
                                         IMarker.SEVERITY_ERROR );
                    marker.setAttribute( IMarker.LINE_NUMBER,
                                         lineNumber );
                }
            };
            res.getWorkspace().run( r,
                                    null,
                                    IWorkspace.AVOID_UPDATE,
                                    null );
        } catch ( CoreException e ) {
            DroolsEclipsePlugin.log( e );
        }
    }

    protected void removeProblemsFor(IResource resource) {
        try {
            if ( resource != null && resource.exists() ) {
                resource.deleteMarkers(
                                        IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER,
                                        false,
                                        IResource.DEPTH_INFINITE );
            }
        } catch ( CoreException e ) {
            DroolsEclipsePlugin.log( e );
        }
    }

    private IProject[] getRequiredProjects(IProject project) {
        IJavaProject javaProject = JavaCore.create( project );
        List<IProject> projects = new ArrayList<IProject>();
        try {
            IClasspathEntry[] entries = javaProject.getResolvedClasspath( true );
            for ( int i = 0, l = entries.length; i < l; i++ ) {
                IClasspathEntry entry = entries[i];
                if ( entry.getEntryKind() == IClasspathEntry.CPE_PROJECT ) {
                    IProject p = project.getWorkspace().getRoot().getProject( entry.getPath().lastSegment() ); // missing projects are considered too
                    if ( p != null && !projects.contains( p ) ) {
                        projects.add( p );
                    }
                }
            }
        } catch ( JavaModelException e ) {
            return new IProject[0];
        }
        return (IProject[]) projects.toArray( new IProject[projects.size()] );
    }

}
