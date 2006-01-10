package org.drools.ide.debug;

import org.eclipse.jface.viewers.IContentProvider;

/**
 * The Working Memory view.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class WorkingMemoryView extends DroolsDebugEventHandlerView {

    protected IContentProvider createContentProvider() {
        WorkingMemoryViewContentProvider contentProvider = new WorkingMemoryViewContentProvider(this);
        contentProvider.setExceptionHandler(this);
        return contentProvider;
    }
}
