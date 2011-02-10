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

package org.drools.eclipse.editors.rete.part;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.rete.model.Connection;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.reteoo.BaseVertex;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * Factory mapping model elements to edit parts
 */
public class VertexEditPartFactory
    implements
    EditPartFactory {

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart(EditPart context,
                                   Object modelElement) {
        // get EditPart for model element
        EditPart part = getPartForElement( modelElement );
        // store model element in EditPart
        part.setModel( modelElement );
        return part;
    }

    /**
     * Maps object to EditPart.
     *  
     * @throws RuntimeException if no match was found
     */
    private EditPart getPartForElement(Object modelElement) {
        if ( modelElement instanceof ReteGraph ) {
            return new DiagramEditPart();
        }
        if ( modelElement instanceof BaseVertex ) {
            return new VertexEditPart();
        }
        if ( modelElement instanceof Connection ) {
            return new ConnectionEditPart();
        }
        DroolsEclipsePlugin.log( new Exception( "Can't create part for model element: " + ((modelElement != null) ? modelElement.getClass().getName() : "null") ) );
        return null;
        
    }

}
