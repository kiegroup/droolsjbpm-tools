package org.drools.eclipse.core;

public class AgendaGroup extends RuleGroup {

	protected AgendaGroup(Package parent, Rule rule, String groupName) {
		super(parent, rule, groupName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return AGENDA_GROUP;
	}
}
