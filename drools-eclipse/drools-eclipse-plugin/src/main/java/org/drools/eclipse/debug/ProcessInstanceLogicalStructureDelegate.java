package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * The logical structures of a process instance.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class ProcessInstanceLogicalStructureDelegate implements ILogicalStructureTypeDelegate {

    public boolean providesLogicalStructure(IValue value) {
        if (!(value instanceof IJavaObject)) {
            return false;
        }
        IJavaObject javaValue= (IJavaObject) value;
        try {
            IJavaType type= javaValue.getJavaType();
            if (!(type instanceof IJavaClassType)) {
                return false;
            }
            IJavaClassType classType = (IJavaClassType) type;
            IJavaInterfaceType[] interfaceTypes = classType.getAllInterfaces();
            for ( int i = 0; i < interfaceTypes.length; i++ ) {
                if ("org.drools.process.instance.ProcessInstance".equals(interfaceTypes[i].getName())) {
                    return true;
                }                
            }
            return false;
        } catch (DebugException e) {
            DroolsEclipsePlugin.log(e);
            return false;
        }
    }

    public IValue getLogicalStructure(IValue value) throws CoreException {
        if (!(value instanceof IJavaObject)) {
            return null;
        }
        IJavaObject javaValue = (IJavaObject) value;
        if (!providesLogicalStructure(value)) {
            return null;
        }
        List<IJavaVariable> variables = new ArrayList<IJavaVariable>();
        
        IJavaValue id = null;
        IJavaValue processId = null;
        IJavaValue processName = null;
        IVariable[] vars = value.getVariables();
        for ( int j = 0; j < vars.length; j++ ) {
            IVariable var = vars[j];
            if ("id".equals(var.getName())) {
                id = (IJavaValue)var.getValue();
            } else if ("process".equals(var.getName())) {
                IJavaValue process = (IJavaValue) var.getValue();
                IVariable[] vars2 = process.getVariables();
                for ( int k = 0; k < vars2.length; k++ ) {
                    IVariable var2 = vars2[k];
                    if ("id".equals(var2.getName())) {
                        processId = (IJavaValue) var2.getValue();
                    } else if ("name".equals(var2.getName())) {
                        processName = (IJavaValue) var2.getValue();
                    }
                }
            }
        }
        variables.add(new VariableWrapper("id", id));
        variables.add(new VariableWrapper("processName", processName));
        variables.add(new VariableWrapper("processId", processId));
        
        IJavaArray nodeInstances = (IJavaArray) DebugUtil.getValueByExpression("return getNodeInstances().toArray();", value);
        List<IVariable> nodeInstancesResult = new ArrayList<IVariable>();
        IJavaValue[] javaVals = nodeInstances.getValues();
        for ( int i = 0; i < javaVals.length; i++ ) {
            IJavaValue nodeInstance = javaVals[i];
            id = null;
            vars = nodeInstance.getVariables();
            for ( int j = 0; j < vars.length; j++ ) {
                IVariable var = vars[j];
                if ("id".equals(var.getName())) {
                    id = (IJavaValue) var.getValue();
                }
            }
            nodeInstancesResult.add(new VariableWrapper("[" + id.getValueString() + "]", nodeInstance));
        }
        variables.add(new VariableWrapper("nodeInstances", new ObjectWrapper(nodeInstances, nodeInstancesResult.toArray(new IJavaVariable[nodeInstancesResult.size()]))));
        
        return new ObjectWrapper(javaValue, variables.toArray(new IJavaVariable[variables.size()]));
    }
}
