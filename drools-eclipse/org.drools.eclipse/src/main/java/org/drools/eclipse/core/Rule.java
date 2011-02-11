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

package org.drools.eclipse.core;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a rule. 
 */
public class Rule extends DroolsElement {

    private final String ruleName;
    private Map attributes = new HashMap();
    private RuleGroup group = null;
    
    Rule(Package parent, String ruleName) {
        super(parent);
        this.ruleName = ruleName;
    }

    public Package getParentPackage() {
        return (Package) getParent();
    }

    public String getRuleName() {
        return ruleName;
    }

    public RuleGroup getGroup() {
        Package pkg = (Package)getParent();
        if (pkg.getDefaultGroup().equals(group)) {
            return null;
        }
        return group;
    }

    public void setGroup(RuleGroup group) {
        this.group = group;
    }

    public RuleAttribute getAttribute(String attributeName) {
        return (RuleAttribute) attributes.get(attributeName);
    }

    public int getType() {
        return RULE;
    }

    public DroolsElement[] getChildren() {
        return NO_ELEMENTS;
    }

    public String toString() {
        return ruleName;
    }

    // These are helper methods for creating the model and should not
    // be used directly.  Use DroolsModelBuilder instead.

    void addAttribute(RuleAttribute attribute) {
        attributes.put(attribute.getAttributeName(), attribute);
    }

}
