package org.drools.eclipse.debug;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.flow.common.editor.ProcessExtension;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPartFactory;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementFigure;
import org.drools.process.core.Process;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Kris Verlaenen
 *  
 */
public class ProcessInstanceViewer extends ViewPart implements ISelectionListener {

    private CTabFolder tabFolder;
    private Map<String, ProcessInstanceTabItem> processInstanceTabItems = new HashMap<String, ProcessInstanceTabItem>();

    public ProcessInstanceViewer() {
    }

    public void createPartControl(Composite parent) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        tabFolder = new CTabFolder(parent, SWT.CLOSE);
        tabFolder.setBorderVisible(true);
        GridData gridData = new GridData();
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        tabFolder.setLayoutData(gridData);
    }

    public void setFocus() {
        // Do nothing
    }
    
    private class ProcessInstanceTabItem {
        
        private GraphicalViewer graphicalViewer;
        private CTabItem tabItem;
        private String projectName;
        
        public ProcessInstanceTabItem(CTabFolder folder, String processInstanceId, String processId, List<String> nodeIds, String projectName) {
        	this.projectName = projectName;
            tabItem = new CTabItem(folder, SWT.NONE);
            ProcessInfo processInfo = DroolsEclipsePlugin.getDefault().getProcessInfo(processId);
            if (processInfo == null) {
                throw new IllegalArgumentException(
                    "Could not find process with id " + processId);
            }
            tabItem.setText(processInstanceId + " = " + processInfo.getProcess().getName() + "[" + processInfo.getProcessId() + "]");

            Composite composite = new Composite(folder, SWT.NONE);
            composite.setLayout(new FillLayout());
            tabItem.setControl(composite);
            
            graphicalViewer = new ScrollingGraphicalViewer();
            graphicalViewer.createControl(composite);
            graphicalViewer.getControl().setBackground(ColorConstants.listBackground);
            graphicalViewer.setRootEditPart(new ScalableRootEditPart());
            IJavaProject javaProject = getJavaProject(projectName);
            
            IExtensionRegistry reg = Platform.getExtensionRegistry();
            IConfigurationElement[] extensions =
                reg.getConfigurationElementsFor(
                    "org.drools.eclipse.processExtension");
            for (IConfigurationElement element: extensions) {
                try {
                    ProcessExtension processExtension = (ProcessExtension)
                        element.createExecutableExtension("className");
                    Process process = processInfo.getProcess();
                    if (processExtension.acceptsProcess(process.getType())) {
                        ProcessEditPartFactory editPartFactory = processExtension.getProcessEditPartFactory();
                        editPartFactory.setProject(javaProject);
                        graphicalViewer.setEditPartFactory(editPartFactory);
                        ProcessWrapper processWrapper = processExtension.getProcessWrapperBuilder().getProcessWrapper(process, javaProject);
                        graphicalViewer.setContents(processWrapper);
                        break;
                    }
                } catch (CoreException e) {
                    DroolsEclipsePlugin.log(e);
                }
            }

            for (String nodeId: nodeIds) {
            	handleNodeInstanceSelection(nodeId);
            }
            folder.setSelection(tabItem);
        }
        
        private void handleNodeInstanceSelection(String nodeId) {
            boolean found = false;
            Iterator iterator = ((ProcessEditPart) graphicalViewer.getContents()).getChildren().iterator();
            while (iterator.hasNext()) {
                ElementEditPart elementEditPart = (ElementEditPart) iterator.next();
                if (((ElementWrapper) elementEditPart.getModel()).getId().equals(nodeId)) {
                    ((ElementFigure) elementEditPart.getFigure()).setSelected(true);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(
                    "Could not find node with id " + nodeId);
            }
        }
        
        public CTabItem getTabItem() {
            return tabItem;
        }
    }

    public void showProcessInstance(String processInstanceId, String processId, List nodeIds, String projectName) {
        processInstanceTabItems.put(processInstanceId,
            new ProcessInstanceTabItem(tabFolder, processInstanceId, processId, nodeIds, projectName));
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // Do nothing
    }
    
    private IJavaProject getJavaProject(String projectName) {
    	if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
				if (project != null) {
					try {
						if (project.getNature("org.eclipse.jdt.core.javanature") != null) {
			                IJavaProject javaProject = JavaCore.create(project);
			                if (javaProject.exists()){
			                	return javaProject;
			                }
			            }
					} catch (CoreException e) {
						DroolsEclipsePlugin.log(e);
					}
				}
			}
		}
    	return null;
    }

}

