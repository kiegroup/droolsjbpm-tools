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