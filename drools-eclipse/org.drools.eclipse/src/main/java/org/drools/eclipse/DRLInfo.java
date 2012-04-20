/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.base.ClassObjectType;
import org.drools.compiler.Dialect;
import org.drools.compiler.DialectCompiletimeRegistry;
import org.drools.compiler.DroolsError;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.ConditionalElementDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.rule.DialectRuntimeRegistry;
import org.drools.rule.GroupElement;
import org.drools.rule.LineMappings;
import org.drools.rule.Package;
import org.drools.rule.Pattern;
import org.drools.rule.Rule;
import org.drools.rule.RuleConditionElement;
import org.drools.spi.ObjectType;
import org.eclipse.core.resources.IResource;

public class DRLInfo {

    private static final DroolsError[] EMPTY_DROOLS_ERROR_ARRAY = new DroolsError[0];
    private static final List<DroolsError> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<DroolsError>());

    private final String sourcePathName;
    private final PackageDescr packageDescr;
    private List<DroolsError> parserErrors;
    private Package compiledPackage;
    private DroolsError[] builderErrors;
    // cached entry
    private transient RuleInfo[] ruleInfos;
    private transient FunctionInfo[] functionInfos;
    private DialectCompiletimeRegistry dialectRegistry;
    
    private IResource resource;

    public DRLInfo( String sourcePathName, 
    				PackageDescr packageDescr, 
    				List<DroolsError> parserErrors, 
    				DialectCompiletimeRegistry dialectRegistry ) {
        if (sourcePathName == null) {
            throw new IllegalArgumentException("Invalid sourcePathName " + sourcePathName);
        }
        this.sourcePathName = sourcePathName;
        this.packageDescr = packageDescr;
        this.parserErrors = parserErrors;
        this.builderErrors = EMPTY_DROOLS_ERROR_ARRAY;
        this.dialectRegistry = dialectRegistry;
    }

    public DRLInfo( String pathName, 
    				PackageDescr packageDescr, 
    				List<DroolsError> parserErrors, 
    				Package compiledPackage, 
    				DroolsError[] builderErrors, 
    				DialectCompiletimeRegistry dialectRegistry ) {
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
    
    public void addError(DroolsError error) {
    	parserErrors.add(error);
    }

    public Package getPackage() {
        return compiledPackage;
    }

    public DroolsError[] getBuilderErrors() {
        return builderErrors;
    }

    public String getPackageName() {
        return packageDescr == null ? null : packageDescr.getName();
    }

    public boolean isCompiled() {
        return compiledPackage != null;
    }

    public RuleInfo[] getRuleInfos() {
        if (ruleInfos == null) {
            List<RuleInfo> ruleInfosList = new ArrayList<RuleInfo>();
            if (packageDescr != null) {
                for (RuleDescr ruleDescr: packageDescr.getRules()) {
                    RuleInfo ruleInfo = new RuleInfo(packageDescr, compiledPackage, ruleDescr, dialectRegistry, sourcePathName);
                    ruleInfosList.add(ruleInfo);
                }
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

    public DialectCompiletimeRegistry getDialectRegistry() {
        return dialectRegistry;
    }
    
    public IResource getResource() {
    	return resource;
    }
    
    public void setResource(IResource resource) {
    	this.resource = resource;
    }

    public static class RuleInfo {

        private final PackageDescr packageDescr;
        private final Package compiledPackage;
        private final RuleDescr ruleDescr;
        private final DialectCompiletimeRegistry dialectRegistry;
        private final String sourcePathName;
        
        // cached entries
        private transient String className;
        private transient int consequenceJavaLineNumber = -1;
        private transient List<PatternInfo> patternInfos = null;
        private transient int endPatternsCharacter = -1;

        public RuleInfo(PackageDescr packageDescr, Package compiledPackage, RuleDescr ruleDescr, DialectCompiletimeRegistry dialectRegistry, String sourcePathName) {
            if (ruleDescr == null) {
                throw new IllegalArgumentException("Null ruleDescr");
            }
            this.packageDescr = packageDescr;
            this.compiledPackage = compiledPackage;
            this.ruleDescr = ruleDescr;
            this.dialectRegistry = dialectRegistry;
            this.sourcePathName = sourcePathName;
        }

        public String getDialectName() {
            String dialectName = null;
            dialectName = ruleDescr.getAttributes().get("dialect").getValue();
            if (dialectName == null && packageDescr != null) {
                for (AttributeDescr attribute: packageDescr.getAttributes()) {
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
            return dialectRegistry.getDialect(dialectName);
        }

        public String getSourcePathName() {
            return sourcePathName;
        }

        public String getClassName() {
            // ruleDescr is only filled in during compilation
            if (!isCompiled()) {
                throw new IllegalArgumentException("Package has not been compiled");
            }
            if (className == null) {
                String packageName = getPackageName();
                className = (packageName == null ? "" : packageName + ".") + ruleDescr.getClassName();
            }
            return className;
        }
        
        public int getConsequenceStart() {
        	return endPatternsCharacter + 5;
        }

        public int getConsequenceEnd() {
        	return getRuleEnd();
        }
        
        public int getRuleStart() {
        	return ruleDescr.getStartCharacter() + 5;
        }

        public int getRuleEnd() {
        	return ruleDescr.getEndCharacter() - 4;
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

        public boolean isCompiled() {
            return compiledPackage != null;
        }

        public String getPackageName() {
            return packageDescr == null ? null : packageDescr.getName();
        }

        public String getRuleName() {
            return ruleDescr.getName();
        }
        
        public Rule getRule() {
        	return compiledPackage.getRule(getRuleName());
        }
        
        public String toString() {
        	return ruleDescr.toString();
        }
        
        public List<PatternInfo> getPatternInfos() {
        	if (patternInfos == null && isCompiled()) {
        		patternInfos = findPatternInfos();
        	}
        	return patternInfos;
        }
        
        private List<PatternInfo> findPatternInfos() {
        	List<PatternInfo> patternInfos = new ArrayList<PatternInfo>();
        	traversePatternTree(patternInfos, getRule().getLhs().getChildren(), ruleDescr.getLhs().getDescrs());
        	return patternInfos;
        }
        
        private void traversePatternTree(List<PatternInfo> patternInfos, List<RuleConditionElement> ruleElements, List<BaseDescr> lhsDescrs) {
        	if (ruleElements.size() != lhsDescrs.size()) {
        		throw new RuntimeException("Cannot traverse pattern tree");
        	}
        	for (int i = 0; i < ruleElements.size(); i++) {
        		RuleConditionElement ruleElement = ruleElements.get(i);
        		BaseDescr lhsDescr = lhsDescrs.get(i);
        		
        		if (ruleElement instanceof Pattern && lhsDescr instanceof PatternDescr) {
        			patternInfos.add(new PatternInfo((PatternDescr)lhsDescr, (Pattern)ruleElement));
        			endPatternsCharacter = Math.max(endPatternsCharacter, ((PatternDescr)lhsDescr).getEndCharacter());
        		} else if (ruleElement instanceof GroupElement && lhsDescr instanceof ConditionalElementDescr) {
        			traversePatternTree(patternInfos, ((GroupElement)ruleElement).getChildren(), (List<BaseDescr>)((ConditionalElementDescr)lhsDescr).getDescrs());
        		} else {
            		throw new RuntimeException("Cannot traverse pattern tree");
        		}
        	}
        }
    }
    
    public static class PatternInfo {
        private final PatternDescr patternDescr;
        private final Pattern pattern;
        
        private PatternInfo(PatternDescr patternDescr, Pattern pattern) {
        	this.patternDescr = patternDescr;
        	this.pattern = pattern;
        }
    	
        public String getPatternTypeName() {
        	ObjectType objectType = pattern.getObjectType();
        	return objectType instanceof ClassObjectType ? ((ClassObjectType)objectType).getClassType().getName() : "";
        }
        
        public int getStart() {
        	return patternDescr.getStartCharacter();
        }
        
        public int getEnd() {
        	return patternDescr.getEndCharacter();
        }
        
        public String toString() {
        	return patternDescr.toString();
        }
    }

    public FunctionInfo[] getFunctionInfos() {
        if (functionInfos == null) {
            List<FunctionInfo> functionInfosList = new ArrayList<FunctionInfo>();
            if (packageDescr != null) {
                for (FunctionDescr functionDescr: packageDescr.getFunctions()) {
                    FunctionInfo functionInfo = new FunctionInfo(packageDescr, compiledPackage, functionDescr, sourcePathName);
                    functionInfosList.add(functionInfo);
                }
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

    public static class FunctionInfo {

        private final PackageDescr packageDescr;
        private final Package compiledPackage;
        private final FunctionDescr functionDescr;
        private final String sourcePathName;

        // cached entries
        private transient String className;
        private transient int javaLineNumber = -1;

        public FunctionInfo(PackageDescr packageDescr, Package compiledPackage, FunctionDescr functionDescr, String sourcePathName) {
            if (functionDescr == null) {
                throw new IllegalArgumentException("Null functionDescr");
            }
            this.packageDescr = packageDescr;
            this.compiledPackage = compiledPackage;
            this.functionDescr = functionDescr;
            this.sourcePathName = sourcePathName;
        }

        public String getSourcePathName() {
            return sourcePathName;
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
            return packageDescr == null ? null : packageDescr.getName();
        }

        public String getFunctionName() {
            return functionDescr.getName();
        }

        public int getFunctionStart() {
        	return functionDescr.getStartCharacter() + 8;
        }

        public int getFunctionEnd() {
        	return functionDescr.getEndCharacter() - 1;
        }

        public boolean isCompiled() {
            return compiledPackage != null;
        }
    }

}
