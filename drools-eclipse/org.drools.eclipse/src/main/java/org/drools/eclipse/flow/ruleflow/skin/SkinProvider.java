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
