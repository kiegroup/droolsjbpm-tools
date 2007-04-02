package org.drools.eclipse.debug.core;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class DroolsLineBreakpointMarkerUpdater implements IMarkerUpdater {

	private static final String[] ATTRIBUTES = { 
		IMarker.LINE_NUMBER, 
		IDroolsDebugConstants.DRL_LINE_NUMBER 
	};
	
	public String getMarkerType() {
		// responsible for only Drools line breakpoint markers
		return IDroolsDebugConstants.DROOLS_MARKER_TYPE;
	}

	public String[] getAttribute() {
		return ATTRIBUTES;
	}

	public boolean updateMarker(IMarker marker, IDocument document, Position position) {
		if (position == null) {
			return true;
		}
		if (position.isDeleted()) {
			return false;
		}
		boolean offsetsInitialized = false;
		boolean offsetsChanged = false;
		int markerStart = MarkerUtilities.getCharStart(marker);
		int markerEnd = MarkerUtilities.getCharEnd(marker);
		if (markerStart != -1 && markerEnd != -1) {
			offsetsInitialized = true;
			int offset = position.getOffset();
			if (markerStart != offset) {
				MarkerUtilities.setCharStart(marker, offset);
				offsetsChanged= true;
			}
			offset += position.getLength();
			if (markerEnd != offset) {
				MarkerUtilities.setCharEnd(marker, offset);
				offsetsChanged= true;
			}
		}
		if (!offsetsInitialized || (offsetsChanged && MarkerUtilities.getLineNumber(marker) != -1)) {
			try {
				int drlLineNumber = document.getLineOfOffset(position.getOffset()) + 1;
				marker.setAttribute(IDroolsDebugConstants.DRL_LINE_NUMBER, drlLineNumber);
			} catch (Throwable t) {
				DroolsEclipsePlugin.log(t);
			}
		}
		return true;
	}

}
