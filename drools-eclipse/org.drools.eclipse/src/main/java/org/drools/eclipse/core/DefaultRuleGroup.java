package org.drools.eclipse.core;

public class DefaultRuleGroup extends RuleGroup {

	protected DefaultRuleGroup(Package parent, Rule rule, String groupName) {
		super(parent, rule, groupName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return DEFAULT_RULE_GROUP;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "*default*";
	}
}
