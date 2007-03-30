package org.drools.eclipse.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsIDEPlugin;
import org.drools.eclipse.editors.outline.RuleContentOutlinePage;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.FunctionImportDescr;
import org.drools.lang.descr.ImportDescr;
import org.drools.lang.descr.PackageDescr;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public class DRLRuleEditor extends AbstractRuleEditor {

	public DRLRuleEditor() {
		setSourceViewerConfiguration(new DRLSourceViewerConfig(this));
		setDocumentProvider(new DRLDocumentProvider());
		getPreferenceStore().setDefault(IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(getPreferenceStore(), IDroolsConstants.DRL_EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192));
	}

    protected ContentOutlinePage getContentOutline() {
        if (ruleContentOutline == null) {
            ruleContentOutline = new RuleContentOutlinePage(this);
            ruleContentOutline.update();
        }
        return ruleContentOutline;
    }

    protected void loadImportsAndFunctions() {
        try {
            DRLInfo drlInfo = DroolsIDEPlugin.getDefault().parseResource(this, true, false);
            PackageDescr descr = drlInfo.getPackageDescr();
            // package
            this.packageName = drlInfo.getPackageName();
            // imports
            List allImports = descr.getImports();
            this.imports = new ArrayList();
            Iterator iterator = allImports.iterator();
            while (iterator.hasNext()) {
                String importName = ((ImportDescr) iterator.next()).getTarget();
                if (importName.endsWith(".*")) {
                    String packageName = importName.substring(0, importName.length() - 2);
                    imports.addAll(getAllClassesInPackage(packageName));
                } else {
                    imports.add(importName);
                }
            }
            // functions
            List functionDescrs = descr.getFunctions();
            List functionImports = descr.getFunctionImports();
            functions = new ArrayList(functionDescrs.size());
            iterator = functionDescrs.iterator();
            while (iterator.hasNext()) {
                functions.add(((FunctionDescr) iterator.next()).getName());
            }
            iterator = functionImports.iterator();
            while (iterator.hasNext()) {
                String functionImport = ((FunctionImportDescr) iterator.next()).getTarget();
                if (functionImport.endsWith(".*")) {
                    String className = functionImport.substring(0, functionImport.length() - 2);
                    functions.addAll(getAllStaticMethodsInClass(className));
                } else {
                    int index = functionImport.lastIndexOf('.');
                    if (index != -1) {
                        functions.add(functionImport.substring(index + 1));
                    }
                }
            }
            // templates
            List templateDescrs = descr.getFactTemplates();
            templates = new HashMap(templateDescrs.size());
            iterator = templateDescrs.iterator();
            while (iterator.hasNext()) {
                FactTemplateDescr template = (FactTemplateDescr) iterator.next();
                templates.put(template.getName(), template);
            }
        } catch (DroolsParserException e) {
            DroolsIDEPlugin.log(e);
        }
    }

}
