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
