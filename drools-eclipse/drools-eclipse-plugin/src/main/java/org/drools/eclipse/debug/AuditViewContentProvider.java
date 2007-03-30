package org.drools.eclipse.debug;

import java.util.List;

import org.drools.eclipse.debug.AuditView.Event;

public class AuditViewContentProvider extends DroolsDebugViewContentProvider {

    protected String getEmptyString() {
    	return "The selected audit log is empty.";
    }

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
