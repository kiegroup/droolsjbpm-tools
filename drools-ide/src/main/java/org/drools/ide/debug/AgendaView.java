package org.drools.ide.debug;

import org.eclipse.jface.viewers.IContentProvider;

/**
 * The Agenda View.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaView extends DroolsDebugEventHandlerView {

    protected IContentProvider createContentProvider() {
        AgendaViewContentProvider contentProvider = new AgendaViewContentProvider(this);
        contentProvider.setExceptionHandler(this);
        return contentProvider;
    }
}
