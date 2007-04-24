package org.drools.eclipse.editors.completion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.codehaus.jfdi.interpreter.ClassTypeResolver;
import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.DSLAdapter;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.lang.descr.AccumulateDescr;
import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.FieldTemplateDescr;
import org.drools.lang.descr.FromDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.FileEditorInput;

/**
 * For handling within rules, including DSLs. At present this provides a fixed
 * list, plus what is available in the DSL configuration.
 * 
 * TODO: This can be enhanced to look back for declarations, and introspect to
 * get field names. (More can be done as well, this would just be the first
 * step).
 * 
 * This also handles queries, as they are just a type of rule essentially.
 * 
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

	private static final Pattern query = Pattern.compile(".*\\Wquery\\W.*",
			Pattern.DOTALL);

	private static final Image droolsIcon = DroolsPluginImages
			.getImage(DroolsPluginImages.DROOLS);

	private static final Image dslIcon = DroolsPluginImages
			.getImage(DroolsPluginImages.DSL_EXPRESSION);

	private static final Image classIcon = DroolsPluginImages
			.getImage(DroolsPluginImages.CLASS);

	private static final Pattern START_OF_CONSEQUENCE = Pattern.compile(
			".*then\\s*", Pattern.DOTALL);

	private DSLTree dslTree = new DSLTree();

	public RuleCompletionProcessor(AbstractRuleEditor editor) {
		super(editor);
	}

	protected List getCompletionProposals(ITextViewer viewer,
			final int documentOffset) {
		try {
			final List list = new ArrayList();
			DSLAdapter adapter = getDSLAdapter(viewer);

			IDocument doc = viewer.getDocument();
			String backText = readBackwards(documentOffset, doc);

			final String prefix = CompletionUtil.stripLastWord(backText);

			if (backText.length() < 5) {
				return list;
			}

			if (consequence(backText)) {
				List dslConsequences = adapter.listConsequenceItems();
				addDSLProposals(list, prefix, dslConsequences);
				if (!adapter.hasConsequences()) {
					// only add functions and keywords if at the beginning of a
					// new statement
					String backTextWithoutPrefix = backText.substring(0,
							backText.length() - prefix.length());
					if (START_OF_CONSEQUENCE.matcher(backTextWithoutPrefix)
							.matches()
							|| START_OF_NEW_JAVA_STATEMENT.matcher(
									backTextWithoutPrefix).matches()) {
						addRHSCompletionProposals(list, prefix);
						addRHSFunctionCompletionProposals(viewer, list, prefix);
					}
					addRHSJavaCompletionProposals(list, backText, prefix);
				}
			} else if (condition(backText) || query(backText)) {
				String lastobj = this.getLastNonDashLine(backText);
				String last = this.getLastLine(backText);
				// we have to check if the last line is when. if it is we set
				// the last line to zero length string
				if (last.equals("when")) {
					last = "";
					lastobj = "*";
				}
				// pass the last string in the backText to getProposals
				List dslConditions = this.getProposals(lastobj, last);
				// if we couldn't find any matches, we add the list from
				// the DSLAdapter so that there's something
				if (dslConditions.size() == 0) {
					dslConditions.addAll(adapter.listConditionItems());
				}
				addDSLProposals(list, prefix, dslConditions);
				addLHSCompletionProposals(viewer, list, adapter, prefix,
						backText);
			} else {
				// we are in rule header
				addRuleHeaderItems(list, prefix);
			}

			filterProposalsOnPrefix(prefix, list);
			return list;
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
		}
		return null;
	}

	private void addLHSCompletionProposals(ITextViewer viewer, final List list,
			DSLAdapter adapter, final String prefix, String backText)
			throws CoreException, DroolsParserException {
		Iterator iterator;
		if (!adapter.hasConditions()) {
			// determine location in condition
			LocationDeterminator.Location location = LocationDeterminator
					.getLocationInCondition(backText);

			switch (location.getType()) {
			case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION:
				// if we are at the beginning of a new condition
				// add drools keywords
				list.add(new RuleCompletionProposal(prefix.length(), "and",
						"and ", droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), "or",
						"or ", droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), "from",
						"from ", droolsIcon));
				RuleCompletionProposal prop = new RuleCompletionProposal(prefix
						.length(), "eval", "eval(  )", 6);
				prop.setImage(droolsIcon);
				list.add(prop);
				prop = new RuleCompletionProposal(prefix.length(), "then",
						"then" + System.getProperty("line.separator") + "\t");
				prop.setImage(droolsIcon);
				list.add(prop);
				// we do not break but also add all elements that are needed for
				// and/or
			case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_AND_OR:
				list.add(new RuleCompletionProposal(prefix.length(), "not",
						"not ", droolsIcon));
				// we do not break but also add all elements that are needed for
				// not
			case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_NOT:
				list.add(new RuleCompletionProposal(prefix.length(), "exists",
						"exists ", droolsIcon));
				// we do not break but also add all elements that are needed for
				// exists
			case LocationDeterminator.LOCATION_FROM_ACCUMULATE:
			case LocationDeterminator.LOCATION_FROM_COLLECT:
			case LocationDeterminator.LOCATION_BEGIN_OF_CONDITION_EXISTS:
				// and add imported classes
				List imports = getDRLEditor().getImports();
				iterator = imports.iterator();
				while (iterator.hasNext()) {
					String name = (String) iterator.next();
					int index = name.lastIndexOf(".");
					if (index != -1) {
						String className = name.substring(index + 1);
						RuleCompletionProposal p = new RuleCompletionProposal(
								prefix.length(), className, className + "(  )",
								className.length() + 2);
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
						RuleCompletionProposal p = new RuleCompletionProposal(
								prefix.length(), className, className + "(  )",
								className.length() + 2);
						p.setPriority(-1);
						p.setImage(classIcon);
						list.add(p);
					}
				}
				Set templates = getDRLEditor().getTemplates();
				iterator = templates.iterator();
				while (iterator.hasNext()) {
					String name = (String) iterator.next();
					RuleCompletionProposal p = new RuleCompletionProposal(
							prefix.length(), name, name + "(  )",
							name.length() + 2);
					p.setPriority(-1);
					p.setImage(classIcon);
					list.add(p);
				}
				break;
			case LocationDeterminator.LOCATION_INSIDE_CONDITION_START:
				String className = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
				if (className != null) {
					boolean isTemplate = addFactTemplatePropertyProposals(
							prefix, className, list);
					if (!isTemplate) {
						ClassTypeResolver resolver = new ClassTypeResolver(
								getDRLEditor().getImports(), ProjectClassLoader
										.getProjectClassLoader(getEditor()));
						try {
							Class clazz = resolver.resolveType(className);
							if (clazz != null) {
								Iterator iterator2 = new ClassFieldInspector(
										clazz).getFieldNames().keySet()
										.iterator();
								while (iterator2.hasNext()) {
									String name = (String) iterator2.next();
									RuleCompletionProposal p = new RuleCompletionProposal(
											prefix.length(), name, name + " ");
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
				}
				break;
			case LocationDeterminator.LOCATION_INSIDE_CONDITION_OPERATOR:
				className = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
				String property = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME);
				String type = getPropertyClass(className, property);

				list.add(new RuleCompletionProposal(prefix.length(), "==",
						"== ", droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), "!=",
						"!= ", droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), ":", ": ",
						droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), "->",
						"-> (  )", 5, droolsIcon));

				if (isComparable(type)) {
					list.add(new RuleCompletionProposal(prefix.length(), "<",
							"< ", droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(), "<=",
							"<= ", droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(), ">",
							"> ", droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(), ">=",
							">= ", droolsIcon));
				}
				if (type.equals("java.lang.String")) {
					list.add(new RuleCompletionProposal(prefix.length(),
							"matches", "matches \"\"", 9, droolsIcon));
				}
				if (isSubtypeOf(type, "java.util.Collection")) {
					list.add(new RuleCompletionProposal(prefix.length(),
							"contains", "contains ", droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(),
							"excludes", "excludes ", droolsIcon));
				}
				break;
			case LocationDeterminator.LOCATION_INSIDE_CONDITION_ARGUMENT:
				// determine type
				className = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_CLASS_NAME);
				property = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_PROPERTY_NAME);
				String operator = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_OPERATOR);
				type = getPropertyClass(className, property);

				if ("contains".equals(operator) || "excludes".equals(operator)) {
					type = "java.lang.Object";
				}

				boolean isObject = false;
				if ("java.lang.Object".equals(type)) {
					isObject = true;
				}

				list.add(new RuleCompletionProposal(prefix.length(), "null",
						"null ", droolsIcon));
				if ("boolean".equals(type)) {
					list.add(new RuleCompletionProposal(prefix.length(),
							"true", "true ", droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(),
							"false", "false ", droolsIcon));
				}
				if (isObject || "java.lang.String".equals(type)) {
					list.add(new RuleCompletionProposal(prefix.length(),
							"\"\"", "\"\"", 1, droolsIcon));
				}
				if (isObject || "java.util.Date".equals(type)) {
					list
							.add(new RuleCompletionProposal(prefix.length(),
									"\"dd-mmm-yyyy\"", "\"dd-mmm-yyyy\"", 1,
									droolsIcon));
				}
				list.add(new RuleCompletionProposal(prefix.length(), "()",
						"(  )", 2, droolsIcon));
				DrlParser parser = new DrlParser();
				try {
					PackageDescr descr = parser.parse(backText);
					List rules = descr.getRules();
					if (rules != null && rules.size() == 1) {
						Map result = new HashMap();
						getRuleParameters(result, ((RuleDescr) rules.get(0))
								.getLhs().getDescrs());
						Iterator iterator2 = result.entrySet().iterator();
						while (iterator2.hasNext()) {
							Map.Entry entry = (Map.Entry) iterator2.next();
							String paramName = (String) entry.getKey();
							String paramType = (String) entry.getValue();
							if (isSubtypeOf(paramType, type)) {
								RuleCompletionProposal proposal = new RuleCompletionProposal(
										prefix.length(), paramName);
								proposal.setPriority(-1);
								proposal.setImage(methodIcon);
								list.add(proposal);
							}
						}
					}
				} catch (DroolsParserException exc) {
					// do nothing
				}
				break;
			case LocationDeterminator.LOCATION_INSIDE_EVAL:
				String content = (String) location
						.getProperty(LocationDeterminator.LOCATION_EVAL_CONTENT);
				list.addAll(getJavaCompletionProposals(content, prefix,
						getRuleParameters(backText)));
				break;
			case LocationDeterminator.LOCATION_INSIDE_CONDITION_END:
				list.add(new RuleCompletionProposal(prefix.length(), "&", "& ",
						droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), "|", "| ",
						droolsIcon));
				list.add(new RuleCompletionProposal(prefix.length(), ",", ", ",
						droolsIcon));
				break;
			case LocationDeterminator.LOCATION_FROM:
				String fromText = (String) location
						.getProperty(LocationDeterminator.LOCATION_FROM_CONTENT);
				int index = fromText.indexOf('.');
				if (index == -1) {
					// add accumulate and collect keyword
					list
							.add(new RuleCompletionProposal(
									prefix.length(),
									"accumulate",
									"accumulate (  , init (  ), action (  ), result (  ) )",
									13, droolsIcon));
					list.add(new RuleCompletionProposal(prefix.length(),
							"collect", "collect (  )", 10, droolsIcon));
					// add all functions
					if ("".equals(fromText)) {
						List functions = getDRLEditor().getFunctions();
						iterator = functions.iterator();
						while (iterator.hasNext()) {
							String name = (String) iterator.next() + "()";
							prop = new RuleCompletionProposal(prefix.length(),
									name, name, name.length() - 1);
							prop.setPriority(-1);
							prop.setImage(methodIcon);
							list.add(prop);
						}
					}
					list.addAll(getJavaCompletionProposals(fromText, prefix,
							getRuleParameters(backText)));
				}
				break;
			case LocationDeterminator.LOCATION_FROM_ACCUMULATE_INIT_INSIDE:
				content = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
				list.addAll(getJavaCompletionProposals(content, prefix,
						getRuleParameters(backText)));
				break;
			case LocationDeterminator.LOCATION_FROM_ACCUMULATE_ACTION_INSIDE:
				content = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
				content += (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT);
				list.addAll(getJavaCompletionProposals(content, prefix,
						getRuleParameters(backText)));
				break;
			case LocationDeterminator.LOCATION_FROM_ACCUMULATE_RESULT_INSIDE:
				content = (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
				content += (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT);
				content += (String) location
						.getProperty(LocationDeterminator.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT);
				list.addAll(getJavaCompletionProposals(content, prefix,
						getRuleParameters(backText)));
				break;
			}
		}
	}

	private String getPropertyClass(String className, String propertyName) {
		if (className != null && propertyName != null) {
			FactTemplateDescr template = getDRLEditor().getTemplate(className);
			if (template != null) {
				Iterator iterator = template.getFields().iterator();
				while (iterator.hasNext()) {
					FieldTemplateDescr field = (FieldTemplateDescr) iterator
							.next();
					if (propertyName.equals(field.getName())) {
						String type = field.getClassType();
						if (isPrimitiveType(type)) {
							return type;
						}
						ClassTypeResolver resolver = new ClassTypeResolver(
								getDRLEditor().getImports(), ProjectClassLoader
										.getProjectClassLoader(getEditor()));
						try {
							Class clazz = resolver.resolveType(type);
							if (clazz != null) {
								return clazz.getName();
							}
						} catch (ClassNotFoundException exc) {
							exc.printStackTrace();
							// Do nothing
						}
					}
				}
				// if not found, return null
			} else {
				ClassTypeResolver resolver = new ClassTypeResolver(
						getDRLEditor().getImports(), ProjectClassLoader
								.getProjectClassLoader(getEditor()));
				try {
					Class clazz = resolver.resolveType(className);
					if (clazz != null) {
						Class clazzz = (Class) new ClassFieldInspector(clazz)
								.getFieldTypes().get(propertyName);
						if (clazzz != null) {
							return clazzz.getName();
						}
					}
				} catch (IOException exc) {
					// Do nothing
				} catch (ClassNotFoundException exc) {
					// Do nothing
				}
			}
		}
		return null;
	}

	private Map getRuleParameters(String backText) {
		Map result = new HashMap();
		try {
			DrlParser parser = new DrlParser();
			PackageDescr descr = parser.parse(backText);
			List rules = descr.getRules();
			if (rules != null && rules.size() == 1) {
				getRuleParameters(result, ((RuleDescr) rules.get(0)).getLhs()
						.getDescrs());
			}
		} catch (DroolsParserException exc) {
			// do nothing
		}
		return result;
	}

	private boolean isComparable(String type) {
		if (type == null) {
			return false;
		}
		if (isPrimitiveNumericType(type)) {
			return true;
		}
		if (isObjectNumericType(type)) {
			return true;
		}
		if (isSubtypeOf(type, "java.lang.Comparable")) {
			return true;
		}
		return false;
	}

	private boolean isPrimitiveType(String type) {
		return isPrimitiveNumericType(type) || type.equals("boolean");
	}

	private boolean isPrimitiveNumericType(String type) {
		return type.equals("byte") || type.equals("short")
				|| type.equals("int") || type.equals("long")
				|| type.equals("float") || type.equals("double")
				|| type.equals("char");
	}

	private boolean isObjectNumericType(String type) {
		return type.equals("java.lang.Byte") || type.equals("java.lang.Short")
				|| type.equals("java.lang.Integer")
				|| type.equals("java.lang.Long")
				|| type.equals("java.lang.Float")
				|| type.equals("java.lang.Double")
				|| type.equals("java.lang.Char");
	}

	/**
	 * Returns true if the first class is the same or a subtype of the second
	 * class.
	 * 
	 * @param class1
	 * @param class2
	 * @return
	 */
	private boolean isSubtypeOf(String class1, String class2) {
		if (class1 == null || class2 == null) {
			return false;
		}
		class1 = convertToNonPrimitiveClass(class1);
		class2 = convertToNonPrimitiveClass(class2);
		// TODO add code to take primitive types into account
		ClassTypeResolver resolver = new ClassTypeResolver(getDRLEditor()
				.getImports(), ProjectClassLoader
				.getProjectClassLoader(getEditor()));
		try {
			Class clazz1 = resolver.resolveType(class1);
			Class clazz2 = resolver.resolveType(class2);
			if (clazz1 == null || clazz2 == null) {
				return false;
			}
			return clazz2.isAssignableFrom(clazz1);
		} catch (ClassNotFoundException exc) {
			return false;
		}
	}

	private String convertToNonPrimitiveClass(String clazz) {
		if (!isPrimitiveType(clazz)) {
			return clazz;
		}
		if ("byte".equals(clazz)) {
			return "java.lang.Byte";
		} else if ("short".equals(clazz)) {
			return "java.lang.Short";
		} else if ("int".equals(clazz)) {
			return "java.lang.Integer";
		} else if ("long".equals(clazz)) {
			return "java.lang.Long";
		} else if ("float".equals(clazz)) {
			return "java.lang.Float";
		} else if ("double".equals(clazz)) {
			return "java.lang.Double";
		} else if ("char".equals(clazz)) {
			return "java.lang.Char";
		} else if ("boolean".equals(clazz)) {
			return "java.lang.Boolean";
		}
		// should never occur
		return null;
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
		while (st.hasMoreTokens()) {
			if (st.nextToken().trim().equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	private void addRHSFunctionCompletionProposals(ITextViewer viewer,
			final List list, final String prefix) throws CoreException,
			DroolsParserException {
		Iterator iterator;
		RuleCompletionProposal prop;
		List functions = getDRLEditor().getFunctions();
		iterator = functions.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next() + "()";
			prop = new RuleCompletionProposal(prefix.length(), name,
					name + ";", name.length() - 1);
			prop.setPriority(-1);
			prop.setImage(methodIcon);
			list.add(prop);
		}
	}

	private void addRHSCompletionProposals(final List list, final String prefix) {
		RuleCompletionProposal prop = new RuleCompletionProposal(prefix
				.length(), "modify", "modify();", 7);
		prop.setImage(droolsIcon);
		list.add(prop);
		prop = new RuleCompletionProposal(prefix.length(), "retract",
				"retract();", 8);
		prop.setImage(droolsIcon);
		list.add(prop);
		prop = new RuleCompletionProposal(prefix.length(), "assert",
				"assert();", 7);
		prop.setImage(droolsIcon);
		list.add(prop);
		prop = new RuleCompletionProposal(prefix.length(), "assertLogical",
				"assertLogical();", 14);
		prop.setImage(droolsIcon);
		list.add(prop);
	}

	private void addRHSJavaCompletionProposals(List list, String backText,
			String prefix) {
		int thenPosition = backText.lastIndexOf("then");
		String conditions = backText.substring(0, thenPosition);
		String consequence = backText.substring(thenPosition + 4);
		list.addAll(getJavaCompletionProposals(consequence, prefix,
				getRuleParameters(conditions)));
	}

	private void getRuleParameters(Map result, List descrs) {
		if (descrs == null) {
			return;
		}
		Iterator iterator = descrs.iterator();
		while (iterator.hasNext()) {
			BaseDescr descr = (BaseDescr) iterator.next();
			getRuleParameters(result, descr);
		}
	}

	private void getRuleParameters(Map result, BaseDescr descr) {
		if (descr == null) {
			return;
		}
		if (descr instanceof PatternDescr) {
			String name = ((PatternDescr) descr).getIdentifier();
			if (name != null) {
				result.put(name, ((PatternDescr) descr).getObjectType());
			}
			getRuleSubParameters(result, ((PatternDescr) descr).getDescrs(),
					((PatternDescr) descr).getObjectType());
		} else if (descr instanceof AndDescr) {
			getRuleParameters(result, ((AndDescr) descr).getDescrs());
		} else if (descr instanceof OrDescr) {
			getRuleParameters(result, ((OrDescr) descr).getDescrs());
		} else if (descr instanceof ExistsDescr) {
			getRuleParameters(result, ((ExistsDescr) descr).getDescrs());
		} else if (descr instanceof NotDescr) {
			getRuleParameters(result, ((NotDescr) descr).getDescrs());
		} else if (descr instanceof FromDescr) {
			getRuleParameters(result, ((FromDescr) descr).getReturnedPattern());
		} else if (descr instanceof AccumulateDescr) {
			AccumulateDescr accumulateDescr = (AccumulateDescr) descr;
			getRuleParameters(result, accumulateDescr.getResultPattern());
			if (accumulateDescr.getSourcePattern() != null) {
				getRuleParameters(result, accumulateDescr.getSourcePattern());
			}
		}

	}

	private void getRuleSubParameters(Map result, List descrs, String clazz) {
		if (descrs == null) {
			return;
		}
		Iterator iterator = descrs.iterator();
		while (iterator.hasNext()) {
			BaseDescr descr = (BaseDescr) iterator.next();
			if (descr instanceof FieldBindingDescr) {
				FieldBindingDescr fieldDescr = (FieldBindingDescr) descr;
				String name = fieldDescr.getIdentifier();
				String field = fieldDescr.getFieldName();
				String type = getPropertyClass(clazz, field);
				if (name != null) {
					result.put(name, type);
				}
			}
		}
	}

	private void addRuleHeaderItems(final List list, final String prefix) {
		list.add(new RuleCompletionProposal(prefix.length(), "salience",
				"salience ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "no-loop",
				"no-loop ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "agenda-group",
				"agenda-group ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "duration",
				"duration ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "auto-focus",
				"auto-focus ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "when", "when"
				+ System.getProperty("line.separator") + "\t ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(),
				"activation-group", "activation-group ", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "date-effective",
				"date-effective \"dd-MMM-yyyy\"", 16, droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "date-expires",
				"date-expires \"dd-MMM-yyyy\"", 14, droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "enabled",
				"enabled false", droolsIcon));
		list.add(new RuleCompletionProposal(prefix.length(), "ruleflow-group",
				"ruleflow-group \"\"", 16, droolsIcon));
        list.add(new RuleCompletionProposal(prefix.length(), "lock-on-active",
                                            "lock-on-active ", droolsIcon));        
	}

	private void addDSLProposals(final List list, final String prefix,
			List dslItems) {
		Iterator iterator = dslItems.iterator();
		while (iterator.hasNext()) {
			String consequence = (String) iterator.next();
			RuleCompletionProposal p = new RuleCompletionProposal(prefix
					.length(), consequence);
			p.setImage(dslIcon);
			list.add(p);
		}
	}

	/**
	 * Lazily get the adapter for DSLs, and cache it with the editor for future
	 * reference. If it is unable to load a DSL, it will try again next time.
	 * But once it has found and loaded one, it will keep it until the editor is
	 * closed.
	 * 
	 * This delegates to DSLAdapter to poke around the project to try and load
	 * the DSL.
	 */
	private DSLAdapter getDSLAdapter(ITextViewer viewer) {
		// TODO: cache DSL adapter in plugin, and reset when dsl file saved
		// retrieve dsl name always (might have changed) and try retrieving
		// cached dsl from plugin first
		// return new DSLAdapter(viewer.getDocument().get(), ((FileEditorInput)
		// getEditor().getEditorInput()).getFile());
		DSLAdapter adapter = getDRLEditor().getDSLAdapter();
		if (adapter == null) {
			String content = viewer.getDocument().get();
			adapter = new DSLAdapter(content, ((FileEditorInput) getEditor()
					.getEditorInput()).getFile());
			if (adapter.isValid()) {
				getDRLEditor().setDSLAdapter(adapter);
			}
			if (this.dslTree.isEmpty()) {
				try {
					Reader dslContents = DSLAdapter.getDSLContent(content,
						((FileEditorInput) getEditor().getEditorInput()).getFile());
					if (dslContents != null) {
						this.dslTree.buildTree(dslContents);
					}
				} catch (CoreException e) {
					DroolsEclipsePlugin.log(e);
				}
			}
		}
		return adapter;
	}

	private boolean addFactTemplatePropertyProposals(String prefix,
			String templateName, List list) {
		FactTemplateDescr descr = getDRLEditor().getTemplate(templateName);
		if (descr == null) {
			return false;
		}
		Iterator iterator = descr.getFields().iterator();
		while (iterator.hasNext()) {
			FieldTemplateDescr field = (FieldTemplateDescr) iterator.next();
			String fieldName = field.getName();
			RuleCompletionProposal p = new RuleCompletionProposal(prefix
					.length(), fieldName, fieldName + " ");
			p.setImage(methodIcon);
			list.add(p);
		}
		return true;
	}

	/**
	 * because of how the backText works, we need to get the last line, so that
	 * we can pass it to the DSLUtility
	 * 
	 * @param backText
	 * @return
	 */
	public String getLastLine(String backText) {
		BufferedReader breader = new BufferedReader(new StringReader(backText));
		String last = "";
		String line = null;
		try {
			while ((line = breader.readLine()) != null) {
				// only if the line has text do we set last to it
				if (line.length() > 0) {
					last = line;
				}
			}
		} catch (IOException e) {
			// TODO need to log this.
			// I'm leaving this for mic_hat, so he has something to do
		}
		// now that all the conditions for a single object are on the same line
		// we need to check for the left parenthesis
		if (last.indexOf("(") > -1) {
			last = last.substring(last.lastIndexOf("(") + 1);
		}
		// if the string has a comma "," we get the substring starting from
		// the index after the last comma
		if (last.indexOf(",") > -1) {
			last = last.substring(last.lastIndexOf(",") + 1);
		}
		// if the line ends with right parenthesis, we change it to zero length
		// string
		if (last.endsWith(")")) {
			last = "";
		}
		return last;
	}

	/**
	 * Returns the last line that doesn't start with a dash
	 * 
	 * @param backText
	 * @return
	 */
	public String getLastNonDashLine(String backText) {
		BufferedReader breader = new BufferedReader(new StringReader(backText));
		String last = "";
		String line = null;
		try {
			while ((line = breader.readLine()) != null) {
				// there may be blank lines, so we trim first
				line = line.trim();
				// only if the line has text do we set last to it
				if (line.length() > 0 && !line.startsWith("-")) {
					last = line;
				}
			}
		} catch (IOException e) {
			// TODO need to log this.
			// I'm leaving this for mic_hat, so he has something to do
		}
		if (last.indexOf("(") > -1 && !last.endsWith(")")) {
			last = last.substring(0, last.indexOf("("));
		} else if (last.indexOf("(") > -1 && last.endsWith(")")) {
			last = "";
		}
		return last;
	}

	/**
	 * The DSLTree is configurable. It can either return just the child of the
	 * last token found, or it can traverse the tree and generate all the
	 * combinations beneath the last matching node. TODO I don't know how to add
	 * configuration to the editor, so it needs to be hooked up to the
	 * configuration for the editor later.
	 * 
	 * @param last
	 * @return
	 */
	protected List getProposals(String obj, String last) {
		if (last.length() == 0) {
			last = " ";
		}
		return this.dslTree.getChildrenList(obj, last, false);
	}
}
