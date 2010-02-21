package org.drools.eclipse.flow.common.editor.core;

import java.util.List;

public interface ElementContainer {
    
    void addElement(ElementWrapper element);
    
    void localAddElement(ElementWrapper element);
    
    void removeElement(ElementWrapper element);
    
    List<ElementWrapper> getElements();
    
    ProcessWrapper getProcessWrapper();
    
    boolean canAddElement(ElementWrapper element);

}
