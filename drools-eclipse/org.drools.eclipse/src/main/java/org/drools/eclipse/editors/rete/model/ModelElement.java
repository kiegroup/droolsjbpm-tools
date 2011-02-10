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
