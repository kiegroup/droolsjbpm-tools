package org.drools.eclipse.core;

public class RuleFlowGroup extends RuleGroup {

	protected RuleFlowGroup(Package parent, Rule rule, String groupName) {
		super(parent, rule, groupName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return RULEFLOW_GROUP;
	}
}
