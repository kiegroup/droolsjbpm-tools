package org.drools.eclipse.debug;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.base.MapGlobalResolver;
import org.drools.compiler.PackageBuilder;
import org.drools.reteoo.ReteooStatefulSession;
import org.drools.spi.AgendaGroup;

/**
 *
 * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
 * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
 * VIEWS (which are using reflection)
 *    
 * @author Kris Verlaenen
 */
public class DebugViewsTest extends TestCase {
	
	/*
	 * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
	 * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
	 * VIEWS (which are using reflection)
	 */
    
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
    	assertEquals("list", globals[0].getKey());
    	assertEquals(list, globals[0].getValue());
    	assertEquals("s", globals[1].getKey());
    	assertEquals("String", globals[1].getValue());
    }
    
	/*
	 * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
	 * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
	 * VIEWS (which are using reflection)
	 */
    
    public void testAgendaView() throws Exception {
    	RuleBase ruleBase = RuleBaseFactory.newRuleBase();
    	ReteooStatefulSession session = (ReteooStatefulSession) ruleBase.newStatefulSession();
    	AgendaGroup[] agendaGroups = session.getAgenda().getAgendaGroups();
    	assertEquals(1, agendaGroups.length);
    	assertEquals("MAIN", agendaGroups[0].getName());
    	assertEquals(0, agendaGroups[0].getActivations().length);
    	AgendaGroup focus = session.getAgenda().getFocus();
    	assertEquals("MAIN", focus.getName());
    }
    
	/*
	 * WARNING: DO NOT CHANGE ANYTHING IN THIS TEST CLASS
	 * WITHOUT ALSO CHANGING THE IMPLEMENTATION IN THE DEBUG
	 * VIEWS (which are using reflection)
	 */
    
    public void testWorkingMemoryView() throws Exception {
    	RuleBase ruleBase = RuleBaseFactory.newRuleBase();
    	ReteooStatefulSession session = (ReteooStatefulSession) ruleBase.newStatefulSession();
    	session.insert("Test1");
    	session.insert("Test2");
    	Object[] objects = session.iterateObjectsToList().toArray();
    	assertEquals(2, objects.length);
    	assertEquals("Test1", objects[0]);
    	assertEquals("Test2", objects[1]);
    }
    
}
