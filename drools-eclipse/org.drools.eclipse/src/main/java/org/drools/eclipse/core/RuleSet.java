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
 * This represents a rule set. 
 */
public class RuleSet extends DroolsElement {

    private Map packages = new HashMap();

    RuleSet() {
        super(null);
    }

    public Package getPackage(String packageName) {
        return (Package) packages.get(packageName);
    }

    public int getType() {
        return RULESET;
    }

    public DroolsElement[] getChildren() {
        return (DroolsElement[]) packages.values().toArray(
            new DroolsElement[packages.size()]);
    }

    // These are helper methods for creating the model and should not
    // be used directly.  Use DroolsModelBuilder instead.

    void addPackage(Package pkg) {
        packages.put(pkg.getPackageName(), pkg);
    }

    void removePackage(String packageName) {
        packages.remove(packageName);
    }

    void clear() {
        packages.clear();
    }

}
