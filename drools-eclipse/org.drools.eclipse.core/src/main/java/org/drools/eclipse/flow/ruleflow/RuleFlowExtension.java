package org.drools.eclipse.flow.ruleflow;

import org.drools.eclipse.flow.common.editor.ProcessExtension;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapperBuilder;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPartFactory;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowWrapperBuilder;
import org.drools.eclipse.flow.ruleflow.editor.editpart.RuleFlowEditPartFactory;

public class RuleFlowExtension implements ProcessExtension {

    public boolean acceptsProcess(String type) {
        return "RuleFlow".equals(type);
    }
    
    public ProcessEditPartFactory getProcessEditPartFactory() {
        return new RuleFlowEditPartFactory();
    }

    public ProcessWrapperBuilder getProcessWrapperBuilder() {
        return new RuleFlowWrapperBuilder();
    }

}
