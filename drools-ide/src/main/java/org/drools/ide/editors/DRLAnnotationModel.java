package org.drools.ide.editors;

import java.util.Map;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.debug.core.IDroolsDebugConstants;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

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
	        Map attributes = marker.getAttributes();
	        try {
	        	return new Position(fDocument.getLineOffset(line - 1));
	        } catch (BadLocationException exc) {
	        	return super.createPositionFromMarker(marker);
	        }
        } catch (CoreException exc) {
        	DroolsIDEPlugin.log(exc);
        	return null;
        }
    }
}
