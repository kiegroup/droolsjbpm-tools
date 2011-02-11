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

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a rule group of type Agenda-Group Or RuleFlow-Group
 */
public abstract class RuleGroup extends DroolsElement {
    private String groupName = null;
    private List rules = new ArrayList();

    protected RuleGroup(Package parent, Rule rule, String groupName) {
        super(parent);
        this.groupName = groupName;
        addRule(rule);
        parent.addGroup(this);
    }

    public DroolsElement[] getRules() {
        return (DroolsElement[]) rules.toArray(new DroolsElement[0]);
    }

    @Override
    public DroolsElement[] getChildren() {
        return NO_ELEMENTS;
    }

    @Override
    public abstract int getType();

    @Override
    public String toString() {
        return groupName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            String other = (String) obj;
            return toString().equals(other.toString());
        }
        return false;
    }

    protected void addRule(Rule rule) {
        if (rule!=null) {
            if (!rules.contains(rule)) rules.add(rule);
        }
    }
}
