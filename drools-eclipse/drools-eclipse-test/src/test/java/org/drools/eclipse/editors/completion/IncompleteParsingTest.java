package org.drools.eclipse.editors.completion;

import java.util.List;

import junit.framework.TestCase;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.EvalDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.FieldConstraintDescr;
import org.drools.lang.descr.FromDescr;
import org.drools.lang.descr.LiteralRestrictionDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RestrictionConnectiveDescr;
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
         *   PatternDescr [objectType = "Class"]
         *     FieldConstraintDescr [fieldName = "condition"]
         *       LiteralRestrictionDescr [evaluator = "==", text = "true"]
         */
    }


    public void testParsingColumn1() {
        String input =
        	"rule MyRule \n" +
        	"  when \n" +
        	"    Class( condition == true ) \n" +
        	"    ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertTrue(pattern.getEndLine() != -1 || pattern.getEndColumn() != -1);
        assertEquals(1, pattern.getDescrs().size());
        assertEquals(input.indexOf( "Class" ), pattern.getStartCharacter());
        assertEquals(input.indexOf( "(" ), pattern.getLeftParentCharacter());
        assertEquals(input.indexOf( ")" ), pattern.getRightParentCharacter());
        assertEquals(input.indexOf( ")" ), pattern.getEndCharacter());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("condition", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr restriction = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", restriction.getEvaluator());
        assertEquals("true", restriction.getText());
    }


    public void testParsingColumn2() {
        String input =

        	"rule MyRule \n" +
	    	"  when \n" +
	    	"    class: Class( condition == true, condition2 == null ) \n" +
	    	"    ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(pattern.getEndCharacter() != -1);
    }


    public void testParsingColumn3() {
        String input =
	    	"rule MyRule \n" +
	    	"  when \n" +
	    	"    Cl";
        RuleDescr rule = parseRuleString(input);
        assertEquals(0, rule.getLhs().getDescrs().size());

    }


    public void testParsingColumn4() {
        String input =
	    	"rule MyRule \n" +
	    	"  when \n" +
	    	"    Class( condition == true ) \n" +
	    	"    Cl";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(pattern.getEndCharacter() != -1);

    }


    public void testParsingColumn5() {
        String input =
			"rule MyRule \n" +
			"  when \n" +
			"    class:";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertNull(pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());

    }


    public void testParsingColumn6() {
        String input =
			"rule MyRule \n" +
			"  when \n" +
			"    class: Cl";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertEquals("Cl", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

    }


    public void testParsingColumn7() {
        String input =
			"rule MyRule \n" +
			"  when \n" +
			"    class:Cl";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertEquals("Cl", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

    }


    public void testParsingColumn8() {
        /** Inside of condition: start */
        String input =
			"rule MyRule \n" +
			"  when \n" +
			"    Class (";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

    }


    public void testParsingColumn9() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( na";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

    }


    public void testParsingColumn10() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name['xyz'].subname.subsubn";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals( "name['xyz'].subname.subsubn", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

    }


    public void testParsingColumn11() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals(-1, field.getEndCharacter());

    }


    public void testParsingColumn12() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( c : condition, ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr fieldBinding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals(-1, fieldBinding.getEndCharacter());

    }


    public void testParsingColumn13() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, na";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(2, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals(-1, field.getEndCharacter());
        assertEquals( "condition", field.getFieldName() );
         field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

    }


    public void FAILINGtestParsingColumn14() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr binding1 = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding1.getIdentifier());
        assertNull(binding1.getFieldName());

    }


    public void testParsingColumn15() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(0, field.getRestrictions().size());
        assertEquals(-1, field.getEndCharacter());

    }


    public void testParsingColumn16() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());

    }


    public void testParsingColumn17() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1: property1 == \"value1\", name2: property2 ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(3, pattern.getDescrs().size());
        FieldBindingDescr binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name1", binding.getIdentifier());
        assertEquals("property1", binding.getFieldName());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals("property1", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", literal.getEvaluator());
        assertEquals("value1", literal.getText());
        binding = (FieldBindingDescr) pattern.getDescrs().get(2);
        assertEquals("name2", binding.getIdentifier());
        assertEquals("property2", binding.getFieldName());

    }


    public void testParsingColumn18() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class(name:property==";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(2, pattern.getDescrs().size());
        FieldBindingDescr binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());

    }


    public void testParsingColumn19() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property == otherPropertyN";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        VariableRestrictionDescr variable = (VariableRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", variable.getEvaluator());
        assertEquals("otherPropertyN", variable.getIdentifier());
        assertEquals(-1, field.getEndCharacter());
    }


    public void testParsingColumn20() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property == \"someth";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        // KRISV: for now, it would be really messy to make this work. String is a
        // lexer rule (not parser), and changing that or controling the behavior of it
        // is not simple. Can we leave the way it is for now?
        //
        // TODO literal should be a LiteralRestrictionDescr with filled in evaluator and text, not null
        // assertEquals("==", literal.getEvaluator());
        // assertEquals("someth", literal.getText());
        // TODO this method does not yet exist
        // assertEquals(-1, field.getEndCharacter());

    }


    public void testParsingColumn21() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property contains ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
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
    }


    public void testParsingColumn22() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property matches \"someth";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        // KRISV: see comments above
        //
        // TODO literal should be a LiteralRestrictionDescr with filled in evaluator and text, not null
        // assertEquals("matches", literal.getEvaluator());
        // assertEquals("someth", literal.getText());
        // TODO this method does not yet exist
        // assertEquals(-1, field.getEndCharacter());
    }


    public void testParsingColumn23() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       eval ( ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        EvalDescr eval = (EvalDescr) rule.getLhs().getDescrs().get(0);
        assertEquals(input.indexOf( "eval" ), eval.getStartCharacter());
        assertEquals(-1, eval.getEndCharacter());
    }


    public void testParsingColumn24() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( property > 0 & ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldConstraintDescr field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals(">", literal.getEvaluator());
        assertEquals("0", literal.getText());
        RestrictionConnectiveDescr connective = (RestrictionConnectiveDescr) field.getRestriction();
        assertEquals(RestrictionConnectiveDescr.AND, connective.getConnective());
    }


    public void testParsingColumn25() {
        String input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( ) from a";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        FromDescr from = (FromDescr) pattern.getSource();
        assertEquals(-1, from.getEndCharacter());
        assertTrue(pattern.getEndCharacter() != -1);
    }


    public void testParsingColumn26() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.getList() \n" +
        	"       ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        FromDescr from = (FromDescr) pattern.getSource();
        assertTrue(from.getEndCharacter() != -1);
    }


    public void testParsingColumn27() {
        String input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from getDroolsFunction() \n" +
        	"       ";
        RuleDescr rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        FromDescr from = (FromDescr) pattern.getSource();
        assertTrue(from.getEndCharacter() != -1);
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
        PatternDescr pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals(input.indexOf( "Class" ), pattern.getStartCharacter());
        assertEquals(input.indexOf( "(" ), pattern.getLeftParentCharacter());
        assertEquals(input.indexOf( ")" ), pattern.getRightParentCharacter());
        assertEquals(input.indexOf( ")" ), pattern.getEndCharacter());
    }
}
