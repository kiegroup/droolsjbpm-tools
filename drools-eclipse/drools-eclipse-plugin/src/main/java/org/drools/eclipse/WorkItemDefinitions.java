package org.drools.eclipse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.process.core.WorkDefinition;
import org.drools.process.core.datatype.impl.type.StringDataType;
import org.drools.process.core.impl.ParameterDefinitionImpl;
import org.drools.process.core.impl.WorkDefinitionExtensionImpl;

public final class WorkItemDefinitions {
    
    private static Map workDefinitions = new HashMap();
    
    static {
        // Email
        WorkDefinitionExtensionImpl emailDefinition = new WorkDefinitionExtensionImpl();
        emailDefinition.setName("Email");
        emailDefinition.addParameter(new ParameterDefinitionImpl("From", new StringDataType()));
        emailDefinition.addParameter(new ParameterDefinitionImpl("To", new StringDataType()));
        emailDefinition.addParameter(new ParameterDefinitionImpl("Subject", new StringDataType()));
        emailDefinition.addParameter(new ParameterDefinitionImpl("Text", new StringDataType()));
        emailDefinition.setDisplayName("Email");
        emailDefinition.setIcon("icons/import_statement.gif");
        emailDefinition.setCustomEditor("org.drools.eclipse.flow.common.editor.editpart.work.SampleCustomEditor");
        addWorkDefinition(emailDefinition);
        // Log
        WorkDefinitionExtensionImpl logDefinition = new WorkDefinitionExtensionImpl();
        logDefinition.setName("Log");
        logDefinition.addParameter(new ParameterDefinitionImpl("Message", new StringDataType()));
        logDefinition.setDisplayName("Log");
        logDefinition.setIcon("icons/open.gif");
        addWorkDefinition(logDefinition);
        // Order
//        WorkDefinitionExtensionImpl orderDefinition = new WorkDefinitionExtensionImpl();
//        orderDefinition.setName("Order");
//        orderDefinition.addParameter(new ParameterDefinitionImpl("OrderId", new StringDataType()));
//        orderDefinition.setDisplayName("Order");
//        orderDefinition.setIcon("icons/open.gif");
//        addWorkDefinition(orderDefinition);

    }
    
    private WorkItemDefinitions() {
    }
    
    private static void addWorkDefinition(WorkDefinition workDefinition) {
        workDefinitions.put(workDefinition.getName(), workDefinition);
    }
    
    public static Collection getWorkDefinitions() {
        return workDefinitions.values();
    }

    public static WorkDefinition getWorkDefinition(String name) {
        return (WorkDefinition) workDefinitions.get(name);
    }

}
