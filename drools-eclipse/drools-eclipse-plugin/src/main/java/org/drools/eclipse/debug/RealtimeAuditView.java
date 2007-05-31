package org.drools.eclipse.debug;

import java.io.ObjectInputStream;
import java.io.StringReader;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.thoughtworks.xstream.XStream;

/**
 * An audit view that shows the contents of the selected logger when debugging.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RealtimeAuditView extends AuditView implements ISelectionListener {

    public void dispose() {
        getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_VARIABLE_VIEW, this);
        super.dispose();
    }

    protected Viewer createViewer(Composite parent) {
        getSite().getPage().addSelectionListener(IDebugUIConstants.ID_VARIABLE_VIEW, this);
    	return super.createViewer(parent);
    }
    
    protected void setViewerInput(Object context) {
     	// if an in memory logger has been explicitly selected as variable
    	if (context instanceof IVariable) {
        	IVariable variable = (IVariable) context;
            try {
                IValue value = ((IVariable) context).getValue();
                if (value != null && value instanceof IJavaObject
                        && "org.drools.audit.WorkingMemoryInMemoryLogger".equals(
                            variable.getValue().getReferenceTypeName())) {
                	setAuditEvents((IJavaObject) value);
                }
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
            }
        }
    }

	protected void becomesHidden() {
		setViewerInput(null);
		super.becomesHidden();
	}

	protected void becomesVisible() {
		super.becomesVisible();
        ISelection selection = getSite().getPage().getSelection(
            IDebugUIConstants.ID_VARIABLE_VIEW);
        if (selection instanceof IStructuredSelection) {
            setViewerInput(((IStructuredSelection) selection).getFirstElement());
        }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!isAvailable()) {
            return;
        }
        if (selection == null) {
            setViewerInput(null);
        } else if (selection instanceof IStructuredSelection) {
            setViewerInput(((IStructuredSelection) selection).getFirstElement());
        }
    }
    
	protected void createActions() {
	}
	
    protected void configureToolBar(IToolBarManager tbm) {
    }
    
    private void setAuditEvents(IJavaObject inMemoryLogger) throws DebugException {
        IValue eventString = DebugUtil.getValueByExpression("return getEvents();", inMemoryLogger);
        String s = eventString.getValueString();
        if (s != null) {
        	try {
				XStream xstream = new XStream();
				ObjectInputStream in = xstream.createObjectInputStream(
					new StringReader(s));
				getViewer().setInput(createEventList((List) in.readObject()));
        	} catch (Throwable t) {
        		DroolsEclipsePlugin.log(t);
        		getViewer().setInput(null);
        	}
        } else {
        	getViewer().setInput(null);
        }
    }
}
