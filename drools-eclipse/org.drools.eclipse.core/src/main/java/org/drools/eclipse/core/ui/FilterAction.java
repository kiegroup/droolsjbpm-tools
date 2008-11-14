package org.drools.eclipse.core.ui;

import org.drools.eclipse.DroolsPluginImages;
import org.eclipse.jface.action.Action;

/**
 * Action used to enable / disable filter properties
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class FilterAction extends Action {

	private int filterType;

	private FilterActionGroup filterActionGroup;

	public FilterAction(FilterActionGroup actionGroup, String title,
			int property, boolean initValue, String imageDescriptorKey) {
		super(title);
		filterActionGroup = actionGroup;
		filterType = property;
		setChecked(initValue);
		setImageDescriptor(DroolsPluginImages.getImageDescriptor(imageDescriptorKey));
	}

	public int getFilterType() {
		return filterType;
	}

	public void run() {
		filterActionGroup.setFilter(filterType, isChecked());
	}
	
}
