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

import java.util.List;
import java.util.Map;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.jbpm.workflow.core.node.RuleSetNode;

/**
 * DRL element hyperlink detector.
 */
public class DRLHyperlinkDetector implements IHyperlinkDetector {

	private AbstractRuleEditor editor;

	public DRLHyperlinkDetector(final AbstractRuleEditor editor) {
		this.editor = editor;
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
	if (region == null || textViewer == null) {
		return null;
	}

	IDocument document = textViewer.getDocument();
	if (document == null) {
		return null;
	}

	final BaseDescr descr = getDescr(region.getOffset());
	if(descr instanceof AttributeDescr) {	
		return createHyperlinks((AttributeDescr) descr);
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
	
	private BaseDescr getDescr(int offset) {
		try {
			DRLInfo info = DroolsEclipsePlugin.getDefault().parseResource(
					editor, true, false);
			return DescrUtil.getDescr(info.getPackageDescr(), offset);
		} catch (DroolsParserException exc) {
			return null;
		}
	}

}
