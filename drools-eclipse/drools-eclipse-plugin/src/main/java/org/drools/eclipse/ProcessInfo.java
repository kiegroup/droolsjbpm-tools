package org.drools.eclipse;

import java.util.List;

import org.drools.process.core.Process;

public class ProcessInfo {
    
    private String processId;
    private Process process;
    private List errors;
    
    public ProcessInfo(String processId, Process process) {
        this.processId = processId;
        this.process = process;
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public Process getProcess() {
        return process;
    }
    
    public List getErrors() {
        return errors;
    }
    
    public void setErrors(List errors) {
        this.errors = errors;
    }

}
