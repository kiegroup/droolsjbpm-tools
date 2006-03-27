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
            if (!adapter.hasConsequences()) {
                list.add(new RuleCompletionProposal("modify", "modify( );"));
                list.add(new RuleCompletionProposal("retract", "retract( );"));
                list.add(new RuleCompletionProposal("assert", "assert( );"));
            }
        } else if (condition.matcher(backText).matches()) {
            list.addAll(adapter.listConditionItems());
            if (!adapter.hasConditions()) {
                list.add( new RuleCompletionProposal("exists") );
                list.add( new RuleCompletionProposal("not") );
                list.add( new RuleCompletionProposal("and") );
                list.add( new RuleCompletionProposal("or") );
                list.add( new RuleCompletionProposal("eval", "eval(   )") );
            }
            list.add(new RuleCompletionProposal("then", "then\n\t"));
        } else {             
            //we are in rule header
            list.add(new RuleCompletionProposal("salience"));
            list.add(new RuleCompletionProposal("no-loop"));
            list.add(new RuleCompletionProposal("agenda-group"));
            list.add(new RuleCompletionProposal("duration"));            
            list.add(new RuleCompletionProposal("when", "when\n\t"));
        }
        
        return list;           
    }

    /** 
     * Lazily get the adapter for DSLs, and cache it with the editor for future reference.
     * If it is unable to load a DSL, it will try again next time.
     * But once it has found and loaded one, it will keep it until the editor is closed.
     * 
     * This delegates to DSLAdapter to poke around the project to try and load the DSL.
     */
    private DSLAdapter getDSLAdapter(ITextViewer viewer, DRLRuleEditor editor) {
        DSLAdapter adapter = editor.getDSLAdapter();
        if (adapter == null) {
            String content = viewer.getDocument().get();
            adapter = new DSLAdapter(content, ((FileEditorInput) editor.getEditorInput()).getFile());
            if (adapter.isValid()) {
                editor.setDSLAdapter( adapter );
            }
        }
        return adapter;
    }

	


}
