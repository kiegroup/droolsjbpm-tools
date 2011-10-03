/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.editors;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.flow.bpmn2.editor.BPMNModelEditor;
import org.drools.eclipse.flow.ruleflow.core.RuleSetNodeWrapper;
import org.drools.lang.descr.AttributeDescr;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.jbpm.workflow.core.node.RuleSetNode;

/**
 * hyperlink to ruleflow-group declaration.
 */
public class RuleFlowGroupHyperlink implements IHyperlink {

	AttributeDescr descr;
	ProcessInfo processInfo;
	List<RuleSetNode> nodes;
	
	public RuleFlowGroupHyperlink(AttributeDescr descr, ProcessInfo processInfo, List<RuleSetNode> nodes) {
		this.descr = descr;
		this.processInfo = processInfo;
		this.nodes = nodes;
	}

	public IRegion getHyperlinkRegion() {
		return new Region(descr.getStartCharacter(),descr.getEndCharacter()-descr.getStartCharacter());
	}

	public String getTypeLabel() {
		return "drools-" + descr.getType(); //$NON-NLS-1$;
	}

	public String getHyperlinkText() {
		return "Open " + processInfo.getProcessId();
	}

	public void open() {
		IResource resource = DroolsEclipsePlugin.getDefault().findProcessResource(processInfo.getProcessId());
		if(resource!=null) {
			try {
		        IWorkbench workbench = PlatformUI.getWorkbench();
		        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                IEditorPart editor = IDE.openEditor(page, (IFile) resource);
                if (editor instanceof FormEditor) {
                    editor = ((FormEditor) editor).getActiveEditor();
                }
                if (editor instanceof BPMNModelEditor) {
                	EditPart editPart = (EditPart) editor.getAdapter(EditPart.class);
                	List<EditPart> selectedParts = new ArrayList<EditPart>();
                	for (RuleSetNode node : nodes) {
                		EditPart part = findEditPart(editPart,node);
                		if(part!=null)
                			selectedParts.add(part);
                	}
                	if(selectedParts.size()>0)
                		((BPMNModelEditor)editor).getSite().getSelectionProvider().setSelection(new StructuredSelection(selectedParts));
        	
                }
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
            }
        }
	}
	
	private EditPart findEditPart(EditPart root, RuleSetNode node) {
		for (Object element : root.getChildren()) {
			if (((EditPart)element).getModel() instanceof RuleSetNodeWrapper) {
				if(node.getId() == ((RuleSetNodeWrapper)(((EditPart)element).getModel())).getNode().getId()) {
					return (EditPart) element;
				}
			} else {
				EditPart result = findEditPart(((EditPart)element), node);
				if(result!=null)
					return result;
			}
		}
		return null;
	} 
}
