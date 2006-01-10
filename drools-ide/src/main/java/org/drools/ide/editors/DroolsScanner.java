package org.drools.ide.editors;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;

/**
 * The Drools scanner.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsScanner extends RuleBasedScanner {

	public DroolsScanner(ColorManager manager) {
		IRule[] rules = new IRule[1];
		// Add generic whitespace rule.
		rules[0] = new WhitespaceRule(new DroolsWhitespaceDetector());
		setRules(rules);
	}
}
