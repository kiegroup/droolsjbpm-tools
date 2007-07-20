package org.drools.eclipse.debug;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Creates a Variable from an IValue and a given name.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class VariableWrapper extends PlatformObject implements IJavaVariable  {

    private String name;
    private IJavaValue value;
    private boolean isLocal = false;
    private boolean isPublic = false;
    
    public VariableWrapper(String name, IJavaValue value) {
        this.name = name;
        this.value = value;
    }
    
    public String getSignature() throws DebugException {
        return ((IJavaValue) getValue()).getSignature();
    }

    public String getGenericSignature() throws DebugException {
        return ((IJavaValue) getValue()).getGenericSignature();
    }
    
    public IJavaType getJavaType() throws DebugException {
        return ((IJavaValue) getValue()).getJavaType();
    }

    public boolean isLocal() {
        return isLocal;
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
        return isPublic;
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
        return ((IJavaValue) getValue()).getDebugTarget();
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
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj instanceof VariableWrapper) {
            VariableWrapper var = (VariableWrapper) obj;
            return var.getName().equals(getName()) && var.getValue().equals(getValue());
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode() + value.hashCode();
    }

    public void setLocal(boolean val) {
        isLocal=val;
    }
    
    public void setPublic(boolean val) {
        isPublic = val;
    }
}
