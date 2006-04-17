package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.eclipse.jface.text.IDocument;
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

    private static final Pattern condition = Pattern.compile(".*\\Wwhen\\W.*", Pattern.DOTALL);
    private static final Pattern consequence = Pattern.compile(".*\\Wthen\\W.*", Pattern.DOTALL);
    private static final Pattern query = Pattern.compile(".*\\Wquery\\W.*", Pattern.DOTALL);

    public RuleCompletionProcessor(DRLRuleEditor editor) {
    	super(editor);
    }
    
    protected DRLRuleEditor getDRLEditor() {
    	return (DRLRuleEditor) getEditor();
    }
    
	protected List getCompletionProposals(ITextViewer viewer, final int documentOffset) {
        try {
	        final List list = new ArrayList();
	        DSLAdapter adapter = getDSLAdapter(viewer);
	        
	        IDocument doc = viewer.getDocument();
	        String backText = readBackwards(documentOffset, doc);            
	
	        final String prefix = stripWhiteSpace(backText);
	        
	        if (query.matcher(backText).matches()) {
	            list.addAll(adapter.listConditionItems());
	        } else if (consequence.matcher(backText).matches()) {
	            list.addAll(adapter.listConsequenceItems());
	            if (!adapter.hasConsequences()) {
	                list.add(new RuleCompletionProposal(prefix.length(), "modify", "modify( );"));
	                list.add(new RuleCompletionProposal(prefix.length(), "retract", "retract( );"));
	                list.add(new RuleCompletionProposal(prefix.length(), "assert", "assert( );"));
	                filterProposalsOnPrefix(prefix, list);
	
//	                IEditorInput input = getEditor().getEditorInput();
//	        		if (input instanceof IFileEditorInput) {
//	        			IProject project = ((IFileEditorInput) input).getFile().getProject();
//	        			IJavaProject javaProject = JavaCore.create(project);
//	        			
//	        			CompletionRequestor requestor = new CompletionRequestor() {
//	        				public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
//	        					String display = new String(proposal.getCompletion());
//	        					String completion = display;
//	        					if (prefix.lastIndexOf(".") != -1) {
//	        						completion = prefix.substring(0, prefix.lastIndexOf(".") + 1) + completion;
//	        					}
//	        					System.out.println(completion);
//	        					list.add(new RuleCompletionProposal(documentOffset, prefix.length(), display, completion));
//	        				}
//	        			};
//	
//	        			try {
//	        				javaProject.newEvaluationContext().codeComplete(backText, backText.length(), requestor);
//	        			} catch (Throwable t) {
//	        				DroolsIDEPlugin.log(t);
//	        			}
//	        		}
	            }
	        } else if (condition.matcher(backText).matches()) {
	            list.addAll(adapter.listConditionItems());
	            if (!adapter.hasConditions()) {
	                list.add(new RuleCompletionProposal(prefix.length(), "exists"));
	                list.add(new RuleCompletionProposal(prefix.length(), "not"));
	                list.add(new RuleCompletionProposal(prefix.length(), "and"));
	                list.add(new RuleCompletionProposal(prefix.length(), "or"));
	                list.add(new RuleCompletionProposal(prefix.length(), "eval", "eval(   )"));
	            }
	            list.add(new RuleCompletionProposal(prefix.length(), "then", "then\n\t"));
	            filterProposalsOnPrefix(prefix, list);
	        } else {             
	            //we are in rule header
	            list.add(new RuleCompletionProposal(prefix.length(), "salience"));
	            list.add(new RuleCompletionProposal(prefix.length(), "no-loop"));
	            list.add(new RuleCompletionProposal(prefix.length(), "agenda-group"));
	            list.add(new RuleCompletionProposal(prefix.length(), "duration"));            
	            list.add(new RuleCompletionProposal(prefix.length(), "auto-focus"));            
	            list.add(new RuleCompletionProposal(prefix.length(), "when", "when\n\t"));
	            filterProposalsOnPrefix(prefix, list);
	        }
	        return list;           
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
    }

    /** 
     * Lazily get the adapter for DSLs, and cache it with the editor for future reference.
     * If it is unable to load a DSL, it will try again next time.
     * But once it has found and loaded one, it will keep it until the editor is closed.
     * 
     * This delegates to DSLAdapter to poke around the project to try and load the DSL.
     */
    private DSLAdapter getDSLAdapter(ITextViewer viewer) {
    	// TODO: cache DSL adapter in plugin, and reset when dsl file saved
    	// retrieve dsl name always (might have changed) and try retrieving
    	// cached dsl from plugin first
    	return new DSLAdapter(viewer.getDocument().get(), ((FileEditorInput) getEditor().getEditorInput()).getFile());
//        DSLAdapter adapter = getDRLEditor().getDSLAdapter();
//        if (adapter == null) {
//            String content = viewer.getDocument().get();
//            adapter = new DSLAdapter(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
//            if (adapter.isValid()) {
//            	getDRLEditor().setDSLAdapter(adapter);
//            }
//        }
//        return adapter;
    }

	


}
