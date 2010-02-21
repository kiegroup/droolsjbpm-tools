package org.drools.eclipse.flow.common.editor.core;

import java.util.ArrayList;
import java.util.List;

public abstract class ElementContainerElementWrapper extends DefaultElementWrapper implements ElementContainer {

	public static final int ADD_ELEMENT = 5;
    public static final int REMOVE_ELEMENT = 6;
    
    private static final long serialVersionUID = 4L;
    
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
