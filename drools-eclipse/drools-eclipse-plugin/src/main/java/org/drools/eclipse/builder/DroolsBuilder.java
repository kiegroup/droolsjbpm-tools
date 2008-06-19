package org.drools.eclipse.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.drools.guvnor.client.modeldriven.brl.RuleModel;
import org.drools.guvnor.server.util.BRDRLPersistence;
import org.drools.guvnor.server.util.BRXMLPersistence;
import org.drools.commons.jci.problems.CompilationProblem;
import org.drools.compiler.DescrBuildError;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.FactTemplateError;
import org.drools.compiler.FieldTemplateError;
import org.drools.compiler.FunctionError;
import org.drools.compiler.GlobalError;
import org.drools.compiler.ImportError;
import org.drools.compiler.ParserError;
import org.drools.compiler.RuleBuildError;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.lang.ExpanderException;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Automatically syntax checks .drl files and adds possible
 * errors or warnings to the problem list. Nominally is triggerd on save.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = "org.drools.eclipse.droolsbuilder";

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        IProject currentProject = getProject();
        if (currentProject == null || !currentProject.isAccessible()) {
            return new IProject[0];
        }
        try {
            if (monitor != null && monitor.isCanceled())
                throw new OperationCanceledException();

            if (kind == IncrementalProjectBuilder.FULL_BUILD) {
                fullBuild(monitor);
            } else {
                IResourceDelta delta = getDelta(getProject());
                if (delta == null) {
                    fullBuild(monitor);
                } else {
                    incrementalBuild(delta, monitor);
                }
            }
        } catch (CoreException e) {
            IMarker marker = currentProject.createMarker(IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER);
            marker.setAttribute(IMarker.MESSAGE, "Error when trying to build Drools project: " + e.getLocalizedMessage());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        }
        return getRequiredProjects(currentProject);
    }
    
    protected void fullBuild(IProgressMonitor monitor)
            throws CoreException {
        getProject().accept(new DroolsBuildVisitor());
    }
    
    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
    	boolean buildAll = DroolsEclipsePlugin.getDefault().getPreferenceStore().getBoolean(IDroolsConstants.BUILD_ALL);
        if (buildAll) {
        	// to make sure that all rules are checked when a java file is changed 
        	fullBuild(monitor);
        } else {
        	delta.accept(new DroolsBuildDeltaVisitor());
        }
    }

    private class DroolsBuildVisitor implements IResourceVisitor {
        public boolean visit(IResource res) {
            return parseResource(res, true);
        }
    }

    private class DroolsBuildDeltaVisitor implements IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) throws CoreException {
            return parseResource(delta.getResource(), false);
        }
    }
    
    protected boolean parseResource(IResource res, boolean clean) {
        try {
            IJavaProject project = JavaCore.create(res.getProject());
            // exclude files that are located in the output directory,
            // unless the ouput directory is the same as the project location
            if (!project.getOutputLocation().equals(project.getPath())
                    && project.getOutputLocation().isPrefixOf(res.getFullPath())) {
                return false;
            }
        } catch (JavaModelException e) {
            // do nothing
        }

        if (res instanceof IFile
        		&& ("drl".equals(res.getFileExtension())
    				|| "dslr".equals(res.getFileExtension())
    				|| ".package".equals(res.getName()))) {
            removeProblemsFor(res);
            try {
            	if (clean) {
            		DroolsEclipsePlugin.getDefault().invalidateResource(res);
            	}
            	DroolsBuildMarker[] markers = parseDRLFile((IFile) res, new String(Util.getResourceContentsAsCharArray((IFile) res)));
		        for (int i = 0; i < markers.length; i++) {
		        	createMarker(res, markers[i].getText(), markers[i].getLine());
		        }
            } catch (Throwable t) {
            	createMarker(res, t.getMessage(), -1);
            }
            return false;
        } else if (res instanceof IFile && "xls".equals(res.getFileExtension())) {
            removeProblemsFor(res);
            try {
            	if (clean) {
            		DroolsEclipsePlugin.getDefault().invalidateResource(res);
            	}
            	DroolsBuildMarker[] markers = parseXLSFile((IFile) res);
		        for (int i = 0; i < markers.length; i++) {
		        	createMarker(res, markers[i].getText(), markers[i].getLine());
		        }
            } catch (Throwable t) {
            	createMarker(res, t.getMessage(), -1);
            }
            return false;
        } else if (res instanceof IFile && "csv".equals(res.getFileExtension())) {
            removeProblemsFor(res);
            try {
                if (clean) {
                    DroolsEclipsePlugin.getDefault().invalidateResource(res);
                }
                DroolsBuildMarker[] markers = parseCSVFile((IFile) res);
                for (int i = 0; i < markers.length; i++) {
                    createMarker(res, markers[i].getText(), markers[i].getLine());
                }
            } catch (Throwable t) {
                createMarker(res, t.getMessage(), -1);
            }
            return false;
        } else if (res instanceof IFile && "brl".equals(res.getFileExtension())) {
            removeProblemsFor(res);
            try {
            	if (clean) {
            		DroolsEclipsePlugin.getDefault().invalidateResource(res);
            	}
            	DroolsBuildMarker[] markers = parseBRLFile((IFile) res);
		        for (int i = 0; i < markers.length; i++) {
		        	createMarker(res, markers[i].getText(), markers[i].getLine());
		        }
            } catch (Throwable t) {
            	createMarker(res, t.getMessage(), -1);
            }
            return false;
        } else if (res instanceof IFile && "rf".equals(res.getFileExtension())) {
            removeProblemsFor(res);
            try {
            	if (clean) {
            		DroolsEclipsePlugin.getDefault().invalidateResource(res);
            	}
            	DroolsBuildMarker[] markers = parseRuleFlowFile((IFile) res);
		        for (int i = 0; i < markers.length; i++) {
		        	createMarker(res, markers[i].getText(), markers[i].getLine());
		        }
            } catch (Throwable t) {
            	createMarker(res, t.getMessage(), -1);
            }
            return false;
        }

        return true;
    }
    
    private DroolsBuildMarker[] parseDRLFile(IFile file, String content) {
    	List markers = new ArrayList();
		try {
            DRLInfo drlInfo =
            	DroolsEclipsePlugin.getDefault().parseResource(file, true);
            //parser errors
            markParseErrors(markers, drlInfo.getParserErrors());  
            markOtherErrors(markers, drlInfo.getBuilderErrors());
        } catch (DroolsParserException e) {
            // we have an error thrown from DrlParser
            Throwable cause = e.getCause();
            if (cause instanceof RecognitionException ) {
                RecognitionException recogErr = (RecognitionException) cause;
                markers.add(new DroolsBuildMarker(recogErr.getMessage(), recogErr.line)); //flick back the line number
            }
        } catch (Exception t) {
        	String message = t.getMessage();
            if (message == null || message.trim().equals("")) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add(new DroolsBuildMarker(message));
        }
        return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
    }

    private DroolsBuildMarker[] parseXLSFile(IFile file) {
    	List markers = new ArrayList();
		try {
			SpreadsheetCompiler converter = new SpreadsheetCompiler();
	        String drl = converter.compile(file.getContents(), InputType.XLS);
	        DRLInfo drlInfo =
            	DroolsEclipsePlugin.getDefault().parseXLSResource(drl, file);
            // parser errors
            markParseErrors(markers, drlInfo.getParserErrors());  
            markOtherErrors(markers, drlInfo.getBuilderErrors());
        } catch (DroolsParserException e) {
            // we have an error thrown from DrlParser
            Throwable cause = e.getCause();
            if (cause instanceof RecognitionException ) {
                RecognitionException recogErr = (RecognitionException) cause;
                markers.add(new DroolsBuildMarker(recogErr.getMessage(), recogErr.line)); //flick back the line number
            }
        } catch (Exception t) {
        	String message = t.getMessage();
            if (message == null || message.trim().equals( "" )) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add(new DroolsBuildMarker(message));
        }
        return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
    }

    private DroolsBuildMarker[] parseCSVFile(IFile file) {
        List markers = new ArrayList();
        try {
            SpreadsheetCompiler converter = new SpreadsheetCompiler();
            String drl = converter.compile(file.getContents(), InputType.CSV);
            DRLInfo drlInfo =
                DroolsEclipsePlugin.getDefault().parseXLSResource(drl, file);
            // parser errors
            markParseErrors(markers, drlInfo.getParserErrors());  
            markOtherErrors(markers, drlInfo.getBuilderErrors());
        } catch (DroolsParserException e) {
            // we have an error thrown from DrlParser
            Throwable cause = e.getCause();
            if (cause instanceof RecognitionException ) {
                RecognitionException recogErr = (RecognitionException) cause;
                markers.add(new DroolsBuildMarker(recogErr.getMessage(), recogErr.line)); //flick back the line number
            }
        } catch (Exception t) {
            String message = t.getMessage();
            if (message == null || message.trim().equals( "" )) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add(new DroolsBuildMarker(message));
        }
        return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
    }

    private DroolsBuildMarker[] parseBRLFile(IFile file) {
    	List markers = new ArrayList();
		try {
			String brl = convertToString(file.getContents());
			RuleModel model = BRXMLPersistence.getInstance().unmarshal(brl);
			String drl = BRDRLPersistence.getInstance().marshal(model);
			
			// TODO pass this through DSL converter in case brl is based on dsl
			
	        DRLInfo drlInfo =
            	DroolsEclipsePlugin.getDefault().parseBRLResource(drl, file);
            // parser errors
            markParseErrors(markers, drlInfo.getParserErrors());  
            markOtherErrors(markers, drlInfo.getBuilderErrors());
        } catch (DroolsParserException e) {
            // we have an error thrown from DrlParser
            Throwable cause = e.getCause();
            if (cause instanceof RecognitionException ) {
                RecognitionException recogErr = (RecognitionException) cause;
                markers.add(new DroolsBuildMarker(recogErr.getMessage(), recogErr.line)); //flick back the line number
            }
        } catch (Exception t) {
        	String message = t.getMessage();
            if (message == null || message.trim().equals( "" )) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add(new DroolsBuildMarker(message));
        }
        return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
    }
    
    protected DroolsBuildMarker[] parseRuleFlowFile(IFile file) {
        if (!file.exists()) {
            return new DroolsBuildMarker[0];
        }
    	List markers = new ArrayList();
		try {
            String input = convertToString(file.getContents());
		    ProcessInfo processInfo =
                DroolsEclipsePlugin.getDefault().parseProcess(input, file);
		    if (processInfo != null) {
		        markParseErrors(markers, processInfo.getErrors());
		    }
        } catch (Exception t) {
            t.printStackTrace();
        	String message = t.getMessage();
            if (message == null || message.trim().equals( "" )) {
                message = "Error: " + t.getClass().getName();
            }
            markers.add(new DroolsBuildMarker(message));
        }
        return (DroolsBuildMarker[]) markers.toArray(new DroolsBuildMarker[markers.size()]);
    }
    
    protected static String convertToString(final InputStream inputStream) throws IOException {
    	Reader reader = new InputStreamReader(inputStream);
    	final StringBuffer text = new StringBuffer();
        final char[] buf = new char[1024];
        int len = 0;
        while ((len = reader.read(buf)) >= 0) {
            text.append(buf, 0, len);
        }
        return text.toString();
    }

    /**
     * This will create markers for parse errors.
     * Parse errors mean that antlr has picked up some major typos in the input source.
     */
    protected void markParseErrors(List markers, List parserErrors) {
        for ( Iterator iter = parserErrors.iterator(); iter.hasNext(); ) {
        	Object error = iter.next();
        	if (error instanceof ParserError) {
        		ParserError err = (ParserError) error;
        		markers.add(new DroolsBuildMarker(err.getMessage(), err.getRow()));
        	} else if (error instanceof ExpanderException) {
        		ExpanderException exc = (ExpanderException) error;
        		// TODO line mapping is incorrect
        		markers.add(new DroolsBuildMarker(exc.getMessage(), -1));
        	} else {
        		markers.add(new DroolsBuildMarker(error.toString()));
        	}
        }
    }

    /**
     * This will create markers for build errors that happen AFTER parsing.
     */
    private void markOtherErrors(List markers,
                                        DroolsError[] buildErrors) {
        // TODO are there warnings too?
        for (int i = 0; i < buildErrors.length; i++ ) {
        	DroolsError error = buildErrors[i];
        	if (error instanceof GlobalError) {
        		GlobalError globalError = (GlobalError) error;
        		markers.add(new DroolsBuildMarker("Global error: " + globalError.getGlobal(), -1));
        	} else if (error instanceof RuleBuildError) {
        	    RuleBuildError ruleError = (RuleBuildError) error;
        		// TODO try to retrieve line number (or even character start-end)
        		// disabled for now because line number are those of the rule class,
        		// not the rule file itself
        		if (ruleError.getObject() instanceof CompilationProblem[]) {
        			CompilationProblem[] problems = (CompilationProblem[]) ruleError.getObject();
        			for (int j = 0; j < problems.length; j++) {
        				markers.add(new DroolsBuildMarker(problems[j].getMessage(), ruleError.getLine()));
        			}
        		} else {
        			markers.add(new DroolsBuildMarker(ruleError.getRule().getName() + ":" + ruleError.getMessage(), ruleError.getLine()));
        		}
        	} else if (error instanceof ParserError) {
        		ParserError parserError = (ParserError) error;
        		// TODO try to retrieve character start-end
        		markers.add(new DroolsBuildMarker(parserError.getMessage(), parserError.getRow()));
        	} else if (error instanceof FunctionError) {
        		FunctionError functionError = (FunctionError) error;
        		// TODO add line to function error
        		// TODO try to retrieve character start-end
        		if (functionError.getObject() instanceof CompilationProblem[]) {
        			CompilationProblem[] problems = (CompilationProblem[]) functionError.getObject();
        			for (int j = 0; j < problems.length; j++) {
        				markers.add(new DroolsBuildMarker(problems[j].getMessage(), functionError.getErrorLines()[j]));
        			}
        		} else {
        			markers.add(new DroolsBuildMarker(functionError.getFunctionDescr().getName() + ":" + functionError.getMessage(), -1));
        		}
        	} else if (error instanceof FieldTemplateError) {
        		markers.add(new DroolsBuildMarker(error.getMessage(), ((FieldTemplateError) error).getLine()));
        	} else if (error instanceof FactTemplateError) {
        		markers.add(new DroolsBuildMarker(error.getMessage(), ((FactTemplateError) error).getLine()));
        	} else if (error instanceof ImportError) {
        		markers.add(new DroolsBuildMarker("ImportError: " + error.getMessage()));
        	} else if (error instanceof DescrBuildError) {
        		markers.add(new DroolsBuildMarker("BuildError: " + error.getMessage(), ((DescrBuildError) error).getLine()));
        	} else {
        		markers.add(new DroolsBuildMarker("Unknown DroolsError " + error.getClass() + ": " + error));
        	}
        }
    }

    protected void createMarker(final IResource res, final String message, final int lineNumber) {
        try {
        	IWorkspaceRunnable r= new IWorkspaceRunnable() {
        		public void run(IProgressMonitor monitor) throws CoreException {
            		IMarker marker = res
                    	.createMarker(IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER);
		            marker.setAttribute(IMarker.MESSAGE, message);
		            marker.setAttribute(IMarker.SEVERITY,
		                    IMarker.SEVERITY_ERROR);
		            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
	    		}
			};
			res.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            DroolsEclipsePlugin.log(e);
        }
    }
    
    protected void removeProblemsFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(
                        IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER, false,
                        IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            DroolsEclipsePlugin.log(e);
        }
    }
    
    private IProject[] getRequiredProjects(IProject project) {
    	IJavaProject javaProject = JavaCore.create(project);
    	List projects = new ArrayList();
    	try {
    		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
    		for (int i = 0, l = entries.length; i < l; i++) {
    			IClasspathEntry entry = entries[i];
    			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IProject p = project.getWorkspace().getRoot().getProject(entry.getPath().lastSegment()); // missing projects are considered too
	    			if (p != null && !projects.contains(p)) {
	    				projects.add(p);
	    			}
    			}
    		}
    	} catch(JavaModelException e) {
    		return new IProject[0];
    	}
    	return (IProject[]) projects.toArray(new IProject[projects.size()]);
    }

}
