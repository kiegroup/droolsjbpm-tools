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

package org.drools.eclipse.editors;

import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.AnnotationDescr;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.ConditionalElementDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.FunctionImportDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.lang.descr.ImportDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;

/**
 * Util class for searching Descr tree.
 */
public final class DescrUtil {

    private DescrUtil() {
    }

    public static BaseDescr getDescr(BaseDescr descr, int offset) {
        if (descr instanceof PackageDescr) {
            PackageDescr packageDescr = (PackageDescr) descr;
            // rules
            for (RuleDescr ruleDescr : packageDescr.getRules()) {
	            if (ruleDescr != null) {
                    BaseDescr result = getDescr(ruleDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            // imports
            for (ImportDescr importDescr : packageDescr.getImports()) {
                if (importDescr != null) {
                    BaseDescr result = getDescr(importDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            // function imports
            for (FunctionImportDescr functionImportDescr : packageDescr.getFunctionImports()) {
				if (functionImportDescr != null) {
                    BaseDescr result = getDescr(functionImportDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            // functions
            for (FunctionDescr functionDescr : packageDescr.getFunctions()) {
			   if (functionDescr != null) {
                    BaseDescr result = getDescr(functionDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            // attributes
            for (AttributeDescr attributeDescr : packageDescr.getAttributes()) {
			   if (attributeDescr != null) {
                    BaseDescr result = getDescr(attributeDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            // globals
            for (GlobalDescr globalDescr : packageDescr.getGlobals()) {
			   if (globalDescr != null) {
                    BaseDescr result = getDescr(globalDescr, offset);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        } else {
            if (offset < descr.getStartCharacter() || offset > descr.getEndCharacter()) {
                return null;
            }
            if(descr instanceof RuleDescr) {
                RuleDescr ruleDescr = (RuleDescr) descr;
                // rules attributes
                for (AttributeDescr attributeDescr : ruleDescr.getAttributes().values()) {
                     if (attributeDescr != null) {
                         BaseDescr result = getDescr(attributeDescr, offset);
                         if (result != null) {
                             return result;
                         }
                     }
                }
	            // rules annotations    
                for (AnnotationDescr annotationDescr : ruleDescr.getAnnotations().values()) {
				    if (annotationDescr != null) {
	                    BaseDescr result = getDescr(annotationDescr, offset);
	                    if (result != null) {
	                        return result;
	                    }
	                }
	            }
	            // rules Lhs                
	            AndDescr andDescr =ruleDescr.getLhs();
	            if (andDescr != null) {
	                BaseDescr result = getDescr(andDescr, offset);
	                if (result != null) {
	                    return result;
	                }
	            }
	        // AndDescr descrs
	        } else if(descr instanceof AndDescr) {
	        	AndDescr andDescr = (AndDescr) descr;
	        	for (BaseDescr baseDescr : andDescr.getDescrs()) {
					 if (baseDescr != null) {
	                     BaseDescr result = getDescr(baseDescr, offset);
	                     if (result != null) {
	                         return result;
	                     }
	                 }
	            }
	        // PatternDescr descrs
	         } else if(descr instanceof PatternDescr) {
	        	PatternDescr patternDescr = (PatternDescr) descr;
	        	for (BaseDescr baseDescr : patternDescr.getDescrs()) {
					 if (baseDescr != null) {
	                     BaseDescr result = getDescr(baseDescr, offset);
	                     if (result != null) {
	                         return result;
	                     }
	                 }
	            }
	        // ConditionalElementDescr descrs
	        } else if(descr instanceof ConditionalElementDescr){
	        	ConditionalElementDescr conditionalElementDescr = (ConditionalElementDescr) descr;
	        	for (BaseDescr baseDescr : conditionalElementDescr.getDescrs()) {
					 if (baseDescr != null) {
	                     BaseDescr result = getDescr(baseDescr, offset);
	                     if (result != null) {
	                         return result;
	                     }
	                 }
	            }
	        } 
            
            // TODO: select subDescr if possible
            return descr;
        }
    }

}
