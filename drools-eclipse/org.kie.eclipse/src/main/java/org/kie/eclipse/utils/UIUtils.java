package org.kie.eclipse.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class UIUtils {

	private UIUtils() {
	}

	public static IWizard createWizard(String id) {
		// First see if this is a "new wizard".
		IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(id);
		// If not check if it is an "import wizard".
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getImportWizardRegistry().findWizard(id);
		}
		// Or maybe an export wizard
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getExportWizardRegistry().findWizard(id);
		}
		try {
			// Then if we have a wizard, open it.
			if (descriptor != null) {
				IWizard wizard = descriptor.createWizard();
				return wizard;
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
