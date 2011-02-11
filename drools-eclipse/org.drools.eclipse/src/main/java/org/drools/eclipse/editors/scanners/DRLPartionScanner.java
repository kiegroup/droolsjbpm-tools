/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
