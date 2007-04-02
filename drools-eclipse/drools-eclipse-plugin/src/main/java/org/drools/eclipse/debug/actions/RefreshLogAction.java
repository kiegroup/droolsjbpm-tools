package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.debug.AuditView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to refresh the log.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class RefreshLogAction extends Action {
    
    private AuditView view;

    public RefreshLogAction(AuditView view) {
        super(null, IAction.AS_PUSH_BUTTON);
        this.view = view;
        setToolTipText("Refresh Log");
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.REFRESH_LOG));
        setDisabledImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.REFRESH_LOG_DISABLED));
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".RefreshLogAction");
    }

    public void run() {
        if (!view.isAvailable()) {
            return;
        }
        view.refresh();  
        BusyIndicator.showWhile(view.getViewer().getControl().getDisplay(), new Runnable() {
            public void run() {
            	view.getViewer().refresh();                    
            }
        });         
    }
}
