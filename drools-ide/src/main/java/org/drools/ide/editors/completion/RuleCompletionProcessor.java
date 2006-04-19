package org.drools.ide.editors.completion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.drools.lang.descr.PackageDescr;
import org.drools.semantics.java.ClassTypeResolver;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
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
	                list.add(new RuleCompletionProposal(prefix.length(), "modify", "modify();", 7));
	                list.add(new RuleCompletionProposal(prefix.length(), "retract", "retract();", 8));
	                list.add(new RuleCompletionProposal(prefix.length(), "assert", "assert();", 7));
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
	            Image image = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	            if (!adapter.hasConditions()) {
	            	RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "exists", "exists ");
	            	prop.setImage(image);
	                list.add(prop);
	                prop = new RuleCompletionProposal(prefix.length(), "not", "not ");
	            	prop.setImage(image);
	                list.add(prop);
	                prop = new RuleCompletionProposal(prefix.length(), "and", "and ");
	            	prop.setImage(image);
	                list.add(prop);
	                prop = new RuleCompletionProposal(prefix.length(), "or", "or ");
	            	prop.setImage(image);
	                list.add(prop);
	                prop = new RuleCompletionProposal(prefix.length(), "eval", "eval()", 5);
	            	prop.setImage(image);
	                list.add(prop);
	            }
	            RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "then", "then\n\t");
            	prop.setImage(image);
	            list.add(prop);
	            
	            String content = viewer.getDocument().get();
	            Reader dslReader = DSLAdapter.getDSLContent(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
	            DrlParser parser = new DrlParser();
	            PackageDescr descr = DroolsBuilder.parsePackage(content, parser, dslReader);
	            List imports = descr.getImports();
	            Iterator iterator = imports.iterator();
	            while (iterator.hasNext()) {
		            String name = (String) iterator.next();
		            int index = name.lastIndexOf(".");
		            if (index != -1) {
		            	String className = name.substring(index + 1);
		            	prop = new RuleCompletionProposal(prefix.length(), className, className + "()", className.length() + 1);
		            	prop.setPriority(-1);
		            	prop.setImage(DroolsPluginImages.getImage(DroolsPluginImages.CLASS));
		            	list.add(prop);
		            }
	            }
//	            if (true) {
//	            	String className = "";
//	            	ClassTypeResolver resolver = new ClassTypeResolver(imports);
//	            	Class conditionClass = resolver.resolveType(className);
//	            	ClassFieldInspector inspector = new ClassFieldInspector(conditionClass);
//	            	Map fields = inspector.getFieldNames();
//	            	Iterator iterator2 = fields.keySet().iterator();
//	            	while (iterator2.hasNext()) {
//	            		String varName = (String) iterator2.next();
//	            		list.add(new RuleCompletionProposal(prefix.length(), varName, varName + " "));
//	            	}
//	            }
	            
	            filterProposalsOnPrefix(prefix, list);
	        } else {             
	            //we are in rule header
	            list.add(new RuleCompletionProposal(prefix.length(), "salience", "salience "));
	            list.add(new RuleCompletionProposal(prefix.length(), "no-loop", "no-loop "));
	            list.add(new RuleCompletionProposal(prefix.length(), "agenda-group", "agenda-group "));
	            list.add(new RuleCompletionProposal(prefix.length(), "duration", "duration "));            
	            list.add(new RuleCompletionProposal(prefix.length(), "auto-focus", "auto-focus "));            
	            list.add(new RuleCompletionProposal(prefix.length(), "when", "when\n\t "));
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
