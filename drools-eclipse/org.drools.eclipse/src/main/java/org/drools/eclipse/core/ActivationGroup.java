package org.drools.eclipse.core;

public class ActivationGroup extends RuleGroup {

	protected ActivationGroup(Package parent, Rule rule, String groupName) {
		super(parent, rule, groupName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return ACTIVATION_GROUP;
	}
}
