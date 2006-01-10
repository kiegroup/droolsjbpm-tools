package org.drools.ide.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A generic Drools debug view content provider.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsDebugViewContentProvider implements ITreeContentProvider {
    
    private Map<Object, Object> parentCache;
    private IDebugExceptionHandler exceptionHandler;
    
    public DroolsDebugViewContentProvider() {
        parentCache = new HashMap<Object, Object>(10);
    }
    
    public Object[] getChildren(Object parent) {
        return null;
    }
    
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    protected void cache(Object parent, Object[] children) {        
        for (int i = 0; i < children.length; i++) {
            parentCache.put(children[i], parent);
        }       
    }
    
    public Object getParent(Object item) {
        return parentCache.get(item);
    }

    public void dispose() {
        parentCache= null;
    }
    
    protected void clearCache() {
        if (parentCache != null) {
            parentCache.clear();
        }
    }
    
    public void removeCache(Object[] children) {
        if (parentCache == null) {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            parentCache.remove(children[i]);   
        }
    }

    public boolean hasChildren(Object element) {
        try {
            if (element instanceof IVariable) {
                IValue v = ((IVariable)element).getValue();
                return v != null && v.hasVariables();
            }
            if (element instanceof IValue) {
                return ((IValue)element).hasVariables();
            }
            if (element instanceof IStackFrame) {
                return ((IStackFrame)element).hasVariables();
            }
        } catch (DebugException e) {
            DroolsIDEPlugin.log(e);
            return false;
        }
        return false;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        clearCache();
    }
    
    public List<Object> getCachedDecendants(Object parent) {
        Iterator children = parentCache.keySet().iterator();
        List<Object> cachedChildren = new ArrayList<Object>(10);
        while (children.hasNext()) {
            Object child = children.next();
            if (isCachedDecendant(child, parent)) {
                cachedChildren.add(child);
            }
        }
        return cachedChildren;
    }
    
    protected boolean isCachedDecendant(Object child, Object parent) {
        Object p = getParent(child);
        while (p != null) {
            if (p.equals(parent)) {
                return true;
            }
            p = getParent(p);
        }
        return false;
    }
    
    public void setExceptionHandler(IDebugExceptionHandler handler) {
        exceptionHandler = handler;
    }
    
    protected IDebugExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
    
}