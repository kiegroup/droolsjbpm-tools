package org.drools.ide.debug;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILogicalStructureProvider;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * The logical structures of Agenda.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class AgendaLogicalStructures implements ILogicalStructureProvider {

    private static final ILogicalStructureType[] agendaLogicalStructures;
    static {
        agendaLogicalStructures = new ILogicalStructureType[] {
            new AgendaItemLogicalStructure()
        };
    }
    
    public ILogicalStructureType[] getLogicalStructureTypes(IValue value) {
        if (!(value instanceof IJavaObject)) {
            return new ILogicalStructureType[0];
        }
        IJavaObject javaValue= (IJavaObject) value;
        try {
            IJavaType type= javaValue.getJavaType();
            if (!(type instanceof IJavaClassType)) {
                return new ILogicalStructureType[0];
            }
            IJavaClassType classType = (IJavaClassType) type;
            IJavaInterfaceType[] interfaceTypes = classType.getAllInterfaces();
            for (IJavaInterfaceType interfaceType: interfaceTypes) {
                if ("org.drools.spi.Activation".equals(interfaceType.getName())) {
                    return agendaLogicalStructures;
                }
            }
            return new ILogicalStructureType[0];
        } catch (DebugException e) {
            DroolsIDEPlugin.log(e);
            return new ILogicalStructureType[0];
        }
    }

}
