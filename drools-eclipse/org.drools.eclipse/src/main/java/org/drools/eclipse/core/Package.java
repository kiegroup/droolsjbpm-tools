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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This represents a package.
 */
public class Package extends DroolsElement {

    private String packageName;
    private List rules = new ArrayList();
    private List functions = new ArrayList();
    private List expanders = new ArrayList();
    private List imports = new ArrayList();
    private List globals = new ArrayList();
    private List queries = new ArrayList();
    private List templates = new ArrayList();
    private List processes = new ArrayList();
    private Map groups = new HashMap();

    private DefaultRuleGroup defaultGroup = null;

    Package(RuleSet parent, String packageName) {
        super(parent);
        this.packageName = packageName == null ? "(default package)"
                : packageName;
        defaultGroup = new DefaultRuleGroup(this, null, "MAIN");
    }

    public RuleSet getParentRuleSet() {
        return (RuleSet) getParent();
    }

    public String getPackageName() {
        return packageName;
    }

    public int getType() {
        return PACKAGE;
    }

    public DroolsElement[] getChildren() {
        List children = new ArrayList();
        children.addAll(rules);
        children.addAll(queries);
        children.addAll(globals);
        children.addAll(functions);
        children.addAll(expanders);
        children.addAll(imports);
        children.addAll(templates);
        children.addAll(processes);
        // }
        return (DroolsElement[]) children.toArray(new DroolsElement[0]);
    }

    public String toString() {
        return packageName;
    }

    // These are helper methods for creating the model and should not
    // be used directly. Use DroolsModelBuilder instead.

    void addRule(Rule rule) {
        rules.add(rule);
    }

    void removeRule(Rule rule) {
        rules.remove(rule);
    }

    void addFunction(Function function) {
        functions.add(function);
    }

    void removeFunction(Function function) {
        functions.remove(function);
    }

    void addExpander(Expander expander) {
        expanders.add(expander);
    }

    void removeExpander(Expander expander) {
        expanders.remove(expander);
    }

    void addImport(Import imp) {
        imports.add(imp);
    }

    void removeImport(Import imp) {
        imports.remove(imp);
    }

    void addGlobal(Global global) {
        globals.add(global);
    }

    void removeGlobal(Global global) {
        globals.remove(global);
    }

    void addQuery(Query query) {
        queries.add(query);
    }

    void removeQuery(Query query) {
        queries.remove(query);
    }

    void addTemplate(Template template) {
        templates.add(template);
    }

    void removeTemplate(Template template) {
        templates.remove(template);
    }

    void addProcess(Process process) {
        processes.add(process);
    }

    void removeProcess(Process process) {
        processes.remove(process);
    }

    public RuleGroup getGroup(String name, int type) {
        Map entries = (Map) groups.get(type);
        if ((entries != null) && (entries.containsKey(name))) {
            return (RuleGroup) entries.get(name);
        }
        return null;
    }

    void addGroup(RuleGroup group) {
        Map entries = (Map) groups.get(group.getType());
        if (entries == null) {
            entries = new HashMap();
            entries.put(group.toString(), group);
            groups.put(group.getType(), entries);
        } else {
            if (!entries.containsKey(group.toString())) {
                entries.put(group.toString(), group);
            }
        }
    }

    public RuleGroup getDefaultGroup() {
        return defaultGroup;
    }

    void removeGroup(RuleGroup group) {
        Map entries = (Map) groups.get(group.getType());
        if (entries.containsValue(group)) {
            entries.remove(group);
        }
    }

    public DroolsElement[] getGroups() {
        List returnedValue = new ArrayList();
        for (Iterator it = groups.keySet().iterator(); it.hasNext();) {
            int key = (Integer) it.next();
            Map entries = (Map) groups.get(key);
            returnedValue.addAll(entries.values());
        }
        return (DroolsElement[]) returnedValue.toArray(new DroolsElement[0]);
    }
}
