package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.ide.editors.DSLAdapter;
import org.drools.ide.editors.Keywords;
import org.eclipse.jface.text.ITextViewer;



/**
 * For handling withing consequences, including DSLs.
 * 
 *        
 *TODO: make this "look back" in the viewer to get context,
 *rather then rely on partitioning. Then can
 *provide more relevant pop ups. 
 * 
 * @author Michael Neale
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

    static final Pattern condition = Pattern.compile(".*\\Wwhen\\W.*", Pattern.DOTALL);

    static final Pattern consequence = Pattern.compile(".*\\Wthen\\W.*", Pattern.DOTALL);

    
    
    protected List getPossibleProposals(ITextViewer viewer, String backText) {

        List list = new ArrayList();
        
        //now load from DSL
        DSLAdapter adapter = new DSLAdapter(viewer);
        
        if (consequence.matcher(backText).matches()) {
            list.addAll(adapter.loadConsequenceItems());
            list.add(new RuleCompletionProposal("end"));
            list.add(new RuleCompletionProposal("modify"));
            list.add(new RuleCompletionProposal("retract"));
            list.add(new RuleCompletionProposal("assert"));
        } else if (condition.matcher(backText).matches()) {
            list.addAll(adapter.loadConditionItems());
            list.add(new RuleCompletionProposal("then", "then\n\t"));
        } else {             
            //we are in rule header
            list.add(new RuleCompletionProposal("salience"));
            list.add(new RuleCompletionProposal("when", "when\n\t"));
        }
        
        return list;           
    }

	


}
