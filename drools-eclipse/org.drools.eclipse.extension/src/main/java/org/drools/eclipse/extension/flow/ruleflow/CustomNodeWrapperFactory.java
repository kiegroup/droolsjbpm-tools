package org.drools.eclipse.extension.flow.ruleflow;

import org.drools.eclipse.flow.ruleflow.core.HumanTaskNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.NodeWrapperFactory;

public class CustomNodeWrapperFactory extends NodeWrapperFactory {	

	
	public HumanTaskNodeWrapper getHumanTaskNodeWrapper() {
		return new CustomHumanTaskNodeWrapper();
	}

}
