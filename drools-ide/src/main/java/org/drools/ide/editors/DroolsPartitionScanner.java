package org.drools.ide.editors;

import org.eclipse.jface.text.rules.*;

/**
 * The Drools partition scanner.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsPartitionScanner extends RuleBasedPartitionScanner {
	public final static String DEFAULT = "__default";
	public final static String COMMENT = "__comment";
	public final static String TAG = "__tag";

	public DroolsPartitionScanner() {

		IToken xmlComment = new Token(COMMENT);
		IToken tag = new Token(TAG);

		IPredicateRule[] rules = new IPredicateRule[2];

		rules[0] = new MultiLineRule("<!--", "-->", xmlComment);
		rules[1] = new TagRule(tag);

		setPredicateRules(rules);
	}
}
