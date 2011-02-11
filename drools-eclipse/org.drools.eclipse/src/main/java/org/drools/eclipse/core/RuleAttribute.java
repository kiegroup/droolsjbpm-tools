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

/**
 * This represents a rule attribute. 
 */
public class RuleAttribute extends DroolsElement {

    private final String attributeName;
    private final Object attributeValue;

    RuleAttribute(Rule parent, String attributeName, Object attributeValue) {
        super(parent);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public Rule getParentRule() {
        return (Rule) getParent();
    }

    public String getAttributeName() {
        return attributeName;
    }

    public int getType() {
        return RULE_ATTRIBUTE;
    }

    public DroolsElement[] getChildren() {
        return NO_ELEMENTS;
    }

    public String toString() {
        return attributeName + " = " + attributeValue;
    }

}
