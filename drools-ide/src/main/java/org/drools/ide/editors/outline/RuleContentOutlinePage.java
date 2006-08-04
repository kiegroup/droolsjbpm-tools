package org.drools.ide.editors.outline;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Simple outline view of a DRL file. At present this is not wired in with the Parser, so it is fault
 * tolerant of incorrect syntax. 
 * Should provide navigation assistance in large rule files.
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

    //the editor that this outline view is linked to.
    private DRLRuleEditor       editor;

    //the "root" node
    private final RuleFileTreeNode ruleFileTreeNode    = new RuleFileTreeNode();
    private Map rules;
//    private Map lineToCharactersMapping;

    ///////////////////////////////////
    // Patterns that the parser uses
    ///////////////////////////////////
    private static final Pattern RULE_PATTERN1 = Pattern.compile(
			"\\n\\s*rule\\s+\"([^\"]+)\"", Pattern.DOTALL);

    private static final Pattern RULE_PATTERN2 = Pattern.compile(
			"\\n\\s*rule\\s+([^\\s;#\"]+)", Pattern.DOTALL);

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
			"\\s*package\\s+([^\\s;#]+);?", Pattern.DOTALL);

	private static final Pattern FUNCTION_PATTERN = Pattern.compile(
			"\\n\\s*function\\s+(\\S+)\\s+(\\S+)\\(.*\\)", Pattern.DOTALL);

	private static final Pattern IMPORT_PATTERN = Pattern.compile(
			"\\n\\s*import\\s+([^\\s;#]+);?", Pattern.DOTALL);

	private static final Pattern EXPANDER_PATTERN = Pattern.compile(
			"\\n\\s*expander\\s+([^\\s;#]+);?", Pattern.DOTALL);

	private static final Pattern GLOBAL_PATTERN = Pattern.compile(
			"\\n\\s*global\\s+(\\S+)\\s+([^\\s;#]+);?", Pattern.DOTALL);

	private static final Pattern QUERY_PATTERN1 = Pattern.compile(
			"\\n\\s*query\\s+\"([^\"]+)\"", Pattern.DOTALL);

	private static final Pattern QUERY_PATTERN2 = Pattern.compile(
			"\\n\\s*query\\s+([^\\s;#\"]+)", Pattern.DOTALL);

    public RuleContentOutlinePage(DRLRuleEditor editor) {
        super();
        this.editor = editor;
    }

    public void createControl(Composite parent) {
        super.createControl( parent );
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider( new WorkbenchContentProvider() );
        viewer.setLabelProvider( new WorkbenchLabelProvider() );

        viewer.setInput( ruleFileTreeNode );
        update();

        // add the listener for navigation of the rule document.
        super.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                Object selectionObj = event.getSelection();
                if ( selectionObj != null && selectionObj instanceof StructuredSelection ) {
                    StructuredSelection sel = (StructuredSelection) selectionObj;
                    OutlineNode node = (OutlineNode) sel.getFirstElement();
                    if ( node != null ) {
                        editor.selectAndReveal( node.getOffset(),
                                                node.getLength() );
                    }
                }
            }
        } );
    }

    /**
     * Updates the outline page.
     */
    public void update() {
        TreeViewer viewer = getTreeViewer();

        if ( viewer != null ) {
            Control control = viewer.getControl();
            if ( control != null && !control.isDisposed() ) {
                PackageTreeNode packageTreeNode = createPackageTreeNode();
                ruleFileTreeNode.setPackageTreeNode( packageTreeNode );
                viewer.refresh();
                control.setRedraw( false );
                viewer.expandToLevel(2);
                control.setRedraw( true );
            }
        }
    }

    /**
     * 
     * @return a PackageTreeNode representing the current state of the 
     * document, populated with all of the package's child elements
     */
    private PackageTreeNode createPackageTreeNode() {
        PackageTreeNode packageTreeNode = new PackageTreeNode();
        String ruleFileContents = getRuleFileContents();
        initRules(ruleFileContents);
        populatePackageTreeNode(packageTreeNode, ruleFileContents);
        return packageTreeNode;
    }

    /**
     * populates the PackageTreeNode with all of its child elements
     * 
     * @param packageTreeNode the node to populate
     */
    public void populatePackageTreeNode(PackageTreeNode packageTreeNode, String ruleFileContents) {
        Matcher matcher = RULE_PATTERN1.matcher(ruleFileContents);
        while (matcher.find()) {
            String ruleName = matcher.group(1);
            RuleDescr ruleDescr = (RuleDescr) rules.get(ruleName);
            if (ruleDescr == null) {
                packageTreeNode.addRule(ruleName, matcher.start(1), matcher.end(1) - matcher.start(1));
            } else {
            	packageTreeNode.addRule(ruleName, matcher.start(1), matcher.end(1) - matcher.start(1), ruleDescr.getAttributes());
            }
        }
        matcher = RULE_PATTERN2.matcher(ruleFileContents);
        while (matcher.find()) {
            String ruleName = matcher.group(1);
            RuleDescr ruleDescr = (RuleDescr) rules.get(ruleName);
            if (ruleDescr == null) {
                packageTreeNode.addRule(ruleName, matcher.start(1), matcher.end(1) - matcher.start(1));
            } else {
            	packageTreeNode.addRule(ruleName, matcher.start(1), matcher.end(1) - matcher.start(1), ruleDescr.getAttributes());
            }
         } 
        matcher = PACKAGE_PATTERN.matcher(ruleFileContents);
        if (matcher.find()) {
            String packageName = matcher.group(1);
            packageTreeNode.setPackageName(packageName);
            packageTreeNode.setOffset(matcher.start(1));
            packageTreeNode.setLength(matcher.end(1) - matcher.start(1));
        }
        matcher = FUNCTION_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String functionName = matcher.group(2);
			packageTreeNode.addFunction(functionName + "()",
					matcher.start(2), matcher.end(2) - matcher.start(2));
		}
		matcher = EXPANDER_PATTERN.matcher(ruleFileContents);
		if (matcher.find()) {
			String expanderName = matcher.group(1);
			packageTreeNode.addExpander(expanderName, 
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = IMPORT_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String importName = matcher.group(1);
			packageTreeNode.addImport(importName, 
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = GLOBAL_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String globalType = matcher.group(1);
			String globalName = matcher.group(2);
			String name = globalName + " : " + globalType;
			packageTreeNode.addGlobal(name, 
					matcher.start(2), matcher.end(2) - matcher.start(2));
		}
		matcher = QUERY_PATTERN1.matcher(ruleFileContents);
		while (matcher.find()) {
			String queryName = matcher.group(1);
			packageTreeNode.addQuery(queryName, 
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = QUERY_PATTERN2.matcher(ruleFileContents);
		while (matcher.find()) {
			String queryName = matcher.group(1);
			packageTreeNode.addQuery(queryName, 
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
    }

    public void initRules(String ruleFileContents) {
    	rules = new HashMap();
    	PackageDescr packageDescr = getPackageDescr(ruleFileContents);
    	if (packageDescr != null) {
    		Iterator iterator = packageDescr.getRules().iterator();
    		while (iterator.hasNext()) {
    			RuleDescr ruleDescr = (RuleDescr) iterator.next();
    			rules.put(ruleDescr.getName(), ruleDescr);
    		}
    	}
    }
    
	private PackageDescr getPackageDescr(String ruleFileContents) {
		if (editor.getEditorInput() instanceof IFileEditorInput) {
			try {
	            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
	            ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
	            IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
	            if (file.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
	                IJavaProject project = JavaCore.create(file.getProject());
	                newLoader = ProjectClassLoader.getProjectClassLoader(project);
	            }
	            
	            Reader dslReader = DSLAdapter.getDSLContent(ruleFileContents, file);
	            
	            try {
	                Thread.currentThread().setContextClassLoader(newLoader);

	                DrlParser parser = new DrlParser();
	                
	                PackageDescr packageDescr = null;
	                if (dslReader == null) {
	                	packageDescr = parser.parse(ruleFileContents);
	                } else {
	                	packageDescr = parser.parse(ruleFileContents, dslReader);
	                }

                	return packageDescr;
	            } catch (Exception t) {
	                throw t;
	            } finally {
	                Thread.currentThread().setContextClassLoader(oldLoader);
	            }
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return null;
	}
	
	/**
     * 
     * @return the current contents of the document
     */
    private String getRuleFileContents() {
        IDocumentProvider documentProvider = editor.getDocumentProvider();
        IEditorInput editorInput = editor.getEditorInput();
        IDocument document = documentProvider.getDocument( editorInput );
        String ruleFileContents = document.get();
        return ruleFileContents;
    }
}