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

package org.drools.eclipse.flow.common.editor.core;

import java.util.ArrayList;
import java.util.List;

public abstract class ElementContainerElementWrapper extends DefaultElementWrapper implements ElementContainer {

    public static final int ADD_ELEMENT = 5;
    public static final int REMOVE_ELEMENT = 6;
    
    private static final long serialVersionUID = 510l;
    
    List<ElementWrapper> elements = new ArrayList<ElementWrapper>();
    
    public void addElement(ElementWrapper element) {
        internalAddElement(element);
        localAddElement(element);
        notifyListeners(ADD_ELEMENT);
    }
    
    public void localAddElement(ElementWrapper element) {
        elements.add(element);
    }
    
    protected abstract void internalAddElement(ElementWrapper element);
    
    public void removeElement(ElementWrapper element) {
        internalRemoveElement(element);
        elements.remove(element);
        element.setParent(null);
        notifyListeners(REMOVE_ELEMENT);
    }
    
    protected abstract void internalRemoveElement(ElementWrapper element);
    
    public List<ElementWrapper> getElements() {
        return elements;
    }
    
    public ProcessWrapper getProcessWrapper() {
        return getParent().getProcessWrapper();
    }
    
    public boolean canAddElement(ElementWrapper element) {
        return true;
    }

}
