package org.drools.ide;

public class RuleInfo {
	
	private String ruleName;
	private String packageName;
	private String drlName;
	private String pathName;
	private String className;
	private int consequenceDrlLineNumber;
	private int consequenceJavaLineNumber;
	
	public RuleInfo(String ruleName, String packageName, String drlName,
					String pathName, String className,
					int consequenceDrlLineNumber, int consequenceJavaLineNumber) {
		this.ruleName = ruleName;
		this.packageName = packageName;
		this.drlName = drlName;
		this.pathName = pathName;
		this.className = className;
		this.consequenceDrlLineNumber = consequenceDrlLineNumber;
		this.consequenceJavaLineNumber = consequenceJavaLineNumber;
	}

	public String getClassName() {
		return className;
	}

	public int getConsequenceDrlLineNumber() {
		return consequenceDrlLineNumber;
	}

	public int getConsequenceJavaLineNumber() {
		return consequenceJavaLineNumber;
	}

	public String getDrlName() {
		return drlName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPathName() {
		return pathName;
	}

	public String getRuleName() {
		return ruleName;
	}
}
