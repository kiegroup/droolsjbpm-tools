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
package org.drools.eclipse.editors.hyperlinks;

import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.TypeDeclarationDescr;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * hyperlink to object type declaration.
 */
public class ObjectTypeHyperlinkDetector implements IHyperlink {

	PatternDescr descr;
	IProject project;
	String type;
	IRegion region;

	public ObjectTypeHyperlinkDetector(PatternDescr descr, IProject project, String type, IRegion region) {
		this.descr = descr;
		this.project = project;
		this.type = type;
		this.region = region;
	}

	public IRegion getHyperlinkRegion() {
 		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return null;
	}

	public void open() {
		if (type != null) {
			try {
				IJavaProject javaProject = JavaCore.create(project);
				IType javaElement = javaProject.findType(type, (IProgressMonitor)null);
				if(javaElement != null) {
					JavaUI.openInEditor(javaElement, true, true);
				} else {
					String namespace = type.substring(0, type.lastIndexOf('.'));
			    	for (DRLInfo drlInfo : DroolsEclipsePlugin.getDefault().getAllDRLInfo() ) {
						if(drlInfo.getPackageDescr().getNamespace().equals(namespace)){
							for (TypeDeclarationDescr typeDeclarationDescr : drlInfo.getPackageDescr().getTypeDeclarations()) {
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				                IEditorPart editor = IDE.openEditor(page, project.getFile(drlInfo.getSourcePathName()));
				                DRLRuleEditor drlRuleEditor = (DRLRuleEditor) editor.getAdapter(DRLRuleEditor.class);
				                if(drlRuleEditor != null)
				                	drlRuleEditor.selectAndReveal(typeDeclarationDescr.getStartCharacter(), typeDeclarationDescr.getTypeName().length());
				                return;
							}
						}
					}
				}
			} catch (CoreException ex) {
				DroolsEclipsePlugin.log(ex);
			}
		}
	}
}
