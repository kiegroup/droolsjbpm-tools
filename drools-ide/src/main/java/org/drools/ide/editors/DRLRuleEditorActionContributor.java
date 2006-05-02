package org.drools.ide.editors;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;

public class DRLRuleEditorActionContributor extends MultiPageEditorActionBarContributor {

	private TextEditorActionContributor contributor = new TextEditorActionContributor();
	
	public void init(IActionBars bars, IWorkbenchPage page) {
		contributor.init(bars);
		super.init(bars, page);
	}
		
	public void setActivePage(IEditorPart activeEditor) {
		IActionBars bars = getActionBars();
		if (activeEditor instanceof ITextEditor) {
			if (bars != null) {
				contributor.setActiveEditor(activeEditor);
			}
		}
	}

}
