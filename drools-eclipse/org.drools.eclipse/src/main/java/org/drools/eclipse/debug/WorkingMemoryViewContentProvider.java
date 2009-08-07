package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * The Working Memory view content provider.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class WorkingMemoryViewContentProvider extends DroolsDebugViewContentProvider {

    private DroolsDebugEventHandlerView view;
    
    public WorkingMemoryViewContentProvider(DroolsDebugEventHandlerView view) {
        this.view = view;
    }
    
    protected String getEmptyString() {
    	return "The selected working memory is empty.";
    }

    public Object[] getChildren(Object obj) {
        try {
            IVariable[] variables = null;
            if (obj != null && obj instanceof IJavaObject
                    && "org.drools.reteoo.ReteooStatefulSession".equals(
                        ((IJavaObject) obj).getReferenceTypeName())) {
                variables = getWorkingMemoryElements((IJavaObject) obj);
            } else if (obj instanceof IVariable) {
            	if (view.isShowLogicalStructure()) {
            		IValue value = getLogicalValue(((IVariable) obj).getValue(), new ArrayList());
                	variables = value.getVariables();
                }
            	if (variables == null) {
                	variables = ((IVariable) obj).getValue().getVariables();
                }
            }
            if (variables == null) {
                return new Object[0];
            } else {
                cache(obj, variables);
                return variables;
            }
        } catch (DebugException e) {
            DroolsEclipsePlugin.log(e);
            return new Object[0];
        }
    }
    
    private IVariable[] getWorkingMemoryElements(IJavaObject stackObj) throws DebugException {
        IValue objects = DebugUtil.getValueByExpression("return iterateObjectsToList().toArray();", stackObj);
        if (objects instanceof IJavaArray) {
            IJavaArray array = (IJavaArray) objects;
            List result = new ArrayList();
            
            IJavaValue[] vals = array.getValues();
            
            for ( int i = 0; i < vals.length; i++ ) {
                result.add(new MyJavaVariable("[" + i + "]", vals[i]));
            }
            
//            objects = DebugUtil.getValueByExpression("return iterateNonDefaultEntryPointObjectsToList().toArray();", stackObj);
//
//            if (objects instanceof IJavaArray) {
//                IJavaArray array = (IJavaArray) objects;
//	            vals = array.getValues();
//	            for ( int i = 0; i < vals.length; i++ ) {
//	            	vals = array.getValues();
//	            	name = name.replace(' ', '_');
//	            	result.add(new MyVariableWrapper(name, 
//	            		new ObjectWrapper((IJavaObject) agendaGroup,
//	        				(IJavaVariable[]) activationsResult.toArray(new IJavaVariable[activationsResult.size()]))));
//	            }
//            }
            
            return (IVariable[]) result.toArray(new IVariable[0]);
        }
        
        return null;
    }
    
    public class MyJavaVariable implements IJavaVariable {

    	private String name;
    	private IJavaValue value;

    	public MyJavaVariable(String name, IJavaValue value) {
    		this.name = name;
    		this.value = value;
    	}
    	
    	public String getSignature() throws DebugException {
    		return ((IJavaValue)getValue()).getSignature();
    	}

    	public String getGenericSignature() throws DebugException {
    		return ((IJavaValue)getValue()).getGenericSignature();
    	}
    	
    	public IJavaType getJavaType() throws DebugException {
    		return ((IJavaValue)getValue()).getJavaType();
    	}

    	public boolean isLocal() {
    		return false;
    	}

    	public IValue getValue() {
    		return value;
    	}

    	public String getName() {
    		return name;
    	}

    	public String getReferenceTypeName() throws DebugException {
    		return ((IJavaValue) getValue()).getReferenceTypeName();
    	}

    	public boolean hasValueChanged() {
    		return false;
    	}

    	public boolean isPublic() {
    		return false;
    	}

    	public boolean isPrivate() {
    		return false;
    	}

    	public boolean isProtected() {
    		return false;
    	}

    	public boolean isPackagePrivate() {
    		return false;
    	}

    	public boolean isFinal() {
    		return false;
    	}

    	public boolean isStatic() {
    		return false;
    	}

    	public boolean isSynthetic() {
    		return false;
    	}

    	public String getModelIdentifier() {
    		return getValue().getModelIdentifier();
    	}

    	public IDebugTarget getDebugTarget() {
    		return ((IJavaValue)getValue()).getDebugTarget();
    	}

    	public ILaunch getLaunch() {
    		return getValue().getLaunch();
    	}

    	public void setValue(String expression) {
    	}

    	public void setValue(IValue value) {
    	}

    	public boolean supportsValueModification() {
    		return false;
    	}

    	public boolean verifyValue(String expression) {
    		return false;
    	}

    	public boolean verifyValue(IValue value) {
    		return false;
    	}

    	public Object getAdapter(Class adapter) {
    		if (IJavaVariable.class.equals(adapter) ||
    			IJavaModifiers.class.equals(adapter)) {
    			return this;
    		}
    		return null;
    	}

    	public boolean equals(Object obj) {
    		if (obj instanceof MyJavaVariable) {
    			MyJavaVariable var = (MyJavaVariable) obj;
    			return var.getName().equals(getName()) && var.getValue().equals(getValue());
    		}
    		return false;
    	}

    	public int hashCode() {
    		return name.hashCode() + value.hashCode();
    	}
    }
    
}
