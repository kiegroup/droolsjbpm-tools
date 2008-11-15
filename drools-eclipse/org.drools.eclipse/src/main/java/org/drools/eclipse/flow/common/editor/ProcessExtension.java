package org.drools.eclipse.flow.common.editor;

import org.drools.eclipse.flow.common.editor.core.ProcessWrapperBuilder;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPartFactory;

public interface ProcessExtension {

    boolean acceptsProcess(String type);
    
    ProcessWrapperBuilder getProcessWrapperBuilder();
    
    ProcessEditPartFactory getProcessEditPartFactory();
    
}
