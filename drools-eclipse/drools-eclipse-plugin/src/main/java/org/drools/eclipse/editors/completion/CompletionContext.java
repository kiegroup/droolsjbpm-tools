package org.drools.eclipse.editors.completion;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.drools.lang.DRLLexer;
import org.drools.lang.DRLParser;
import org.drools.lang.DroolsEditorType;
import org.drools.lang.DroolsToken;
import org.drools.lang.DroolsTreeAdaptor;
import org.drools.lang.Location;

/**
 * A utility class that invokes the DRLParser on some partial drl text, and
 * provides information back about the context of that parsed drl, such as a
 * location type, a dialect, and so on.
 * 
 */
public class CompletionContext {

	static final Pattern MVEL_DIALECT_PATTERN = Pattern.compile(
			".*dialect\\s+\"mvel\".*", Pattern.DOTALL);

	static final Pattern JAVA_DIALECT_PATTERN = Pattern.compile(
			".*dialect\\s+\"java\".*", Pattern.DOTALL);

	static final String MVEL_DIALECT = "mvel";
	static final String JAVA_DIALECT = "java";

	private LinkedList<Object> parserList;
	private int location;
	private int locationIndex;
	private String dialect;

	@SuppressWarnings("unchecked")
	public CompletionContext(String backText) {
		DRLParser parser = getParser(backText);

		try {
			parser.compilation_unit();
		} catch (Exception ex) {
		}
		parserList = parser.getEditorInterface().get(0).getContent();
		deriveLocation();
		determineDialect(backText);
	}

	public boolean isJavaDialect() {
		return JAVA_DIALECT.equalsIgnoreCase(dialect);
	}

	public boolean isMvelDialect() {
		return MVEL_DIALECT.equalsIgnoreCase(dialect);
	}

	public boolean isDefaultDialect() {
		return !isJavaDialect() && !isMvelDialect();
	}

	// note: this is a crude but reasonably fast way to determine the dialect,
	// especially when parsing incomplete rules
	private void determineDialect(String backText) {
		dialect = null;
		boolean mvel = MVEL_DIALECT_PATTERN.matcher(backText).matches();
		boolean java = JAVA_DIALECT_PATTERN.matcher(backText).matches();
		if (mvel) {
			dialect = MVEL_DIALECT;
		} else if (java) {
			dialect = JAVA_DIALECT;
		}
	}

	public Location getLocation() {
		Location location = new Location(this.location);
		switch (this.location) {
			case Location.LOCATION_LHS_INSIDE_CONDITION_START:
				int index = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					Object o = parserList.get(index - 1);
					if (o instanceof DroolsToken) {
						String className = ((DroolsToken) o).getText(); 
						location.setProperty(Location.LOCATION_PROPERTY_CLASS_NAME, className);	
					}
				}
				String propertyName = null;
				if (locationIndex + 1 < parserList.size()) {
					propertyName = "";
				}
				int i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						propertyName += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME, propertyName);	
				break;
			case Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR:
				index = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					Object o = parserList.get(index - 1);
					if (o instanceof DroolsToken) {
						String className = ((DroolsToken) o).getText(); 
						location.setProperty(Location.LOCATION_PROPERTY_CLASS_NAME, className);	
					}
				}
				propertyName = null;
				index = findToken(Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					if (index + 1 < locationIndex) {
						propertyName = "";
					}
					i = index + 1;
					while (i < locationIndex) {
						Object o = parserList.get(i++);
						if (o instanceof DroolsToken) {
							String token = ((DroolsToken) o).getText();
							if (":".equals(token)) {
								propertyName = "";
							} else {
								propertyName += token;
							}
						} else {
							break;
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME, propertyName);
				}
				break;
			case Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT:
				int index1 = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				int index2 = findToken(Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR, locationIndex);
				int index3 = findToken(Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index1 != -1 && index2 != -1) {
					Object o = parserList.get(index1 - 1);
					if (o instanceof DroolsToken) {
						String className = ((DroolsToken) o).getText(); 
						location.setProperty(Location.LOCATION_PROPERTY_CLASS_NAME, className);	
					}
				}
				String operator = "";
				for (i = index2 + 1; i < locationIndex; i++) {
					Object o = parserList.get(i);
					if (o instanceof DroolsToken) {
						operator += ((DroolsToken) o).getText(); 
					}
					if (i < locationIndex - 1) {
						operator += " ";
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_OPERATOR, operator);
				propertyName = null;
				if (index1 != -1) {
					if (index3 + 1 < locationIndex - 1) {
						propertyName = "";
					}
					i = index3 + 1;
					while (i < locationIndex - 1) {
						Object o = parserList.get(i++);
						if (o instanceof DroolsToken) {
							String token = ((DroolsToken) o).getText();
							if (":".equals(token)) {
								propertyName = "";
							} else {
								propertyName += token;
							}
						} else {
							break;
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME, propertyName);
				}
				break;
			case Location.LOCATION_LHS_INSIDE_CONDITION_END:
				index = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					Object o = parserList.get(index - 1);
					if (o instanceof DroolsToken) {
						String className = ((DroolsToken) o).getText(); 
						location.setProperty(Location.LOCATION_PROPERTY_CLASS_NAME, className);	
					}
				}
				break;
			case Location.LOCATION_LHS_INSIDE_EVAL:
				String eval = "";
				i = locationIndex + 2;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						eval += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_EVAL_CONTENT, eval);	
				break;
			case Location.LOCATION_LHS_FROM:
				String from = null;
				if (locationIndex + 1 < parserList.size()) {
					from = "";
				}
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_FROM_CONTENT, from);	
				break;
			case Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE:
				from = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
					if (i < parserList.size()) {
						from += " ";
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, from);
				break;
			case Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE:
				from = "";
				index = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, locationIndex);
				index2 = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION, locationIndex);
				if (index != -1 && index2 != -1) {
					for (i = index + 1; i < index2 - 2; i++) {
						Object o = parserList.get(i);
						if (o instanceof DroolsToken) {
							from += ((DroolsToken) o).getText(); 
						}
						if (i < index2 - 3) {
							from += " ";
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, from);
				}
				from = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
					if (i < parserList.size()) {
						from += " ";
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT, from);
				break;
			case Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE:
				from = "";
				index = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, locationIndex);
				index2 = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION, locationIndex);
				index3 = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE, locationIndex);
				int index4 = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_REVERSE, locationIndex);
				if (index != -1 && index2 != -1) {
					for (i = index + 1; i < index2 - 2; i++) {
						Object o = parserList.get(i);
						if (o instanceof DroolsToken) {
							from += ((DroolsToken) o).getText(); 
						}
						if (i < index2 - 3) {
							from += " ";
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, from);
				}
				from = "";
				if (index3 != -1 && index4 != -1) {
					for (i = index3 + 1; i < index4 - 2; i++) {
						Object o = parserList.get(i);
						if (o instanceof DroolsToken) {
							from += ((DroolsToken) o).getText(); 
						}
						if (i < index4 - 3) {
							from += " ";
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT, from);
				}
				from = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
					if (i < parserList.size()) {
						from += " ";
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT, from);
				break;
			case Location.LOCATION_RHS:
				String rhs = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						rhs += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_RHS_CONTENT, rhs);	
				break;
			case Location.LOCATION_RULE_HEADER:
				String header = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						header += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_HEADER_CONTENT, header);
				break;
			case Location.LOCATION_RULE_HEADER_KEYWORD:
				header = "";
				index = findToken(Location.LOCATION_RULE_HEADER, locationIndex);
				if (index != -1) {
					for (i = index + 1; i < locationIndex; i++) {
						Object o = parserList.get(i);
						if (o instanceof DroolsToken) {
							header += ((DroolsToken) o).getText(); 
						}
					}
				}
				if (locationIndex + 1 < parserList.size()) {
					header += " ";
				}
				i = locationIndex + 1;
				while (i < parserList.size()) {
					Object o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						header += ((DroolsToken) o).getText(); 
					}
					if (i != parserList.size()) {
						header += " ";
					}
				}
				location.setProperty(Location.LOCATION_HEADER_CONTENT, header);
				break;
		}
		return location;
	}
	
	public String getRuleName() {
		if (parserList.size() >= 2) {
			Object o = parserList.get(1);
			if (o instanceof DroolsToken) {
				String name = ((DroolsToken) o).getText();
				if (name.startsWith("\"") && (name.endsWith("\""))) {
					name = name.substring(1, name.length() - 1);
				}
				return name;
			}
		}
		return null;
	}
	
	/** 
	 * Returns the variables defined in the given rule (fragment).
	 * The key is the name of the variable.
	 * The value is a list of 2 String:
	 *  - the first one is the class name of the variable
	 *  - the second one is the property of the given class that defines the type of this variable,
	 *    note that this property could be nested,
	 *    if this property is null then the given class is the type of the variable 
	 */
	public Map<String, String[]> getRuleParameters() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		int i = 0;
		int lastLocation = -1;
		for (Object o: parserList) {
			if (o instanceof DroolsToken) {
				DroolsToken token = (DroolsToken) o;
				if (DroolsEditorType.IDENTIFIER_VARIABLE.equals(token.getEditorType())) {
					String variableName = token.getText();
					if (lastLocation == Location.LOCATION_LHS_BEGIN_OF_CONDITION) {
						int j = i + 2;
						String className = "";
						while (j < parserList.size()) {
							Object obj = parserList.get(j++);
							if (obj instanceof DroolsToken) {
								String s = ((DroolsToken) obj).getText();
								if ("(".equals(s)) {
									result.put(variableName, new String[] { className, null });
									break;
								} else {
									className += s; 
								}
								
							}
						}
					} else if (lastLocation == Location.LOCATION_LHS_INSIDE_CONDITION_START) {
						int index = findToken(Location.LOCATION_LHS_BEGIN_OF_CONDITION, i);
						int j = index + 3;
						String className = "";
						while (j < i) {
							Object obj = parserList.get(j++);
							if (obj instanceof DroolsToken) {
								String s = ((DroolsToken) obj).getText();
								if ("(".equals(s)) {
									break;
								} else {
									className += s; 
								}
								
							}
						}
						j = i + 2;
						String propertyName = "";
						while (j < parserList.size()) {
							Object obj = parserList.get(j++);
							if (obj instanceof DroolsToken) {
								String s = ((DroolsToken) obj).getText();
								if (",".equals(s) || ")".equals(s)) {
									result.put(variableName, new String[] { className, propertyName });
									break;
								} else {
									propertyName += s; 
								}
							} else {
								result.put(variableName, new String[] { className, propertyName });
							}
						}
					}
				}
			} else if (o instanceof Integer) {
				lastLocation = (Integer) o;
			}
			i++;
		}
		return result;
	}

	private int findToken(String token, int integer, int location) {
		int index = location - 1;
		while (index >= 0) {
			Object o = parserList.get(index);
			if (o instanceof DroolsToken) {
				if ("(".equals(((DroolsToken) o).getText())) {
					o = parserList.get(index + 1);
					if (o instanceof Integer) {
						if (integer == (Integer) o) {
							return index;
						}
					}
				}
			}
			index--;
		}
		return -1;
	}

	private int findToken(int token, int location) {
		int index = location - 1;
		while (index >= 0) {
			Object o = parserList.get(index);
			if (o instanceof Integer) {
				if (token == (Integer) o) {
					return index;
				}
			}
			index--;
		}
		return -1;
	}

	private void deriveLocation() {
		location = -1;
		int i = 0;
		for (Object object : parserList) {
			if (object instanceof Integer) {
				location = (Integer) object;
				locationIndex = i;
			}
			i++;
		}
	}

	private DRLParser getParser(final String text) {
		DRLParser parser = new DRLParser(new CommonTokenStream(new DRLLexer(
				new ANTLRStringStream(text))));
		parser.setTreeAdaptor(new DroolsTreeAdaptor());
		parser.enableEditorInterface();
		return parser;
	}

}