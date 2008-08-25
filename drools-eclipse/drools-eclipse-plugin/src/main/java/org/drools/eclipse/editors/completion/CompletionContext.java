package org.drools.eclipse.editors.completion;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.drools.lang.DRLLexer;
import org.drools.lang.DRLParser;
import org.drools.lang.DroolsToken;
import org.drools.lang.DroolsTreeAdaptor;
import org.drools.lang.Location;
import org.drools.lang.descr.RuleDescr;

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
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME, propertyName);
				}
				break;
			case Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT:
				index = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					Object o = parserList.get(index - 1);
					if (o instanceof DroolsToken) {
						String className = ((DroolsToken) o).getText(); 
						location.setProperty(Location.LOCATION_PROPERTY_CLASS_NAME, className);	
					}
				}
				Object o = parserList.get(locationIndex - 1);
				if (o instanceof DroolsToken) {
					location.setProperty(Location.LOCATION_PROPERTY_OPERATOR, ((DroolsToken) o).getText());
				}
				propertyName = null;
				index = findToken(Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex - 1);
				if (index != -1) {
					if (index + 1 < locationIndex - 1) {
						propertyName = "";
					}
					i = index + 1;
					while (i < locationIndex - 1) {
						o = parserList.get(i++);
						if (o instanceof DroolsToken) {
							String token = ((DroolsToken) o).getText();
							if (":".equals(token)) {
								propertyName = "";
							} else {
								propertyName += token;
							}
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_PROPERTY_NAME, propertyName);
				}
				break;
			case Location.LOCATION_LHS_INSIDE_CONDITION_END:
				index = findToken("(", Location.LOCATION_LHS_INSIDE_CONDITION_START, locationIndex);
				if (index != -1) {
					o = parserList.get(index - 1);
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
					o = parserList.get(i++);
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
					o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_FROM_CONTENT, from);	
				break;
			case Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE:
				System.out.println(parserList);
				from = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, from);
				break;
			case Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE:
				System.out.println(parserList);
				from = "";
				index = findToken(Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE, locationIndex);
				if (index != -1) {
					for (i = index + 1; i < locationIndex - 1; i++) {
						o = parserList.get(i);
						if (o instanceof DroolsToken) {
							from += ((DroolsToken) o).getText(); 
						}
					}
					location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, from);
				}
				from = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						from += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT, from);
				break;
			case Location.LOCATION_RHS:
				String rhs = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					o = parserList.get(i++);
					if (o instanceof DroolsToken) {
						rhs += ((DroolsToken) o).getText(); 
					}
				}
				location.setProperty(Location.LOCATION_RHS_CONTENT, rhs);	
				break;
			case Location.LOCATION_RULE_HEADER:
				System.out.println(parserList);
				String header = "";
				i = locationIndex + 1;
				while (i < parserList.size()) {
					o = parserList.get(i++);
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
	
	public RuleDescr getRule() {
		// TODO
		return null;
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