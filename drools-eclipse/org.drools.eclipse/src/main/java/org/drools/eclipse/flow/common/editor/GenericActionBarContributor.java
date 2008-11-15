package org.drools.eclipse.flow.common.editor;

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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

/**
 * Common implementation of a ActionBarContributor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class GenericActionBarContributor extends ActionBarContributor {

    protected void buildActions() {
        addRetargetAction( new UndoRetargetAction() );
        addRetargetAction( new RedoRetargetAction() );
        addRetargetAction( new DeleteRetargetAction() );

    	addRetargetAction( new ZoomInRetargetAction() );
    	addRetargetAction( new ZoomOutRetargetAction() );
    	
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.LEFT ) );
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.CENTER ) );
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.RIGHT ) );
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.TOP ) );
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.MIDDLE ) );
        addRetargetAction( new AlignmentRetargetAction( PositionConstants.BOTTOM ) );
        
    	addRetargetAction( new RetargetAction(
			GEFActionConstants.TOGGLE_GRID_VISIBILITY, "Grid" ));
    }

    public void contributeToToolBar(IToolBarManager toolBarManager) {
        toolBarManager.add( getAction( ActionFactory.UNDO.getId() ) );
        toolBarManager.add( getAction( ActionFactory.REDO.getId() ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( new ZoomComboContributionItem( getPage() ) );
        toolBarManager.add( new Separator() );                              
        
        DropDownMenuWithDefaultAction alignMenu = new DropDownMenuWithDefaultAction( getActionRegistry().getAction( GEFActionConstants.ALIGN_LEFT ) );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_LEFT ) );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_CENTER ) );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_RIGHT ) );
        alignMenu.add( new Separator() );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_TOP ) );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_MIDDLE ) );
        alignMenu.add( getActionRegistry().getAction( GEFActionConstants.ALIGN_BOTTOM ) );
        toolBarManager.add( alignMenu );
        
        toolBarManager.add( new Separator() );                              
        toolBarManager.add( getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY) );
    }
    
    protected void declareGlobalActionKeys() {
    }
}
