package org.drools.eclipse.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a rule group of type Agenda-Group Or RuleFlow-Group
 * 
 * @author gDelebecque
 * 
 */
public abstract class RuleGroup extends DroolsElement {
	private String groupName = null;
	private List rules = new ArrayList();

	protected RuleGroup(Package parent, Rule rule, String groupName) {
		super(parent);
		this.groupName = groupName;
		addRule(rule);
		parent.addGroup(this);
	}

	public DroolsElement[] getRules() {
		return (DroolsElement[]) rules.toArray(new DroolsElement[0]);
	}

	@Override
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	@Override
	public abstract int getType();

	@Override
	public String toString() {
		return groupName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			String other = (String) obj;
			return toString().equals(other.toString());
		}
		return false;
	}
	
	protected void addRule(Rule rule) {
		if (rule!=null) {
			if (!rules.contains(rule)) rules.add(rule);			
		}
	}
}
