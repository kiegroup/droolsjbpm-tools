package org.drools.ide.editors;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * The Drools editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsEditor extends TextEditor {

	private ColorManager colorManager;

	public DroolsEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new DroolsConfiguration(colorManager));
		setDocumentProvider(new DroolsDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
