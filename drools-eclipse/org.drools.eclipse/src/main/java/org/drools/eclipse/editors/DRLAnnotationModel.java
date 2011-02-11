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
