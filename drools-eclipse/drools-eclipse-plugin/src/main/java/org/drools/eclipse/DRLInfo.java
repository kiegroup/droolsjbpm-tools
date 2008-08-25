package org.drools.eclipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.compiler.Dialect;
import org.drools.compiler.DialectCompiletimeRegistry;
import org.drools.compiler.DroolsError;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.rule.DialectRuntimeRegistry;
import org.drools.rule.LineMappings;
import org.drools.rule.Package;

public class DRLInfo {

	private static final DroolsError[] EMPTY_DROOLS_ERROR_ARRAY = new DroolsError[0];
	private static final List<DroolsError> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<DroolsError>());

	private String sourcePathName;
	private PackageDescr packageDescr;
	private List<DroolsError> parserErrors;
	private Package compiledPackage;
	private DroolsError[] builderErrors;
	// cached entry
	private transient RuleInfo[] ruleInfos;
	private transient FunctionInfo[] functionInfos;
	private DialectCompiletimeRegistry dialectRegistry;

	public DRLInfo(String sourcePathName, PackageDescr packageDescr, List<DroolsError> parserErrors, DialectCompiletimeRegistry dialectRegistry) {
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
		this.builderErrors = EMPTY_DROOLS_ERROR_ARRAY;
		this.dialectRegistry = dialectRegistry;
	}

	public DRLInfo(String pathName, PackageDescr packageDescr, List<DroolsError> parserErrors, Package compiledPackage, DroolsError[] builderErrors, DialectCompiletimeRegistry dialectRegistry) {
		this(pathName, packageDescr, parserErrors, dialectRegistry);
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

	public List<DroolsError> getParserErrors() {
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
			List<RuleInfo> ruleInfosList = new ArrayList<RuleInfo>();
	        for (RuleDescr ruleDescr: packageDescr.getRules()) {
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
			int ruleDrlLineNumber = ruleInfos[i].getDrlLineNumber();
			if (ruleDrlLineNumber > ruleLine
					&& ruleDrlLineNumber <= drlLineNumber + 1) {
				ruleLine = ruleDrlLineNumber;
				result = ruleInfos[i];
			}
		}
		return result;
	}

	public class RuleInfo {

		private final RuleDescr ruleDescr;
		// cached entries
		private transient String className;
		private transient int consequenceJavaLineNumber = -1;

		public RuleInfo(RuleDescr ruleDescr) {
			if (ruleDescr == null) {
				throw new IllegalArgumentException("Null ruleDescr");
			}
			this.ruleDescr = ruleDescr;
		}

        public String getDialectName() {
            String dialectName = null;
            for (AttributeDescr attribute: ruleDescr.getAttributes()) {
                if ("dialect".equals(attribute.getName())) {
                    dialectName = (String) attribute.getValue();
                    break;
                }
            }
            if (dialectName == null) {
                for (AttributeDescr attribute: DRLInfo.this.packageDescr.getAttributes()) {
                    if ("dialect".equals(attribute.getName())) {
                        dialectName = (String) attribute.getValue();
                        break;
                    }
                }
            }
            return dialectName;
        }
        
        public Dialect getDialect() {
            String dialectName = getDialectName();
            if (dialectName == null) {
                return null;
            }
            return DRLInfo.this.dialectRegistry.getDialect(dialectName);
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

		public int getDrlLineNumber() {
			return ruleDescr.getLine();
		}

		public int getConsequenceDrlLineNumber() {
			return ruleDescr.getConsequenceLine();
		}

		public int getConsequenceJavaLineNumber() {
			if (consequenceJavaLineNumber == -1) {
				if (!isCompiled()) {
					throw new IllegalArgumentException("Package has not been compiled");
				}
				DialectRuntimeRegistry datas = compiledPackage.getDialectRuntimeRegistry();

                LineMappings mappings = datas.getLineMappings(className);
                consequenceJavaLineNumber = mappings.getOffset();

			}
			return consequenceJavaLineNumber;
		}

		public String getPackageName() {
			return packageDescr.getName();
		}

		public String getRuleName() {
			return ruleDescr.getName();
		}
	}

	public FunctionInfo[] getFunctionInfos() {
		if (functionInfos == null) {
			List<FunctionInfo> functionInfosList = new ArrayList<FunctionInfo>();
	        for (FunctionDescr functionDescr: packageDescr.getFunctions()) {
	    		FunctionInfo functionInfo = new FunctionInfo(functionDescr);
	    		functionInfosList.add(functionInfo);
	    	}
	        functionInfos = (FunctionInfo[]) functionInfosList.toArray(new FunctionInfo[0]);
		}
		return functionInfos;
	}

	public FunctionInfo getFunctionInfo(int drlLineNumber) {
		FunctionInfo[] functionInfos = getFunctionInfos();
		int functionLine = -1;
		FunctionInfo result = null;
		for (int i = 0; i < functionInfos.length; i++) {
			int functionDrlLineNumber = functionInfos[i].getDrlLineNumber();
			if (functionDrlLineNumber > functionLine
					&& functionDrlLineNumber <= drlLineNumber + 1) {
				functionLine = functionDrlLineNumber;
				result = functionInfos[i];
			}
		}
		return result;
	}

	public class FunctionInfo {

		private FunctionDescr functionDescr;
		// cached entries
		private transient String className;
		private transient int javaLineNumber = -1;

		public FunctionInfo(FunctionDescr functionDescr) {
			if (functionDescr == null) {
				throw new IllegalArgumentException("Null functionDescr");
			}
			this.functionDescr = functionDescr;
		}

		public String getSourcePathName() {
			return DRLInfo.this.getSourcePathName();
		}

		public String getClassName() {
			// functionDescr is only filled in during compilation
			if (!isCompiled()) {
				throw new IllegalArgumentException("Package has not been compiled");
			}
			if (className == null) {
	    		className = functionDescr.getClassName();
			}
			return className;
		}

		public int getDrlLineNumber() {
			return functionDescr.getLine();
		}

		public int getJavaLineNumber() {
			if (javaLineNumber == -1) {
				if (!isCompiled()) {
					throw new IllegalArgumentException("Package has not been compiled");
				}
				javaLineNumber = compiledPackage.getDialectRuntimeRegistry().getLineMappings(className).getOffset();
			}
			return javaLineNumber;
		}

		public String getPackageName() {
			return packageDescr.getName();
		}

		public String getFunctionName() {
			return functionDescr.getName();
		}
	}

}
