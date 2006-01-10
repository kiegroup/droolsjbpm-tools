package org.drools.ide.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

/**
 * The Drools tag scanner.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsTagScanner extends RuleBasedScanner {

	public DroolsTagScanner(ColorManager manager) {
		IToken string =
			new Token(
				new TextAttribute(manager.getColor(IDroolsColorConstants.STRING)));

		IRule[] rules = new IRule[3];

		// Add rule for double quotes
		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("'", "'", string, '\\');
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new DroolsWhitespaceDetector());

		setRules(rules);
	}
}
