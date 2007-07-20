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
        input =
        	"rule MyRule \n" +
        	"  when \n" +
        	"    Class( condition == true ) \n" +
        	"    ";
        rule = parseRuleString(input);
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

        input =
        	"rule MyRule \n" +
	    	"  when \n" +
	    	"    class: Class( condition == true, condition2 == null ) \n" +
	    	"    ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(pattern.getEndCharacter() != -1);

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
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertTrue(pattern.getEndCharacter() != -1);

        input =
			"rule MyRule \n" +
			"  when \n" +
			"    class:";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertNull(pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());

        input =
			"rule MyRule \n" +
			"  when \n" +
			"    class: Cl";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertEquals("Cl", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

        input =
			"rule MyRule \n" +
			"  when \n" +
			"    class:Cl";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("class", pattern.getIdentifier());
        assertEquals("Cl", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

        /** Inside of condition: start */
        input =
			"rule MyRule \n" +
			"  when \n" +
			"    Class (";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertTrue(pattern.getEndLine() == -1 && pattern.getEndColumn() == -1);
        assertEquals(0, pattern.getDescrs().size());
        assertEquals(-1, pattern.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( na";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name['xyz'].subname.subsubn";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals( "name['xyz'].subname.subsubn", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals(-1, field.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( c : condition, ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr fieldBinding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals(-1, fieldBinding.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( condition == true, na";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(2, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals(-1, field.getEndCharacter());
        assertEquals( "condition", field.getFieldName() );
        field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals( "na", field.getFieldName() );
        assertEquals(-1, field.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name:";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr binding1 = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding1.getIdentifier());
        assertNull(binding1.getFieldName());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(0, field.getRestrictions().size());
        assertEquals(-1, field.getEndCharacter());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name: property ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        FieldBindingDescr binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( name1: property1 == \"value1\", name2: property2 ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(3, pattern.getDescrs().size());
        binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name1", binding.getIdentifier());
        assertEquals("property1", binding.getFieldName());
        field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals("property1", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        LiteralRestrictionDescr literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals("==", literal.getEvaluator());
        assertEquals("value1", literal.getText());
        binding = (FieldBindingDescr) pattern.getDescrs().get(2);
        assertEquals("name2", binding.getIdentifier());
        assertEquals("property2", binding.getFieldName());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class(name:property==";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(2, pattern.getDescrs().size());
        binding = (FieldBindingDescr) pattern.getDescrs().get(0);
        assertEquals("name", binding.getIdentifier());
        assertEquals("property", binding.getFieldName());
        field = (FieldConstraintDescr) pattern.getDescrs().get(1);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class( property == otherPropertyN";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
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
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
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
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
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
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
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

        input =
            "rule MyRule \n" +
            "   when \n" +
            "       eval ( ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        EvalDescr eval = (EvalDescr) rule.getLhs().getDescrs().get(0);
        assertEquals(input.indexOf( "eval" ), eval.getStartCharacter());
        assertEquals(-1, eval.getEndCharacter());

        input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( property > 0 & ";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        assertEquals(-1, pattern.getEndCharacter());
        assertEquals(1, pattern.getDescrs().size());
        field = (FieldConstraintDescr) pattern.getDescrs().get(0);
        assertEquals("property", field.getFieldName());
        assertEquals(1, field.getRestrictions().size());
        literal = (LiteralRestrictionDescr) field.getRestrictions().get(0);
        assertEquals(">", literal.getEvaluator());
        assertEquals("0", literal.getText());
        RestrictionConnectiveDescr connective = (RestrictionConnectiveDescr) field.getRestriction();
        assertEquals(RestrictionConnectiveDescr.AND, connective.getConnective());

        input =
            "rule MyRule \n" +
            "   when \n" +
            "       Class ( ) from a";
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        assertEquals("Class", pattern.getObjectType());
        FromDescr from = (FromDescr) pattern.getSource();
        assertEquals(-1, from.getEndCharacter());
        assertTrue(pattern.getEndCharacter() != -1);

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from myGlobal.getList() \n" +
        	"       ";
        rule = parseRuleString(input);
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        from = (FromDescr) pattern.getSource();
        assertTrue(from.getEndCharacter() != -1);

        input =
        	"rule MyRule \n" +
        	"	when \n" +
        	"		Class ( property > 0 ) from getDroolsFunction() \n" +
        	"       ";
        rule = parseRuleString(input);
        rule = parseRuleString(input);
        assertEquals(1, rule.getLhs().getDescrs().size());
        pattern = (PatternDescr) rule.getLhs().getDescrs().get(0);
        from = (FromDescr) pattern.getSource();
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
