package org.drools.ide.debug.actions;


import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.debug.DroolsDebugEventHandlerView;
import org.eclipse.jface.action.Action;

/**
 * Action that toggles the Detail page of a Drools debug event handler view.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsShowDetailPaneAction extends Action {

    private DroolsDebugEventHandlerView view;

    public DroolsShowDetailPaneAction(DroolsDebugEventHandlerView view) {
        super("Show Detail Pane", Action.AS_CHECK_BOX); 
        setView(view);
        setToolTipText("Show Detail Pane"); 
        setImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_DETAIL));
        setDisabledImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_DETAIL_DISABLED));
        setHoverImageDescriptor(DroolsPluginImages.getImageDescriptor(DroolsPluginImages.IMG_DETAIL));
        setId(DroolsIDEPlugin.getUniqueIdentifier() + ".ShowDetailPaneAction"); 
    }
    
    public void run() {
        toggleDetailPane(isChecked());
    }

    private void toggleDetailPane(boolean on) {
        getView().toggleDetailPane(on);
    }

    public void setChecked(boolean value) {
        super.setChecked(value);
        toggleDetailPane(value);
    }
    
    protected DroolsDebugEventHandlerView getView() {
        return view;
    }

    protected void setView(DroolsDebugEventHandlerView view) {
        this.view = view;
    }
}