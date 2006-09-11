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
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class(property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property   ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\", name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1 == \"value\",property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, \n" + 
        	"			name : property ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        /** Inside of condition: argument */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property== ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property <= ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("<=", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:property != ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("!=", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1 : property1, property2 == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class (name:property== ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == otherPropertyN";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == \"someth";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("==", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property contains ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("contains", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property excludes ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("excludes", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property matches \"prop";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("matches", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
        
        /** EXISTS */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists(";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists ( name : Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

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
        	"		exists Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        /** NOT */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not exists Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class (";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

// TODO        
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not exists Class (";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not exists name : Class (";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		not Class () \n" +
        	"		";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    
        /** AND */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) &&  ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class () and   ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		name : Class ( name: property ) and ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ) \n" + 
        	"       and ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and name : Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) && name : Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and not Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and exists Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( name ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("name", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and Class ( name == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) and not ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) and exists ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) and not Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        /** OR */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) || ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class () or   ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
    
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		name : Class ( name: property ) or ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ) \n" + 
        	"       or ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or name : Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) || name : Cl";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( name ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("name", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( ) or Class ( name == ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) or not ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		exists Class ( ) or exists ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());

        /** EVAL */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval ( ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval(";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( myCla";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("myCla", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMetho";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getMetho", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod().get";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod().get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(\"someStringWith)))\").get";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(\"someStringWith)))\").get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getMethod(\"someStringWith(((\").get";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getMethod(\"someStringWith(((\").get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( true )";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty(name).isTrue() )";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty(\"someStringWith(((\").isTrue() )";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty((((String) s) )";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
        assertEquals("param.getProperty((((String) s) )", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( param.getProperty((((String) s))))";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		eval( true ) \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        /** MULTIPLE RESTRICTIONS */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 & ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 & " +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property1, property2 > 0 & ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property1 < 20, property2 > 0 & ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 & < ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
        assertEquals("<", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 | ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 | \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name : property1, property2 > 0 | ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property1 < 20, property2 > 0 | ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 & < 10 ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 | < 10 ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property == \"test\" | == \"test2\" ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());

        /** FROM */
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) fr";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlob";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
        assertEquals("myGlob", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.get";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
        assertEquals("myGlobal.get", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.getList() \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from getDroolsFunction() \n" +
        	"       ";
        location = LocationDeterminator.getLocationInCondition(input);
        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
    }
    
}
