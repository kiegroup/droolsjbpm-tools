package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the Domain Specific Language editor.
 * @author Michael Neale
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DSLContentProvider
    implements
    IStructuredContentProvider, IMappingListViewer {

    private TableViewer viewer;
    private NLGrammarModel model;
    
    public DSLContentProvider(TableViewer viewer, NLGrammarModel model) {
        this.viewer = viewer;
        this.model = model;
    }
    
    public void dispose() {
        model.removeChangeListener( this );
    }
    
    public void inputChanged(Viewer viewer,
                             Object oldInput,
                             Object newInput) {
        if (newInput != null)
            ((NLGrammarModel) newInput).addChangeListener(this);
        if (oldInput != null)
            ((NLGrammarModel) oldInput).removeChangeListener(this);

    }

    public void addMapping(DSLMappingEntry item) {
        viewer.add( item );
        
    }

    public void removeMapping(DSLMappingEntry item) {

        viewer.remove( item );
    }

    public void updateMapping(DSLMappingEntry item) {
        viewer.update( item, null );
    }

    public Object[] getElements(Object inputElement) {
        return model.getEntries().toArray();        
    }

}
