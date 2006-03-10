package org.drools.ide.editors.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This represents a package in the outline view. 
 * Each rule resource must have a package declaration.
 * 
 * @author Jeff Brown
 */
public class PackageTreeNode extends OutlineNode {

    private List   rules       = new ArrayList();

    private List   functions   = new ArrayList();

    private List   expanders   = new ArrayList();

    private List   imports   = new ArrayList();

    private List   globals   = new ArrayList();

    private List   queries   = new ArrayList();

    private String packageName = "<unknown package name>";

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** Add a rule node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addRule(String ruleName,
                        int offset,
                        int length) {
        RuleTreeNode node = new RuleTreeNode( this,
                                              ruleName );
        node.setOffset( offset );
        node.setLength( length );
        rules.add( node );
    }

    /** Add a function node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addFunction(String functionLabel,
                            int offset,
                            int length) {
        FunctionTreeNode node = new FunctionTreeNode( this,
                                                      functionLabel );
        node.setOffset( offset );
        node.setLength( length );
        functions.add( node );
    }

    /** Add an expander node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addExpander(String expanderLabel,
                            int offset,
                            int length) {
        ExpanderTreeNode node = new ExpanderTreeNode( this, expanderLabel );
        node.setOffset( offset );
        node.setLength( length );
        expanders.add( node );
    }

    /** Add an import node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addImport(String importLabel,
                          int offset,
                          int length) {
        ImportTreeNode node = new ImportTreeNode( this, importLabel );
        node.setOffset( offset );
        node.setLength( length );
        imports.add( node );
    }

    /** Add a global node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addGlobal(String globalLabel,
                          int offset,
                          int length) {
        GlobalTreeNode node = new GlobalTreeNode( this, globalLabel );
        node.setOffset( offset );
        node.setLength( length );
        globals.add( node );
    }

    /** Add a query node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addQuery(String queryLabel,
                          int offset,
                          int length) {
        QueryTreeNode node = new QueryTreeNode( this, queryLabel );
        node.setOffset( offset );
        node.setLength( length );
        queries.add( node );
    }

    /** 
     * This will return the child nodes, as they are to be displayed on screen (sorted an all !) 
     * Rules should appear at the top, sorted, as they are the most important assets.
     * */
    public Object[] getChildren(Object o) {
        List children = new ArrayList();

        //sort and add rules
        Collections.sort( rules );
        children.addAll( rules );

        //sort and add queries
        Collections.sort( queries );
        children.addAll( queries );

        // sort and add globals
        Collections.sort( globals );
        children.addAll( globals );

        // sort and add functions
        Collections.sort( functions );
        children.addAll( functions );

        // sort and add expanders
        Collections.sort( expanders );
        children.addAll( expanders );

        // sort and add imports
        Collections.sort( imports );
        children.addAll( imports );

        return children.toArray();
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        ImageDescriptor des = DroolsIDEPlugin.getImageDescriptor( "icons/package_obj.gif" );
        return des;

    }

    public String getLabel(Object o) {
        return packageName;
    }

    public Object getParent(Object o) {
        return null;
    }

}
