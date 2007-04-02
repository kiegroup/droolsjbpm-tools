package org.drools.eclipse.debug.actions;


import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.debug.DroolsDebugEventHandlerView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to toggle the display of the logical structure of variables
 * that are shown in the tree.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ShowLogicalStructureAction extends Action {
    
    private DroolsDebugEventHandlerView view;

    public ShowLogicalStructureAction(DroolsDebugEventHandlerView view) {
        super(null, IAction.AS_CHECK_BOX);
        this.view = view;
        setToolTipText("Show Logical Structure");
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_LOGICAL));
        setDisabledImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_LOGICAL_DISABLED));
        setId(DroolsEclipsePlugin.getUniqueIdentifier() + ".ShowLogicalStructureAction");
    }

    public void run() {
        valueChanged(isChecked());
    }

    private void valueChanged(boolean on) {
        if (!view.isAvailable()) {
            return;
        }
        view.setShowLogicalStructure(on);  
        BusyIndicator.showWhile(view.getViewer().getControl().getDisplay(), new Runnable() {
            public void run() {
            	view.getViewer().refresh();                    
            }
        });         
    }
}
