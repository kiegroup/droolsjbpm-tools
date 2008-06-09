package org.drools.eclipse.flow.common.editor.core;

import org.drools.process.core.Process;
import org.eclipse.jdt.core.IJavaProject;

public interface ProcessWrapperBuilder {
    
    ProcessWrapper getProcessWrapper(Process process, IJavaProject project);

}
