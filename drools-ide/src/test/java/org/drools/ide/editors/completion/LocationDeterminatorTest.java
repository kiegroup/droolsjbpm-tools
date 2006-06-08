package org.drools.ide.editors.completion;

import junit.framework.TestCase;

/**
 * Test to check the location determination when doing code completion inside
 * rule condtions.
 * 
 * Possible locations:
 * LOCATION_BEGIN_OF_CONDITION
 * 		-> all drools condition keywords + imported classes
 * LOCATION_INSIDE_CONDITION_START
 * 		-> all properties of specified class
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 *
 */
public class LocationDeterminatorTest extends TestCase {

    public void testColumnOperatorPattern() {
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(    property ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property   ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( name : property ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(name:property ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(    name:property ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( name:property   ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(   name  :  property  ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property1 == \"value\", property2 ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property1 == \"value\", name : property2 ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property1 == \"value\", name:property2 ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property1 == \"value\",   name  :  property2  ").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(    prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( name:prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(name:prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( name : prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(   name  :  prop").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property <= ").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( name : property == ").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("(property==").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property contains ").matches());
    	assertFalse(LocationDeterminator.COLUMN_PATTERN_OPERATOR.matcher("( property1 == \"value\", property2 >= ").matches());
    }

    public void testColumnArgumentPattern() {
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property == ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property >= ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("(property== ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("(   property   ==   ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( name : property == ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("(name:property== ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("(  name  :  property  ==  ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\", property2 == ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\",property2== ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property1 == \"value\",  property2  ==  ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("( property == otherProp").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher("(property==otherProp").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_CONTAINS_ARGUMENT.matcher("( property contains ").matches());
    	assertTrue(LocationDeterminator.COLUMN_PATTERN_EXCLUDES_ARGUMENT.matcher("(   property   excludes   ").matches());
    }
    
    public void testCheckLocationDetermination() {
        String input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		";
        LocationDeterminator.Location location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( condition == true ) \n" +
        	"		";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		class: Class( condition == true, condition2 == null ) \n" +
        	"		";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( condition == true ) \n" +
        	"		Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		class: Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		class:Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        /** Inside of condition: start */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( na";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, na";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( \n" +
        	"			";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, \n" +
        	"			";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        /** Inside of condition: Operator */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class(property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property   ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\", name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\",property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, \n" + 
        	"			name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        /** Inside of condition: argument */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property== ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:property == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, property2 == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property== ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == otherPropertyN";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == \"someth";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        /** EXISTS */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class (";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class () \n" + 
        	"		";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    
        /** NOT */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class (";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class () \n" +
        	"		";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    }
    
}
