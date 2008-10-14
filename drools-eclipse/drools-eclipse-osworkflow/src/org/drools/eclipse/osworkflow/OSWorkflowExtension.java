package org.drools.eclipse.osworkflow;

import org.drools.eclipse.flow.common.editor.ProcessExtension;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapperBuilder;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPartFactory;
import org.drools.eclipse.osworkflow.core.OSWorkflowWrapperBuilder;
import org.drools.eclipse.osworkflow.editor.editpart.OSWorkflowEditPartFactory;

public class OSWorkflowExtension implements ProcessExtension {

    public boolean acceptsProcess(String type) {
        return "OSWorkflow".equals(type);
    }

    public ProcessEditPartFactory getProcessEditPartFactory() {
        return new OSWorkflowEditPartFactory();
    }

    public ProcessWrapperBuilder getProcessWrapperBuilder() {
        return new OSWorkflowWrapperBuilder();
    }

}
