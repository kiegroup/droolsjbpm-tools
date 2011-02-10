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

package org.guvnor.tools.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.guvnor.tools.views.IGuvnorConstants;
/**
 * Definition of the Guvnor repository perspective.
 * @author jgraham
 */
public class GuvnorRepExplorerPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout top =
			layout.createFolder("top", IPageLayout.LEFT, 0.30f, editorArea); //$NON-NLS-1$
		top.addView(IGuvnorConstants.REPVIEW_ID);
		IFolderLayout botLeft = 
			layout.createFolder("botleft", IPageLayout.BOTTOM, 0.70f, "top"); //$NON-NLS-1$ //$NON-NLS-2$
		botLeft.addView(IPageLayout.ID_PROP_SHEET);
		layout.addView(IGuvnorConstants.RESHISTORYVIEW_ID, IPageLayout.BOTTOM, 0.70f, editorArea);
		IFolderLayout right =
			layout.createFolder("right", IPageLayout.RIGHT, 0.70f, editorArea); //$NON-NLS-1$
		right.addView(IPageLayout.ID_RES_NAV);
		layout.setEditorAreaVisible(true);
		
		addActions(layout);
	}
	
	private void addActions(IPageLayout layout) {
		// Add "new wizards". They will be present in File/New menu
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.project"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); //$NON-NLS-1$

		// Add "show views". They will be present in "show view" menu
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IGuvnorConstants.RESHISTORYVIEW_ID);
		layout.addShowViewShortcut(IGuvnorConstants.REPVIEW_ID);
		
		// Add  "perspective short cut"
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); //$NON-NLS-1$
		layout.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective"); //$NON-NLS-1$
	}
}
