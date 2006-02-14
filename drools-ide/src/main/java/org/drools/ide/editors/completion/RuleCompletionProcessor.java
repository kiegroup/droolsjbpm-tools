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

    static final Pattern condition = Pattern.compile(".*\\W" 
                                                + Keywords.getInstance().lookup("when") 
                                                + "\\W.*", Pattern.DOTALL);

    static final Pattern consequence = Pattern.compile(".*\\W" + Keywords.getInstance().lookup("then") 
                                                + "\\W.*", Pattern.DOTALL);

    
    
    protected List getPossibleProposals(ITextViewer viewer, String backText) {

        Keywords keys = Keywords.getInstance();
        List list = new ArrayList();
        
        //now load from DSL
        DSLAdapter adapter = new DSLAdapter(viewer);
        if (consequence.matcher(backText).matches()) {
            list.addAll(adapter.loadConsequenceItems());
            list.add(keys.lookup("end"));
            list.add(keys.lookup("modify"));
            list.add(keys.lookup("retract"));
            list.add(keys.lookup("assert"));

            
        } else if (condition.matcher(backText).matches()) {
            list.addAll(adapter.loadConditionItems());
            list.add(keys.lookup("then"));

        } else { 
            
            //some defaults
            list.add(keys.lookup("salience"));
            list.add(keys.lookup("when"));

        }
        
        

        
        return list;           
    }

	


}
