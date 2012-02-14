package org.jbpm.eclipse.action;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.drools.definition.process.NodeContainer;
import org.drools.definition.process.Process;
import org.drools.xml.SemanticModules;
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
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.WorkItemNode;

public class GenerateBPMN2JUnitTests implements IObjectActionDelegate {

    private IFile file;
    private IWorkbenchPart targetPart;
    
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {
        if (file != null && file.exists()) {
            try {
                generateJUnitTests();
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

    public void generateJUnitTests() {
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
            	String packageName = process.getPackageName();
            	if (packageName == null || packageName.trim().length() == 0 || "org.drools.bpmn2".equals(packageName)) {
            		packageName = "org.jbpm";
            	}
            	final String pName = packageName;
                WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
                    public void execute(final IProgressMonitor monitor)
                            throws CoreException {
                        try {
                            IFolder folder = file.getProject().getFolder("src/main/java");
                            IPackageFragmentRoot packageFragmentRoot = javaProject
                                    .getPackageFragmentRoot(folder);
                            IPackageFragment packageFragment = packageFragmentRoot
                                    .createPackageFragment(pName, true, monitor);
                        	String processName = process.getName();
                        	processName = processName.replaceAll("\\s", "_");
                        	if (processName == null || processName.trim().length() == 0) {
                        		processName = "Process";
                        	}
                        	String fileName = processName + "JUnitTest";
                        	String output = 
                        		"package " + pName + ";\n" +
                				"\n" +
                				"import java.util.ArrayList;\n" +
                				"import java.util.HashMap;\n" +
                				"import java.util.List;\n" +
                				"import java.util.Map;\n" +
                        		"\n" +
                        		"import org.drools.runtime.StatefulKnowledgeSession;\n" +
                        		"import org.drools.runtime.process.ProcessInstance;\n" +
                        		"import org.jbpm.task.TaskService;\n" +
                        		"import org.jbpm.task.query.TaskSummary;\n" +
                        		"import org.jbpm.test.JbpmJUnitTestCase;\n" +
                        		"import org.junit.Test;\n" +
                        		"\n" +
                        		"public class " + fileName + " extends JbpmJUnitTestCase {\n" +
                        		"\n";
                        	boolean containsHumanTasks = containsHumanTasks(process);
                        	if (containsHumanTasks) {
                        		output +=
                        			"    public " + fileName + "() {\n" +
                					"        super(true);\n" +
                					"    }\n" +
                					"\n";
                        	}
                        	Map<String, String> cases = new HashMap<String, String>();
                        	processNodes("", process.getStart(), "", cases);
                        	for (Map.Entry<String, String> entry: cases.entrySet()) {
	                        	output +=
	                        		"	@Test\n" +
	                        		"    public void test" + entry.getKey() + "() {\n" +
	                        		"        StatefulKnowledgeSession ksession = createKnowledgeSession(\"" + file.getName() + "\");\n";
	                        	Set<String> serviceTasks = new HashSet<String>();
	                        	containsServiceTasks(serviceTasks, process);
	                        	for (String service: serviceTasks) {
	                        		output += "        ksession.getWorkItemManager().registerWorkItemHandler(\"" + service + "\", handler);\n";
	                        	}
	                        	if (containsHumanTasks) {
	                        		output += "        TaskService taskService = getTaskService(ksession);\n";
	                        	}
	                        	List<Variable> variables = process.getVariableScope().getVariables();
	                        	if (variables != null && variables.size() > 0) {
		                        	output +=
	                    				"        Map<String, Object> params = new HashMap<String, Object>();\n" +
	                    				"        // initialize variables here if necessary\n";
		                        	for (Variable v: variables) {
		                        		output +=
		                        			"        // params.put(\"" + v.getName() + "\", value); // type " + v.getType().getStringType() + "\n";
		                        	}
		                        	output +=
		                				"        ProcessInstance processInstance = ksession.startProcess(\"" + process.getId() + "\", params);\n";
	                        	} else {
	                        		output +=
		                				"        ProcessInstance processInstance = ksession.startProcess(\"" + process.getId() + "\");\n";
	                        	}
	                        	output +=
	                				entry.getValue() +
	                				"        // do your checks here\n" +
	        						"        // for example, assertProcessInstanceCompleted(processInstance.getId(), ksession);\n" +
	        						"    }\n" +
	        						"\n";
                        	}
                        	output +="}";
                            packageFragment.createCompilationUnit(fileName + ".java", output, true, monitor);
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
    
    private static void processNodes(String name, Node currentNode, String testCode, Map<String, String> cases) {
    	if (currentNode instanceof Split) {
    		Split split = (Split) currentNode;
    		switch (split.getType()) {
    			case Split.TYPE_AND:
    				for (Connection c: split.getDefaultOutgoingConnections()) {
    					processNodes(name, c.getTo(), testCode, cases);
    				}
    				return;
    			case Split.TYPE_XOR:
    			case Split.TYPE_OR:
    				int i = 1;
    				for (Connection c: split.getDefaultOutgoingConnections()) {
    					String newTestCode = testCode +
    						"        // please make sure that the following constraint is selected to node " + c.getTo().getName() + ":\n" +
							"        // " + split.getConstraint(c).getConstraint() + "\n";
    					processNodes(name + "Constraint" + i++, c.getTo(), newTestCode, cases);
    				}
    				return;
    		}
    	} else if (currentNode instanceof EndNode) {
    		cases.put(name, testCode);
    	} else if (currentNode instanceof HumanTaskNode) {
    		HumanTaskNode taskNode = (HumanTaskNode) currentNode;
    		String actorId = (String) taskNode.getWork().getParameter("ActorId");
    		if (actorId == null) {
    			actorId = "";
    		}
    		String groupId = (String) taskNode.getWork().getParameter("GroupId");
    		if (groupId == null) {
    			groupId = "";
    		}
    		String[] actorIds = new String[0];
    		if (actorId.trim().length() > 0) {
    			actorIds = actorId.split(","); 
    		}
    		String[] groupIds = new String[0];
    		if (groupId.trim().length() > 0) {
    			groupIds = groupId.split(",");
    		}
    		actorId = actorIds.length > 0 ? actorIds[0] : "";
    		testCode += 
    			"        // execute task\n" +
    			"        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(\"" + actorId + "\", new ArrayList<String>(), \"en-UK\");\n" +
    			"        TaskSummary task = list.get(0);\n";
    		if (actorIds.length > 1 || groupIds.length > 0) {
    			testCode +=
        			"        taskService.claim(task.getId(), \"" + actorId + "\", new ArrayList<String>());\n";
    		}
    		testCode +=
    			"        taskService.start(task.getId(), \"" + actorId + "\");\n" +
				"        Map<String, Object> results = new HashMap<String, Object>();\n" +
				"        // add results here\n";
    		for (Map.Entry<String, String> entry: taskNode.getOutMappings().entrySet()) {
    			String type = null;
    			VariableScope variableScope = (VariableScope) taskNode.resolveContext(VariableScope.VARIABLE_SCOPE, entry.getValue());
    			if (variableScope != null) {
    				type = variableScope.findVariable(entry.getValue()).getType().getStringType();
    			}
    			testCode +=
    				"        // results.put(\"" + entry.getKey() + "\", value);" + (type == null ? "" : " // type " + type) + "\n";
    		}
    		testCode +=
    			"        taskService.completeWithResults(task.getId(), \"" + actorId + "\", results);\n" +
    			"\n";
    		processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases);
    	} else if (currentNode instanceof WorkItemNode) {
    		WorkItemNode taskNode = (WorkItemNode) currentNode;
    		testCode += 
    			"        // if necessary, complete request for service task \"" + taskNode.getWork().getName() + "\"\n";
    		processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases);
    	} else if (currentNode instanceof NodeImpl) {
    		processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases);
    	}
    }

    private static boolean containsHumanTasks(NodeContainer c) {
    	for (Node node: c.getNodes()) {
    		if (node instanceof HumanTaskNode) {
    			return true;
    		}
    		if (node instanceof NodeContainer) {
    			if (containsHumanTasks((NodeContainer) node)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    private void containsServiceTasks(Set<String> result, NodeContainer c) {
    	for (Node node: c.getNodes()) {
    		if (node instanceof WorkItemNode && !(node instanceof HumanTaskNode)) {
    			result.add(((WorkItemNode) node).getWork().getName());
    		}
    		if (node instanceof NodeContainer) {
    			containsServiceTasks(result, (NodeContainer) c);
    		}
    	}
    }
}
