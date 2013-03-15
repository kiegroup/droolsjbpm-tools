package org.jbpm.eclipse.action;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.kie.definition.process.Connection;
import org.kie.definition.process.Node;
import org.kie.definition.process.NodeContainer;
import org.kie.definition.process.Process;
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
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
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
                        		"import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;\n" +
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
                        	Map<String, String> ongoingCases = new HashMap<String, String>();
                        	boolean done = processNodes("", process.getStart(), "", cases, ongoingCases);
                        	if (!done) {
                        		if (ongoingCases.size() == 1) {
                        			cases.put("Implicit", ongoingCases.values().iterator().next());
                        		} else {
                        			throw new IllegalArgumentException("Could not create implicit case: " + ongoingCases.size());
                        		}
                        	}
                        	for (Map.Entry<String, String> entry: cases.entrySet()) {
	                        	output +=
	                        		"	@Test\n" +
	                        		"    public void test" + entry.getKey() + "() {\n" +
	                        		"        StatefulKnowledgeSession ksession = createKnowledgeSession(\"" + file.getName() + "\");\n";
	                        	Set<String> serviceTasks = new HashSet<String>();
	                        	containsServiceTasks(serviceTasks, process);
	                        	for (String service: serviceTasks) {
	                        		output += "        ksession.getWorkItemManager().registerWorkItemHandler(\"" + service + "\", new SystemOutWorkItemHandler());\n";
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
                            MessageDialog.openError(null, "Error", e.getMessage());
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
    
    private static boolean processNodes(String name, Node currentNode, String testCode, Map<String, String> cases, Map<String, String> ongoingCases) {
    	if (currentNode instanceof Split) {
    		Split split = (Split) currentNode;
    		switch (split.getType()) {
    			case Split.TYPE_AND:
    				boolean done = false;
    				String startTestCode = testCode;
    				int counter = 1;
    				for (Connection c: split.getDefaultOutgoingConnections()) {
    					if (processNodes(name + counter++, c.getTo(), startTestCode, cases, ongoingCases)) {
    						done = true;
    					}
    				}
    				if (!done) {
        				String implicitCompleteTestCode = startTestCode;
    					for (String ongoingCase: ongoingCases.values()) {
							implicitCompleteTestCode += ongoingCase.substring(startTestCode.length(), ongoingCase.length());
    					}
    					ongoingCases.clear();
    					ongoingCases.put(name, implicitCompleteTestCode);
    				}
    				return done;
    			case Split.TYPE_XOR:
    			case Split.TYPE_OR:
    				int i = 1;
    				done = true;
    				for (Connection c: split.getDefaultOutgoingConnections()) {
    					String newTestCode = testCode +
    						"        // please make sure that the following constraint is selected to node " + c.getTo().getName() + ":\n" +
							"        // " + split.getConstraint(c).getConstraint() + "\n";
    					if (!processNodes(name + "Constraint" + i++, c.getTo(), newTestCode, cases, ongoingCases)) {
    						done = false;
    					}
    				}
    				return done;
    			default:
    				throw new IllegalArgumentException("Unknown split type " + split.getType());
    		}
    	} else if (currentNode instanceof EndNode) {
    		EndNode endNode = (EndNode) currentNode;
    		if (endNode.isTerminate()) {
    			cases.put(name, testCode);
    			return true;
    		} else {
    			ongoingCases.put(name, testCode);
    			return false;
    		}
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
    			"        String actorId = \"" + actorId + "\";\n" +
    			"        List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner(actorId, new ArrayList<String>(), \"en-UK\");\n" +
    			"        TaskSummary task = list.get(0);\n";
    		if (actorIds.length > 1 || groupIds.length > 0) {
    			testCode +=
        			"        taskService.claim(task.getId(), actorId, new ArrayList<String>());\n";
    		}
    		testCode +=
    			"        taskService.start(task.getId(), actorId);\n" +
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
    			"        taskService.completeWithResults(task.getId(), actorId, results);\n" +
    			"\n";
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof WorkItemNode) {
    		WorkItemNode taskNode = (WorkItemNode) currentNode;
    		testCode += 
    			"        // if necessary, complete request for service task \"" + taskNode.getWork().getName() + "\"\n";
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof EventNode) {
    		EventNode eventNode = (EventNode) currentNode;
    		testCode += 
    			"        ksession.signalEvent(\"" + eventNode.getType() + "\", null, processInstance.getId());\n";
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof TimerNode) {
    		testCode += 
    			"        // wait for timer to expire\n" +
    		    "        // for example, try { Thread.sleep(delay); } catch (Exception e) { /* Do nothing */ }";
    		// TODO simulation time
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof ForEachNode) {
    		CompositeNode compositeNode = ((ForEachNode) currentNode).getCompositeNode();
    		testCode += 
    			"        // --> triggering each element in the collection:\n";
    		boolean done = true;
        	for (Node node: compositeNode.getNodes()) {
	    		if (node instanceof StartNode) {
	    			StartNode startNode = (StartNode) node;
	    			if (startNode.getTriggers() == null || startNode.getTriggers().isEmpty()) {
	    				done = processNodes(name, startNode.getTo().getTo(), testCode, cases, ongoingCases);
	    				break;
	    			}
	    		}
        	}
        	if (done) {
        		for (Map.Entry<String, String> c: cases.entrySet()) {
        			if (c.getKey().startsWith(name)) {
        				cases.put(c.getKey(), c.getValue() +
    	        			"        // <-- do this for one element in the collection:\n");
        			}
        		}
        		return true;
        	} else {
        		if (ongoingCases.size() == 1) {
        			testCode = ongoingCases.values().iterator().next() +
    					"        // <-- do this for each element in the collection:\n";
        			return processNodes(name + "Implicit", ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
        		} else {
        			throw new IllegalArgumentException("Could not create implicit case: " + ongoingCases.size());
        		}
        	}
    	} else if (currentNode instanceof CompositeNode) {
    		CompositeNode compositeNode = (CompositeNode) currentNode;
    		boolean done = true;
        	for (Node node: compositeNode.getNodes()) {
	    		if (node instanceof StartNode) {
	    			StartNode startNode = (StartNode) node;
	    			if (startNode.getTriggers() == null || startNode.getTriggers().isEmpty()) {
	    				done = processNodes(name, startNode.getTo().getTo(), testCode, cases, ongoingCases);
	    				break;
	    			}
	    		}
        	}
        	if (done) {
        		return true;
        	} else {
        		if (ongoingCases.size() == 1) {
        			return processNodes(name + "Implicit", ((NodeImpl) currentNode).getTo().getTo(), ongoingCases.values().iterator().next(), cases, ongoingCases);
        		} else {
        			throw new IllegalArgumentException("Could not create implicit case: " + ongoingCases.size());
        		}
        	}
    	} else if (currentNode instanceof RuleSetNode) {
    		testCode += 
    			"        ksession.fireAllRules();\n";
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof SubProcessNode) {
    		SubProcessNode subProcessNode = (SubProcessNode) currentNode;
    		if (subProcessNode.isWaitForCompletion()) {
	    		testCode += 
        			"        // invoking subprocess " + subProcessNode.getProcessId() + ", if necessary make sure it is completed\n";
    		}
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else if (currentNode instanceof FaultNode) {
    		FaultNode faultNode = (FaultNode) currentNode;
    		testCode += 
    			"        // handle fault " + faultNode.getFaultName() + " if necessary\n";
    		return true;
    	} else if (currentNode instanceof Join) {
    		// TODO looping
    		Join join = (Join) currentNode;
    		switch (join.getType()) {
				case Join.TYPE_AND:
					// TODO: cannot just call processNodes as this will add it to the cases if an end is reached, 
					// while it should also still include the work that is necessary to complete the other branches					
		    		// processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
		    		// TODO: this isn't 100% correct, as a join should only wait for the other incoming connections,
		    		// not all other non-terminating connections, but not doing this would make test ignore the other
		    		// branches of the divering parallel gateway
		    		// return false;
					throw new IllegalArgumentException("Generation of tests that include a convering parallel gateway is not yet supported");
				case Join.TYPE_XOR:
		    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
				default:
					throw new IllegalArgumentException("Unknown join type " + join.getType());
			}
    	} else if (currentNode instanceof NodeImpl) {
    		return processNodes(name, ((NodeImpl) currentNode).getTo().getTo(), testCode, cases, ongoingCases);
    	} else {
    		throw new IllegalArgumentException("Unknown node " + currentNode);
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
    			containsServiceTasks(result, (NodeContainer) node);
    		}
    	}
    }
}
