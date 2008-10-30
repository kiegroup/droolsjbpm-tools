package org.drools.eclipse.flow.common.editor.core;

import org.drools.knowledge.definitions.process.Process;
import org.eclipse.jdt.core.IJavaProject;

public interface ProcessWrapperBuilder {
    
    ProcessWrapper getProcessWrapper(Process process, IJavaProject project);

}
