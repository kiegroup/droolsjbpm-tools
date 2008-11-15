package org.drools.eclipse.editors;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

/**
 * Drools annotation model.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DRLAnnotationModel extends ResourceMarkerAnnotationModel {

	public DRLAnnotationModel(IResource resource) {
		super(resource);
	}

	protected Position createPositionFromMarker(IMarker marker) {
        try {
        	if (!marker.getType().equals(IDroolsDebugConstants.DROOLS_MARKER_TYPE)) {
	            return super.createPositionFromMarker(marker);
	        }
	        int line = marker.getAttribute(IDroolsDebugConstants.DRL_LINE_NUMBER, -1);
	        try {
	        	return new Position(fDocument.getLineOffset(line - 1));
	        } catch (BadLocationException exc) {
	        	return super.createPositionFromMarker(marker);
	        }
        } catch (CoreException exc) {
        	DroolsEclipsePlugin.log(exc);
        	return null;
        }
    }
}
