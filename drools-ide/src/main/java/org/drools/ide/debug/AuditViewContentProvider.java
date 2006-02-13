package org.drools.ide.debug;

import java.util.List;

import org.drools.ide.debug.AuditView.Event;

public class AuditViewContentProvider extends DroolsDebugViewContentProvider {

    public Object[] getChildren(Object obj) {
		if (obj instanceof List) {
			return ((List) obj).toArray();
		}
        if (obj instanceof Event) {
    		return ((Event) obj).getSubEvents();
        }
        return new Object[0];
    }
    
    public boolean hasChildren(Object obj) {
        if (obj instanceof Event) {
    		return ((Event) obj).hasSubEvents();
        }
        return false;
    }
}
