package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.debug.AuditView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to clear the log.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DeleteLogAction extends Action {
    
    private AuditView view;

    public DeleteLogAction(AuditView view) {
        super(null, IAction.AS_PUSH_BUTTON);
        this.view = view;
        setToolTipText("Clear Log");
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.DELETE_LOG));
        setDisabledImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.DELETE_LOG_DISABLED));
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".ClearLogAction");
    }

    public void run() {
        if (!view.isAvailable()) {
            return;
        }
        view.deleteLog();  
        BusyIndicator.showWhile(view.getViewer().getControl().getDisplay(), new Runnable() {
            public void run() {
            	view.getViewer().refresh();                    
            }
        });         
    }
}
