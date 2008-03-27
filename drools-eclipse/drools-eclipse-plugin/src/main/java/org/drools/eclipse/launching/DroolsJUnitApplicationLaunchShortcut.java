package org.drools.eclipse.launching;

import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;

public class DroolsJUnitApplicationLaunchShortcut extends JUnitLaunchShortcut {

    protected String getLaunchConfigurationTypeId() {
        return IDroolsDebugConstants.JUNIT_LAUNCH_CONFIGURATION_TYPE;
    }
    
}
