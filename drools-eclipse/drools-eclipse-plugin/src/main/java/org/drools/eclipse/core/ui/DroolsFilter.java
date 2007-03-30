package org.drools.eclipse.core.ui;

import org.drools.eclipse.core.DroolsElement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for the rules viewer.
 */
public class DroolsFilter extends ViewerFilter {

	public static final int FILTER_RULES = 1;
	public static final int FILTER_QUERIES = 2;
	public static final int FILTER_FUNCTIONS = 4;
	public static final int FILTER_TEMPLATES = 8;
	public static final int FILTER_GLOBALS = 16;
	
	private int filterProperties;

	public final void addFilter(int filter) {
		filterProperties |= filter;
	}

	public final void removeFilter(int filter) {
		filterProperties &= (-1 ^ filter);
	}

	public final boolean hasFilter(int filter) {
		return (filterProperties & filter) != 0;
	}
	
	public boolean isFilterProperty(Object element, Object property) {
		return false;
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof DroolsElement) {
			DroolsElement droolsElement = (DroolsElement) element;
			int type = droolsElement.getType();
			if (hasFilter(FILTER_RULES) && type == DroolsElement.RULE) {
				return false;
			}
			if (hasFilter(FILTER_QUERIES) && type == DroolsElement.QUERY) {
				return false;
			}
			if (hasFilter(FILTER_FUNCTIONS) && type == DroolsElement.FUNCTION) {
				return false;
			}
			if (hasFilter(FILTER_TEMPLATES) && type == DroolsElement.TEMPLATE) {
				return false;
			}
			if (hasFilter(FILTER_GLOBALS) && type == DroolsElement.GLOBAL) {
				return false;
			}
		}			
		return true;
	}
	
}
