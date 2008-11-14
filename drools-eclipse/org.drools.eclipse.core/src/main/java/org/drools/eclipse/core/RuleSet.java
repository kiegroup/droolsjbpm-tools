package org.drools.eclipse.core;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a rule set. 
 *
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleSet extends DroolsElement {
	
	private Map packages = new HashMap();
	
	RuleSet() {
		super(null);
	}
	
	public Package getPackage(String packageName) {
		return (Package) packages.get(packageName);
	}
	
	public int getType() {
		return RULESET;
	}
	
	public DroolsElement[] getChildren() {
		return (DroolsElement[]) packages.values().toArray(
			new DroolsElement[packages.size()]);
	}

	// These are helper methods for creating the model and should not
	// be used directly.  Use DroolsModelBuilder instead.

	void addPackage(Package pkg) {
		packages.put(pkg.getPackageName(), pkg);
	}
	
	void removePackage(String packageName) {
		packages.remove(packageName);
	}
	
	void clear() {
		packages.clear();
	}

}
