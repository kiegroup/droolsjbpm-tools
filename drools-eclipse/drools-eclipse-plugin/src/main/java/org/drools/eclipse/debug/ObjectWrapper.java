package org.drools.eclipse.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Creates a IJavaObject from an IJavaObject and given variables.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ObjectWrapper implements IJavaObject {
    
    private IJavaObject object;
    private IJavaVariable[] variables;
    
    public ObjectWrapper(IJavaObject object, IJavaVariable[] variables) {
        this.object = object;
        this.variables = variables;
    }

    public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend) throws DebugException {
        return object.sendMessage(selector, signature, args, thread, superSend);
    }

    public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, String typeSignature) throws DebugException {
        return object.sendMessage(selector, signature, args, thread, typeSignature);
    }

    public IJavaFieldVariable getField(String name, boolean superField) throws DebugException {
        return object.getField(name, superField);
    }

    public IJavaFieldVariable getField(String name, String typeSignature) throws DebugException {
        return object.getField(name, typeSignature);
    }

    public String getSignature() throws DebugException {
        return object.getSignature();
    }

    public String getGenericSignature() throws DebugException {
        return object.getGenericSignature();
    }

    public IJavaType getJavaType() throws DebugException {
        return object.getJavaType();
    }

    public String getReferenceTypeName() throws DebugException {
        return object.getReferenceTypeName();
    }

    public String getValueString() throws DebugException {
        return object.getValueString();
    }

    public boolean isAllocated() throws DebugException {
        return object.isAllocated();
    }

    public IVariable[] getVariables() {
        return variables;
    }

    public boolean hasVariables() {
        return variables.length > 0;
    }
    
    protected void setVariables(IJavaVariable[] variables) {
    	this.variables = variables;
    }

    public String getModelIdentifier() {
        return object.getModelIdentifier();
    }

    public IDebugTarget getDebugTarget() {
        return object.getDebugTarget();
    }

    public ILaunch getLaunch() {
        return object.getLaunch();
    }

    public Object getAdapter(Class adapter) {
        return object.getAdapter(adapter);
    }

    public IJavaThread[] getWaitingThreads() throws DebugException {
        return null;
    }

    public IJavaThread getOwningThread() throws DebugException {
        return null;
    }
}
