package org.drools.ide.debug;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * A generic Drools debug view event handler.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsDebugViewEventHandler extends AbstractDebugEventHandler {   
    
    public DroolsDebugViewEventHandler(AbstractDebugView view) {
        super(view);
    }
    
    protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
        for (int i = 0; i < events.length; i++) {   
            DebugEvent event = events[i];
            switch (event.getKind()) {
                case DebugEvent.SUSPEND:
                    doHandleSuspendEvent(event);
                    break;
                case DebugEvent.CHANGE:
                    doHandleChangeEvent(event);
                    break;
                case DebugEvent.RESUME:
                    doHandleResumeEvent(event);
                    break;
            }
        }
    }
    
    protected void updateForDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {   
            DebugEvent event = events[i];
            switch (event.getKind()) {
                case DebugEvent.TERMINATE:
                    doHandleTerminateEvent(event);
                    break;
            }
        }
    }   

    protected void doHandleResumeEvent(DebugEvent event) {
        if (!event.isStepStart() && !event.isEvaluation()) {
            getDebugView().psetViewerInput(StructuredSelection.EMPTY);
        }
    }

    protected void doHandleTerminateEvent(DebugEvent event) {
        getDebugView().pclearExpandedVariables(event.getSource());
    }
    
    protected void doHandleSuspendEvent(DebugEvent event) {
        if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
            if (event.getSource() instanceof ISuspendResume) {
                if (!((ISuspendResume)event.getSource()).isSuspended()) {
                    return;
                }
            }
            refresh();
            getDebugView().populateDetailPane();
        }       
    }
    
    protected void doHandleChangeEvent(DebugEvent event) {
        if (event.getDetail() == DebugEvent.STATE) {
            if (event.getSource() instanceof IVariable) {
                refresh(event.getSource());
            }
        } else {
            if (!(event.getSource() instanceof IExpression)) {
                refresh();
            }
        }   
    }   

    protected DroolsDebugEventHandlerView getDebugView() {
        return (DroolsDebugEventHandlerView) getView();
    }
    
    protected void viewBecomesVisible() {
        super.viewBecomesVisible();
        getDebugView().populateDetailPane();
    }

}