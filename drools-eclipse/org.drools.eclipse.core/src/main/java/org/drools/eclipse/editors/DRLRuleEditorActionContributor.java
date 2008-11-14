package org.drools.eclipse.editors;

/*
 * Copyright 2006 JBoss Inc
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

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * ActionContributors for DRLRuleEditor2
 * 
 * Currently implements contributors to zoom feature at rete viewer. 
 * 
 * @author Ahti Kitsik
 *
 */
public class DRLRuleEditorActionContributor extends MultiPageEditorActionBarContributor {

    private TextEditorActionContributor contributor = new TextEditorActionContributor();

    private ZoomComboContributionItem   zitem;
    private ZoomOutAction2              zoomOut;
    private ZoomInAction2               zoomIn;

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.IActionBars, org.eclipse.ui.IWorkbenchPage)
     */
    public void init(IActionBars bars,
                     IWorkbenchPage page) {
        contributor.init( bars );
        super.init( bars,
                    page );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorActionBarContributor#setActivePage(org.eclipse.ui.IEditorPart)
     */
    public void setActivePage(IEditorPart activeEditor) {
        IActionBars bars = getActionBars();
        if ( activeEditor instanceof ITextEditor ) {
            if ( bars != null ) {
                contributor.setActiveEditor( activeEditor );
            }
        }
    }

    /**
     * In addition to @link org.eclipse.ui.part.MultiPageEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
     * it sets contribution items to DRLRuleEditor2 for later use when
     * multipageditor tabs are switched.
     * 
     */
    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor( part );
        if ( part instanceof DRLRuleEditor2 ) {
            DRLRuleEditor2 p = (DRLRuleEditor2) part;
            p.setZoomComboContributionItem( zitem );
            p.setZoomInAction( zoomIn );
            p.setZoomOutAction( zoomOut );
        }
    }

    /**
     * Adds Zoom-related contributions.
     * 
     * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
     */
    public void contributeToToolBar(IToolBarManager toolBarManager) {
        super.contributeToToolBar( toolBarManager );
        toolBarManager.add( new Separator() );
        String[] zoomStrings = new String[]{ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH};

        zitem = new ZoomComboContributionItem( getPage(),
                                               zoomStrings );
        zitem.setZoomManager( null );
        zitem.setVisible( false );

        zoomIn = new ZoomInAction2();
        zoomIn.setEnabled( false );

        zoomOut = new ZoomOutAction2();
        zoomOut.setEnabled( false );

        toolBarManager.add( zitem );
        toolBarManager.add( zoomIn );
        toolBarManager.add( zoomOut );

    }

}
