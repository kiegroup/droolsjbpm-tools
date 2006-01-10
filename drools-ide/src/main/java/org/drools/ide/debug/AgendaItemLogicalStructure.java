package org.drools.ide.debug;


import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILogicalStructureType;
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
 * The logical structure of an Agenda Item.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaItemLogicalStructure implements ILogicalStructureType {

    public String getDescription() {
        return "Agenda Item Logical Structure";
    }

    public String getId() {
        return DroolsIDEPlugin.getUniqueIdentifier() + ".AgendaItemLogicalStructure";
    }

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
            for (IJavaInterfaceType interfaceType: interfaceTypes) {
                if ("org.drools.spi.Activation".equals(interfaceType.getName())) {
                    return true;
                }
            }
            return false;
        } catch (DebugException e) {
            DroolsIDEPlugin.log(e);
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
        IJavaArray ruleParameters = (IJavaArray) DebugUtil.getValueByExpression("return getRule().getParameterDeclarations().toArray();", value);
        IJavaVariable[] variables = new IJavaVariable[ruleParameters.getSize() + 1];
        variables[0] = new VariableWrapper("ruleName", (IJavaValue) DebugUtil.getValueByExpression("return getRule().getName();", value));
        int i = 1;
        for (IJavaValue declaration: ruleParameters.getValues()) {
            for (IVariable declarationVar: declaration.getVariables()) {
                if ("identifier".equals(declarationVar.getName())) {
                    String paramName = declarationVar.getValue().getValueString();
                    variables[i++] = new VariableWrapper(paramName, (IJavaValue) DebugUtil.getValueByExpression("return getTuple().get(getRule().getParameterDeclaration(\"" + paramName + "\"));", value));
                    break;
                }
            }
        }
        return new ObjectWrapper(javaValue, variables);
    }

    public String getDescription(IValue value) {
        return getDescription();
    }

}
