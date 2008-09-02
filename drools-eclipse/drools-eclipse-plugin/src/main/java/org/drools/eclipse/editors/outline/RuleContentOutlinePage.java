package org.drools.eclipse.editors.outline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.DroolsModelBuilder;
import org.drools.eclipse.core.Package;
import org.drools.eclipse.core.RuleSet;
import org.drools.eclipse.core.ui.DroolsContentProvider;
import org.drools.eclipse.core.ui.DroolsLabelProvider;
import org.drools.eclipse.core.ui.DroolsTreeSorter;
import org.drools.eclipse.core.ui.FilterActionGroup;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Simple outline view of a DRL file. At present this is not wired in with the Parser, so it is fault
 * tolerant of incorrect syntax. 
 * Should provide navigation assistance in large rule files.
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

    private AbstractRuleEditor editor;
    private RuleSet ruleSet = DroolsModelBuilder.createRuleSet();
    private Map rules;

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
			"\\n\\s*function\\s+(\\S+)\\s+(\\S+)\\(.*?\\)", Pattern.DOTALL);

	private static final Pattern TEMPLATE_PATTERN = Pattern.compile(
			"\\n\\s*template\\s+([^\\s;#\"]+)", Pattern.DOTALL);

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

    public RuleContentOutlinePage(AbstractRuleEditor editor) {
        this.editor = editor;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new DroolsContentProvider());
        viewer.setLabelProvider(new DroolsLabelProvider());
        viewer.setSorter(new DroolsTreeSorter());
        viewer.setInput(ruleSet);
        FilterActionGroup filterActionGroup = new FilterActionGroup(
    		viewer, "org.drools.eclipse.editors.outline.RuleContentOutlinePage");
		filterActionGroup.fillActionBars(getSite().getActionBars());
        update();

        // add the listener for navigation of the rule document.
        super.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                Object selectionObj = event.getSelection();
                if (selectionObj != null && selectionObj instanceof StructuredSelection) {
                    StructuredSelection sel = (StructuredSelection) selectionObj;
                    DroolsElement element = (DroolsElement) sel.getFirstElement();
                    if (element != null) {
                        editor.selectAndReveal(element.getOffset(),
                                                element.getLength());
                    }
                }
            }
        });
    }

    /**
     * Updates the outline page.
     */
    public void update() {
        TreeViewer viewer = getTreeViewer();
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
            	initRules();
            	populatePackageTreeNode();
            	viewer.refresh();
                control.setRedraw(false);
                viewer.expandToLevel(2);
                control.setRedraw(true);
            }
        }
    }

    /**
     * populates the PackageTreeNode with all of its child elements
     * 
     * @param packageTreeNode the node to populate
     */
    public void populatePackageTreeNode() {
    	String ruleFileContents = editor.getContent();
    	populatePackageTreeNode(ruleFileContents);
    }
    
    void populatePackageTreeNode(String ruleFileContents) {
    	DroolsModelBuilder.clearRuleSet(ruleSet);
    	Matcher matcher = PACKAGE_PATTERN.matcher(ruleFileContents);
    	String packageName = null;
    	int startChar = 0;
    	int endChar = 0; 
        if (matcher.find()) {
            packageName = matcher.group(1);
            startChar = matcher.start(1);
            endChar = matcher.end(1);
        }
        Package pkg = DroolsModelBuilder.addPackage(ruleSet, packageName,
    		startChar, endChar - startChar);

        matcher = RULE_PATTERN1.matcher(ruleFileContents);
        while (matcher.find()) {
            String ruleName = matcher.group(1);
            DroolsModelBuilder.addRule(pkg, ruleName, null,
        		matcher.start(1), matcher.end(1) - matcher.start(1),
        		extractAttributes((RuleDescr) rules.get(ruleName)));
        }
        matcher = RULE_PATTERN2.matcher(ruleFileContents);
        while (matcher.find()) {
            String ruleName = matcher.group(1);
            DroolsModelBuilder.addRule(pkg, ruleName, null,
        		matcher.start(1), matcher.end(1) - matcher.start(1),
        		extractAttributes((RuleDescr) rules.get(ruleName)));
         } 
        matcher = FUNCTION_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String functionName = matcher.group(2);
			DroolsModelBuilder.addFunction(pkg, functionName + "()", null,
				matcher.start(2), matcher.end(2) - matcher.start(2));
		}
		matcher = EXPANDER_PATTERN.matcher(ruleFileContents);
		if (matcher.find()) {
			String expanderName = matcher.group(1);
			DroolsModelBuilder.addExpander(pkg, expanderName, null,
				matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = IMPORT_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String importName = matcher.group(1);
			DroolsModelBuilder.addImport(pkg, importName, null,
				matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = GLOBAL_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String globalType = matcher.group(1);
			String globalName = matcher.group(2);
			String name = globalName + " : " + globalType;
			DroolsModelBuilder.addGlobal(pkg, name, null,
				matcher.start(2), matcher.end(2) - matcher.start(2));
		}
		matcher = QUERY_PATTERN1.matcher(ruleFileContents);
		while (matcher.find()) {
			String queryName = matcher.group(1);
			DroolsModelBuilder.addQuery(pkg, queryName, null,
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = QUERY_PATTERN2.matcher(ruleFileContents);
		while (matcher.find()) {
			String queryName = matcher.group(1);
			DroolsModelBuilder.addQuery(pkg, queryName, null,
				matcher.start(1), matcher.end(1) - matcher.start(1));
		}
		matcher = TEMPLATE_PATTERN.matcher(ruleFileContents);
		while (matcher.find()) {
			String templateName = matcher.group(1);
			DroolsModelBuilder.addTemplate(pkg, templateName, null,
					matcher.start(1), matcher.end(1) - matcher.start(1));
		}
    }
    
    RuleSet getRuleSet() {
    	return ruleSet;
    }
    
    private Map extractAttributes(RuleDescr ruleDescr) {
        Map attributes = null;
        if (ruleDescr != null) {
        	attributes = new HashMap();
        	for (Iterator iterator = ruleDescr.getAttributes().iterator(); iterator.hasNext();) {
        		AttributeDescr attribute = (AttributeDescr) iterator.next();
        		if (attribute != null && attribute.getName() != null) {
        			attributes.put(attribute.getName(), attribute.getValue());
        		}
        	}
        }
        return attributes;
    }

    public void initRules() {
    	rules = new HashMap();
    	try {
    		DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(editor, true, false);
    		if (drlInfo != null) {
		    	PackageDescr packageDescr = drlInfo.getPackageDescr();
		    	if (packageDescr != null) {
		    		for (Iterator iterator = packageDescr.getRules().iterator(); iterator.hasNext(); ) {
		    			RuleDescr ruleDescr = (RuleDescr) iterator.next();
		    			rules.put(ruleDescr.getName(), ruleDescr);
		    		}	
		    	}
    		}
    	} catch (DroolsParserException e) {
    		DroolsEclipsePlugin.log(e);
    	}
    }
    
}