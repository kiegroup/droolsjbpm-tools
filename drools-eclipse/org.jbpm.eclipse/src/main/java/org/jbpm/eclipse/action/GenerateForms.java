package org.jbpm.eclipse.action;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.Process;
import org.drools.core.xml.SemanticModules;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.processes.RuleFlowMigrator;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.HumanTaskNode;

public class GenerateForms implements IObjectActionDelegate {

    private IFile file;
    private IWorkbenchPart targetPart;
    
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {
        if (file != null && file.exists()) {
            try {
                generateForms();
            } catch (Throwable t) {
                JBPMEclipsePlugin.log(t);
            }
        }

    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection) selection;
            if (structured.size() == 1) {
                Object element = structured.getFirstElement();
                if (element instanceof IFile) {
                    file = (IFile) element;
                }
            }
        }
    }

    public void generateForms() {
        try {
            final IJavaProject javaProject = JavaCore.create(file.getProject());
            if (javaProject == null || !javaProject.exists()) {
                return;
            }
            InputStreamReader isr = new InputStreamReader(((File) file).getContents());
            PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
            SemanticModules modules = configuration.getSemanticModules();
            modules.addSemanticModule(new BPMNSemanticModule());
            modules.addSemanticModule(new BPMNDISemanticModule());
            modules.addSemanticModule(new BPMNExtensionsSemanticModule());
            XmlProcessReader xmlReader = new XmlProcessReader( modules, Thread.currentThread().getContextClassLoader() );
            String xml = RuleFlowMigrator.convertReaderToString(isr);
            Reader reader = new StringReader(xml);
            List<Process> processes = xmlReader.read(reader);
            if (processes != null && processes.size() == 1) {
            	final RuleFlowProcess process = (RuleFlowProcess) processes.get(0);
            	List<HumanTaskNode> result = new ArrayList<HumanTaskNode>();
            	processNodes(process.getNodes(), result);
            	final Map<String, TaskDef> tasks = new HashMap<String, TaskDef>();
            	for (HumanTaskNode node: result) {
            		String taskName = (String) node.getWork().getParameter("TaskName");
            		if (taskName == null) {
            			break;
            		}
            		TaskDef task = tasks.get(taskName);
            		if (task == null) {
            			task = new TaskDef(taskName);
            			tasks.put(taskName, task);
            		}
            		for (Map.Entry<String, String> entry: node.getInMappings().entrySet()) {
            			if (task.getInputParams().get(entry.getKey()) == null) {
            				VariableScope variableScope = (VariableScope) node.resolveContext(VariableScope.VARIABLE_SCOPE, entry.getValue());
            	        	if (variableScope != null) {
            	        		task.getInputParams().put(entry.getKey(), variableScope.findVariable(entry.getValue()).getType().getStringType());
            	        	}
            			}
            		}
            		for (Map.Entry<String, String> entry: node.getOutMappings().entrySet()) {
            			if (task.getOutputParams().get(entry.getKey()) == null) {
            				VariableScope variableScope = (VariableScope) node.resolveContext(VariableScope.VARIABLE_SCOPE, entry.getValue());
            	        	if (variableScope != null && !"outcome".equals(entry.getKey())) {
            	        		task.getOutputParams().put(entry.getKey(), variableScope.findVariable(entry.getValue()).getType().getStringType());
            	        	}
            			}
            		}
            	}
                WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
                    public void execute(final IProgressMonitor monitor)
                            throws CoreException {
                        try {
                            IFolder folder = file.getProject().getFolder("src/main/resources");
                        	for (TaskDef task: tasks.values()) {
	                        	String fileName = task.getTaskName() + ".ftl";
	                        	String output = 
	                        		"<html>\n" +
	                        		"<body>\n" +
	                        		"<h2>" + task.getTaskName() + "</h2>\n" +
	                        		"<hr>\n" +
	                        		"<#if task.descriptions[0]??>\n" +
	                        		"Description: ${task.descriptions[0].text}<BR/>\n" +
	                        		"</#if>\n";
	                        	for (String input: task.getInputParams().keySet()) {
	                        		output += input + ": ${" + input + "}<BR/>\n"; 
	                        	}
	                        	output +=
	                        		"<form action=\"complete\" method=\"POST\" enctype=\"multipart/form-data\">\n";
	                        	for (String outputP: task.getOutputParams().keySet()) {
	                        		output += outputP + ": <input type=\"text\" name=\"" + outputP + "\" /><BR/>\n"; 
	                        	}
	                        	output +=
	                        		"<BR/>\n" +
	                        		"<input type=\"submit\" name=\"outcome\" value=\"Complete\"/>\n" +
	                        		"</form>\n" +
	                        		"</body>\n" +
	                        		"</html>";
	                        	IFile file = folder.getFile(fileName);
	                        	if (!file.exists()) {
	                                file.create(new ByteArrayInputStream(output.getBytes()), true, monitor);
	                            } else {
	                                file.setContents(new ByteArrayInputStream(output.getBytes()), true, false, monitor);
	                            }
                        	}
                        	String fileName = process.getId() + ".ftl";
                        	String output = 
                        		"<html>\n" +
                        		"<body>\n" +
                        		"<h2>" + process.getName() + "</h2>\n" +
                        		"<hr>\n" + 
                        		"<form action=\"complete\" method=\"POST\" enctype=\"multipart/form-data\">\n";
                        	for (Variable variable: process.getVariableScope().getVariables()) {
                        		if ("String".equals(variable.getType().getStringType())) {
                        			output += variable.getName() + ": <input type=\"text\" name=\"" + variable.getName() + "\" /><BR/>\n";
                        		}
                        	}
                        	output +=
                        		"<input type=\"submit\" value=\"Complete\"/>\n" +
                        		"</form>\n" +
                        		"</body>\n" +
                        		"</html>";
                        	IFile file = folder.getFile(fileName);
                        	if (!file.exists()) {
                                file.create(new ByteArrayInputStream(output.getBytes()), true, monitor);
                            } else {
                                file.setContents(new ByteArrayInputStream(output.getBytes()), true, false, monitor);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                try {
                    new ProgressMonitorDialog(targetPart.getSite().getShell()).run(false, true, op);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private static void processNodes(Node[] nodes, List<HumanTaskNode> result) {
    	for (Node node: nodes) {
    		if (node instanceof HumanTaskNode) {
    			result.add((HumanTaskNode) node);
    		} else if (node instanceof NodeContainer) {
    			processNodes(((NodeContainer) node).getNodes(), result);
    		}
    	}
    }
    
    private class TaskDef {
    
    	private String taskName;
    	private Map<String, String> inputParams = new HashMap<String, String>();
    	private Map<String, String> outputParams = new HashMap<String, String>();
    	
    	public TaskDef(String taskName) {
    		this.taskName = taskName;
    	}

		public String getTaskName() {
			return taskName;
		}

		public Map<String, String> getInputParams() {
			return inputParams;
		}

		public Map<String, String> getOutputParams() {
			return outputParams;
		}
    	
    }

}
