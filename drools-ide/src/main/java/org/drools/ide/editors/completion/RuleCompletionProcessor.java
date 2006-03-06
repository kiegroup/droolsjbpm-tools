package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.part.FileEditorInput;



/**
 * For handling within rules, including DSLs.
 * At present this provides a fixed list, plus what is available
 * in the DSL configuration. 
 * 
 * TODO: This can be enhanced to look back for declarations, and introspect to get
 * field names. (More can be done as well, this would just be the first step).
 * 
 * This also handles queries, as they are just a type of rule essentially.
 * 
 * @author Michael Neale
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

    static final Pattern condition = Pattern.compile(".*\\Wwhen\\W.*", Pattern.DOTALL);

    static final Pattern consequence = Pattern.compile(".*\\Wthen\\W.*", Pattern.DOTALL);
    static final Pattern query = Pattern.compile(".*\\Wquery\\W.*", Pattern.DOTALL);

    private DRLRuleEditor editor;
    
    public RuleCompletionProcessor(DRLRuleEditor editor) {
        this.editor = editor;
    }
    
    protected List getPossibleProposals(ITextViewer viewer, String backText) {

        List list = new ArrayList();
        DSLAdapter adapter = getDSLAdapter(viewer, editor);
        
        if (query.matcher( backText ).matches()) {
            list.addAll(adapter.listConditionItems());
        } else if (consequence.matcher(backText).matches()) {
            list.addAll(adapter.listConsequenceItems());
            list.add(new RuleCompletionProposal("end"));
            list.add(new RuleCompletionProposal("modify"));
            list.add(new RuleCompletionProposal("retract"));
            list.add(new RuleCompletionProposal("assert"));
        } else if (condition.matcher(backText).matches()) {
            list.addAll(adapter.listConditionItems());
            list.add(new RuleCompletionProposal("then", "then\n\t"));
        } else {             
            //we are in rule header
            list.add(new RuleCompletionProposal("salience"));
            list.add(new RuleCompletionProposal("when", "when\n\t"));
        }
        
        return list;           
    }

    /** 
     * Get the adapter for DSLs, and cache it with the editor for future reference.
     * If it is unable to load a DSL, it will try again next time.
     * But once it has found and loaded one, it will keep it until the editor is closed.
     *  
     */
    private DSLAdapter getDSLAdapter(ITextViewer viewer, DRLRuleEditor editor) {
        DSLAdapter adapter = editor.getDSLAdapter();
        if (adapter == null) {
            String content = viewer.getDocument().get();
            adapter = new DSLAdapter(content, (FileEditorInput) editor.getEditorInput());
            if (adapter.isValid()) {
                editor.setDSLAdapter( adapter );
            }
        }
        return adapter;
    }

	


}
