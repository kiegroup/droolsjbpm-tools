/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
