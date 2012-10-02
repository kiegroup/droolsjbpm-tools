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

import java.util.List;
import java.util.Map;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.PatternDescr;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.jbpm.workflow.core.node.RuleSetNode;

/**
 * DRL element hyperlink detector.
 */
public class DRLHyperlinkDetector implements IHyperlinkDetector {

	private DRLRuleEditor editor;

	public DRLHyperlinkDetector(final AbstractRuleEditor editor) {
		this.editor = (DRLRuleEditor) editor.getAdapter(DRLRuleEditor.class);
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
	
		IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}		
		
		final BaseDescr descr = editor.getDescr(region.getOffset());
		if(descr instanceof AttributeDescr) {	
			return createHyperlinks((AttributeDescr) descr);
		} else if(descr instanceof PatternDescr) {
			return createHyperlinks((PatternDescr) descr, region);
		}
		
		return null;
	}
	
	protected IHyperlink[] createHyperlinks(AttributeDescr descr) {
		if(((AttributeDescr)descr).getName().equals("ruleflow-group")) {
			Map<ProcessInfo, List<RuleSetNode>> nodes = DroolsEclipsePlugin.getDefault().getRuleSetNodeByFlowGroup(descr.getValue());
			if(nodes.size()>0) {
				IHyperlink[] result = new IHyperlink[nodes.size()];
				int i = 0;
				for (ProcessInfo processInfo : nodes.keySet()) {
					result[i++] = new RuleFlowGroupHyperlink(descr,processInfo, nodes.get(processInfo));
				}
				return result;
			}
		} 
		return null;
	}
	
	protected IHyperlink[] createHyperlinks(PatternDescr descr, IRegion region) {
		int start = descr.getStartCharacter();
		if(descr.getIdentifier()!=null) {
			String source = editor.getContent().substring(descr.getStartCharacter() + descr.getIdentifier().length(), descr.getEndCharacter());
			start = start + descr.getIdentifier().length() + source.indexOf(descr.getObjectType());
		}
		int end = start + descr.getObjectType().length();
		if (region.getOffset() >= start && region.getOffset() <= end)  {
			for (String type : editor.getImports()){
				if(descr.getObjectType().equals(type.substring(type.lastIndexOf('.')+1))) {
					
					return new IHyperlink[] { new ObjectTypeHyperlinkDetector(descr,editor.getResource().getProject(), type, new Region(start, descr.getObjectType().length()))};		
				}
			}		
		} 
		return null;
	}

}
