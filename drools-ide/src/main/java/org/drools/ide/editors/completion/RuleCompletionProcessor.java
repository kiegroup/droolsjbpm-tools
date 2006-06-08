package org.drools.ide.editors.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.DroolsPluginImages;
import org.drools.ide.editors.DRLRuleEditor;
import org.drools.ide.editors.DSLAdapter;
import org.drools.ide.util.ProjectClassLoader;
import org.drools.lang.descr.ColumnDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.semantics.java.ClassTypeResolver;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * For handling within rules, including DSLs.
 * At present this provides a fixed list, plus what is available
 * in the DSL configuration. 
 * 
 * TODO: This can be enhanced to look back for declarations, and introspect to get
 * field names. (More can be done as well, this would just be the first step).
 * 
 * This also handles queries, as they are just a type of rule essentially.
 * 
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {
	
    private static final Pattern query = Pattern.compile(".*\\Wquery\\W.*", Pattern.DOTALL);
    private static final Image droolsIcon = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
    private static final Image dslIcon = DroolsPluginImages.getImage( DroolsPluginImages.DSL_EXPRESSION );
    private static final Image methodIcon = DroolsPluginImages.getImage(DroolsPluginImages.METHOD);
    private static final Image classIcon = DroolsPluginImages.getImage(DroolsPluginImages.CLASS);
    
    public RuleCompletionProcessor(DRLRuleEditor editor) {
    	super(editor);
    }
    
    protected DRLRuleEditor getDRLEditor() {
    	return (DRLRuleEditor) getEditor();
    }
    
	protected List getCompletionProposals(ITextViewer viewer, final int documentOffset) {
        try {
	        final List list = new ArrayList();
	        DSLAdapter adapter = getDSLAdapter(viewer);
	        
	        IDocument doc = viewer.getDocument();
	        String backText = readBackwards(documentOffset, doc);            
	
	        final String prefix = stripWhiteSpace(backText);
            
	        if (backText.length() < 5) {
	        	return list;
	        }
            
	        if (query(backText)) {
	            list.addAll(adapter.listConditionItems());
	        } else if (consequence(backText)) {
	        	List dslConsequences = adapter.listConsequenceItems();
                addDSLProposals( list,
                                 prefix,
                                 dslConsequences );
	            if (!adapter.hasConsequences()) {

	            	
                    addRHSCompletionProposals( list,
                                               prefix );                    
        			
        			addRHSFunctionCompletionProposals( viewer,
                                                       list,
                                                       prefix );
        			
        			addRHSJavaCompletionProposals(list, backText, prefix);
	            }
	        } else if (condition(backText)) {
	        	List dslConditions = adapter.listConditionItems();
	        	addDSLProposals( list,
                                 prefix,
                                 dslConditions );
	            addLHSCompletionProposals(viewer, list, adapter, prefix, backText);
	        } else {             
	            //we are in rule header
	            addRuleHeaderItems( list,
                                    prefix );
	        }
            
            filterProposalsOnPrefix(prefix, list);
	        return list;           
        } catch (Throwable t) {
        	DroolsIDEPlugin.log(t);
        }
        return null;
    }

	private void addLHSCompletionProposals(ITextViewer viewer, final List list, DSLAdapter adapter, final String prefix, String backText) throws CoreException, DroolsParserException {
		Iterator iterator;
		Image droolsIcon = DroolsPluginImages.getImage(DroolsPluginImages.DROOLS);
		if (!adapter.hasConditions()) {
			// determine location in condition
			LocationDeterminator.Location location = LocationDeterminator.getLocationInCondition(backText);
			
			switch (location.getType()) {
				case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION: 
					// if we are at the beginning of a new condition
					// add drools keywords
					list.add( new RuleCompletionProposal(prefix.length(), "exists", "exists ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "not", "not ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "and", "and ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "or", "or ", droolsIcon));
				    RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "eval", "eval(  )", 6 );
					prop.setImage(droolsIcon);
				    list.add(prop);
				    // and add imported classes
				    List imports = getDRLEditor().getImports();
				    iterator = imports.iterator();
				    while (iterator.hasNext()) {
				        String name = (String) iterator.next();
				        int index = name.lastIndexOf(".");
				        if (index != -1) {
				        	String className = name.substring(index + 1);
				        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), className, className + "(  )", className.length() + 2);
				        	p.setPriority(-1);
				        	p.setImage(classIcon);
				        	list.add(p);
				        }
				    }
				    List classesInPackage = getDRLEditor().getClassesInPackage();
				    iterator = classesInPackage.iterator();
				    while (iterator.hasNext()) {
				        String name = (String) iterator.next();
				        int index = name.lastIndexOf(".");
				        if (index != -1) {
				        	String className = name.substring(index + 1);
				        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), className, className + "(  )", className.length() + 2);
				        	p.setPriority(-1);
				        	p.setImage(classIcon);
				        	list.add(p);
				        }
				    }
					prop = new RuleCompletionProposal(prefix.length(), "then", "then" + System.getProperty("line.separator") + "\t");
					prop.setImage(droolsIcon);
					list.add(prop);
				    break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_START :
					String className = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
					if (className != null) {
						ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor().getImports(), ProjectClassLoader.getProjectClassLoader(getEditor()));
						try {
							Class clazz = resolver.resolveType(className);
							if (clazz != null) {
								Iterator iterator2 = new ClassFieldInspector(clazz).getFieldNames().keySet().iterator();
								while (iterator2.hasNext()) {
							        String name = (String) iterator2.next();
						        	RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), name, name + " ");
						        	p.setImage(methodIcon);
						        	list.add(p);
							    }
							}
						} catch (IOException exc) {
							// Do nothing
						} catch (ClassNotFoundException exc) {
							// Do nothing
						}
					}
					break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR :
					list.add( new RuleCompletionProposal(prefix.length(), "<", "< ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "<=", "<= ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), ">", "> ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), ">=", ">= ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "==", "== ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "!=", "!= ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "matches", "matches ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "contains", "contains ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "excludes", "excludes ", droolsIcon));
					list.add( new RuleCompletionProposal(prefix.length(), ":", ": ", droolsIcon));
				    list.add( new RuleCompletionProposal(prefix.length(), "->", "-> (  )", 5, droolsIcon));
				    break;
				case LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT :
					// determine type
					className = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
					String property = (String) location.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME);
					String type = null;
					if (className != null) {
						ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor().getImports(), ProjectClassLoader.getProjectClassLoader(getEditor()));
						try {
							Class clazz = resolver.resolveType(className);
							if (clazz != null) {
								Class clazzz = (Class) new ClassFieldInspector(clazz).getFieldTypes().get(property);
								if (clazzz != null) {
									type = clazzz.getName();
								}
							}
						} catch (IOException exc) {
							// Do nothing
						} catch (ClassNotFoundException exc) {
							// Do nothing
						}
					}

				    list.add( new RuleCompletionProposal(prefix.length(), "null", "null", droolsIcon));
					if (type == null || "boolean".equals(type)) {
					    list.add( new RuleCompletionProposal(prefix.length(), "true", "true", droolsIcon));
					    list.add( new RuleCompletionProposal(prefix.length(), "false", "false", droolsIcon));
					}
					if (type == null || "java.lang.String".equals(type)) {
						list.add( new RuleCompletionProposal(prefix.length(), "\"\"", "\"\"", 1, droolsIcon));
					}
					if (type == null || "java.util.Date".equals(type)) {
						list.add( new RuleCompletionProposal(prefix.length(), "\"dd-mmm-yyyy\"", "\"dd-mmm-yyyy\"", 1, droolsIcon));
					}
				    list.add( new RuleCompletionProposal(prefix.length(), "()", "(  )", 2, droolsIcon));
			    	DrlParser parser = new DrlParser();
			    	try {
			    		PackageDescr descr = parser.parse(backText);
			    		List rules = descr.getRules();
			    		if (rules != null && rules.size() == 1) {
			    			Map result = new HashMap();
			    			getRuleParameters(result, ((RuleDescr) rules.get(0)).getLhs().getDescrs());
			    			Iterator iterator2 = result.keySet().iterator();
			    			while (iterator2.hasNext()) {
			    				String name = (String) iterator2.next();
			    				RuleCompletionProposal proposal = new RuleCompletionProposal(prefix.length(), name);
			    				proposal.setPriority(-1);
			    				proposal.setImage(methodIcon);
								list.add(proposal);
			    			}
			    		}
			    	} catch (DroolsParserException exc) {
			    		// do nothing
			    	}
				    break;
			}
		}
	}
	
	private boolean consequence(String backText) {
		return isKeywordOnLine(backText, "then");
	}

	private boolean condition(String backText) {
		return isKeywordOnLine(backText, "when");
	}

	boolean query(String backText) {
		return query.matcher(backText).matches();
	}
	
	/**
	 * Check to see if the keyword appears on a line by itself.
	 */
	private boolean isKeywordOnLine(String chunk, String keyword) {
		StringTokenizer st = new StringTokenizer(chunk, "\n\t");
    	while(st.hasMoreTokens()) {
    		if (st.nextToken().trim().equals(keyword)) {
    			return true;
    		}    		
    	}
    	return false;
	}

    private void addRHSFunctionCompletionProposals(ITextViewer viewer,
                                                   final List list,
                                                   final String prefix) throws CoreException,
                                                                       DroolsParserException {
        Iterator iterator;
        RuleCompletionProposal prop;
        List functions = getDRLEditor().getFunctions();
        iterator = functions.iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next() + "()";
        	prop = new RuleCompletionProposal(prefix.length(), name, name + ";", name.length() - 1);
        	prop.setPriority(-1);
        	prop.setImage(methodIcon);
        	list.add(prop);
        }
    }

    private void addRHSCompletionProposals(final List list,
                                           final String prefix) {
        RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), "modify", "modify();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "retract", "retract();", 8);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assert", "assert();", 7);
        prop.setImage(droolsIcon);
        list.add(prop);
        prop = new RuleCompletionProposal(prefix.length(), "assertLogical", "assertLogical();", 14);
        prop.setImage(droolsIcon);
        list.add(prop);
    }
    
    private void addRHSJavaCompletionProposals(List list, String backText, String prefix) {
    	int thenPosition = backText.lastIndexOf("then");
    	String conditions = backText.substring(0, thenPosition);
    	DrlParser parser = new DrlParser();
    	try {
    		PackageDescr descr = parser.parse(conditions);
    		List rules = descr.getRules();
    		if (rules != null && rules.size() == 1) {
    			Map result = new HashMap();
    			getRuleParameters(result, ((RuleDescr) rules.get(0)).getLhs().getDescrs());
    			Iterator iterator = result.keySet().iterator();
    			while (iterator.hasNext()) {
    				String name = (String) iterator.next();
    				RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), name, name + ".");
					prop.setPriority(-1);
					prop.setImage(methodIcon);
					list.add(prop);
    			}
    		}
    	} catch (DroolsParserException exc) {
    		// do nothing
    	}
    	// String consequence = backText.substring(thenPosition + 4);
    	// list.addAll(getRHSJavaCompletionProposals(consequence, prefix));
    }
    
    private void getRuleParameters(Map result, List descrs) {
    	Iterator iterator = descrs.iterator();
    	while (iterator.hasNext()) {
    		PatternDescr descr = (PatternDescr) iterator.next();
			if (descr instanceof ColumnDescr) {
				String name = ((ColumnDescr) descr).getIdentifier();
				if (name != null) {
					result.put(name, ((ColumnDescr) descr).getObjectType());
				}
				getRuleParameters(result, ((ColumnDescr) descr).getDescrs());
			} else if (descr instanceof FieldBindingDescr) {
				String name = ((FieldBindingDescr) descr).getIdentifier();
				if (name != null) {
					// TODO retrieve type
					result.put(name, null);
				}
			}
		}
    }

	private List getRHSJavaCompletionProposals(final String consequenceStart, final String prefix) {
		final List list = new ArrayList();
		IEditorInput input = getEditor().getEditorInput();
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile().getProject();
			IJavaProject javaProject = JavaCore.create(project);
			
			CompletionRequestor requestor = new CompletionRequestor() {
				public void accept(org.eclipse.jdt.core.CompletionProposal proposal) {
					String completion = new String(proposal.getCompletion());
					RuleCompletionProposal prop = new RuleCompletionProposal(prefix.length(), completion);
					list.add(prop);
				}
			};

			try {
				IEvaluationContext evalContext = javaProject.newEvaluationContext();
				List imports = getDRLEditor().getImports();
				if (imports != null && imports.size() > 0) {
					evalContext.setImports((String[]) imports.toArray(new String[imports.size()]));
				}
				evalContext.codeComplete(consequenceStart, consequenceStart.length(), requestor);
			} catch (Throwable t) {
				DroolsIDEPlugin.log(t);
			}
		}
		return list;
	}
			
    private void addRuleHeaderItems(final List list,
                                    final String prefix) {
        list.add(new RuleCompletionProposal(prefix.length(), "salience", "salience ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "no-loop", "no-loop ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "agenda-group", "agenda-group ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "duration", "duration ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "auto-focus", "auto-focus ", droolsIcon));           
        list.add(new RuleCompletionProposal(prefix.length(), "when", "when" + System.getProperty("line.separator") + "\t ", droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "activation-group", "activation-group ", droolsIcon));        
    }

    private void addDSLProposals(final List list,
                                 final String prefix,
                                 List dslItems) {
        Iterator iterator = dslItems.iterator();
        while (iterator.hasNext()) {
        	String consequence = (String) iterator.next();
            RuleCompletionProposal p = new RuleCompletionProposal(prefix.length(), consequence);
            p.setImage( dslIcon );
            list.add(p);
        }
    }

    /** 
     * Lazily get the adapter for DSLs, and cache it with the editor for future reference.
     * If it is unable to load a DSL, it will try again next time.
     * But once it has found and loaded one, it will keep it until the editor is closed.
     * 
     * This delegates to DSLAdapter to poke around the project to try and load the DSL.
     */
    private DSLAdapter getDSLAdapter(ITextViewer viewer) {
    	// TODO: cache DSL adapter in plugin, and reset when dsl file saved
    	// retrieve dsl name always (might have changed) and try retrieving
    	// cached dsl from plugin first
//    	return new DSLAdapter(viewer.getDocument().get(), ((FileEditorInput) getEditor().getEditorInput()).getFile());
        DSLAdapter adapter = getDRLEditor().getDSLAdapter();
        if (adapter == null) {
            String content = viewer.getDocument().get();
            adapter = new DSLAdapter(content, ((FileEditorInput) getEditor().getEditorInput()).getFile());
            if (adapter.isValid()) {
            	getDRLEditor().setDSLAdapter(adapter);
            }
        }
        return adapter;
    }

}
