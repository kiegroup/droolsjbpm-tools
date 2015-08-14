package org.kie.eclipse.navigator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.kie.eclipse.server.IKieResourceHandler;


public class ResourcePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IResource) {
			IResource resource = (IResource) receiver;
			if (resource.isAccessible()) {
				try {
					Object o = resource.getSessionProperty(IKieResourceHandler.RESOURCE_KEY);
					if (o!=null) {
						return o.getClass().getName().equals(expectedValue);
					}
				}
				catch (Exception e) {
				}
			}
		}
		return false;
	}

}
