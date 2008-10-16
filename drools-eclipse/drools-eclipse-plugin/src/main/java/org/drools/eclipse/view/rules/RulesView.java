package org.drools.eclipse.view.rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.DroolsModelBuilder;
import org.drools.eclipse.core.Function;
import org.drools.eclipse.core.Global;
import org.drools.eclipse.core.Package;
import org.drools.eclipse.core.Process;
import org.drools.eclipse.core.Query;
import org.drools.eclipse.core.Rule;
import org.drools.eclipse.core.RuleSet;
import org.drools.eclipse.core.Template;
import org.drools.eclipse.core.ui.DroolsContentProvider;
import org.drools.eclipse.core.ui.DroolsLabelProvider;
import org.drools.eclipse.core.ui.DroolsTreeSorter;
import org.drools.eclipse.core.ui.FilterActionGroup;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.lang.descr.QueryDescr;
import org.drools.lang.descr.RuleDescr;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulesView extends ViewPart implements IDoubleClickListener, IResourceVisitor, IResourceChangeListener {

	private final RuleSet ruleSet = DroolsModelBuilder.createRuleSet();

	private Map resourcesMap = new HashMap();
	private TreeViewer treeViewer;
	
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new DroolsContentProvider());
		treeViewer.setLabelProvider(new DroolsLabelProvider());
		treeViewer.setSorter(new DroolsTreeSorter());
		treeViewer.addDoubleClickListener(this);
		treeViewer.setUseHashlookup(true);
		treeViewer.setInput(ruleSet);
		FilterActionGroup filterActionGroup = new FilterActionGroup(
			treeViewer, "org.drools.eclipse.view.rules.RulesView");
		filterActionGroup.fillActionBars(getViewSite().getActionBars());
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(this);
		} catch (CoreException e) {
			DroolsEclipsePlugin.log(e);
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public boolean visit(IResource resource) throws CoreException {
		return updateResource(resource);
	}
	
	private boolean updateResource(IResource resource) {
    	IProject project = resource.getProject();
    	if (project != null) {
            IJavaProject javaProject = JavaCore.create(project);
            if (!javaProject.exists()) {
            	return false;
            }
    		if (resource instanceof IFile 
                    && javaProject.isOnClasspath(resource)) {
                IFile file = (IFile) resource;
    		    if ("drl".equals(resource.getFileExtension())
    				        || "dslr".equals(resource.getFileExtension())) {
        			try {
        				DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(resource, false);
        				String packageName = drlInfo.getPackageName();
        				Package pkg = ruleSet.getPackage(packageName);
        				if (pkg == null) {
        					pkg = DroolsModelBuilder.addPackage(ruleSet, packageName, 0, 0);
        				}
        				if (drlInfo.getBuilderErrors() != null && drlInfo.getBuilderErrors().length > 0) {
        					return false;
        				}
        				// add rules
        				List rules = drlInfo.getPackageDescr().getRules();
        				for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
        					RuleDescr ruleDescr = (RuleDescr) iterator.next();
        					boolean isQuery = ruleDescr instanceof QueryDescr;
        					String ruleName = ruleDescr.getName();
        					if (!isQuery) {
        						Rule rule = DroolsModelBuilder.addRule(
    								pkg, ruleName, file, ruleDescr.getStartCharacter(),
    								ruleDescr.getEndCharacter() - ruleDescr.getStartCharacter() + 1, null);
    	    					// create link between resource and created rule nodes
    	    					List droolsElements = (List) resourcesMap.get(file);
    	    					if (droolsElements == null) {
    	    						droolsElements = new ArrayList();
    	    						resourcesMap.put(file, droolsElements);
    	    					}
    	    					droolsElements.add(rule);
        					} else {
        						Query query = DroolsModelBuilder.addQuery(
    								pkg, ruleName, file, ruleDescr.getStartCharacter(), 
    								ruleDescr.getEndCharacter() - ruleDescr.getStartCharacter() + 1);
    	    					// create link between resource and created rule nodes
    	    					List droolsElements = (List) resourcesMap.get(file);
    	    					if (droolsElements == null) {
    	    						droolsElements = new ArrayList();
    	    						resourcesMap.put(file, droolsElements);
    	    					}
    	    					droolsElements.add(query);
        					}
        				}
        				// add templates
        				List templates = drlInfo.getPackageDescr().getFactTemplates();
        				for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
        					FactTemplateDescr templateDescr = (FactTemplateDescr) iterator.next();
    						Template template = DroolsModelBuilder.addTemplate(
    							pkg, templateDescr.getName(), file, templateDescr.getStartCharacter(),
    							templateDescr.getEndCharacter() - templateDescr.getStartCharacter() + 1);
        					// create link between resource and created rule nodes
        					List droolsElements = (List) resourcesMap.get(file);
        					if (droolsElements == null) {
        						droolsElements = new ArrayList();
        						resourcesMap.put(file, droolsElements);
        					}
        					droolsElements.add(template);
        				}
        				// add globals
        				List globals = drlInfo.getPackageDescr().getGlobals();
        				for (Iterator iterator = globals.iterator(); iterator.hasNext();) {
        					GlobalDescr globalDescr = (GlobalDescr) iterator.next();
        					Global global = DroolsModelBuilder.addGlobal(
    							pkg, globalDescr.getIdentifier(), file, globalDescr.getStartCharacter(),
    							globalDescr.getEndCharacter() - globalDescr.getStartCharacter() + 1);
        					// create link between resource and created rule nodes
        					List droolsElements = (List) resourcesMap.get(file);
        					if (droolsElements == null) {
        						droolsElements = new ArrayList();
        						resourcesMap.put(file, droolsElements);
        					}
        					droolsElements.add(global);
        				}
        				// add functions
        				List functions = drlInfo.getPackageDescr().getFunctions();
        				for (Iterator iterator = functions.iterator(); iterator.hasNext();) {
        					FunctionDescr functionDescr = (FunctionDescr) iterator.next();
        					String functionName = functionDescr.getName();
        					Function function = DroolsModelBuilder.addFunction(
    							pkg, functionName, file, functionDescr.getStartCharacter(),
    							functionDescr.getEndCharacter() - functionDescr.getStartCharacter() + 1);
        					// create link between resource and created rule nodes
        					List droolsElements = (List) resourcesMap.get(file);
        					if (droolsElements == null) {
        						droolsElements = new ArrayList();
        						resourcesMap.put(file, droolsElements);
        					}
        					droolsElements.add(function);
        				}
        			} catch (Throwable t) {
        				DroolsEclipsePlugin.log(t);
        			}
        			return false;
    		    } else if ("rf".equals(resource.getFileExtension())) {
    		        try {
        	            String processString = convertToString(file.getContents());
                        ProcessInfo processInfo = DroolsEclipsePlugin.getDefault().parseProcess(processString, resource);
                        if (processInfo != null && processInfo.getProcess() != null) {
    	                    String packageName = processInfo.getProcess().getPackageName();
                            Package pkg = ruleSet.getPackage(packageName);
                            if (pkg == null) {
                                pkg = DroolsModelBuilder.addPackage(ruleSet, packageName, 0, 0);
                            }
                            Process process = DroolsModelBuilder.addProcess(pkg, processInfo.getProcess().getId(), file);
                            List droolsElements = (List) resourcesMap.get(file);
                            if (droolsElements == null) {
                                droolsElements = new ArrayList();
                                resourcesMap.put(file, droolsElements);
                            }
                            droolsElements.add(process);
    	                }
        		    } catch (Throwable t) {
                        DroolsEclipsePlugin.log(t);
                    }     
    		        return false;
    		    }
    		}
        }
        return true;
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					delta.accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) throws CoreException {
							IResource resource = delta.getResource();
							removeElementsFromResource(resource);
							boolean result = true;
							if (delta.getKind() != IResourceDelta.REMOVED) {
								result = updateResource(resource);
							}
							treeViewer.getControl().getDisplay().asyncExec(
						        new Runnable() {
									public void run() {
									    if (!treeViewer.getControl().isDisposed()) {
									        treeViewer.refresh();
									    }
									}
								}
					        );
							return result;
						}
					});
				}
			} else if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IResource resource = event.getResource();
				if (resource != null) {
					resource.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							removeElementsFromResource(resource);
							return true;
						}
					});
				}
			} else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
				IResource resource = event.getResource();
				if (resource != null) {
					resource.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							removeElementsFromResource(resource);
							return true;
						}
					});
				}
			}
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
		}
	}
	
	private void removeElementsFromResource(IResource resource) {
		List droolsElements = (List) resourcesMap.get(resource);
		if (droolsElements != null) {
			for (Iterator iterator = droolsElements.iterator(); iterator.hasNext();) {
				DroolsModelBuilder.removeElement((DroolsElement) iterator.next());
			}
			resourcesMap.remove(resource);
		}
	}

	public void doubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object selected = ((StructuredSelection) selection).getFirstElement();
			if (selected != null && selected instanceof DroolsElement) {
				DroolsElement droolsSelected = (DroolsElement) selected;
				IFile file = droolsSelected.getFile();
				if (file != null) {
					try {
						IEditorPart editor = IDE.openEditor(getSite().getPage(), file);
						if (editor instanceof FormEditor) {
							editor = ((FormEditor) editor).getActiveEditor();
						}
						if (editor instanceof ITextEditor) {
							((ITextEditor)editor).selectAndReveal(
								droolsSelected.getOffset(), droolsSelected.getLength());
						}
					} catch (Throwable t) {
						DroolsEclipsePlugin.log(t);
					}
				}
			}
		}
	}

    private static String convertToString(final InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        final StringBuffer text = new StringBuffer();
        final char[] buf = new char[1024];
        int len = 0;
        while ((len = reader.read(buf)) >= 0) {
            text.append(buf, 0, len);
        }
        return text.toString();
    }
}
