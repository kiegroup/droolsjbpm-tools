package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.debug.AuditView;
import org.drools.eclipse.debug.AuditView.Event;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

/**
 * Action to show the cause event of an audit event.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ShowEventCauseAction extends Action {
    
    private AuditView view;

    public ShowEventCauseAction(AuditView view) {
        super(null, IAction.AS_PUSH_BUTTON);
        this.view = view;
        setToolTipText("Show Cause");
        setText("Show Cause");
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".ShowEventCause");
    }

    public void run() {
    	Event event = view.getSelectedEvent();
    	if (event != null) {
    		Event cause = event.getCauseEvent();
    		if (cause != null) {
    			view.showEvent(cause);
    		}
    	}
    }
}
