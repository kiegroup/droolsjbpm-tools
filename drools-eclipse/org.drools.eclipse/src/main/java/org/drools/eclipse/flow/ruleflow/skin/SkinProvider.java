package org.drools.eclipse.flow.ruleflow.skin;

import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.SplitEditPart.SplitFigureInterface;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart.WorkItemFigureInterface;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;

public interface SkinProvider {
	
	PaletteContainer createComponentsDrawer();
	
	PaletteEntry createConnectionEntry();
	
	IFigure createActionNodeFigure();
	
	IFigure createEndNodeFigure();
	
	IFigure createEventNodeFigure();

	IFigure createFaultNodeFigure();

	JoinFigureInterface createJoinFigure();

	SplitFigureInterface createSplitFigure();

	IFigure createStartNodeFigure();

	IFigure createTimerNodeFigure();

	IFigure createMilestoneFigure();

	IFigure createRuleSetNodeFigure();

	IFigure createSubFlowFigure();

	IFigure createStateFigure();

	WorkItemFigureInterface createWorkItemFigure();

	IFigure createCompositeNodeFigure();

	IFigure createForEachNodeFigure();
	
	String getWorkItemsName();

}
