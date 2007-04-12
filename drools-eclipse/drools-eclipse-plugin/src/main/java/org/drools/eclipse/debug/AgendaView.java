package org.drools.eclipse.debug;

import org.eclipse.jface.viewers.IContentProvider;

/**
 * The Agenda View.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaView extends DroolsDebugEventHandlerView {

    protected IContentProvider createContentProvider() {
        return new AgendaViewContentProvider(this);
    }
    
    protected int getAutoExpandLevel() {
    	return 1;
    }
}
