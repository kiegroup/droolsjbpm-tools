package org.drools.eclipse.flow.common.editor.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.flow.common.editor.editpart.figure.ElementFigure;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;

public class DirectedGraphLayoutVisitor {

	private Map partToNodesMap;
	private DirectedGraph graph;

	public void layoutDiagram(ProcessEditPart diagram) {
		partToNodesMap = new HashMap();
		graph = new DirectedGraph();
		addNodes(diagram);
		if (graph.nodes.size() > 0) {	
			addEdges(diagram);
			new DirectedGraphLayout().visit(graph);
			applyResults(diagram);
		}
	}

	protected void addNodes(ProcessEditPart diagram) {
		for (int i = 0; i < diagram.getChildren().size(); i++) {
			ElementEditPart elementEditPart = (ElementEditPart) diagram.getChildren().get(i);
			addNodes(elementEditPart);
		}
	}

	protected void addNodes(ElementEditPart elementEditPart) {
		Node n = new Node(elementEditPart);
		n.width = elementEditPart.getFigure().getPreferredSize(400, 300).width;
		n.height = elementEditPart.getFigure().getPreferredSize(400, 300).height;
		n.setPadding(new Insets(10, 8, 10, 12));
		partToNodesMap.put(elementEditPart, n);
		graph.nodes.add(n);
	}

	protected void addEdges(ProcessEditPart diagram) {
		for (int i = 0; i < diagram.getChildren().size(); i++) {
			ElementEditPart elementEditPart = (ElementEditPart) diagram.getChildren().get(i);
			addEdges(elementEditPart);
		}
	}

	protected void addEdges(ElementEditPart elementEditPart) {
		List outgoing = elementEditPart.getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			ElementConnectionEditPart connectionPart = (ElementConnectionEditPart) elementEditPart.getSourceConnections().get(i);
			addEdges(connectionPart);
		}
	}

	protected void addEdges(ElementConnectionEditPart connectionPart) {
		Node source = (Node) partToNodesMap.get(connectionPart.getSource());
		Node target = (Node) partToNodesMap.get(connectionPart.getTarget());
		Edge e = new Edge(connectionPart, source, target);
		e.weight = 2;
		graph.edges.add(e);
		partToNodesMap.put(connectionPart, e);
	}

	protected void applyResults(ProcessEditPart diagram) {
		applyChildrenResults(diagram);
	}

	protected void applyChildrenResults(ProcessEditPart diagram) {
		for (int i = 0; i < diagram.getChildren().size(); i++) {
			ElementEditPart elementEditPart = (ElementEditPart) diagram.getChildren().get(i);
			applyResults(elementEditPart);
		}
	}

	protected void applyOwnResults(ProcessEditPart diagram) {
	}

	public void applyResults(ElementEditPart elementEditPart) {
		Node n = (Node) partToNodesMap.get(elementEditPart);
		ElementFigure elementFigure = (ElementFigure) elementEditPart.getFigure();
		Rectangle bounds = new Rectangle(n.x, n.y, elementFigure.getPreferredSize().width,
				elementFigure.getPreferredSize().height);
		elementFigure.setBounds(bounds);
		for (int i = 0; i < elementEditPart.getSourceConnections().size(); i++) {
			ElementConnectionEditPart connectionPart = (ElementConnectionEditPart) elementEditPart.getSourceConnections().get(i);
			applyResults(connectionPart);
		}
	}

	protected void applyResults(ElementConnectionEditPart connectionPart)	{
		Edge e = (Edge) partToNodesMap.get(connectionPart);
		NodeList nodes = e.vNodes;
		PolylineConnection conn = (PolylineConnection) connectionPart.getConnectionFigure();
		conn.setTargetDecoration(new PolygonDecoration());
		if (nodes != null) {
			List bends = new ArrayList();
			for (int i = 0; i < nodes.size(); i++) {
				Node vn = nodes.getNode(i);
				int x = vn.x;
				int y = vn.y;
				if (e.isFeedback()) {
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
					bends.add(new AbsoluteBendpoint(x, y));
				} else {
					bends.add(new AbsoluteBendpoint(x, y));
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
				}
			}
			conn.setRoutingConstraint(bends);
		} else {
			conn.setRoutingConstraint(Collections.EMPTY_LIST);
		}
	}

}
