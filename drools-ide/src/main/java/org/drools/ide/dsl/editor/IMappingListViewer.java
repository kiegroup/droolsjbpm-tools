package org.drools.ide.dsl.editor;

import org.drools.lang.dsl.template.NLMappingItem;

/**
 * Used to keep the view up to date with changes in mappings.
 * 
 * @author Michael Neale
 */
public interface IMappingListViewer {

    public void addMapping(NLMappingItem item);
    
    public void removeMapping(NLMappingItem item);
    
    public void updateMapping(NLMappingItem item);
    
}
