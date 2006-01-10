package org.drools.ide.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.IRemoteTreeViewerUpdateListener;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

/**
 * Variables viewer.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsVariablesViewer extends RemoteTreeViewer {
    
    private List<IRemoteTreeViewerUpdateListener> listeners = new ArrayList<IRemoteTreeViewerUpdateListener>();
    private StateRestorationJob stateRestorationJob = new StateRestorationJob(DebugUIViewsMessages.RemoteTreeViewer_0); //$NON-NLS-1$
    private DroolsDebugEventHandlerView view = null;
    
    private class StateRestorationJob extends UIJob {
        public StateRestorationJob(String name) {
            super(name);
            setSystem(true);
        }

        public IStatus runInUIThread(IProgressMonitor monitor) {
            restoreExpansionState();
            return Status.OK_STATUS;
        }   
    }

    public DroolsVariablesViewer(Composite parent, int style, DroolsDebugEventHandlerView view) {
        super(parent, style);
        this.view = view;
    }
    
    protected Item newItem(Widget parent, int style, int index) {
        Item item = super.newItem(parent, style, index);
        if (index != -1 && getSelection(getControl()).length == 0) {
            //ignore the dummy items
            showItem(item);
        } 
        return item;
    }
    
    public void setExpandedElements(Object[] elements) {
        getControl().setRedraw(false);
        super.setExpandedElements(elements);
        getControl().setRedraw(true);
    }
    
    protected void runDeferredUpdates() {
        super.runDeferredUpdates();
        stateRestorationJob.schedule();
    }

    public void collapseAll() {
        if (getRoot() != null) {
            super.collapseAll();
        }
    }
    

    protected synchronized void restoreExpansionState() {
        cancelJobs();
        for (IRemoteTreeViewerUpdateListener listener: listeners) {
            listener.treeUpdated();
        }
    }
    
    public void addUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        listeners.remove(listener);
    }

    public synchronized void replace(Object parent, Object[] children, int offset) {
        if (view != null) {
            if (children.length == 1 && children[0] instanceof DebugException) {
                IStatus status = ((DebugException)children[0]).getStatus();
                if (status != null) {
                    String message = status.getMessage();
                    if (message != null) {
                        view.showMessage(message);
                    }
                }
                return;
            }
            view.showViewer();
        }
        super.replace(parent, children, offset);
    }
    
    
    public boolean expandPath(IPath path) {
        String[] strings = path.segments();
        Item[] children = getChildren(getControl());
        return internalExpandPath(strings, 0, children);

    }
    protected boolean internalExpandPath(String[] segments, int index, Item[] children) {
        try {
            String pathSegment = segments[index];
            for (int j = 0; j < children.length; j++) {
                Item child = children[j];
                Object data = child.getData();
                String name = null;
                if (data instanceof IVariable) {
                    IVariable var = (IVariable) data;
                    name = var.getName();
                } else if (data instanceof IRegisterGroup) {
                    IRegisterGroup  registerGroup = (IRegisterGroup) data;
                    name = registerGroup.getName();
                }
                
                if (name != null && pathSegment.equals(name)) {
                    ITreeContentProvider provider = (ITreeContentProvider) getContentProvider();
                    provider.getChildren(child.getData());
                    setExpanded(child, true);
                    index++;
                    if (index < segments.length) {
                        Item[] newChildren = getChildren(child);
                        return internalExpandPath(segments, index, newChildren);
                    }
                    return true;
                } 
            }
        } catch (DebugException e) {
            DroolsIDEPlugin.log(e);
        }
        return false;
    }
}
