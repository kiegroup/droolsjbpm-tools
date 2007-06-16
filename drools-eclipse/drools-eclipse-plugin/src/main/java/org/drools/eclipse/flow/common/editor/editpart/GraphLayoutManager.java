package org.drools.eclipse.flow.common.editor.editpart;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;

public class GraphLayoutManager extends AbstractLayout {

	private ProcessEditPart diagram;

	public GraphLayoutManager(ProcessEditPart diagram) {
		this.diagram = diagram;
	}
	
	protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {		
		container.validate();
		return container.getSize();
	}

	
	public void layout(IFigure container) {
		new DirectedGraphLayoutVisitor().layoutDiagram(diagram);
		// diagram.setTableModelBounds();
	}
	
}