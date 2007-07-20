package org.drools.eclipse.editors.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


/**
 * Break apart the rule source, very very simply.
 *
 * The job of the partitioner is to identify if the cursor position
 * is in a rule block, or not.  Comments are also generated as a
 * separate partition.
 *  TODO: add support for dialect based partitioning for correct syntaxhighlighting
 * @author Michael Neale
 */
public class DRLPartionScanner extends RuleBasedPartitionScanner {

    public static final String RULE_PART_CONTENT = "__partition_rule_content";
    public static final String RULE_COMMENT = "__partition_multiline_comment";

    public static final String[] LEGAL_CONTENT_TYPES = {
    	IDocument.DEFAULT_CONTENT_TYPE,
    	RULE_PART_CONTENT,
    	RULE_COMMENT
    };

    public DRLPartionScanner() {
        initialise();
    }

    private void initialise() {
        List rules = new ArrayList();

        // rules
        IToken rulePartition = new Token(RULE_PART_CONTENT);
        rules.add(new MultiLineRule("\nrule", "\nend", rulePartition));
        //a query is really just a rule for most purposes.
        rules.add(new MultiLineRule("\nquery", "\nend", rulePartition));

        // comments
        IToken comment = new Token(RULE_COMMENT);
        rules.add( new MultiLineRule("/*", "*/", comment, (char) 0, true));

        setPredicateRules((IPredicateRule[]) rules.toArray(new IPredicateRule[rules.size()]));
    }
}
