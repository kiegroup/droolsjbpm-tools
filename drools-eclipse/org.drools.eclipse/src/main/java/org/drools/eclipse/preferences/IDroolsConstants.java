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

package org.drools.eclipse.preferences;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.IAccessRule;

public interface IDroolsConstants {

    String BUILD_ALL = "Drools.BuildAll";
    String CROSS_BUILD = "Drools.CrossBuild";
    String EDITOR_FOLDING = "Drools.EditorFolding";
    String CACHE_PARSED_RULES = "Drools.CacheParsedRules";
    String DRL_EDITOR_MATCHING_BRACKETS = "Drools.DRLMatchingBrackets";
    String DRL_EDITOR_MATCHING_BRACKETS_COLOR = "Drools.DRLMatchingBracketsColor";
    String DSL_RULE_EDITOR_COMPLETION_FULL_SENTENCES = "Drools.DSLRuleEditorCompletionFullSentences";
    String SKIN = "Drools.Flow.Skin";
    String ALLOW_NODE_CUSTOMIZATION = "Drools.Flow.AllowNodeCustomization";
    String DROOLS_RUNTIMES = "Drools.Runtimes";
    String INTERNAL_API = "Drools.InternalAPI";
    String FLOW_NODES = "Drools.FlowNodes";

    public enum InternalApiChoice {
    	ACCESSIBLE("Accessible"), NOT_ACCESSIBLE("Not Accessible"), DISCOURAGED("Discouraged");
    	
    	private String text;
    	
    	private InternalApiChoice(String text) {
    		this.text = text;
    	}
    	
    	public String toString() {
    		return text;
    	}
    	
    	public static InternalApiChoice valueAt(int pos) {
    		for (InternalApiChoice choice : values()) {
    			if (choice.ordinal() == pos) {
    				return choice;
    			}
    		}
    		return null;
    	}
    	
    	public static InternalApiChoice getPreferenceChoice() {
    		return valueAt(DroolsEclipsePlugin.getDefault().getPreferenceStore().getInt(IDroolsConstants.INTERNAL_API));
    	}
    	
    	public int getAccessRule() {
    		switch (this) {
    			case NOT_ACCESSIBLE:
    				return IAccessRule.K_NON_ACCESSIBLE;
    			case DISCOURAGED:
    				return IAccessRule.K_DISCOURAGED;
    			default:
    				return IAccessRule.K_ACCESSIBLE;
    		}
    	}
    }
}
