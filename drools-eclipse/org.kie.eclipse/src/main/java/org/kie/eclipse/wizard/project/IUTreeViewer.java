package org.kie.eclipse.wizard.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class IUTreeViewer extends CheckboxTreeViewer {
    
	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
	
	class IUTreeItem {
    	final private IInstallableUnit iu;
    	final private IUTreeViewer.IUTreeItem parent;
    	final private IUTreeViewer.IUTreeContentProvider provider;
    	private Collection<IUTreeViewer.IUTreeItem> children;
    	private boolean checked;
    	
    	public IUTreeItem(IUTreeViewer.IUTreeContentProvider provider, IUTreeViewer.IUTreeItem parent, IInstallableUnit iu) {
    		this.provider = provider;
    		this.parent = parent;
    		this.iu = iu;
    		checked = false;
    	}
    	
    	public boolean isChecked() {
    		if (parent==null) {
    			for (IUTreeViewer.IUTreeItem child : getChildren()) {
    				if (child.isChecked())
    					return true;
    			}
    			return false;
    		}
    		return checked;
    	}
    	
    	public boolean isGrayed() {
    		if (parent==null) {
    			int countChecked = 0;
    			for (IUTreeViewer.IUTreeItem child : getChildren()) {
    				if (child.isChecked())
    					++countChecked;
    			}
    			return countChecked>0 && countChecked!=children.size();
    		}
    		return false;
    	}

    	public void setChecked(boolean checked) {
    		this.checked = checked;
    	}
    	
    	public IUTreeViewer.IUTreeItem getParent() {
    		return parent;
    	}
    	
		public Collection<IUTreeViewer.IUTreeItem> getChildren() {
			if (children==null) {
		    	children = new ArrayList<IUTreeViewer.IUTreeItem>();
				if (parent==null) {
					for (IRequirement r : iu.getRequirements()) {
						for (IInstallableUnit riu : provider.getInstallableUnits()) {
							if (r.isMatch(riu)) {
								IMatchExpression<IInstallableUnit> me = r.getFilter();
								if (me!=null) {
									if (me.isMatch(riu))
										children.add(new IUTreeItem(provider, this, riu));
								}
								else
									children.add(new IUTreeItem(provider, this, riu));
							}
						}
					}
				}
			}
			return children;
		}
		
		public String getName() {
			if (parent==null)
				return iu.getProperty(IInstallableUnit.PROP_NAME);	
			return iu.getProperty(IInstallableUnit.PROP_NAME) + "  (v" + iu.getVersion() + ")";	
		}

		public void setSubtreeChecked(boolean checked) {
			this.checked = checked;
			if (parent==null) {
				for (IUTreeItem child : getChildren()) {
					child.checked = checked;
				}
			}
		}

		public int getSubtreeCheckedCount() {
			if (parent==null) {
				int count = 0;
				for (IUTreeItem child : getChildren()) {
					count += child.checked ? 1 : 0;
				}
				return count;
			}
			return checked ? 1 : 0;
		}
    }
    
    class IUTreeContentProvider implements ITreeContentProvider {
    	Collection<IUTreeViewer.IUTreeItem> elements = new ArrayList<IUTreeViewer.IUTreeItem>();
    	Collection<IInstallableUnit> installableUnits = new ArrayList<IInstallableUnit>();
    	
		@Override
		public void dispose() {
		}

		public Collection<IInstallableUnit> getInstallableUnits() {
			return installableUnits;
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			elements.clear();
			installableUnits.clear();
			if  (newInput instanceof IQueryResult) {
				for (Object o : (IQueryResult<?>)newInput) {
					if (o instanceof IInstallableUnit) {
						IInstallableUnit iu = (IInstallableUnit) o;
						installableUnits.add(iu);
						if (isGroup(iu)) {
							elements.add(new IUTreeItem(this, null, iu));
						}
					}
				}
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return elements.toArray(new Object[elements.size()]);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IUTreeViewer.IUTreeItem) {
				Collection<IUTreeViewer.IUTreeItem> children = ((IUTreeViewer.IUTreeItem)parentElement).getChildren();
				return children.toArray(new Object[children.size()]);
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IUTreeViewer.IUTreeItem)
				return ((IUTreeViewer.IUTreeItem)element).getParent();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IUTreeViewer.IUTreeItem)
				return isGroup(((IUTreeViewer.IUTreeItem)element).iu);
			return false;
		}
		
		public boolean isGroup(IInstallableUnit iu) {
			String isGroup = iu==null ? null : iu.getProperty("org.eclipse.equinox.p2.type.group");
			return Boolean.valueOf(isGroup);
		}
    }
    
    class IUTreeLabelProvider implements ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IUTreeViewer.IUTreeItem)
				return ((IUTreeViewer.IUTreeItem)element).getName();
			return element.toString();
		}
    }
    
    class IUTreeCheckStateListener implements ICheckStateListener {
    	
    	private IUTreeViewer treeViewer;
    	public IUTreeCheckStateListener(IUTreeViewer treeViewer) {
    		this.treeViewer = treeViewer;
    	}
    	
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			boolean checked = event.getChecked();
			Object element = event.getElement();
			if (element instanceof IUTreeItem) {
				IUTreeItem item = (IUTreeItem)element;
				updateDescendents(item, checked);
				updateAncestors(item.getParent(), checked);
				
				if (item.parent!=null)
					item = item.parent;
				
				for (Object e : treeViewer.getElements()) {
					if (e instanceof IUTreeItem) {
						if (e != item)
							updateDescendents((IUTreeItem)e, false);
					}
				}
			}
		}
		
		void updateDescendents(IUTreeItem item, boolean checked) {
			for (IUTreeItem child : item.getChildren()) {
				updateDescendents(child,checked);
			}
			item.setSubtreeChecked(checked);
			treeViewer.setSubtreeChecked(item, checked);
			
			treeViewer.setChecked(item, checked);
			treeViewer.setGrayed(item, false);
		}
		
		void updateAncestors(IUTreeItem item, boolean checked) {
			while (item!=null) {
				int checkedCount = item.getSubtreeCheckedCount();
				int size = item.getChildren().size();
				if (checkedCount==0) {
					treeViewer.setChecked(item, false);
					item.setChecked(false);
					checked = true;
				}
				else if (checkedCount==size) {
					treeViewer.setChecked(item, true);
					treeViewer.setGrayed(item, false);
					item.setChecked(true);
				}
				else {
					treeViewer.setGrayChecked(item, true);
					item.setChecked(true);
				}
				item = item.getParent();
			}
		}
	};

    public IUTreeViewer(Composite parent, int style) {
		super(parent, style);
		final Tree tree = getTree();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 3 * tree.getItemHeight();
		tree.setLayoutData(gd);
	}
    
    public void initialize() {
		setContentProvider(new IUTreeContentProvider());
		setCheckStateProvider(new ICheckStateProvider() {

			@Override
			public boolean isChecked(Object element) {
				if (element instanceof IUTreeViewer.IUTreeItem)
					return ((IUTreeViewer.IUTreeItem)element).isChecked();
				return false;
			}

			@Override
			public boolean isGrayed(Object element) {
				if (element instanceof IUTreeViewer.IUTreeItem)
					return ((IUTreeViewer.IUTreeItem)element).isGrayed();
				return false;
			}
			
		});
		setLabelProvider(new IUTreeLabelProvider());
		
		final Tree tree = getTree();

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tree.getSelectionCount()==1) {
					IUTreeItem item = (IUTreeItem) tree.getSelection()[0].getData();
					e.data = item.iu;
					for (SelectionListener l : selectionListeners) {
						l.widgetSelected(e);
					}
				}
			}
		});

		addCheckStateListener(new IUTreeCheckStateListener(this));
    }
    
    /**
     * Returns only the installable units that are features, ignoring feature groups.
     * 
     * @return
     */
    public List<IInstallableUnit> getSelectedIUs(){
    	List<IInstallableUnit> result = new ArrayList<IInstallableUnit>();
    	for (Object o : getCheckedElements()) {
    		if (o instanceof IUTreeItem) {
    			IUTreeItem item = (IUTreeItem) o;
	    		if (item.parent!=null)
	    			result.add(item.iu);
    		}
    	}
    	return result;
    }
    
    public Object[] getElements() {
    	return ((IUTreeContentProvider)getContentProvider()).getElements(null);
    }
    
    public void addSelectionListener(SelectionListener listener) {
    	if (!selectionListeners.contains(listener))
    		selectionListeners.add(listener);
    }
    
    public void removeSelectionListener(SelectionListener listener) {
    	if (selectionListeners.contains(listener))
    		selectionListeners.remove(listener);
    }
    
    public void dispose() {
    	if (getTree()!=null && !getTree().isDisposed()) {
    		getTree().dispose();
    	}
    }
}