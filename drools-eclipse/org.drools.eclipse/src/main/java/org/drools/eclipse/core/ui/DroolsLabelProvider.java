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

package org.drools.eclipse.core.ui;

import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.core.DroolsElement;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DroolsLabelProvider extends LabelProvider {

    private static final String[] ICONS = new String[] {
        DroolsPluginImages.PACKAGE,     // ruleset
        DroolsPluginImages.PACKAGE,  // package
        DroolsPluginImages.DROOLS,      // rule
        DroolsPluginImages.DROOLS,      // query
        DroolsPluginImages.METHOD,      // function
        DroolsPluginImages.CLASS,      // template
        DroolsPluginImages.DSL,      // expander
        DroolsPluginImages.GLOBAL,      // global
        DroolsPluginImages.IMPORT,      // import
        DroolsPluginImages.DROOLS,      // rule attribute
        DroolsPluginImages.RULEFLOW, // process
        DroolsPluginImages.DEFAULTRULEGROUP,   // Default Rule Group
        DroolsPluginImages.RULEGROUP,   // Activation Group
        DroolsPluginImages.RULEGROUP,   // Agenda Group
        DroolsPluginImages.RULEGROUP,   // RuleFlow Group
    };

    public Image getImage(Object element) {
        if (element instanceof DroolsElement) {
            String icon = ICONS[((DroolsElement) element).getType()];
            return DroolsPluginImages.getImageRegistry().get(icon);
        }
        return null;
    }
    
}
