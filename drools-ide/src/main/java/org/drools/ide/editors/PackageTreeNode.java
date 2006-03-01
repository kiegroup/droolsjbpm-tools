package org.drools.ide.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class PackageTreeNode
    implements
    IWorkbenchAdapter,
    IAdaptable {

    private List   rules       = new ArrayList();

    private String packageName = "<unknown package name>";

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void addRule(String ruleName) {
        rules.add( new RuleTreeNode( this,
                                     ruleName ) );
    }

    public Object[] getChildren(Object o) {
        return rules.toArray();
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    public String getLabel(Object o) {
        return packageName;
    }

    public Object getParent(Object o) {
        return null;
    }

    public Object getAdapter(Class adapter) {
        if ( adapter == IWorkbenchAdapter.class ) {
            return this;
        }
        return null;
    }
}
