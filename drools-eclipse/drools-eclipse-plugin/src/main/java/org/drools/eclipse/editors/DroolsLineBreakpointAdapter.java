package org.drools.eclipse.editors;

import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.debug.core.DroolsLineBreakpoint;
import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

public class DroolsLineBreakpointAdapter implements IToggleBreakpointsTarget {

	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IEditorPart && selection instanceof ITextSelection) {
			IEditorPart editor = (IEditorPart) part;
			IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
			ITextSelection textSelection = (ITextSelection) selection;
			int lineNumber = textSelection.getStartLine();
			try {
				DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(resource, false);
				if (drlInfo != null) {
					RuleInfo ruleInfo = drlInfo.getRuleInfo(lineNumber);
					if (ruleInfo != null) {
						if (ruleInfo.getConsequenceDrlLineNumber() <= lineNumber) {
							return true;
						}
					}
					FunctionInfo functionInfo = drlInfo.getFunctionInfo(lineNumber);
					if (functionInfo != null) {
						if (functionInfo.getDrlLineNumber() <= lineNumber) {
							return true;
						}
					}
				}
			} catch (Throwable t) {
				DroolsEclipsePlugin.log(t);
			}
		}
		return false;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
			ITextSelection textSelection = (ITextSelection) selection;
			int lineNumber = textSelection.getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IDroolsDebugConstants.ID_DROOLS_DEBUG_MODEL);
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (resource.equals(breakpoint.getMarker().getResource())) {
					if (breakpoint.getMarker().getType().equals(IDroolsDebugConstants.DROOLS_MARKER_TYPE)) {
						if (((DroolsLineBreakpoint) breakpoint).getDRLLineNumber() == (lineNumber + 1)) {
							breakpoint.delete();
							return;
						}
					}
				}
			}
			// TODO: drools breakpoints can only be created in functions and consequences
			DroolsLineBreakpoint lineBreakpoint = new DroolsLineBreakpoint(resource, lineNumber + 1);
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
		}
	}

    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// do nothing
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		// do nothing
	}
}
