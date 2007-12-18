package org.drools.eclipse.editors.completion;

import org.drools.lang.Location;

import junit.framework.TestCase;

/**
 * Test to check the location determination when doing code completion inside
 * rule condtions.
 *
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 *
 */
public class CompletionContextTest extends TestCase {

    public void testColumnOperatorPattern() {
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(    property ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property   ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( name : property ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(name:property ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(    name:property ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( name:property   ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(   name  :  property  ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property1 == \"value\", property2 ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property1 == \"value\", name : property2 ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property1 == \"value\", name:property2 ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property1 == \"value\",   name  :  property2  ").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(    prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( name:prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(name:prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( name : prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(   name  :  prop").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property <= ").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( name : property == ").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("(property==").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property contains ").matches());
    	assertFalse(CompletionContext.PATTERN_PATTERN_OPERATOR.matcher("( property1 == \"value\", property2 >= ").matches());
    }

    public void testColumnArgumentPattern() {
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property == ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property >= ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("(property== ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("(   property   ==   ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( name : property == ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("(name:property== ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("(  name  :  property  ==  ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\", property2 == ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\",property2== ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\",  property2  ==  ").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property == otherProp").matches());
    	assertTrue(CompletionContext.PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher("(property==otherProp").matches());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( condition == true ) \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		class: Class( condition == true, condition2 == null ) \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION5() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class( condition == true ) \n" +
        	"		Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION6() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		class: Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION7() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		class:Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    /** Inside of condition: start */
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START2() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( na";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("na", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START3() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name.subProperty['test'].subsu";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("name.subProperty['test'].subsu", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START4() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START5() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, na";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START6() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( \n" +
        	"			";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START7() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, \n" +
        	"			";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));

    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START8() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( c: condition, \n" +
        	"			";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }


    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START9a() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( name:";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }


    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START9b() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START10() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));

    }

    /** Inside of  condition: Operator */
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR1() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR2() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class(property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR3() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR4() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR5() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property   ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR6() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, name : property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR7() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\", name : property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR8() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\",property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR9() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, \n" +
        	"			name : property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
    }

    /** Inside of condition: argument */
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT1() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property== ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT3() {
        String input =
            "rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property <= ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("<=", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:property != ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("!=", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT5() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, property2 == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property== ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT7() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == otherPropertyN";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT8() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == \"someth";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT9() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property contains ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("contains", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT10() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property excludes ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("excludes", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT11() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property matches \"prop";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("matches", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT12() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property in ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("in", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property in ('1', '2') ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START11() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property in ('1', '2'), ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT13() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not in ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("in", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not in ('1', '2') ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START12() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not in ('1', '2'), ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT14() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property memberOf ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("memberOf", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property memberOf collection ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START13() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property memberOf collection, ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT15() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not memberOf ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("memberOf", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not memberOf collection ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START14() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property not memberOf collection, ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    /** EXISTS */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists(";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS5() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( name : Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDeterminationINSIDE_CONDITION_START16() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    /** NOT */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS7() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS8() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START21() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START22() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START23() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists name : Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION9() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class () \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    /** AND */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) &&  ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class () and   ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		name : Class ( name: property ) and ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR5() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ) \n" +
        	"       and ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR7() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and name : Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR8() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) && name : Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION31() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION32() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and not Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION33() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and exists Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START20() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR21() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( name ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("name", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR22() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( name == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_NOT() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) and not ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) and exists ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION30() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and not Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());

        /** OR */
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR21() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR22() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) || ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR23() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class () or   ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR24() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		name : Class ( name: property ) or ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR25() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ) \n" +
        	"       or ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR26() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR27() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or name : Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_AND_OR28() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) || name : Cl";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION40() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START40() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( name ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("name", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT30() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( name == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        }

    public void testCheckLHSLocationDetermination_EGIN_OF_CONDITION_NOT() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) or not ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION_EXISTS40() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) or exists ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS, location.getType());
        }

    /** EVAL */
    public void testCheckLHSLocationDetermination_INSIDE_EVAL1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval(";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( myCla";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("myCla", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL4() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMetho";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getMetho", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL5() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod().get";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod().get", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL7() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(\"someStringWith)))\").get";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(\"someStringWith)))\").get", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL8() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(\"someStringWith(((\").get";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(\"someStringWith(((\").get", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL9() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( true )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION50() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty(name).isTrue() )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION51() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty(\"someStringWith(((\").isTrue() )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_EVAL10() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty((((String) s) )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_EVAL, location.getType());
        assertEquals("param.getProperty((((String) s) )", location.getProperty(Location.LOCATION_EVAL_CONTENT));
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION52() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty((((String) s))))";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION53() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( true ) \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    /** MULTIPLE RESTRICTIONS */
    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR12() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 && ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR13() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property1, property2 > 0 && ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR14() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property1 < 20, property2 > 0 && ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT20() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 && < ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("<", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 && < 10 ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START41() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 && < 10, ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR60() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 || ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR61() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 || \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR62() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property1, property2 > 0 || ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR63() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property1 < 20, property2 > 0 || ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END10() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END11() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END12() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 && < 10 ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END13() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 || < 10 ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_END14() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == \"test\" || == \"test2\" ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_END, location.getType());
        }

    /** FROM */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION60() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION61() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) fr";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void FIXME_testCheckLHSLocationDetermination_FROM1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM, location.getType());
        assertEquals("", location.getProperty(Location.LOCATION_FROM_CONTENT));
        }

    public void FIXME_testCheckLHSLocationDetermination_FROM2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlob";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM, location.getType());
        assertEquals("myGlob", location.getProperty(Location.LOCATION_FROM_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM3() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.get";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM, location.getType());
        assertEquals("myGlobal.get", location.getProperty(Location.LOCATION_FROM_CONTENT));
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION75() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.getList() \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION71() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from getDroolsFunction() \n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    /** FROM ACCUMULATE */
    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE, location.getType());
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate(";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION73() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total = 0; ), \n" +
        	"			action( total += $cheese.getPrice(); ), \n" +
        	"           result( new Integer( total ) ) \n" +
        	"		) \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_INIT_INSIDE() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, location.getType());
        assertEquals("", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_ACTION_INSIDE() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total = 0; ), \n" +
        	"			action( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, location.getType());
        assertEquals(" int total = 0; ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        assertEquals("", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_RESULT_INSIDE() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total = 0; ), \n" +
        	"			action( total += $cheese.getPrice(); ), \n" +
        	"           result( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE, location.getType());
        assertEquals(" int total = 0; ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        assertEquals(" total += $cheese.getPrice(); ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
        assertEquals("", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_INIT_INSIDE2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total =";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, location.getType());
        assertEquals("int total =", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_ACTION_INSIDE2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total = 0; ), \n" +
        	"			action( total += $ch";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, location.getType());
        assertEquals(" int total = 0; ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        assertEquals("total += $ch", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
        }

    public void testCheckLHSLocationDetermination_FROM_ACCUMULATE_RESULT_INSIDE2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == $likes ), \n" +
        	"			init( int total = 0; ), \n" +
        	"			action( total += $cheese.getPrice(); ), \n" +
        	"           result( new Integer( tot";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE, location.getType());
        assertEquals(" int total = 0; ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
        assertEquals(" total += $cheese.getPrice(); ", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
        assertEquals("new Integer( tot", location.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR40() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("type", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from accumulate( \n" +
        	"			$cheese : Cheese( type == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("type", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    /** FROM COLLECT */
    public void testCheckLHSLocationDetermination_FROM_COLLECT1() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_COLLECT, location.getType());
        }

    public void testCheckLHSLocationDetermination_FROM_COLLECT2() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect(";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM_COLLECT, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION67() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect ( \n" +
        	"			Cheese( type == $likes )" +
        	"		) \n" +
        	"		";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START31() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect ( \n" +
        	"			Cheese( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR31() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect ( \n" +
        	"			Cheese( type ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("type", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT21() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from collect ( \n" +
        	"			Cheese( type == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Cheese", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("type", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    /** NESTED FROM */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION68() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void FIXME_testCheckLHSLocationDetermination_FROM5() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) from ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION69() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from collect( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION70() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void FIXME_testCheckLHSLocationDetermination_FROM6() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_FROM, location.getType());
        }

    /** FORALL */
    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION81() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		forall ( ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START32() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		forall ( " +
        	"           Class ( pr";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("pr", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_OPERATOR32() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		forall ( " +
        	"           Class ( property ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_ARGUMENT22() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		forall ( " +
        	"           Class ( property == ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(Location.LOCATION_PROPERTY_OPERATOR));
        }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION76() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		forall ( " +
        	"           Class ( property == \"test\")" +
        	"           C";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION77a() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		ArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() ) ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_BEGIN_OF_CONDITION77b() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       ArrayList(size > 50) from accumulate( Person( disabled == \"yes\", income > 100000 ) from town.getPersons() )";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START45a() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( name :";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckLHSLocationDetermination_INSIDE_CONDITION_START45b() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( name : ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }

    public void testCheckRHSLocationDetermination_firstLineOfLHS() {
        String input =
        	"rule MyRule \n" +
        	"	when\n" +
        	"		Class ( )\n" +
        	"   then\n" +
        	"       ";

        Location location = new CompletionContext(input).getLocation();

        assertEquals(Location.LOCATION_RHS, location.getType());
        assertEquals("", location.getProperty(Location.LOCATION_RHS_CONTENT));
    }

    public void testCheckRHSLocationDetermination_startOfNewlINE() {
        String input =
        	"rule MyRule \n" +
        	"	when\n" +
        	"		Class ( )\n" +
        	"   then\n" +
        	"       assert(null);\n" +
        	"       ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RHS, location.getType());
        assertEquals("assert(null);\n       ", location.getProperty(Location.LOCATION_RHS_CONTENT));
    }

    public void testCheckRHSLocationDetermination3() {
        String input =
        	"rule MyRule \n" +
        	"	when\n" +
        	"		Class ( )\n" +
        	"   then\n" +
        	"       meth";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RHS, location.getType());
        assertEquals("meth", location.getProperty(Location.LOCATION_RHS_CONTENT));
    }

    public void testCheckRuleHeaderLocationDetermination() {
        String input =
        	"rule MyRule ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void testCheckRuleHeaderLocationDetermination2() {
        String input =
        	"rule MyRule \n" +
        	"	salience 12 activation-group \"my";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void FAILINGtestCheckRuleHeaderLocationDetermination3() {
        // KRISV: still can't make this work... apparently, ANTLR is trying to recover from
        // the error (unkown token) by deleting the token. I don't know why it continues to
        // execute actions though, if the EOF is found.
        String input =
          "rule \"Hello World\" ruleflow-group \"hello\" s";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void testCheckRuleHeaderLocationDetermination_dialect1() {
        String input  =
        	"rule MyRule \n" +
        	"	dialect \"java\"";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void testCheckRuleHeaderLocationDetermination_dialect2() {
        String input  =
        	"rule MyRule \n" +
        	"	dialect \"mvel\"";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void testCheckRuleHeaderLocationDetermination_dialect3() {
        String input  =
        	"rule MyRule \n" +
        	"	dialect ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    public void testCheckRuleHeaderLocationDetermination_dialect4() {
        String input  =
        	"rule MyRule \n" +
        	"	dialect \"";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
    }

    //TODO: add tests for dialect defined at package header level

    public void testCheckQueryLocationDetermination_RULE_HEADER1() {
        String input =
        	"query MyQuery ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
        }

    public void testCheckQueryLocationDetermination_RULE_HEADER2() {
        String input =
        	"query \"MyQuery\" ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_RULE_HEADER, location.getType());
        }

    public void testCheckQueryLocationDetermination_LHS_BEGIN_OF_CONDITION() {
        String input =
            "query MyQuery() ";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_BEGIN_OF_CONDITION, location.getType());
        }

    public void testCheckQueryLocationDetermination_LHS_INSIDE_CONDITION_START() {
        String input =
        	"query MyQuery \n" +
        	"	Class (";
        Location location = new CompletionContext(input).getLocation();
        assertEquals(Location.LOCATION_LHS_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME));
    }
}