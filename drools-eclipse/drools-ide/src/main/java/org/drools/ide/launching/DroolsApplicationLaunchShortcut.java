package org.drools.ide.launching;

import org.drools.ide.debug.core.IDroolsDebugConstants;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaApplicationLaunchShortcut;

public class DroolsApplicationLaunchShortcut extends JavaApplicationLaunchShortcut {

	protected ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IDroolsDebugConstants.LAUNCH_CONFIGURATION_TYPE);		
	}

	protected String getTypeSelectionTitle() {
		return "Select JBoss Rules Application";
	}

}
