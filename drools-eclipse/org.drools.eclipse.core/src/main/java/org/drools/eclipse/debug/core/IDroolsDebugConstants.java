package org.drools.eclipse.debug.core;

public interface IDroolsDebugConstants {

	String ID_DROOLS_DEBUG_MODEL = "org.drools.eclipse.debug";
	String DROOLS_MARKER_TYPE = "org.drools.eclipse.droolsBreakpointMarker";
	String DRL_LINE_NUMBER = "Drools_DRL_LineNumber";
	String LAUNCH_CONFIGURATION_TYPE = "org.drools.eclipse.launching.DroolsLaunchConfigurationDelegate";
    String JUNIT_LAUNCH_CONFIGURATION_TYPE = "org.drools.eclipse.launching.DroolsJUnitLaunchConfigurationDelegate";
    
    /**
     * com.package.HelloWorld:14;com.package.GoodBye:7 style of packed rule info. int is the linenumber at the drl. 
     */
    String DRL_RULES = "org.drools.eclipse.debug.DRL_RULES";

}
