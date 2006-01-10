package org.drools.ide.debug;

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredVariableLogicalStructure;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Remote content manager for variables.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class RemoteVariableContentManager extends RemoteTreeContentManager {

   protected DroolsDebugEventHandlerView fView;
   private IDeferredWorkbenchAdapter fVariableLogicalStructureAdapter = new DeferredVariableLogicalStructure();
   
   public RemoteVariableContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site, DroolsDebugEventHandlerView view) {
       super(provider, viewer, site);
       fView = view;
   }

   protected IDeferredWorkbenchAdapter getAdapter(Object element) {
       if (element instanceof IVariable && fView != null && fView.isShowLogicalStructure()) {
           return fVariableLogicalStructureAdapter;
       }
       return super.getAdapter(element);
   }
   
}
