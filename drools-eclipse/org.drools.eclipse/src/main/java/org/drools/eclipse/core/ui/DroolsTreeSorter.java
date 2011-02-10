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

import org.drools.eclipse.core.ActivationGroup;
import org.drools.eclipse.core.AgendaGroup;
import org.drools.eclipse.core.DefaultRuleGroup;
import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.Expander;
import org.drools.eclipse.core.Function;
import org.drools.eclipse.core.Global;
import org.drools.eclipse.core.Import;
import org.drools.eclipse.core.Package;
import org.drools.eclipse.core.Query;
import org.drools.eclipse.core.Rule;
import org.drools.eclipse.core.RuleAttribute;
import org.drools.eclipse.core.RuleFlowGroup;
import org.drools.eclipse.core.RuleSet;
import org.drools.eclipse.core.Template;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class DroolsTreeSorter extends ViewerSorter {

    // level 0
    private static final int RULESET = 0;

    // level 1
    private static final int PACKAGE = 1;

    // level 2
    private static final int RULE = 2;
    private static final int QUERY = 3;
    private static final int FUNCTION = 4;
    private static final int TEMPLATE = 5;
    private static final int EXPANDER = 6;
    private static final int GLOBAL = 7;
    private static final int IMPORT = 8;

    private static final int DEFAULT_RULE_GROUP = 11;
    private static final int ACTIVATION_GROUP = 12;
    private static final int AGENDA_GROUP = 13;
    private static final int RULEFLOW_GROUP = 14;

    // level 3
    private static final int RULE_ATTRIBUTE = 9;

    private static final int UNKNOWN = 10;

    private static DroolsLabelProvider labelProvider = new DroolsLabelProvider();

    public int compare(Viewer viewer, Object e1, Object e2) {
        DroolsElement node1 = (DroolsElement) e1;
        DroolsElement node2 = (DroolsElement) e2;
        int type1 = getElementType(node1);
        int type2 = getElementType(node2);
        if (type1 != type2) {
            return type1 - type2;
        }
        String label1 = labelProvider.getText(node1);
        String label2 = labelProvider.getText(node2);
        if (label1 == null) {
            return (label2 == null ? 0 : -1);
        }
        return label1.compareTo(label2);
    }

    private int getElementType(DroolsElement o) {
        if (o instanceof RuleSet) {
            return RULESET;
        } else if (o instanceof Package) {
            return PACKAGE;
        } else if (o instanceof Rule) {
            return RULE;
        } else if (o instanceof Query) {
            return QUERY;
        } else if (o instanceof Function) {
            return FUNCTION;
        } else if (o instanceof Template) {
            return TEMPLATE;
        } else if (o instanceof Expander) {
            return EXPANDER;
        } else if (o instanceof Global) {
            return GLOBAL;
        } else if (o instanceof Import) {
            return IMPORT;
        } else if (o instanceof RuleAttribute) {
            return RULE_ATTRIBUTE;
        } else if (o instanceof RuleFlowGroup) {
            return RULEFLOW_GROUP;
        } else if (o instanceof AgendaGroup) {
            return AGENDA_GROUP;
        } else if (o instanceof ActivationGroup) {
            return ACTIVATION_GROUP;
        } else if (o instanceof DefaultRuleGroup) {
            return DEFAULT_RULE_GROUP;
        }
        return UNKNOWN;
    }

}
