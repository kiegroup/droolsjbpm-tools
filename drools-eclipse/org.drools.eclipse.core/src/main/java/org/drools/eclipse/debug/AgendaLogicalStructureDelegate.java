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
 * The logical structures of Agenda.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaLogicalStructureDelegate implements ILogicalStructureTypeDelegate {

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
                if ("org.drools.spi.Activation".equals(interfaceTypes[i].getName())) {
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
        IJavaArray ruleParameters = (IJavaArray) DebugUtil.getValueByExpression("return getRule().getDeclarations();", value);
        List variables = new ArrayList();
        variables.add(new VariableWrapper("ruleName", (IJavaValue) DebugUtil.getValueByExpression("return getRule().getName();", value)));
        
        IJavaValue[] javaValues = ruleParameters.getValues();
        for ( int j = 0; j < javaValues.length; j++ ) {
            IJavaValue declaration = javaValues[j];
            IVariable[] vars = declaration.getVariables();
            for ( int k = 0; k < vars.length; k++ ) {
                IVariable declarationVar = vars[k];
                if ("identifier".equals(declarationVar.getName())) {
                    String paramName = declarationVar.getValue().getValueString();
                    IJavaValue varValue = (IJavaValue) DebugUtil.getValueByExpression("return getRule().getDeclaration(\"" + paramName + "\").getValue(((org.drools.common.InternalFactHandle) getTuple().get(getRule().getDeclaration(\"" + paramName + "\"))).getObject());", value);
                    if (varValue != null) {
                    	variables.add(new VariableWrapper(paramName, varValue));
                    }
                    break;
                }                
            }            
        }
        return new ObjectWrapper(javaValue, (IJavaVariable[]) variables.toArray(new IJavaVariable[variables.size()]));
    }
}
