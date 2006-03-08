package org.drools.ide.builder;

import java.util.Map;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsError;
import org.drools.compiler.GlobalError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.ParserError;
import org.drools.compiler.RuleError;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.PackageDescr;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Automatically syntax checks .drl files and adds possible
 * errors or warnings to the problem list.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = "org.drools.ide.droolsbuilder";

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
        return null;
    }
    
    protected void fullBuild(final IProgressMonitor monitor)
            throws CoreException {
        getProject().accept(new DroolsBuildVisitor());
    }
    
    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        delta.accept(new DroolsBuildDeltaVisitor());
    }

    private class DroolsBuildVisitor implements IResourceVisitor {
        public boolean visit(IResource res) {
            return parseResource(res);
        }
    }

    private class DroolsBuildDeltaVisitor implements IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) throws CoreException {
            return parseResource(delta.getResource());
        }
    }
    
    public static boolean parseResource(IResource res) {
        if (res instanceof IFile && "drl".equals(res.getFileExtension())) {
            removeProblemsFor(res);
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

            DrlParser parser = new DrlParser();
            try {
                ClassLoader oldLoader = Thread.currentThread()
                        .getContextClassLoader();
                ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
                if (res.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
                    IJavaProject project = JavaCore.create(res.getProject());
                    newLoader = ProjectClassLoader.getProjectClassLoader(project);
                }
                try {
                    Thread.currentThread().setContextClassLoader(newLoader);
                    PackageDescr packageDescr = parser.parse(new String(Util.getResourceContentsAsCharArray((IFile) res)));
                    PackageBuilder builder = new PackageBuilder();
                    builder.addPackage(packageDescr);
                    DroolsError[] errors = builder.getErrors();
                    // TODO are there warnings too?
                    for (int i = 0; i < errors.length; i++ ) {
                    	DroolsError error = errors[i];
                    	if (error instanceof GlobalError) {
                    		GlobalError globalError = (GlobalError) error;
                    		createMarker(res, globalError.getGlobal(), -1);
                    	} else if (error instanceof RuleError) {
                    		RuleError ruleError = (RuleError) error;
                    		// TODO try to retrieve line numner (or even character start-end
                    		createMarker(res, ruleError.getRule().getName() + ":" + ruleError.getMessage(), -1);
                    	} else if (error instanceof ParserError) {
                    		ParserError parserError = (ParserError) error;
                    		// TODO try to retrieve character start-end
                    		createMarker(res, parserError.getMessage(), parserError.getRow());
                    	} else {
                    		createMarker(res, "Unknown DroolsError " + error.getClass() + ": " + error, -1);
                    	}
                    }
                } catch (Exception t) {
                    throw t;
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            } catch (Throwable t) {
            	t.printStackTrace();
                // TODO create markers for exceptions containing line number etc.
                createMarker(res, t.getMessage(), -1);
            }
            return false;
        }
        return true;
    }
    
    private static void createMarker(final IResource res, final String message, final int lineNumber) {
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
            DroolsIDEPlugin.log(e);
        }
    }
    
    private static void createWarning(final IResource res, final String message, final int lineNumber, final int charStart) {
        try {
        	IWorkspaceRunnable r= new IWorkspaceRunnable() {
        		public void run(IProgressMonitor monitor) throws CoreException {
		            IMarker marker = res
		                    .createMarker(IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER);
		            marker.setAttribute(IMarker.MESSAGE, message);
		            marker.setAttribute(IMarker.SEVERITY,
		                    IMarker.SEVERITY_WARNING);
		            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		            marker.setAttribute(IMarker.CHAR_START, charStart);
	    		}
			};
			res.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            DroolsIDEPlugin.log(e);
        }
    }
    
    public static void removeProblemsFor(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(
                        IDroolsModelMarker.DROOLS_MODEL_PROBLEM_MARKER, false,
                        IResource.DEPTH_INFINITE);
            }
        } catch (CoreException e) {
            DroolsIDEPlugin.log(e);
        }
    }
    
}
