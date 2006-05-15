package org.drools.ide.editors.completion;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.PackageDescr;
import org.eclipse.core.runtime.CoreException;
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
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

    private static final Pattern condition = Pattern.compile(".*\\Wwhen\\W.*", Pattern.DOTALL);
    private static final Pattern consequence = Pattern.compile(".*\\Wthen\\W.*", Pattern.DOTALL);
    private static final Pattern query = Pattern.compile(".*\\Wquery\\W.*", Pattern.DOTALL);
    private static final Image droolsIcon = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
    private static final Image dslIcon = DroolsPluginImages.getImage( DroolsPluginImages.DSL_EXPRESSION );
    private static final Image methodIcon = DroolsPluginImages.getImage(DroolsPluginImages.METHOD);
    private static final Image classIcon = DroolsPluginImages.getImage(DroolsPluginImages.CLASS);
    
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
	        	List dslConsequences = adapter.listConsequenceItems();
                addDSLProposals( list,
                                 prefix,
                                 dslConsequences );
	            if (!adapter.hasConsequences()) {

	            	
                    addRHSCompletionProposals( list,
                                               prefix );                    
        			
        			addRHSFunctionCompletionProposals( viewer,
                                                       list,
                                                       prefix );
	            }
	        } else if (condition.matcher(backText).matches()) {
	        	List dslConditions = adapter.listConditionItems();
	        	Iterator iterator;
	        	addDSLProposals( list,
                                 prefix,
                                 dslConditions );
	            Image droolsIcon = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
	            if (!adapter.hasConditions()) {
	            	list.add( new RuleCompletionProposal(prefix.length(), "exists", "exists ", droolsIcon));
	                list.add( new RuleCompletionProposal(prefix.length(), "not", "not ", droolsIcon));
	                list.add( new RuleCompletionProposal(prefix.length(), "and", "and ", droolsIcon));
	                list.add( new RuleCompletionProposal(prefix.length(), "or", "or ", droolsIcon));
	                RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "eval", "eval()", 5);
	            	prop.setImage(droolsIcon);
	                list.add(prop);

	                List imports = getImports(viewer);
		            iterator = imports.iterator();
		            while (iterator.hasNext()) {
			            String name = (String) iterator.next();
			            int index = name.lastIndexOf(".");
			            if (index != -1) {
			            	String className = name.substring(index + 1);
			            	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), className, className + "()", className.length() + 1);
			            	p.setPriority(-1);
			            	p.setImage(classIcon);
			            	list.add(p);
			            }
		            }
		            
	            }
	            RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "then", "then\n\t");
            	prop.setImage(droolsIcon);
	            list.add(prop);
	            
	            
	        } else {             
	            //we are in rule header
	            addRuleHeaderItems( list,
                                    prefix );
	        }
            
            filterProposalsOnPrefix(prefix, list);
	        return list;           
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
    }

    private void addRHSFunctionCompletionProposals(ITextViewer viewer,
                                                   final List list,
                                                   final String prefix) throws CoreException,
                                                                       DroolsParserException {
        Iterator iterator;
        RuleCompletionProposal prop;
        List functions = getFunctions(viewer);
        iterator = functions.iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next() + "()";
        	prop = new RuleCompletionProposal(prefix.length(), name, name + ";", name.length() - 1);
        	prop.setPriority(-1);
        	prop.setImage(methodIcon);
        	list.add(prop);
        }
    }

    private void addRHSCompletionProposals(final List list,
                                           final String prefix) {
        RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "modify", "modify();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "retract", "retract();", 8);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assert", "assert();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assertLogical", "assertLogical();", 14);
        prop.setImage(droolsIcon);
        list.add(prop);
    }

    private void addRuleHeaderItems(final List list,
                                    final String prefix) {
        list.add(new RuleCompletionProposal(prefix.length(), "salience", "salience ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "no-loop", "no-loop ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "agenda-group", "agenda-group ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "duration", "duration ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "auto-focus", "auto-focus ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "when", "when\n\t ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "xor-group", "xor-group ", droolsIcon));        
    }

    private void addDSLProposals(final List list,
                                 final String prefix,
                                 List dslItems) {
        Iterator iterator = dslItems.iterator();
        while (iterator.hasNext()) {
        	String consequence = (String) iterator.next();
            RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), consequence);
            p.setImage( dslIcon );
            list.add(p);
        }
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
//    	return new DSLAdapter(viewer.getDocument().get(), ((FileEditorInput) getEditor().getEditorInput()).getFile());
        DSLAdapter adapter = getDRLEditor().getDSLAdapter();
        if (adapter == null) {
            String content = viewer.getDocument().get();
            adapter = new DSLAdapter(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
            if (adapter.isValid()) {
            	getDRLEditor().setDSLAdapter(adapter);
            }
        }
        return adapter;
    }

	private List getImports(ITextViewer viewer) throws CoreException, DroolsParserException {
		List imports = getDRLEditor().getImports();
        if (imports == null) {
            loadImportsAndFunctions(viewer);
            imports = getDRLEditor().getFunctions();
        }
        return imports;
	}
	
	private void loadImportsAndFunctions(ITextViewer viewer) throws CoreException, DroolsParserException {
		String content = viewer.getDocument().get();
        Reader dslReader = DSLAdapter.getDSLContent(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
        DrlParser parser = new DrlParser();
        PackageDescr descr = DroolsBuilder.parsePackage(content, parser, dslReader);
        // imports
        getDRLEditor().setImports(descr.getImports());
        // functions
        List functionDescrs = descr.getFunctions();
        List functions = new ArrayList(functionDescrs.size());
        Iterator iterator = functionDescrs.iterator();
        while (iterator.hasNext()) {
			functions.add(((FunctionDescr) iterator.next()).getName());
		}
        getDRLEditor().setFunctions(functions);
	}

	private List getFunctions(ITextViewer viewer) throws CoreException, DroolsParserException {
		List functions = getDRLEditor().getFunctions();
        if (functions == null) {
            loadImportsAndFunctions(viewer);
            functions = getDRLEditor().getFunctions();
        }
        return functions;
	}

}
