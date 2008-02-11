package org.drools.eclipse;

import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.process.core.Process;
import org.drools.rule.Package;

public class ProcessInfo {
    
    private Package processPackage;
    private String processId;
    private ProcessWrapper processWrapper;
    private List errors;
    
    public ProcessInfo(String processId, ProcessWrapper processWrapper, Package processPackage) {
        this.processId = processId;
        this.processPackage = processPackage;
        this.processWrapper = processWrapper;
    }
    
    public Process getProcess() {
        return (Process) processPackage.getRuleFlows().get(processId);
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public ProcessWrapper getProcessWrapper() {
        return processWrapper;
    }
    
    public List getErrors() {
        return errors;
    }
    
    public void setErrors(List errors) {
        this.errors = errors;
    }

}
