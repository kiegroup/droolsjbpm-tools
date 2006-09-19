package org.drools.ide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.compiler.DroolsError;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.rule.Package;

public class DRLInfo {

	private static final DroolsError[] EMPTY_DROOLS_ERROR_ARRAY = new DroolsError[0];
	private static final List EMPTY_LIST = Collections.unmodifiableList(Collections.EMPTY_LIST);
	
	private String sourcePathName;
	private PackageDescr packageDescr;
	private List parserErrors;
	private Package compiledPackage;
	private DroolsError[] builderErrors;
	// cached entry
	private transient RuleInfo[] ruleInfos;

	public DRLInfo(String sourcePathName, PackageDescr packageDescr, List parserErrors) {
		if (sourcePathName == null || "".equals(sourcePathName)) {
			throw new IllegalArgumentException("Invalid sourcePathName " + sourcePathName);
		}
		if (packageDescr == null) {
			throw new IllegalArgumentException("Null packageDescr");
		}
		this.sourcePathName = sourcePathName;
		this.packageDescr = packageDescr;
		this.parserErrors =
			parserErrors == null ? EMPTY_LIST : Collections.unmodifiableList(parserErrors);
	}

	public DRLInfo(String pathName, PackageDescr packageDescr, List parserErrors, Package compiledPackage, DroolsError[] builderErrors) {
		this(pathName, packageDescr, parserErrors);
		if (compiledPackage == null) {
			throw new IllegalArgumentException("Null package");
		}
		this.compiledPackage = compiledPackage;
		this.builderErrors = 
			builderErrors == null ? EMPTY_DROOLS_ERROR_ARRAY : builderErrors;
	}

	public String getSourcePathName() {
		return sourcePathName;
	}
	
	public PackageDescr getPackageDescr() {
		return packageDescr;
	}

	public List getParserErrors() {
		return parserErrors;
	}
	
	public Package getPackage() {
		return compiledPackage;
	}
	
	public DroolsError[] getBuilderErrors() {
		return builderErrors;
	}

	public String getPackageName() {
		return packageDescr.getName();
	}
	
	public boolean isCompiled() {
		return compiledPackage != null;
	}
	
	public RuleInfo[] getRuleInfos() {
		if (ruleInfos == null) {
			List ruleInfosList = new ArrayList();
	        for ( Iterator rules = packageDescr.getRules().iterator(); rules.hasNext(); ) {
	    		RuleDescr ruleDescr = (RuleDescr) rules.next();
	    		RuleInfo ruleInfo = new RuleInfo(ruleDescr);
	    		ruleInfosList.add(ruleInfo);
	    	}
	    	ruleInfos = (RuleInfo[]) ruleInfosList.toArray(new RuleInfo[0]);
		}
		return ruleInfos;
	}
	
	public RuleInfo getRuleInfo(int drlLineNumber) {
		RuleInfo[] ruleInfos = getRuleInfos();
		int ruleLine = -1;
		RuleInfo result = null;
		for (int i = 0; i < ruleInfos.length; i++) {
			int ruleConsequenceDrlLineNumber = ruleInfos[i].getConsequenceDrlLineNumber();
			if (ruleConsequenceDrlLineNumber > ruleLine
					&& ruleConsequenceDrlLineNumber < drlLineNumber) {
				ruleLine = ruleConsequenceDrlLineNumber;
				result = ruleInfos[i];
			}
		}
		return result;
	}
	
	public class RuleInfo {
		
		private RuleDescr ruleDescr;
		// cached entries
		private transient String className;
		private transient int consequenceJavaLineNumber = -1;
		private transient String fullRuleName;
		
		public RuleInfo(RuleDescr ruleDescr) {
			if (ruleDescr == null) {
				throw new IllegalArgumentException("Null ruleDescr");
			}
			this.ruleDescr = ruleDescr;
		}

		public String getSourcePathName() {
			return DRLInfo.this.getSourcePathName();
		}

		public String getClassName() {
			// ruleDescr is only filled in during compilation
			if (!isCompiled()) {
				throw new IllegalArgumentException("Package has not been compiled");
			}
			if (className == null) {
	    		className = getPackageName() + "." + ruleDescr.getClassName();
			}
			return className;
		}

		public int getConsequenceDrlLineNumber() {
			return ruleDescr.getConsequenceLine();
		}

		public int getConsequenceJavaLineNumber() {
			if (consequenceJavaLineNumber == -1) {
				if (!isCompiled()) {
					throw new IllegalArgumentException("Package has not been compiled");
				}
				consequenceJavaLineNumber = compiledPackage
					.getPackageCompilationData().getLineMappings(className).getOffset();
			}
			return consequenceJavaLineNumber;
		}

		public String getPackageName() {
			return packageDescr.getName();
		}

		public String getRuleName() {
			return ruleDescr.getName();
		}

		public String getFullRuleName() {
			if (fullRuleName == null) {
				fullRuleName = getPackageName() + "." + ruleDescr.getName();
			}
			return fullRuleName;
		}

	}

}
