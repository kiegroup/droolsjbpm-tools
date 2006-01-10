package org.drools.ide.debug.actions;


import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.debug.DroolsDebugEventHandlerView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Drop down action that displays available logical structures for a selected
 * variable or expression.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ShowLogicalStructureAction extends Action {
    
    private DroolsDebugEventHandlerView fView;

    public ShowLogicalStructureAction(DroolsDebugEventHandlerView view) {
        super(null, IAction.AS_CHECK_BOX);
        setView(view);
        setToolTipText("Show Logical Structure");
        setHoverImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_LOGICAL));
        setDisabledImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_LOGICAL_DISABLED));
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_LOGICAL));
        setId(DroolsIDEPlugin.getUniqueIdentifier() + ".ShowLogicalStructureAction");
    }

    public void run() {
        valueChanged(isChecked());
    }

    private void valueChanged(boolean on) {
        if (!getView().isAvailable()) {
            return;
        }
        getView().setShowLogicalStructure(on);  
        BusyIndicator.showWhile(getView().getViewer().getControl().getDisplay(), new Runnable() {
            public void run() {
                getView().getViewer().refresh();                    
            }
        });         
    }

    public void setChecked(boolean value) {
        super.setChecked(value);
    }
    
    protected DroolsDebugEventHandlerView getView() {
        return fView;
    }

    protected void setView(DroolsDebugEventHandlerView view) {
        fView = view;
    }
}
