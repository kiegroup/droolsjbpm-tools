package org.drools.eclipse.launching;

import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;

public class DroolsApplicationLaunchShortcut extends JavaApplicationLaunchShortcut {

	protected ILaunchConfigurationType getConfigurationType() {
		return DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(IDroolsDebugConstants.LAUNCH_CONFIGURATION_TYPE);		
	}

	protected String getTypeSelectionTitle() {
		return "Select Drools Application";
	}

}
