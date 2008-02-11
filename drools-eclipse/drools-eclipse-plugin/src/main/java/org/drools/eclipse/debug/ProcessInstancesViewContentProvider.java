package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * The process instances view content provider.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ProcessInstancesViewContentProvider extends DroolsDebugViewContentProvider {

    private DroolsDebugEventHandlerView view;
    
    public ProcessInstancesViewContentProvider(DroolsDebugEventHandlerView view) {
        this.view = view;
    }
    
    protected String getEmptyString() {
    	return "The selected working memory has no process instances.";
    }

    public Object[] getChildren(Object obj) {
        try {
            IVariable[] instances = null;
            if (obj != null && obj instanceof IJavaObject
                    && "org.drools.reteoo.ReteooStatefulSession".equals(
                        ((IJavaObject) obj).getReferenceTypeName())) {
                instances = getProcessInstances((IJavaObject) obj);
            } else if (obj instanceof IVariable) {
            	if (view.isShowLogicalStructure()) {
            		IValue value = getLogicalValue(((IVariable) obj).getValue(), new ArrayList<IVariable>());
            		instances = value.getVariables();
                }
            	if (instances == null) {
            	    instances = ((IVariable) obj).getValue().getVariables();
                }
            }
            if (instances == null) {
                return new Object[0];
            } else {
                cache(obj, instances);
                return instances;
            }
        } catch (DebugException e) {
            DroolsEclipsePlugin.log(e);
            return new Object[0];
        }
    }
    
    private IVariable[] getProcessInstances(IJavaObject stackObj) throws DebugException {
        IValue objects = DebugUtil.getValueByExpression("return getProcessInstances().toArray();", stackObj);
        if (objects instanceof IJavaArray) {
            IJavaArray array = (IJavaArray) objects;
            List<IVariable> result = new ArrayList<IVariable>();
            IJavaValue[] javaVals = array.getValues();
            for ( int i = 0; i < javaVals.length; i++ ) {
                IJavaValue processInstance = javaVals[i];
                String id = null;
                IVariable[] vars = processInstance.getVariables();
                for ( int j = 0; j < vars.length; j++ ) {
                    IVariable var = vars[j];
                    if ("id".equals(var.getName())) {
                        id = var.getValue().getValueString();
                    }
                }
                result.add(new VariableWrapper("[" + id + "]", processInstance));
            }
            return result.toArray(new IVariable[result.size()]);
        }
        return null;
    }
    
}
