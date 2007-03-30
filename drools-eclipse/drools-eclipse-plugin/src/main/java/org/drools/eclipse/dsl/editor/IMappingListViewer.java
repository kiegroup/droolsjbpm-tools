package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;

/**
 * Used to keep the view up to date with changes in mappings.
 * 
 * @author Michael Neale
 */
public interface IMappingListViewer {

    public void addMapping(DSLMappingEntry item);
    
    public void removeMapping(DSLMappingEntry item);
    
    public void updateMapping(DSLMappingEntry item);
    
}
