package org.drools.ide.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * This is very much nothing more than a stubbed up starting place at
 * this point...
 * 
 * @author "Jeff Brown" <brown_j@ociweb.com>
 */
public class RuleContentOutlinePage extends ContentOutlinePage {

	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());

		viewer.setInput(getTreeStuff());
		viewer.expandAll();
	}

	private Object getTreeStuff() {
		DummyNode dummyNode = new DummyNode("Root");
		DummyNode rule1 = new DummyNode("Rule 1");
		dummyNode.addChild(rule1);
		rule1.addChild(new DummyNode("Some Rule Detail..."));
		rule1.addChild(new DummyNode("Some Rule Detail..."));
		DummyNode rule2 = new DummyNode("Rule 2");
		dummyNode.addChild(rule2);
		rule2.addChild(new DummyNode("Some Rule Detail..."));
		DummyNode rule3 = new DummyNode("Rule 3");
		dummyNode.addChild(rule3);
		rule3.addChild(new DummyNode("Some Rule Detail..."));
		rule3.addChild(new DummyNode("Some Rule Detail..."));
		rule3.addChild(new DummyNode("Some Rule Detail..."));
		rule3.addChild(new DummyNode("Some Rule Detail..."));
		rule3.addChild(new DummyNode("Some Rule Detail..."));
		return dummyNode;
	}
}

class DummyNode implements IWorkbenchAdapter, IAdaptable {

	private Object parent;

	private String label;

	private List children = new ArrayList();

	DummyNode(String label) {
		this.label = label;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return null;
	}

	public void setParent(Object o) {
		parent = o;
	}

	public void addChild(DummyNode o) {
		o.setParent(this);
		children.add(o);
	}

	public Object[] getChildren(Object o) {
		return children.toArray();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		return label;
	}

	public Object getParent(Object o) {
		return parent;
	}
}
