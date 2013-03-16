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

package org.drools.eclipse.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.core.base.MapGlobalResolver;
import org.drools.compiler.PackageBuilder;
import org.drools.reteoo.ReteooStatefulSession;
import org.drools.core.spi.AgendaGroup;
import org.junit.Test;
import org.kie.runtime.rule.Match;

/**
 *
 * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
 * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
 * VIEWS (which are using reflection)
 */
public class DebugViewsTest {

    /*
     * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
     * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
     * VIEWS (which are using reflection)
     */
    
    @Test
    public void testApplicationDataView() throws Exception {
        Reader source = new InputStreamReader(DebugViewsTest.class.getResourceAsStream("/debug.drl"));
        PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl(source);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage(builder.getPackage());
        ReteooStatefulSession session = (ReteooStatefulSession) ruleBase.newStatefulSession();
        session.setGlobal("s", "String");
        List list = new ArrayList();
        list.add("Value");
        session.setGlobal("list", list);
        Entry[] globals = ((MapGlobalResolver) session.getGlobalResolver()).getGlobals();
        assertEquals(2, globals.length);
        if ("list".equals(globals[0].getKey())) {
            assertEquals("list", globals[0].getKey());
            assertEquals(list, globals[0].getValue());
            assertEquals("s", globals[1].getKey());
            assertEquals("String", globals[1].getValue());
        } else {
            assertEquals("list", globals[1].getKey());
            assertEquals(list, globals[1].getValue());
            assertEquals("s", globals[0].getKey());
            assertEquals("String", globals[0].getValue());
        }
    }
    
    /*
     * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
     * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
     * VIEWS (which are using reflection)
     */
    
    @Test
    public void testAgendaView() throws Exception {
        Reader source = new InputStreamReader(DebugViewsTest.class.getResourceAsStream("/debug.drl"));
        PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl(source);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage(builder.getPackage());
        ReteooStatefulSession session = (ReteooStatefulSession) ruleBase.newStatefulSession();
        List list = new ArrayList();
        session.setGlobal("list", list);
        session.insert("String1");
        String focusName = session.getAgenda().getFocusName();
        assertEquals("MAIN", focusName);
        AgendaGroup[] agendaGroups = session.getAgenda().getAgendaGroups();
        assertEquals(1, agendaGroups.length);
        assertEquals("MAIN", agendaGroups[0].getName());
        assertEquals(1, agendaGroups[0].getActivations().length);

        Match activation = agendaGroups[0].getActivations()[0];
        assertEquals("ActivationCreator", activation.getRule().getName());
        Entry[] parameters = session.getActivationParameters(
            ((org.drools.core.spi.Activation) activation).getActivationNumber());
        assertEquals(1, parameters.length);
        assertEquals("o", parameters[0].getKey());
        assertEquals("String1", parameters[0].getValue());
    }
    
    /*
     * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
     * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
     * VIEWS (which are using reflection)
     */
    
    @Test
    public void testWorkingMemoryView() throws Exception {
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ReteooStatefulSession session = (ReteooStatefulSession) ruleBase.newStatefulSession();
        session.insert("Test1");
        session.insert("Test2");
        Object[] objects = session.iterateObjectsToList().toArray();
        assertEquals(2, objects.length);
        assertTrue(("Test1".equals(objects[0]) && "Test2".equals(objects[1])) ||
                   ("Test2".equals(objects[0]) && "Test1".equals(objects[1])));
    }
    
}
