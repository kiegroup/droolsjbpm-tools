package org.drools.ide.editors;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.editors.completion.RuleCompletionProposal;
import org.eclipse.jface.text.ITextViewer;

/**
 * This will connect to the rule parser and get the 
 * grammar from the domain specific language.
 * 
 * @author Michael Neale
 * TODO: Wire this in to the parser, possibly. 
 */
public class DSLAdapter {

    private ITextViewer viewer;
    
    public DSLAdapter(ITextViewer viewer) {
        this.viewer = viewer;
    }
    
    public List loadConditionItems() {
        //TODO: This is where we hook it in.
        //may need to know what DSL is from the expand statement
        //as given by the parser.
        //OR: could parse it out of the document without compiler...
        //but probably want to eventually get bound items anyway.
        
        List list = new ArrayList();
        list.add(new RuleCompletionProposal("Cart contains {item}"));
        list.add(new RuleCompletionProposal("There exists {Person} with name {name} and age of {age}"));
        list.add(new RuleCompletionProposal("The date of {event} is before {cutoff}"));
        return list;
    }
    
    public List loadConsequenceItems() {
        //TODO: This is where we hook it in.
        //may need to know what DSL is from the expand statement
        //as given by the parser.
        //OR: could parse it out of the document without compiler...
        //but probably want to eventually get bound items anyway.
        
        List list = new ArrayList();
        list.add(new RuleCompletionProposal("Change status to {status}"));
        list.add(new RuleCompletionProposal("Send notification to {person} with message {message}"));
        list.add(new RuleCompletionProposal("Set {field} of {variable} to {value}"));
        return list;
    }    
    
}
