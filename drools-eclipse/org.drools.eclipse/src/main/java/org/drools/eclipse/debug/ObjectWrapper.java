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
        return object.getWaitingThreads();
    }

    public IJavaThread getOwningThread() throws DebugException {
        return object.getOwningThread();
    }

    public IJavaObject[] getReferringObjects(long max) throws DebugException {
        return object.getReferringObjects(max);
    }

    public void disableCollection() throws DebugException {
        object.disableCollection();
    }

    public void enableCollection() throws DebugException {
        object.enableCollection();
    }

    public long getUniqueId() throws DebugException {
        return object.getUniqueId();
    }

    public boolean isNull() {
        return false;
    }
}
