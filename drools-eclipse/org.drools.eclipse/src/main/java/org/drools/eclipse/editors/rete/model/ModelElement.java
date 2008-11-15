package org.drools.eclipse.editors.rete.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract prototype of a model element.
 */
public abstract class ModelElement {

    /** Delegate used to implemenent property-change-support. */
    private transient PropertyChangeSupport pcsDelegate = new PropertyChangeSupport( this );

    /** 
     * Attach a non-null PropertyChangeListener to this object.
     * 
     * @param l a non-null PropertyChangeListener instance
     * @throws IllegalArgumentException if the parameter is null
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if ( l == null ) {
            throw new IllegalArgumentException();
        }
        pcsDelegate.addPropertyChangeListener( l );
    }

    /** 
     * Report a property change to registered listeners (for example edit parts).
     * 
     * @param property the programmatic name of the property that changed
     * @param oldValue the old value of this property
     * @param newValue the new value of this property
     */
    protected void firePropertyChange(String property,
                                      Object oldValue,
                                      Object newValue) {
        if ( pcsDelegate.hasListeners( property ) ) {
            pcsDelegate.firePropertyChange( property,
                                            oldValue,
                                            newValue );
        }
    }

    /** 
     * Remove a PropertyChangeListener from this component.
     * 
     * @param l a PropertyChangeListener instance
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if ( l != null ) {
            pcsDelegate.removePropertyChangeListener( l );
        }
    }

}
