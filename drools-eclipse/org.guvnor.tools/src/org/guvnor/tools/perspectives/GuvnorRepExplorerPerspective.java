package org.guvnor.tools.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.guvnor.tools.views.IGuvnorConstants;

public class GuvnorRepExplorerPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout top =
			layout.createFolder("top", IPageLayout.LEFT, 0.40f, editorArea);	//$NON-NLS-1$
		top.addView(IGuvnorConstants.REPVIEW_ID);
		IFolderLayout botLeft = 
			layout.createFolder("botleft", IPageLayout.BOTTOM, 0.70f, "top");
//		botLeft.addView(IPageLayout.ID_OUTLINE);
		botLeft.addView(IPageLayout.ID_PROP_SHEET);
		layout.addView(IGuvnorConstants.RESHISTORYVIEW_ID, IPageLayout.BOTTOM, 0.70f, editorArea);
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
//		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
//		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IGuvnorConstants.RESHISTORYVIEW_ID);
		layout.addShowViewShortcut(IGuvnorConstants.REPVIEW_ID);
		
		// Add  "perspective short cut"
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); //$NON-NLS-1$
		layout.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective"); //$NON-NLS-1$
	}
}
