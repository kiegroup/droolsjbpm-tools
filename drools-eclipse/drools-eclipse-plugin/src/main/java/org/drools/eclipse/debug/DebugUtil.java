package org.drools.eclipse.debug;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class DebugUtil {

    public static final int INFO_EVALUATION_STACK_FRAME = 111;
    private static IStatus fgNeedStackFrame = new Status(IStatus.INFO,
        DroolsEclipsePlugin.getUniqueIdentifier(), INFO_EVALUATION_STACK_FRAME,
        "Provides thread context for an evaluation", null);
    private static IStatusHandler fgStackFrameProvider;

    public static IValue getValueByExpression(String expression, IValue value) {
        if (!(value instanceof IJavaObject)) {
            return null;
        }
        IJavaObject javaValue = (IJavaObject) value;
        try {
            IJavaType type = javaValue.getJavaType();
            if (!(type instanceof IJavaClassType)) {
                return null;
            }
            IJavaStackFrame stackFrame = getStackFrame(javaValue);
            if (stackFrame == null) {
                return null;
            }

			// find the project the snippets will be compiled in.
			ISourceLocator locator= javaValue.getLaunch().getSourceLocator();
			Object sourceElement= null;
			if (locator instanceof ISourceLookupDirector) {
				String[] sourcePaths = ((IJavaClassType) type).getSourcePaths(null);
				if (sourcePaths != null && sourcePaths.length > 0) {
					sourceElement = ((ISourceLookupDirector) locator).getSourceElement(sourcePaths[0]);
				}
				if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
					sourceElement = ((IAdaptable) sourceElement).getAdapter(IJavaElement.class);
				}
			}
			if (sourceElement == null) {
				sourceElement = locator.getSourceElement(stackFrame);
				if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
					Object newSourceElement = ((IAdaptable) sourceElement).getAdapter(IJavaElement.class);
					// if the source is a drl during the execution of the rule
					if (newSourceElement != null) {
						sourceElement = newSourceElement;
					}
				}
			}
			IJavaProject project = null;
			if (sourceElement instanceof IJavaElement) {
				project = ((IJavaElement) sourceElement).getJavaProject();
			} else if (sourceElement instanceof IResource) {
				IJavaProject resourceProject = JavaCore.create(((IResource) sourceElement).getProject());
				if (resourceProject.exists()) {
					project = resourceProject;
				}
			}
            if (project == null) {
                return null;
            }

            IAstEvaluationEngine evaluationEngine = JDIDebugPlugin.getDefault()
                    .getEvaluationEngine(project,
                            (IJavaDebugTarget) stackFrame.getDebugTarget());

            EvaluationBlock evaluationBlock = new EvaluationBlock(javaValue,
                    (IJavaReferenceType) type, (IJavaThread) stackFrame.getThread(),
                    evaluationEngine);
            return evaluationBlock.evaluate(expression);

        } catch (CoreException e) {
            DroolsEclipsePlugin.log(e);
        }
        return null;
    }

    /**
     * Return the current stack frame context, or a valid stack frame for the
     * given value.
     */
    public static IJavaStackFrame getStackFrame(IValue value)
            throws CoreException {
        IStatusHandler handler = getStackFrameProvider();
        if (handler != null) {
            IJavaStackFrame stackFrame = (IJavaStackFrame) handler
                    .handleStatus(fgNeedStackFrame, value);
            if (stackFrame != null) {
                return stackFrame;
            }
        }
        IDebugTarget target = value.getDebugTarget();
        IJavaDebugTarget javaTarget = (IJavaDebugTarget) target
                .getAdapter(IJavaDebugTarget.class);
        if (javaTarget != null) {
            IThread[] threads = javaTarget.getThreads();
            for (int i = 0; i < threads.length; i++) {
                IThread thread = threads[i];
                if (thread.isSuspended()) {
                    return (IJavaStackFrame) thread.getTopStackFrame();
                }
            }
        }
        return null;
    }

    private static IStatusHandler getStackFrameProvider() {
        if (fgStackFrameProvider == null) {
            fgStackFrameProvider = DebugPlugin.getDefault().getStatusHandler(
                    fgNeedStackFrame);
        }
        return fgStackFrameProvider;
    }

    private static class EvaluationBlock implements IEvaluationListener {

        private IJavaObject fEvaluationValue;
        private IJavaReferenceType fEvaluationType;
        private IJavaThread fThread;
        private IAstEvaluationEngine fEvaluationEngine;
        private IEvaluationResult fResult;

        public EvaluationBlock(IJavaObject value, IJavaReferenceType type,
                IJavaThread thread, IAstEvaluationEngine evaluationEngine) {
            fEvaluationValue = value;
            fEvaluationType = type;
            fThread = thread;
            fEvaluationEngine = evaluationEngine;
        }

        public void evaluationComplete(IEvaluationResult result) {
            synchronized (this) {
                fResult = result;
                this.notify();
            }
        }

        public IJavaValue evaluate(String snippet) throws DebugException {
            ICompiledExpression compiledExpression = fEvaluationEngine
                    .getCompiledExpression(snippet, fEvaluationType);
            if (compiledExpression.hasErrors()) {
                String[] errorMessages = compiledExpression.getErrorMessages();
                String message = "";

                for ( int i = 0; i < errorMessages.length; i++ ) {
                    message += errorMessages[i] + "\n";
                }
                throw new DebugException(new Status(IStatus.ERROR, 
                    DroolsEclipsePlugin.PLUGIN_ID, DroolsEclipsePlugin.INTERNAL_ERROR,
                    "Error when compiling snippet " + snippet + ": " + message, null));
            }
            fResult = null;
            fEvaluationEngine.evaluateExpression(compiledExpression,
                    fEvaluationValue, fThread, this,
                    DebugEvent.EVALUATION_IMPLICIT, false);
            synchronized (this) {
                if (fResult == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (fResult == null) {
                return null;
            }
            if (fResult.hasErrors()) {
                return null;
            }
            return fResult.getValue();
        }
    }

}
