package org.drools.ide.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredVariableLogicalStructure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The Application Data View content provider.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ApplicationDataViewContentProvider extends DroolsDebugViewContentProvider {

    private DroolsDebugEventHandlerView view;
    private IWorkbenchAdapter logicalStructureAdapter =
        new DeferredVariableLogicalStructure();
    
    public ApplicationDataViewContentProvider(DroolsDebugEventHandlerView view) {
        this.view = view;
    }
    
    public Object[] getChildren(Object obj) {
        try {
            IVariable[] variables = null;
            if (obj instanceof IJavaStackFrame) {
                variables = getApplicationDataElements((IJavaStackFrame) obj);
                if (variables == null) {
                    for (IStackFrame frame: ((IJavaStackFrame) obj).getThread().getStackFrames()) {
                        variables = getApplicationDataElements((IJavaStackFrame) frame);
                        if (variables != null) {
                            break;
                        }
                    }
                }
            } else if (obj instanceof IVariable) {
                if (view.isShowLogicalStructure()) {
                    Object[] result = logicalStructureAdapter.getChildren(obj);
                    if (result != null) {
                        return result;
                    }
                }
                variables = ((IVariable) obj).getValue().getVariables();
            }
            if (variables == null) {
                return new Object[0];
            } else {
                cache(obj, variables);
                return variables;
            }
        } catch (DebugException e) {
            if (getExceptionHandler() != null) {
                getExceptionHandler().handleException(e);
            } else {
               DroolsIDEPlugin.log(e);
            }
            return new Object[0];
        }
    }
    
    private IVariable[] getApplicationDataElements(IJavaStackFrame stackFrame) throws DebugException {
        IJavaObject stackObj = stackFrame.getThis();
        if ((stackObj != null)
                && (stackObj.getJavaType() != null)
                && ("org.drools.reteoo.WorkingMemoryImpl".equals(stackObj.getJavaType().getName()))) {
            IValue objects = DebugUtil.getValueByExpression("return getApplicationDataMap().entrySet().toArray();", stackObj);
            if (objects instanceof IJavaArray) {
                IJavaArray array = (IJavaArray) objects;
                List<IVariable> result = new ArrayList<IVariable>();
                for (IJavaValue mapEntry: array.getValues()) {
                    String key = null;
                    IJavaValue value = null;
                    for (IVariable var: mapEntry.getVariables()) {
                        if ("key".equals(var.getName())) {
                            key = var.getValue().getValueString();
                        } else if ("value".equals(var.getName())) {
                            value = (IJavaValue) var.getValue();
                        }
                    }
                    result.add(new VariableWrapper(key, value));
                }
                return result.toArray(new IVariable[0]);
            }
        }
        return null;
    }    
}
