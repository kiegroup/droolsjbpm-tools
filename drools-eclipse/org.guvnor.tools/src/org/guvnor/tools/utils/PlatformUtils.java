package org.guvnor.tools.utils;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.Activator;

public class PlatformUtils {
	public static void openEditor(String contents, String name) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStorage storage = new StringStorage(contents, name);
		IStorageEditorInput input = new StringInput(storage);
		IWorkbenchPage page = window.getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().
        							getEditorRegistry().getDefaultEditor(name);
		String editorId = desc != null?desc.getId():"org.eclipse.ui.DefaultTextEditor";
		try {
		if (page != null) {
			page.openEditor(input, editorId);
		}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
}
