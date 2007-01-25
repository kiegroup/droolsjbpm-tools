package org.drools.ide.editors.completion;

import java.util.List;

import junit.framework.TestCase;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.ColumnDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.FieldConstraintDescr;
import org.drools.lang.descr.LiteralRestrictionDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.lang.descr.VariableRestrictionDescr;

/**
 * Test to check the results from parsing incomplete rule fragments. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class IncompleteParsingTest extends TestCase {

	private RuleDescr parseRuleString(String s) {
		PackageDescr packageDescr = parseString(s);
		if (packageDescr != null) {
			List rules = packageDescr.getRules();
			if (rules != null && rules.size() == 1) {
				return (RuleDescr) rules.get(0);
			}
		}
		return null;
	}
	
	private PackageDescr parseString(String s) {
		DrlParser parser = new DrlParser();
		try {
			return parser.parse(s);
		} catch (DroolsParserException exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	
    public void testParsingColumn() {
        String input = 
        	"rule MyRule \n" +
        	"  when \n" +
        	"    ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(0, rule.getLhs().getDescrs().size());

        
        /**
         * This is how the parsed tree should look like:
         * 
         * RuleDescr
         *   ColumnDescr [objectType = "Class"]
         *     FieldConstraintDescr [fieldName = "condition"]
         *       LiteralRestrictionDescr [evaluator = "==", text = "true"]
         */
        input = 
        	"rule MyRule \n" +
        	"  when \n" +
        	"    Class( condition == true ) \n" +
        	"    ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        ColumnDescr column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertTrue(column.getEndLine() != -1 || column.getEndColumn() != -1);
        assertEquals(1, column.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("condition", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr restriction = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", restriction.getEvaluator());
        assertEquals("true", restriction.getText());

        input = 
        	"rule MyRule \n" +
	    	"  when \n" +
	    	"    class: Class( condition == true, condition2 == null ) \n" +
	    	"    ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(column.getEndCharacter() != -1);

        input = 
	    	"rule MyRule \n" +
	    	"  when \n" +
	    	"    Cl";
        rule = parseRuleString(input);
        assertEquals(0, rule.getLhs().getDescrs().size());

        input = 
	    	"rule MyRule \n" +
	    	"  when \n" +
	    	"    Class( condition == true ) \n" +
	    	"    Cl";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(column.getEndCharacter() != -1);

        input = 
			"rule MyRule \n" +
			"  when \n" +
			"    class:";
        rule = parseRuleString(input);
        // KRISV: at this point we don't know if the variable will be bound to a 
        // simple column, or a FROM result column, or ACCUMULATE result column, etc.
        // I tried to set a simple column as default, and changing in case it ends up
        // another thing, but the code is really a hack and made the parser a lot more
        // complex... can we leave as it is for now?
        //
//        assertEquals(1, rule.getLhs().getDescrs().size());
//        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
//        assertEquals("class", column.getIdentifier());
//        assertNull(column.getObjectType());
//        assertEquals(-1, column.getEndCharacter());

        input = 
			"rule MyRule \n" +
			"  when \n" +
			"    class: Cl";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", column.getIdentifier());
        assertEquals("Cl", column.getObjectType());
        assertTrue(column.getEndLine() == -1 && column.getEndColumn() == -1);
        assertEquals(0, column.getDescrs().size());
        assertEquals(-1, column.getEndCharacter());

        input = 
			"rule MyRule \n" +
			"  when \n" +
			"    class:Cl";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", column.getIdentifier());
        assertEquals("Cl", column.getObjectType());
        assertTrue(column.getEndLine() == -1 && column.getEndColumn() == -1);
        assertEquals(0, column.getDescrs().size());
        assertEquals(-1, column.getEndCharacter());

        /** Inside of condition: start */
        input = 
			"rule MyRule \n" +
			"  when \n" +
			"    Class (";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertTrue(column.getEndLine() == -1 && column.getEndColumn() == -1);
        assertEquals(0, column.getDescrs().size());
        assertEquals(-1, column.getEndCharacter());

        input = 
        	"rule MyRule \n" + 
        	"	when \n" + 
        	"		Class ( na";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0); 
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0); 
        assertEquals(-1, field.getEndCharacter());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, na";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(2, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0); 
        assertEquals(-1, field.getEndCharacter());
        assertEquals( "condition", field.getFieldName() );
        field = (FieldConstraintDescr) column.getDescrs().get(1);
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        FieldBindingDescr binding1 = (FieldBindingDescr) column.getDescrs().get(0);
        assertEquals("name", binding1.getIdentifier());
        assertNull(binding1.getFieldName());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(0, field.getRestrictions().size());
        assertEquals(-1, field.getEndCharacter());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        FieldBindingDescr binding = (FieldBindingDescr) column.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1: property1 == \"value1\", name2: property2 ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(3, column.getDescrs().size());
        binding = (FieldBindingDescr) column.getDescrs().get(0);
        assertEquals("name1", binding.getIdentifier());
        assertEquals("property1", binding.getFieldName());
        field = (FieldConstraintDescr) column.getDescrs().get(1);
        assertEquals("property1", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", literal.getEvaluator());
        assertEquals("value1", literal.getText());
        binding = (FieldBindingDescr) column.getDescrs().get(2);
        assertEquals("name2", binding.getIdentifier());
        assertEquals("property2", binding.getFieldName());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class(name:property==";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(2, column.getDescrs().size());
        binding = (FieldBindingDescr) column.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());
        field = (FieldConstraintDescr) column.getDescrs().get(1);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        // KRISV: you are right
        //
        // now I would like to access the evaluator '==', but this seems
        // not possible because the parser cannot create this descr yet
        // since it does not know what class to create (VariableRestrictionDescr
        // or LiteralRestrictionDescr or ?)
        // so maybe I should just extract this info myself, based on the
        // starting character of this FieldConstraintDescr?
        // TODO this method does not yet exist
        // assertEquals(-1, field.getEndCharacter());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property == otherPropertyN";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        VariableRestrictionDescr variable = (VariableRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", variable.getEvaluator());
        assertEquals("otherPropertyN", variable.getIdentifier());
        assertEquals(-1, field.getEndCharacter());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property == \"someth";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        // KRISV: for now, it would be really messy to make this work. String is a
        // lexer rule (not parser), and changing that or controling the behavior of it
        // is not simple. Can we leave the way it is for now?
        //
        // TODO literal should be a LiteralRestrictionDescr with filled in evaluator and text, not null
        // assertEquals("==", literal.getEvaluator());
        // assertEquals("someth", literal.getText());
        // TODO this method does not yet exist
        // assertEquals(-1, field.getEndCharacter());

        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property contains ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        // KRISV: you are right
        //
        // now I would like to access the evaluator 'contains', but this seems
        // not possible because the parser cannot create this descr yet
        // since it does not know what class to create (VariableRestrictionDescr
        // or LiteralRestrictionDescr or ?)
        // so maybe I should just extract this info myself, based on the
        // starting character of this FieldConstraintDescr?
        // TODO this method does not yet exist
        assertEquals(-1, field.getEndCharacter());
        
        input = 
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property matches \"someth";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", column.getObjectType());
        assertEquals(-1, column.getEndCharacter());
        assertEquals(1, column.getDescrs().size());
        field = (FieldConstraintDescr) column.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        // KRISV: see comments above
        //
        // TODO literal should be a LiteralRestrictionDescr with filled in evaluator and text, not null
        // assertEquals("matches", literal.getEvaluator());
        // assertEquals("someth", literal.getText());
        // TODO this method does not yet exist
        // assertEquals(-1, field.getEndCharacter());
    }
    
    public void testParsingCharactersStartEnd() {
        String input = 
        	"package test; \n" +
        	"rule MyRule \n" +
        	"  when \n" +
        	"    Class( condition == true ) \n" +
        	"  then \n" +
        	"    System.out.println(\"Done\") \n" +
        	"end \n";
        RuleDescr rule = parseRuleString(input);
        assertEquals(input.indexOf( "rule" ), rule.getStartCharacter());
        assertEquals(input.indexOf( "end" )+2, rule.getEndCharacter());
        ColumnDescr column = (ColumnDescr) rule.getLhs().getDescrs().get(0);
        assertEquals(input.indexOf( "(" ), column.getStartCharacter());
        assertEquals(input.indexOf( "true )" )+5, column.getEndCharacter());
    }
    
//    public void doTestRemainder() {
//        
//        /** EXISTS */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists ( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists(";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists ( Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists ( name : Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class (";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        /** NOT */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());
//    
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not exists ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not exists Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not Class (";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        // TODO        
////        input = 
////        	"rule MyRule \n" +
////        	"	when \n" +
////        	"		not exists Class (";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
////        input = 
////        	"rule MyRule \n" +
////        	"	when \n" +
////        	"		not exists name : Class (";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		not Class () \n" +
//        	"		";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//    
//        /** AND */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) &&  ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class () and   ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//    
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		name : Class ( name: property ) and ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( name: property ) \n" + 
//        	"       and ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and Cl";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and name : Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) && name : Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and not Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and exists Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and Class ( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and Class ( name ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
//        assertEquals("name", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and Class ( name == ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class ( ) and not ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class ( ) and exists ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) and not Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        /** OR */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) || ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class () or   ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//    
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		name : Class ( name: property ) or ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( name: property ) \n" + 
//        	"       or ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or name : Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) || name : Cl";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or Class ( ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or Class ( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or Class ( name ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
//        assertEquals("name", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( ) or Class ( name == ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class ( ) or not ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		exists Class ( ) or exists ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS, location.getType());
//
//        /** EVAL */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval ( ";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval(";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( myCla";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("myCla", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getMetho";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getMetho", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getMethod(";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getMethod(", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getMethod().get";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getMethod().get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getMethod(\"someStringWith)))\").get";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getMethod(\"someStringWith)))\").get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getMethod(\"someStringWith(((\").get";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getMethod(\"someStringWith(((\").get", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( true )";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getProperty(name).isTrue() )";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getProperty(\"someStringWith(((\").isTrue() )";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getProperty((((String) s) )";
////        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_EVAL, location.getType());
////        assertEquals("param.getProperty((((String) s) )", location.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( param.getProperty((((String) s))))";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		eval( true ) \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        /** MULTIPLE RESTRICTIONS */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 & ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 & " +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( name : property1, property2 > 0 & ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property1 < 20, property2 > 0 & ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 & < ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
////        assertEquals("<", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 | ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 | \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( name : property1, property2 > 0 | ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property1 < 20, property2 > 0 | ";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
////        assertEquals("Class", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
////        assertEquals("property2", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 & < 10 ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 | < 10 ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property == \"test\" | == \"test2\" ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_END, location.getType());
//
//        /** FROM */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) fr";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
//        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from myGlob";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
////        assertEquals("myGlob", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from myGlobal.get";
//        location = LocationDeterminator.getLocationInCondition(input);
////        assertEquals(LocationDeterminator.LOCATION_FROM, location.getType());
////        assertEquals("myGlobal.get", location.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from myGlobal.getList() \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from getDroolsFunction() \n" +
//        	"       ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        /** FROM ACCUMULATE */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate ( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate(";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total = 0; ), \n" +
//        	"			action( total += $cheese.getPrice(); ), \n" +
//        	"           result( new Integer( total ) ) \n" +
//        	"		) \n" +
//        	"		";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_INIT_INSIDE, location.getType());
//        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total = 0; ), \n" +
//        	"			action( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_ACTION_INSIDE, location.getType());
//        assertEquals("int total = 0; ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total = 0; ), \n" +
//        	"			action( total += $cheese.getPrice(); ), \n" +
//        	"           result( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_RESULT_INSIDE, location.getType());
//        assertEquals("int total = 0; ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//        assertEquals("total += $cheese.getPrice(); ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
//        assertEquals("", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total =";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_INIT_INSIDE, location.getType());
//        assertEquals("int total =", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total = 0; ), \n" +
//        	"			action( total += $ch";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_ACTION_INSIDE, location.getType());
//        assertEquals("int total = 0; ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//        assertEquals("total += $ch", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == $likes ), \n" +
//        	"			init( int total = 0; ), \n" +
//        	"			action( total += $cheese.getPrice(); ), \n" +
//        	"           result( new Integer( tot";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_ACCUMULATE_RESULT_INSIDE, location.getType());
//        assertEquals("int total = 0; ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT));
//        assertEquals("total += $cheese.getPrice(); ", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT));
//        assertEquals("new Integer( tot", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//        assertEquals("type", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from accumulate( \n" +
//        	"			$cheese : Cheese( type == ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//        assertEquals("type", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        /** FROM COLLECT */
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect ( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_COLLECT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect(";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_FROM_COLLECT, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect ( \n" +
//        	"			Cheese( type == $likes )" +
//        	"		) \n" +
//        	"		";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_BEGIN_OF_CONDITION, location.getType());
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect ( \n" +
//        	"			Cheese( ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_START, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect ( \n" +
//        	"			Cheese( type ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//        assertEquals("type", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//
//        input = 
//        	"rule MyRule \n" +
//        	"	when \n" +
//        	"		Class ( property > 0 ) from collect ( \n" +
//        	"			Cheese( type == ";
//        location = LocationDeterminator.getLocationInCondition(input);
//        assertEquals(LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT, location.getType());
//        assertEquals("Cheese", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME));
//        assertEquals("type", location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME));
//    }
    
}
