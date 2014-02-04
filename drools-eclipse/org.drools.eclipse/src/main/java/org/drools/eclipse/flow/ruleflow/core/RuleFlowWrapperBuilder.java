/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.drools.eclipse.flow.common.editor.core.ElementContainer;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapperBuilder;
import org.eclipse.jdt.core.IJavaProject;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.CompositeNode;

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
            NodeWrapper nodeWrapper = NodeWrapperFactory.INSTANCE.getNodeWrapper(node, project);
            nodeWrapper.setNode((org.jbpm.workflow.core.Node) node);
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
            connectionWrapper.localSetConnection((org.jbpm.workflow.core.Connection) connection);
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

}
