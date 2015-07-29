package org.kie.eclipse.navigator.preferences;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.content.OrganizationNode;
import org.kie.eclipse.navigator.view.content.ProjectNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.navigator.view.content.ServerNode;
import org.kie.eclipse.server.IKieResourceHandler;

@SuppressWarnings("restriction")
public abstract class AbstractKiePropertyPage extends FieldEditorPropertyPage implements IKieConstants {

	private final static String INITIAL_PAGE_ID = "org.kie.eclipse.navgiator.initialPageId";
	/**
	 * The KIE Resource Handler for the current selection
	 */
	private IKieResourceHandler resourceHandler;
	private IContentNode<?> contentNode;

	public AbstractKiePropertyPage() {
	}

	public AbstractKiePropertyPage(int style) {
		super(style);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		PropertyDialog pd = (PropertyDialog)getContainer();
		Object data = pd.getTreeViewer().getData(INITIAL_PAGE_ID);
		if (data==null) {
			String id = getInitialPageId();
			if (id!=null) {
				pd.setCurrentPageId(id);
				pd.getTreeViewer().setData(INITIAL_PAGE_ID, id);
			}
		}
		return super.createContents(parent);
	}

	@Override
	public void dispose() {
		PropertyDialog pd = (PropertyDialog)getContainer();
		pd.getTreeViewer().getTree().deselectAll();
		super.dispose();
	}

	protected IContentNode<?> getContentNode() {
		if (contentNode==null) {
			PropertyDialog pd = (PropertyDialog) getContainer();
			IStructuredSelection selection = (IStructuredSelection) pd.getSelection();
			TreeSelection ts = (TreeSelection) ((IStructuredSelection) selection).getFirstElement();
			contentNode = (IContentNode<?>) ts.getFirstElement();
		}
		return contentNode;
	}

	protected abstract Class<? extends IKieResourceHandler> getResourceHandlerType();
	
	protected IKieResourceHandler getResourceHandler() {
		if (resourceHandler==null) {
			IContentNode<?> node = getContentNode();
			IKieResourceHandler h = node.getHandler();
			while (h!=null) {
				if (h.getClass()==getResourceHandlerType()) {
					resourceHandler = h;
					break;
				}
				h = h.getParent();
			}
		}
		return resourceHandler;
	}
	
	@Override
	protected String getPreferenceName(String name) {
		setPreferenceStore(getPreferenceStore());
		return resourceHandler.getPreferenceName(name);
	}

	@Override
	protected String getInitialPageId() {
		IContentNode<?> node = getContentNode();
		if (node instanceof ServerNode)
			return "org.kie.eclipse.navigator.serverPropertyPage";
		if (node instanceof OrganizationNode)
			return "org.kie.eclipse.navigator.organizationPropertyPage";
		if (node instanceof RepositoryNode)
			return "org.kie.eclipse.navigator.repositoryPropertyPage";
		if (node instanceof ProjectNode)
			return "org.kie.eclipse.navigator.projectPropertyPage";
		return null;
	}
}
