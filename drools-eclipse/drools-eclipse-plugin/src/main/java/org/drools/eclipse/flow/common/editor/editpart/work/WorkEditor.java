package org.drools.eclipse.flow.common.editor.editpart.work;

import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;

public interface WorkEditor {
    
    void setWorkDefinition(WorkDefinition definition);
    
    void setWork(Work work);
    
    void show();
    
    Work getWork();

}
