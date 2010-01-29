package org.drools.eclipse.flow.ruleflow.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.definition.process.Connection;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.eclipse.WorkItemDefinitions;
import org.drools.eclipse.flow.common.editor.core.ElementContainer;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapperBuilder;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.impl.WorkDefinitionImpl;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.workflow.core.node.ActionNode;
import org.drools.workflow.core.node.CompositeContextNode;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.DynamicNode;
import org.drools.workflow.core.node.EndNode;
import org.drools.workflow.core.node.EventNode;
import org.drools.workflow.core.node.FaultNode;
import org.drools.workflow.core.node.ForEachNode;
import org.drools.workflow.core.node.HumanTaskNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.MilestoneNode;
import org.drools.workflow.core.node.RuleSetNode;
import org.drools.workflow.core.node.Split;
import org.drools.workflow.core.node.StartNode;
import org.drools.workflow.core.node.StateNode;
import org.drools.workflow.core.node.SubProcessNode;
import org.drools.workflow.core.node.TimerNode;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.jdt.core.IJavaProject;

public class RuleFlowWrapperBuilder implements ProcessWrapperBuilder {
    
    public ProcessWrapper getProcessWrapper(Process process, IJavaProject project) {
        if (process instanceof RuleFlowProcess) {
            RuleFlowProcess ruleFlowProcess = (RuleFlowProcess) process;
            RuleFlowProcessWrapper processWrapper = new RuleFlowProcessWrapper();
            processWrapper.localSetProcess(process);
            Set<Node> nodes = new HashSet<Node>();
            nodes.addAll(Arrays.asList(ruleFlowProcess.getNodes()));
            Set<Connection> connections = new HashSet<Connection>();
            processNodes(nodes, connections, processWrapper, project);
            return processWrapper;
        }
        return null;
    }
    
    public static void processNodes(Set<Node> nodes, Set<Connection> connections, ElementContainer container, IJavaProject project) {
        Map<Node, NodeWrapper> nodeWrappers = new HashMap<Node, NodeWrapper>();
        for (Node node: nodes) {
            NodeWrapper nodeWrapper = getNodeWrapper(node, project);
            nodeWrapper.setNode((org.drools.workflow.core.Node) node);
            nodeWrapper.setParent(container);
            container.localAddElement(nodeWrapper);
            nodeWrappers.put(node, nodeWrapper);
            for (List<Connection> inConnections: node.getIncomingConnections().values()) {
                for (Connection connection: inConnections) {
                    connections.add(connection);
                }
            }
            for (List<Connection> outConnections: node.getOutgoingConnections().values()) {
                for (Connection connection: outConnections) {
                    connections.add(connection);
                }
            }
            if (node instanceof CompositeNode) {
            	Set<Node> subNodes = new HashSet<Node>();
            	for (Node subNode: ((CompositeNode) node).getNodes()) {
            		subNodes.add(subNode);
            	}
            	if (subNodes.size() > 0) {
            		processNodes(subNodes, new HashSet<Connection>(), (CompositeNodeWrapper) nodeWrapper, project);
            	}
            }
        }
        for (Connection connection: connections) {
            ConnectionWrapper connectionWrapper = new ConnectionWrapper();
            connectionWrapper.localSetConnection((org.drools.workflow.core.Connection) connection);
            connectionWrapper.localSetBendpoints(null);
            NodeWrapper from = nodeWrappers.get(connection.getFrom());
            NodeWrapper to = nodeWrappers.get(connection.getTo());
            if (from != null && to != null) {
	            connectionWrapper.localSetSource(from);
	            from.localAddOutgoingConnection(connectionWrapper);
	            connectionWrapper.localSetTarget(to);
	            to.localAddIncomingConnection(connectionWrapper);
            }
        }
    }
    
    private static NodeWrapper getNodeWrapper(Node node, IJavaProject project) {
        if (node instanceof StartNode) {
            return new StartNodeWrapper();
        } else if (node instanceof EndNode) {
            return new EndNodeWrapper();
        } else if (node instanceof ActionNode) {
            return new ActionWrapper();
        } else if (node instanceof RuleSetNode) {
            return new RuleSetNodeWrapper();
        } else if (node instanceof SubProcessNode) {
            return new SubProcessWrapper();
        } else if (node instanceof ForEachNode) {
            return new ForEachNodeWrapper();
        } else if (node instanceof DynamicNode) {
            return new DynamicNodeWrapper();
        } else if (node instanceof StateNode) {
        	return new StateNodeWrapper();
        } else if (node instanceof CompositeContextNode) {
            return new CompositeContextNodeWrapper();
        } else if (node instanceof Join) {
            return new JoinWrapper();
        } else if (node instanceof Split) {
            return new SplitWrapper();
        } else if (node instanceof MilestoneNode) {
            return new MilestoneWrapper();
        } else if (node instanceof FaultNode) {
            return new FaultNodeWrapper();
        } else if (node instanceof TimerNode) {
            return new TimerWrapper();
        } else if (node instanceof HumanTaskNode) {
            return new HumanTaskNodeWrapper();
        } else if (node instanceof WorkItemNode) {
            WorkItemWrapper workItemWrapper = new WorkItemWrapper();
            Work work = ((WorkItemNode) node).getWork();
            if (work != null && work.getName() != null) {
                try {
                	WorkDefinition workDefinition = 
	                    WorkItemDefinitions.getWorkDefinitions(project)
	                        .get(work.getName());
	                if (workDefinition == null) {
	//                	DroolsEclipsePlugin.log(
	//                        new IllegalArgumentException("Could not find work definition for work " + work.getName()));
	                    workDefinition = new WorkDefinitionImpl();
	                    ((WorkDefinitionImpl) workDefinition).setName(work.getName());
	                }
	                workItemWrapper.setWorkDefinition(workDefinition);
                } catch (Throwable t) {
            		// an error might be thrown when parsing the work definitions,
                	// but this should already be displayed to the user
                }
            }
            return workItemWrapper;
        } else if (node instanceof EventNode) {
            return new EventNodeWrapper();
        }
        throw new IllegalArgumentException(
            "Could not find node wrapper for node " + node);
    }

}
