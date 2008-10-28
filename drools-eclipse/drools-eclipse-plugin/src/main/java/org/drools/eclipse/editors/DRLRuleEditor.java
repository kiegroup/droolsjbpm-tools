package org.drools.eclipse.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.debug.core.IDroolsDebugConstants;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.FunctionImportDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.lang.descr.ImportDescr;
import org.drools.lang.descr.PackageDescr;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public class DRLRuleEditor extends AbstractRuleEditor {

    protected List<String> imports;
    protected List<String> functions;
    protected Map<String, FactTemplateDescr> templates;
    protected List<GlobalDescr> globals;
    protected String packageName;
    protected List<String> classesInPackage;
	protected Map<String, String> attributes;

    public DRLRuleEditor() {
	}

	public List<String> getImports() {
		if (imports == null || isDirty()) {
			loadImportsAndFunctions();
		}
		return imports;
	}

	public List<String> getFunctions() {
		if (functions == null) {
			loadImportsAndFunctions();
		}
		return functions;
	}

	public Set<String> getTemplates() {
		if (templates == null) {
			loadImportsAndFunctions();
		}
		return templates.keySet();
	}

	public Map<String, String> getAttributes() {
		if ( attributes == null ) {
			loadImportsAndFunctions();
		}
		return attributes;
	}

	public FactTemplateDescr getTemplate(String name) {
		if (templates == null) {
			loadImportsAndFunctions();
		}
		return templates.get(name);
	}

	public List<GlobalDescr> getGlobals() {
		if (globals == null ) {
			loadImportsAndFunctions();
		}
		return globals;
	}

	public String getPackage() {
		if (packageName == null) {
			loadImportsAndFunctions();
		}
		return packageName;
	}

	public List<String> getClassesInPackage() {
		if (classesInPackage == null) {
			classesInPackage = getAllClassesInPackage(getPackage());
		}
		return classesInPackage;
	}

	protected List<String> getAllClassesInPackage(String packageName) {
		List<String> list = new ArrayList<String>();
		if (packageName != null) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				IProject project = ((IFileEditorInput) input).getFile().getProject();
				IJavaProject javaProject = JavaCore.create(project);
				list = getAllClassesInPackage(packageName, javaProject);
			}
		}
		return list;
	}

	public static List<String> getAllClassesInPackage(String packageName, IJavaProject javaProject) {
		final List<String> list = new ArrayList<String>();
		CompletionRequestor requestor = new CompletionRequestor() {
			public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
				String className = new String(proposal.getCompletion());
				if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.TYPE_REF) {
					list.add(className);
				}
				// ignore all other proposals
			}
		};

		try {
			javaProject.newEvaluationContext().codeComplete(packageName + ".", packageName.length() + 1, requestor);
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
		}
		return list;
	}

	protected List<String> getAllStaticMethodsInClass(String className) {
		final List<String> list = new ArrayList<String>();
		if (className != null) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				IProject project = ((IFileEditorInput) input).getFile().getProject();
				IJavaProject javaProject = JavaCore.create(project);

				CompletionRequestor requestor = new CompletionRequestor() {
					public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
						String functionName = new String(proposal.getCompletion());
						if (proposal.getKind() == org.eclipse.jdt.core.CompletionProposal.METHOD_REF) {
							list.add(functionName.substring(0, functionName.length() - 2)); // remove the ()
						}
						// ignore all other proposals
					}
				};

				try {
					javaProject.newEvaluationContext().codeComplete(className + ".", className.length() + 1, requestor);
				} catch (Throwable t) {
					DroolsEclipsePlugin.log(t);
				}
			}
		}
		return list;
	}

    protected void loadImportsAndFunctions() {
        try {
            DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(this, true, false);
            PackageDescr descr = drlInfo.getPackageDescr();
            // package
            this.packageName = drlInfo.getPackageName();
            // imports
            List<ImportDescr> allImports = descr.getImports();
            this.imports = new ArrayList<String>();
            if (packageName != null) {
                imports.addAll(getAllClassesInPackage(packageName));
            }
            Iterator<ImportDescr> iterator = allImports.iterator();
            while (iterator.hasNext()) {
                String importName = iterator.next().getTarget();
                if (importName.endsWith(".*")) {
                    String packageName = importName.substring(0, importName.length() - 2);
                    imports.addAll(getAllClassesInPackage(packageName));
                } else {
                    imports.add(importName);
                }
            }
            // functions
            List<FunctionDescr> functionDescrs = descr.getFunctions();
            List<FunctionImportDescr> functionImports = descr.getFunctionImports();
            functions = new ArrayList<String>(functionDescrs.size());
            Iterator<FunctionDescr> iterator2 = functionDescrs.iterator();
            while (iterator2.hasNext()) {
                functions.add(iterator2.next().getName());
            }
            Iterator<FunctionImportDescr> iterator3 = functionImports.iterator();
            while (iterator3.hasNext()) {
                String functionImport = iterator3.next().getTarget();
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
            List<FactTemplateDescr> templateDescrs = descr.getFactTemplates();
            templates = new HashMap<String, FactTemplateDescr>(templateDescrs.size());
            Iterator<FactTemplateDescr> iterator4 = templateDescrs.iterator();
            while (iterator4.hasNext()) {
                FactTemplateDescr template = iterator4.next();
                templates.put(template.getName(), template);
            }
            // globals
            List<GlobalDescr> globalDescrs = descr.getGlobals();
            globals = new ArrayList<GlobalDescr>();
            Iterator<GlobalDescr> iterator5 = globalDescrs.iterator();
            while (iterator5.hasNext()) {
                globals.add(iterator5.next());
            }

            //attributes
            this.attributes = new HashMap<String, String>();
        	for (Iterator<AttributeDescr> attrIter = descr.getAttributes().iterator(); attrIter.hasNext();) {
        		AttributeDescr attribute = attrIter.next();
        		if (attribute != null && attribute.getName() != null) {
        			attributes.put(attribute.getName(), attribute.getValue());
        		}
        	}

        } catch (DroolsParserException e) {
            DroolsEclipsePlugin.log(e);
        }
    }

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IToggleBreakpointsTarget.class)) {
			return getBreakpointAdapter();
		}
		return super.getAdapter(adapter);
	}

	private Object getBreakpointAdapter() {
		return new DroolsLineBreakpointAdapter();
	}

	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		// remove cached content
		imports = null;
		functions = null;
		templates = null;
		globals = null;
		packageName = null;
		classesInPackage = null;
	}

	public void gotoMarker(IMarker marker) {
		try {
			if (marker.getType().equals(IDroolsDebugConstants.DROOLS_MARKER_TYPE)) {
				int line = marker.getAttribute(IDroolsDebugConstants.DRL_LINE_NUMBER, -1);
	            if (line > -1)
	            	--line;
	                try {
	                    IDocument document = getDocumentProvider().getDocument(getEditorInput());
	                    selectAndReveal(document.getLineOffset(line), document.getLineLength(line));
	                } catch(BadLocationException exc) {
	                	DroolsEclipsePlugin.log(exc);
	                }
			} else {
				super.gotoMarker(marker);
			}
		} catch (CoreException exc) {
			DroolsEclipsePlugin.log(exc);
		}
	}


	public BaseDescr getDescr(int offset) {
		try {
			DRLInfo info = DroolsEclipsePlugin.getDefault().parseResource(this, true, false);
			return DescrUtil.getDescr(info.getPackageDescr(), offset);
		} catch (DroolsParserException exc) {
			return null;
		}
	}
}
