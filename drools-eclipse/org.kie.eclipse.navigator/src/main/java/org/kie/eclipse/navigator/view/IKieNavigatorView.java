package org.kie.eclipse.navigator.view;

public interface IKieNavigatorView {
	void refresh(Object element);
	void setProperty(String key, String value);
	String getProperty(String key);
}
