package org.drools.ide.editors.outline;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This represents a node that is shown in the outline. 
 * 
 * @author Michael Neale
 */
public abstract class OutlineNode implements
    IWorkbenchAdapter,
    IAdaptable {

    private int offset;
    private int length;
    
    /** The offset (in chars) to jump to */
    public int getOffset() {
        return offset;
    }
    

    /** return the length of selection */
    public int getLength() {
        return length;
    }


    public void setLength(int length) {
        this.length = length;
    }


    public void setOffset(int offset) {
        this.offset = offset;
    }


    public Object getAdapter(Class adapter) {
        if ( adapter == IWorkbenchAdapter.class ) {
            return this;
        }
        return null;
    }
}
