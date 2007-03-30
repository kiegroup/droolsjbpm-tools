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
 * The job of the partitioner is to identify if the cursor position is in a rule block, or not.
 * When in a rule block, the RuleCompletion processor will scan to work out a finer grain of context
 * (such as if it is in a condition, or consequence block).
 * 
 * @author Michael Neale
 */
public class DRLPartionScanner extends RuleBasedPartitionScanner {

    
    public static final String RULE_PART_CONTENT = "__partition_rule_content";
    public static final String RULE_COMMENT = "__partition_multiline_comment";
    
    
    public static final String[] LEGAL_CONTENT_TYPES = {IDocument.DEFAULT_CONTENT_TYPE,  RULE_PART_CONTENT, RULE_COMMENT};
    
    
    public DRLPartionScanner() {
        super();
        initialise();
    }
    
    private void initialise() {

        IToken rulePartition = new Token(RULE_PART_CONTENT);
        IToken comment = new Token(RULE_COMMENT);
        
        List rules = new ArrayList();
        rules.add( new MultiLineRule("/*", "*/", comment, (char) 0, true));
        rules.add(new MultiLineRule("\nrule", "\nend", rulePartition));
        
        //a query is really just a rule for most purposes.
        rules.add(new MultiLineRule("\nquery", "\nend", rulePartition));
        
        IPredicateRule[] rulez = new IPredicateRule[rules.size()];
        rules.toArray(rulez);
        setPredicateRules(rulez);
    }
}
