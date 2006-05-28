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
        	"		Class( condition == true ) \n" +
        	"		Cl";
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
        	"		Class (";
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
