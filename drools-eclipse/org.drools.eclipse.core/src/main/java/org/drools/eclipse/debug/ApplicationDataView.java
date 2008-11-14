package org.drools.eclipse.debug;

import org.eclipse.jface.viewers.IContentProvider;

/**
 * The Application Data View.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ApplicationDataView extends DroolsDebugEventHandlerView {

    protected IContentProvider createContentProvider() {
        ApplicationDataViewContentProvider contentProvider = new ApplicationDataViewContentProvider(this);
        return contentProvider;
    }
}
