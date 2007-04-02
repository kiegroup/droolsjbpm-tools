package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.debug.AuditView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.FileDialog;

/**
 * Action to open a log.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class OpenLogAction extends Action {
    
    private AuditView view;

    public OpenLogAction(AuditView view) {
        super(null, IAction.AS_PUSH_BUTTON);
        this.view = view;
        setToolTipText("Open Log");
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.OPEN_LOG));
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".OpenLogAction");
    }

    public void run() {
        if (!view.isAvailable()) {
            return;
        }
        FileDialog dialog = new FileDialog(view.getSite().getShell());
        dialog.setFilterExtensions(new String[] { "*.log" });
        String fileName = dialog.open();
        view.setLogFile(fileName);  
        BusyIndicator.showWhile(view.getViewer().getControl().getDisplay(), new Runnable() {
            public void run() {
            	view.getViewer().refresh();                    
            }
        });         
    }
}
