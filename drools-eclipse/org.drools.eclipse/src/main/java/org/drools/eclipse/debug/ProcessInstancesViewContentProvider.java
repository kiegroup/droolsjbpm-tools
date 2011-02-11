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

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.reteoo.ReteooStatefulSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.jbpm.process.instance.ProcessInstance;

/**
 * The process instances view content provider.
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
    
    @SuppressWarnings("unused")
    private ProcessInstance[] getProcessInstances(ReteooStatefulSession session) {
        return (ProcessInstance[]) session.getProcessInstances().toArray();
    }
    
}
