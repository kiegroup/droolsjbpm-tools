package org.drools.eclipse.extension.flow.ruleflow;

import org.drools.eclipse.extension.flow.ruleflow.properties.HumanTaskCommentPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.core.HumanTaskNodeWrapper;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class CustomHumanTaskNodeWrapper extends HumanTaskNodeWrapper {
	
	private static final long serialVersionUID = -2561220976066328748L;
	

	@Override
	protected void initDescriptors() {
		super.initDescriptors();
		String parameterName = "Comment";
		IPropertyDescriptor desc = new HumanTaskCommentPropertyDescriptor(parameterName, parameterName, getWorkItemNode());
		replacePropertyDescriptor(parameterName, desc);
	}
	
	/**
	 * @param name
	 * @param descriptor
	 */
	private void replacePropertyDescriptor(String name, IPropertyDescriptor descriptor) {
		int index = getPropertyDescriptorIndexByName(name);
		if (index >= 0) {
			for (int t = index; t < descriptors.length - 1; t++) {
				descriptors[t] = descriptors[t + 1];
			}
			descriptors[descriptors.length-1] = descriptor;
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private int getPropertyDescriptorIndexByName(String name) {
		for(int i=0;i<descriptors.length;i++) {
			if (name.equals(descriptors[i].getDisplayName())) {
				return i;
			}
		}
		return -1;
	}
	
	

}
