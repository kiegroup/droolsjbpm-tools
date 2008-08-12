package org.drools.eclipse.flow.common.editor.action;
/*
 * Copyright 2005 JBoss Inc
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.flow.common.editor.GenericModelEditor;
import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.WorkflowProcess;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action for auto layouting a RuleFlow.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class VerticalAutoLayoutAction extends ActionDelegate implements IEditorActionDelegate {

    private IEditorPart editor;
    
    public void run(IAction action) {
        execute();
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
    }

    private void execute() {
        editor.doSave(null);
        Map<Long, Node> mapping = new HashMap<Long, Node>();
        DirectedGraph graph = createDirectedGraph(mapping);
        DirectedGraphLayout layout = new DirectedGraphLayout();
        layout.visit(graph);
        for (Map.Entry<Long, Node> entry: mapping.entrySet()) {
            Node node = entry.getValue();
            DefaultElementWrapper elementWrapper = (DefaultElementWrapper)
                ((ProcessWrapper) ((GenericModelEditor) editor).getModel()).getElement(entry.getKey() + "");
            elementWrapper.setConstraint(new Rectangle(node.x, node.y, node.width, node.height));
        }
        // TODO: implement changes as a command, so we can support undo
        editor.doSave(null);
    }
    
    protected DirectedGraph createDirectedGraph(Map<Long, Node> mapping) {
        DirectedGraph graph = new DirectedGraph();
        WorkflowProcess process = (WorkflowProcess) ((ProcessWrapper) ((GenericModelEditor) editor).getModel()).getProcess();
        for (org.drools.workflow.core.Node processNode: process.getNodes()) {
            Node node = new Node();
            Integer width = (Integer) processNode.getMetaData("width");
            Integer height = (Integer) processNode.getMetaData("height");
            if (width == null || width <= 0) {
                width = 80;
            }
            if (height == null || height <= 0) {
                height = 40;
            }
            node.setSize(new Dimension(width, height));
            graph.nodes.add(node);
            mapping.put(processNode.getId(), node);
        }
        for (org.drools.workflow.core.Node processNode: process.getNodes()) {
            for (List<Connection> connections: processNode.getIncomingConnections().values()) {
                for (Connection connection: connections) {
                    Node source = mapping.get(connection.getFrom().getId());
                    Node target = mapping.get(connection.getTo().getId());
                    graph.edges.add(new Edge(source, target));
                }
            }
        }
        return graph;
    }

}
