package org.drools.eclipse.editors.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.base.ClassTypeResolver;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.lang.Location;
import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.FactTemplateDescr;
import org.drools.lang.descr.FieldBindingDescr;
import org.drools.lang.descr.FieldTemplateDescr;
import org.drools.lang.descr.GlobalDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.rule.builder.dialect.mvel.MVELConsequenceBuilder;
import org.drools.rule.builder.dialect.mvel.MVELDialect;
import org.drools.util.asm.ClassFieldInspector;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;
import org.mvel.CompiledExpression;
import org.mvel.ExpressionCompiler;
import org.mvel.ParserContext;

/**
 * For handling within rules.
 *
 * @author Michael Neale, Kris Verlanen
 */
public class RuleCompletionProcessor extends DefaultCompletionProcessor {

	private static final Image DROOLS_ICON = DroolsPluginImages
			.getImage(DroolsPluginImages.DROOLS);

	private static final Image CLASS_ICON = DroolsPluginImages
			.getImage(DroolsPluginImages.CLASS);

	/**
	 * A CompletionContext contains the DRL backtext parsing results, to avoid
	 * multilpe parser invocations
	 */
	private CompletionContext context;

	public RuleCompletionProcessor(AbstractRuleEditor editor) {
		super(editor);
	}

	protected List getCompletionProposals(ITextViewer viewer, int documentOffset) {
		try {
			final List list = new ArrayList();
			IDocument doc = viewer.getDocument();

			String backText = readBackwards(documentOffset, doc);
			final String prefix = CompletionUtil.stripLastWord(backText);

			// if inside the keyword "rule ", no code completion
			if (backText.length() < 5) {
				return list;
			}

			this.context = new CompletionContext(backText);
			Location location = context.getLocation();

			if (location.getType() == Location.LOCATION_RULE_HEADER) {
				addRuleHeaderProposals(list, documentOffset, prefix, backText);
			} else if (location.getType() == Location.LOCATION_RHS) {
				addRHSCompletionProposals(list, documentOffset, prefix, backText,
					(String) location.getProperty(Location.LOCATION_LHS_CONTENT),
					(String) location.getProperty(Location.LOCATION_RHS_CONTENT));
			} else {
				addLHSCompletionProposals(list, documentOffset, location, prefix, backText);
			}

			filterProposalsOnPrefix(prefix, list);
			return list;
		} catch (Throwable t) {
			DroolsEclipsePlugin.log(t);
		}
		return null;
	}

	protected void addRHSCompletionProposals(List list, int documentOffset, String prefix, String backText,
			String conditions, String consequence) {
		// only add functions and keywords if at the beginning of a
		// new statement
		if (consequence == null || consequence.length() < prefix.length()) {
			// possible if doing code completion directly after "then"
			return;
		}
		String consequenceWithoutPrefix =
			consequence.substring(0,consequence.length() - prefix.length());

        if ( context == null ) {
            context = new CompletionContext( backText );
        }

        boolean startOfDialectExpression = isStartOfDialectExpression( consequenceWithoutPrefix );
		if (startOfDialectExpression) {
			addRHSKeywordCompletionProposals(list, documentOffset, prefix);
			addRHSFunctionCompletionProposals(list, documentOffset, prefix);
		}
		if ( context.isJavaDialect() ) {
            addRHSJavaCompletionProposals( list,
            							   documentOffset,
            							   prefix,
                                           backText,
                                           consequence );
        } else {
            addRHSMvelCompletionProposals( list,
            							   documentOffset,
                                           prefix,
                                           backText,
                                           consequence,
                                           startOfDialectExpression );
        }
	}

	protected void addLHSCompletionProposals(List list, int documentOffset,
			Location location, String prefix, String backText) {
		switch (location.getType()) {
		case Location.LOCATION_LHS_BEGIN_OF_CONDITION:
			// if we are at the beginning of a new condition
			// add drools keywords
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "and",
					"and ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "or",
					"or ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "from",
					"from ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "forall",
					"forall(  )", 8, DROOLS_ICON));
			RuleCompletionProposal prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix
					.length(), "eval", "eval(  )", 6);
			prop.setImage(DROOLS_ICON);
			list.add(prop);
			prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "then",
					"then" + System.getProperty("line.separator") + "\t");
			prop.setImage(DROOLS_ICON);
			list.add(prop);
			// we do not break but also add all elements that are needed for
			// and/or
		case Location.LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR:
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "not",
					"not ", DROOLS_ICON));
			// we do not break but also add all elements that are needed for
			// not
		case Location.LOCATION_LHS_BEGIN_OF_CONDITION_NOT:
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "exists",
					"exists ", DROOLS_ICON));
			// we do not break but also add all elements that are needed for
			// exists
		case Location.LOCATION_LHS_FROM_ACCUMULATE:
		case Location.LOCATION_LHS_FROM_COLLECT:
		case Location.LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS:
			// and add imported classes
			Iterator iterator = getImports().iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				int index = name.lastIndexOf(".");
				if (index != -1) {
					String className = name.substring(index + 1);
					RuleCompletionProposal p = new RuleCompletionProposal(
							documentOffset - prefix.length(), prefix.length(), className, className + "(  )",
							className.length() + 2);
					p.setPriority(-1);
					p.setImage(CLASS_ICON);
					list.add(p);
				}
			}
			iterator = getClassesInPackage().iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				int index = name.lastIndexOf(".");
				if (index != -1) {
					String className = name.substring(index + 1);
					RuleCompletionProposal p = new RuleCompletionProposal(
							documentOffset - prefix.length(), prefix.length(), className, className + "(  )",
							className.length() + 2);
					p.setPriority(-1);
					p.setImage(CLASS_ICON);
					list.add(p);
				}
			}
			iterator = getTemplates().iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				RuleCompletionProposal p = new RuleCompletionProposal(
						documentOffset - prefix.length(), prefix.length(), name, name + "(  )",
						name.length() + 2);
				p.setPriority(-1);
				p.setImage(CLASS_ICON);
				list.add(p);
			}
			break;
		case Location.LOCATION_LHS_INSIDE_CONDITION_START:
			String className = (String) location
					.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME);
			String propertyName = (String) location
					.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME);
			if (className != null) {
				boolean isTemplate = addFactTemplatePropertyProposals(
						documentOffset, prefix, className, list);
				if (!isTemplate) {
					ClassTypeResolver resolver = new ClassTypeResolver(
							getUniqueImports(), ProjectClassLoader
									.getProjectClassLoader(getEditor()));
					try {
						String currentClass = className;
						if (propertyName != null) {
							String[] nestedProperties = propertyName.split("\\.");
							int nbSuperProperties = nestedProperties.length - 1;
							if (propertyName.endsWith(".")) {
								nbSuperProperties++;
							}
							for (int i = 0; i < nbSuperProperties; i++) {
								String simplePropertyName = nestedProperties[i];
								currentClass = getSimplePropertyClass(currentClass, simplePropertyName);
								currentClass = convertToNonPrimitiveClass(currentClass);
							}
						}
						RuleCompletionProposal p = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "this");
							p.setImage(METHOD_ICON);
							list.add(p);
						Class clazz = resolver.resolveType(currentClass);
						if (clazz != null) {
							if (Map.class.isAssignableFrom(clazz)) {
								p = new RuleCompletionProposal(documentOffset - prefix.length(),
									prefix.length(), "this['']", "this['']", 6);
								p.setImage(METHOD_ICON);
								list.add(p);
							}
							ClassFieldInspector inspector = new ClassFieldInspector(clazz);
							Map types = inspector.getFieldTypes();
							Iterator iterator2 = inspector.getFieldNames().keySet().iterator();
							while (iterator2.hasNext()) {
								String name = (String) iterator2.next();
								p = new RuleCompletionProposal(documentOffset - prefix.length(),
										prefix.length(), name, name + " ");
								p.setImage(METHOD_ICON);
								list.add(p);
								Class type = (Class) types.get(name);
								if (type != null && Map.class.isAssignableFrom(type)) {
									name += "['']";
									p = new RuleCompletionProposal(documentOffset - prefix.length(),
										prefix.length(), name, name, name.length() - 2);
									p.setImage(METHOD_ICON);
									list.add(p);
								}
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
		case Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR:
			className = (String) location
					.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME);
			String property = (String) location
					.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME);
			String type = getPropertyClass(className, property);

			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "==",
					"== ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "!=",
					"!= ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), ":", ": ",
					DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "->",
					"-> (  )", 5, DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "memberOf",
					"memberOf ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "not memberOf",
					"not memberOf ", DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "in",
					"in (  )", 5, DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "not in",
					"not in (  )", 9, DROOLS_ICON));

			if (isComparable(type)) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "<",
						"< ", DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "<=",
						"<= ", DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), ">",
						"> ", DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), ">=",
						">= ", DROOLS_ICON));
			}
			if (type.equals("java.lang.String")) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "matches",
						"matches \"\"", 9, DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"not matches", "not matches \"\"", 13, DROOLS_ICON));
			}
			if (isSubtypeOf(type, "java.util.Collection")) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"contains", "contains ", DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"not contains", "not contains ", DROOLS_ICON));
			}
			break;
		case Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT:
			// determine type
			className = (String) location
					.getProperty(Location.LOCATION_PROPERTY_CLASS_NAME);
			property = (String) location
					.getProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME);
			String operator = (String) location
					.getProperty(Location.LOCATION_PROPERTY_OPERATOR);
			type = getPropertyClass(className, property);

			if ("in".equals(operator)) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "()",
					"(  )", 2, DROOLS_ICON));
				break;
			}

			if ("contains".equals(operator) || "excludes".equals(operator)) {
				type = "java.lang.Object";
			}

			if ("memberOf".equals(operator)) {
				type = "java.util.Collection";
			}

			boolean isObject = false;
			if ("java.lang.Object".equals(type)) {
				isObject = true;
			}

			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "null",
					"null ", DROOLS_ICON));
			if ("boolean".equals(type)) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"true", "true ", DROOLS_ICON));
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"false", "false ", DROOLS_ICON));
			}
			if (isObject || "java.lang.String".equals(type)) {
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"\"\"", "\"\"", 1, DROOLS_ICON));
			}
			if (isObject || "java.util.Date".equals(type)) {
				list
						.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
								"\"dd-mmm-yyyy\"", "\"dd-mmm-yyyy\"", 1,
								DROOLS_ICON));
			}
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "()",
					"(  )", 2, DROOLS_ICON));
			// add parameters with possibly matching type
			if (context == null) {
				context = new CompletionContext(backText);
			}

			if (context.getRule() != null) {
				Map result = new HashMap();
				addRuleParameters(result, context.getRule().getLhs()
						.getDescrs());
				Iterator iterator2 = result.entrySet().iterator();
				while (iterator2.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator2.next();
					String paramName = (String) entry.getKey();
					String paramType = (String) entry.getValue();
					if (isSubtypeOf(paramType, type)) {
						RuleCompletionProposal proposal = new RuleCompletionProposal(
								documentOffset - prefix.length(), prefix.length(), paramName);
						proposal.setPriority(-1);
						proposal.setImage(VARIABLE_ICON);
						list.add(proposal);
					}
				}
			}
			// add globals with possibly matching type
			List globals = getGlobals();
			if (globals != null) {
				for (iterator = globals.iterator(); iterator.hasNext(); ) {
					GlobalDescr global = (GlobalDescr) iterator.next();
					if (isSubtypeOf(global.getType(), type)) {
						RuleCompletionProposal proposal = new RuleCompletionProposal(
								documentOffset - prefix.length(), prefix.length(), global.getIdentifier());
						proposal.setPriority(-1);
						proposal.setImage(VARIABLE_ICON);
						list.add(proposal);
					}
				}
			}
			break;
		case Location.LOCATION_LHS_INSIDE_EVAL:
			String content = (String) location
					.getProperty(Location.LOCATION_EVAL_CONTENT);
			list.addAll(getJavaCompletionProposals(documentOffset, content, prefix,
					getRuleParameters(backText)));
			break;
		case Location.LOCATION_LHS_INSIDE_CONDITION_END:
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "&&", "&& ",
					DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "||", "|| ",
					DROOLS_ICON));
			list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), ",", ", ",
					DROOLS_ICON));
			break;
		case Location.LOCATION_LHS_FROM:
			String fromText = (String) location
					.getProperty(Location.LOCATION_FROM_CONTENT);
			int index = fromText.indexOf('.');
			if (index == -1) {
				// add accumulate and collect keyword
				list
						.add(new RuleCompletionProposal(
								documentOffset - prefix.length(), prefix.length(),
								"accumulate",
								"accumulate (  , init (  ), action (  ), result (  ) )",
								13, DROOLS_ICON));
				PackageBuilderConfiguration config = new PackageBuilderConfiguration(
					ProjectClassLoader.getProjectClassLoader(getEditor()), null);
				Map accumulateFunctions = config.getAccumulateFunctionsMap();
				for (Iterator iterator2 = accumulateFunctions.keySet().iterator(); iterator2.hasNext(); ) {
					String accumulateFunction = (String) iterator2.next();
					list.add(new RuleCompletionProposal(
							documentOffset - prefix.length(), prefix.length(),
							"accumulate " + accumulateFunction,
							"accumulate (  , " + accumulateFunction + "(  ) )",
							13, DROOLS_ICON));
				}
				list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
						"collect", "collect (  )", 10, DROOLS_ICON));
				// add all functions
				if ("".equals(fromText)) {
					List functions = getFunctions();
					iterator = functions.iterator();
					while (iterator.hasNext()) {
						String name = (String) iterator.next() + "()";
						prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
								name, name, name.length() - 1);
						prop.setPriority(-1);
						prop.setImage(METHOD_ICON);
						list.add(prop);
					}
				}
				list.addAll(getJavaCompletionProposals(documentOffset, fromText, prefix,
						getRuleParameters(backText)));
			}
			break;
		case Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE:
			content = (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
			list.addAll(getJavaCompletionProposals(documentOffset, content, prefix,
					getRuleParameters(backText)));
			break;
		case Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE:
			content = (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
			content += (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT);
			list.addAll(getJavaCompletionProposals(documentOffset, content, prefix,
					getRuleParameters(backText)));
			break;
		case Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE:
			content = (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT);
			content += (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT);
			content += (String) location
					.getProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT);
			list.addAll(getJavaCompletionProposals(documentOffset, content, prefix,
					getRuleParameters(backText)));
			break;
		}
	}

	private String getPropertyClass(String className, String propertyName) {
		if (className != null && propertyName != null) {
			FactTemplateDescr template = getTemplate(className);
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
								getUniqueImports(), ProjectClassLoader
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
				String[] nestedProperties = propertyName.split("\\.");
				String currentClass = className;
				for (int i = 0; i < nestedProperties.length; i++) {
					String simplePropertyName = nestedProperties[i];
					currentClass = getSimplePropertyClass(currentClass, simplePropertyName);
				}
				return currentClass;
			}
		}
		return null;
	}

	private String getSimplePropertyClass(String className, String propertyName) {
		if ("this".equals(propertyName)) {
			return className;
		}
		if (propertyName.endsWith("]")) {
			// TODO can we take advantage of generics here?
			return "java.lang.Object";
		}
		ClassTypeResolver resolver = new ClassTypeResolver(
				getUniqueImports(), ProjectClassLoader
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
		return "java.lang.Object";
	}

	private Map getRuleParameters(String backText) {
		Map result = new HashMap();
		// add globals
		List globals = getGlobals();
		if (globals != null) {
			for (Iterator iterator = globals.iterator(); iterator.hasNext(); ) {
				GlobalDescr global = (GlobalDescr) iterator.next();
				result.put(global.getIdentifier(), global.getType());
			}
		}

		if (context == null) {
			context = new CompletionContext(backText);
		}
		if (context.getRule() == null) {
			return result;
		}
		// add parameters defined in conditions
		addRuleParameters(result, context.getRule().getLhs().getDescrs());
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
		ClassTypeResolver resolver = new ClassTypeResolver(getUniqueImports(), ProjectClassLoader
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

	private void addRHSFunctionCompletionProposals(List list, int documentOffset, String prefix) {
		Iterator iterator;
		RuleCompletionProposal prop;
		List functions = getFunctions();
		iterator = functions.iterator();
		while (iterator.hasNext()) {
			String name = (String) iterator.next() + "()";
			prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), name,
					name + ";", name.length() - 1);
			prop.setPriority(-1);
			prop.setImage(METHOD_ICON);
			list.add(prop);
		}
	}

	private void addRHSKeywordCompletionProposals(
			List list, int documentOffset, String prefix) {
		RuleCompletionProposal prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix
				.length(), "update", "update();", 7);
		prop.setImage(DROOLS_ICON);
		list.add(prop);
		prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "retract",
				"retract();", 8);
		prop.setImage(DROOLS_ICON);
		list.add(prop);
		prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "insert",
				"insert();", 7);
		prop.setImage(DROOLS_ICON);
		list.add(prop);
		prop = new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "insertLogical",
				"insertLogical();", 14);
		prop.setImage(DROOLS_ICON);
		list.add(prop);
	}

	private void addRHSJavaCompletionProposals(List list, int documentOffset, String prefix, String backText,
			String consequence) {
		list.addAll(getJavaCompletionProposals(documentOffset, consequence, prefix,
				getRuleParameters(backText)));
	}

	private void addRHSMvelCompletionProposals(List list, final int documentOffset, String prefix,
			String backText, String consequence, boolean expressionStart) {

		Collection mvelCompletionProposals = getMvelCompletionProposals(
				consequence, documentOffset, prefix, getRuleParameters(backText), backText,
				expressionStart);
		list.addAll(mvelCompletionProposals);
	}

	private Collection getMvelCompletionProposals(final String consequence,
			final int documentOffset, final String prefix, Map params, String backText,
			boolean startOfExpression) {

		final Set proposals = new HashSet();

		if (!(getEditor() instanceof DRLRuleEditor)) {
			return proposals;
		}

		// TODO: add logic from MVELConsequenceBuilder
		String compilableConsequence = CompletionUtil
				.getCompilableText(consequence);
		compilableConsequence = MVELConsequenceBuilder
				.delimitExpressions(compilableConsequence);

		// attempt to compile and analyze
		try {
			DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(
					(DRLRuleEditor) getEditor(), true, true);

			ParserContext compilationContext = createMvelAnalysisContext(
					params, drlInfo, compilableConsequence);

			if (startOfExpression) {
				Collection jdtProps = getJavaMvelCompletionProposals("",
						prefix, documentOffset, params);
				proposals.addAll(jdtProps);
				addMvelVariables(proposals, compilationContext, documentOffset, prefix);

				addMvelInputs(proposals, compilationContext, documentOffset, prefix);

			} else {
				// we are completing the methods for an existing type or
				// variable
				// find the last type in the expression to complete against

				if (!"".equals(compilableConsequence.trim())) {

					// Class lastType = new
					// PropertyVerifier(analyzableExpression,
					// compilationContext).analyze();
					Class lastType = context.getGetMvelReturnedType();
					if (lastType == null) {
						lastType = Object.class;
					}

					String javaText = "\n" + lastType.getName() + " o = new "
							+ lastType.getName() + "();\no.";
					Collection jdtProps = getJavaMvelCompletionProposals(
							javaText, prefix, documentOffset, params);
					proposals.addAll(jdtProps);
				}
			}

		} catch (Throwable e) {
			DroolsEclipsePlugin.log(e);
		}

		return proposals;
	}

	private ParserContext createMvelAnalysisContext(Map params,
			DRLInfo drlInfo, String compilableConsequence) {

		String currentRulename = context.getRule().getName();
		RuleInfo[] ruleInfos = drlInfo.getRuleInfos();
		RuleInfo currentRule = null;
		for (int i = 0; i < ruleInfos.length; i++) {
			if (currentRulename.equals(ruleInfos[i].getRuleName())) {
				currentRule = ruleInfos[i];
				break;
			}
		}
		// MVEL: test for null
		MVELDialect dialect = (MVELDialect) currentRule.getDialect();

		ExpressionCompiler compiler = new ExpressionCompiler(
				compilableConsequence);

		final ParserContext initialContext = new ParserContext();
		initialContext.setStrictTypeEnforcement(false);
		Map imports = dialect.getClassImportResolverFactory()
				.getImportedClasses();
		imports.putAll(dialect.getStaticMethodImportResolverFactory()
				.getImportedMethods());
		initialContext.setImports(imports);
		initialContext.setInterceptors(dialect.getInterceptors());
		initialContext.setInputs(params);
		initialContext.setCompiled(true);

		try {
			CompiledExpression expression = compiler.compile(initialContext);
			context.setGetMvelReturnedType(expression.getKnownEgressType());
			ParserContext compilationContext = compiler.getParserContextState();
			return compilationContext;
		} catch (Exception e) {
			return initialContext;
		}
	}

	private void addMvelInputs(final Collection proposals,
			ParserContext compilationContext, int documentOffset, String prefix) {
		Map inputs = compilationContext.getInputs();
		for (Iterator iter = inputs.keySet().iterator(); iter.hasNext();) {
			String prop = (String) iter.next();
			RuleCompletionProposal rcp = new RuleCompletionProposal(
				documentOffset - prefix.length(), prefix.length(), prop);
			rcp.setImage(DefaultCompletionProcessor.VARIABLE_ICON);
			proposals.add(rcp);
		}
	}

	private void addMvelVariables(final Collection proposals,
			ParserContext compilationContext, int documentOffset, String prefix) {
		Map variables = compilationContext.getVariables();
		for (Iterator iter = variables.keySet().iterator(); iter.hasNext();) {
			String prop = (String) iter.next();
			RuleCompletionProposal rcp = new RuleCompletionProposal(
				documentOffset - prefix.length(), prefix.length(), prop);
			rcp.setImage(DefaultCompletionProcessor.VARIABLE_ICON);
			proposals.add(rcp);
		}
	}

	private void addRuleParameters(Map result, List descrs) {
		if (descrs == null) {
			return;
		}
		Iterator iterator = descrs.iterator();
		while (iterator.hasNext()) {
			BaseDescr descr = (BaseDescr) iterator.next();
			addRuleParameters(result, descr);
		}
	}

	private void addRuleParameters(Map result, BaseDescr descr) {
		if (descr == null) {
			return;
		}
		if (descr instanceof PatternDescr) {
			String name = ((PatternDescr) descr).getIdentifier();
			if (name != null) {
				result.put(name, ((PatternDescr) descr).getObjectType());
			}
			addRuleSubParameters(result, ((PatternDescr) descr).getDescrs(),
					((PatternDescr) descr).getObjectType());
		} else if (descr instanceof AndDescr) {
			addRuleParameters(result, ((AndDescr) descr).getDescrs());
		} else if (descr instanceof OrDescr) {
			addRuleParameters(result, ((OrDescr) descr).getDescrs());
		} else if (descr instanceof ExistsDescr) {
			addRuleParameters(result, ((ExistsDescr) descr).getDescrs());
		} else if (descr instanceof NotDescr) {
			addRuleParameters(result, ((NotDescr) descr).getDescrs());
		}
	}

	private void addRuleSubParameters(Map result, List descrs, String clazz) {
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

	private void addRuleHeaderProposals(List list, int documentOffset, String prefix, String backText) {
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "salience",
				"salience ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "no-loop",
				"no-loop ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "agenda-group",
				"agenda-group ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "duration",
				"duration ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "auto-focus",
				"auto-focus ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "when", "when"
				+ System.getProperty("line.separator") + "\t ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(),
				"activation-group", "activation-group ", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "date-effective",
				"date-effective \"dd-MMM-yyyy\"", 16, DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "date-expires",
				"date-expires \"dd-MMM-yyyy\"", 14, DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "enabled",
				"enabled false", DROOLS_ICON));
		list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "ruleflow-group",
				"ruleflow-group \"\"", 16, DROOLS_ICON));
        list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "lock-on-active",
                "lock-on-active ", DROOLS_ICON));
        list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "dialect \"java\"",
        		"dialect \"java\" ", DROOLS_ICON));
        list.add(new RuleCompletionProposal(documentOffset - prefix.length(), prefix.length(), "dialect \"mvel\"",
        		"dialect \"mvel\" ", DROOLS_ICON));
	}

	private boolean addFactTemplatePropertyProposals(int documentOffset, String prefix,
			String templateName, List list) {
		FactTemplateDescr descr = getTemplate(templateName);
		if (descr == null) {
			return false;
		}
		Iterator iterator = descr.getFields().iterator();
		while (iterator.hasNext()) {
			FieldTemplateDescr field = (FieldTemplateDescr) iterator.next();
			String fieldName = field.getName();
			RuleCompletionProposal p = new RuleCompletionProposal(documentOffset - prefix.length(), prefix
					.length(), fieldName, fieldName + " ");
			p.setImage(METHOD_ICON);
			list.add(p);
		}
		return true;
	}

}
