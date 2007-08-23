package org.drools.eclipse.editors;

import java.util.Iterator;

import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.FunctionImportDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.lang.descr.ImportDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;

/**
 * Util class for searching Descr tree.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public final class DescrUtil {
	
	private DescrUtil() {
	}

	public static BaseDescr getDescr(BaseDescr descr, int offset) {
		if (descr instanceof PackageDescr) {
			PackageDescr packageDescr = (PackageDescr) descr;
			// rules
			for (Iterator iterator = packageDescr.getRules().iterator(); iterator.hasNext(); ) {
				RuleDescr ruleDescr = (RuleDescr) iterator.next();
				if (ruleDescr != null) {
					BaseDescr result = getDescr(ruleDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// imports
			for (Iterator iterator = packageDescr.getImports().iterator(); iterator.hasNext(); ) {
				ImportDescr importDescr = (ImportDescr) iterator.next();
				if (importDescr != null) {
					BaseDescr result = getDescr(importDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// function imports
			for (Iterator iterator = packageDescr.getFunctionImports().iterator(); iterator.hasNext(); ) {
				FunctionImportDescr functionImportDescr = (FunctionImportDescr) iterator.next();
				if (functionImportDescr != null) {
					BaseDescr result = getDescr(functionImportDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// functions
			for (Iterator iterator = packageDescr.getFunctions().iterator(); iterator.hasNext(); ) {
				FunctionDescr functionDescr = (FunctionDescr) iterator.next();
				if (functionDescr != null) {
					BaseDescr result = getDescr(functionDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// attributes
			for (Iterator iterator = packageDescr.getAttributes().iterator(); iterator.hasNext(); ) {
				AttributeDescr attributeDescr = (AttributeDescr) iterator.next();
				if (attributeDescr != null) {
					BaseDescr result = getDescr(attributeDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// globals
			for (Iterator iterator = packageDescr.getGlobals().iterator(); iterator.hasNext(); ) {
				GlobalDescr globalDescr = (GlobalDescr) iterator.next();
				if (globalDescr != null) {
					BaseDescr result = getDescr(globalDescr, offset);
					if (result != null) {
						return result;
					}
				}
			}
			// fact templates
			for (Iterator iterator = packageDescr.getFactTemplates().iterator(); iterator.hasNext(); ) {
				FactTemplateDescr factTemplDescr = (FactTemplateDescr) iterator.next();
				if (factTemplDescr != null) {
					BaseDescr result = getDescr(factTemplDescr, offset);
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
			// TODO: select subDescr if possible
			return descr;
		}
	}
	
}
