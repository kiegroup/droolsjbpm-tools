package org.drools.ide.editors.completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.ColumnDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;

public class LocationDeterminator {

    static final Pattern COLUMN_PATTERN_START = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern COLUMN_PATTERN_OPERATOR = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:]+)\\s+", Pattern.DOTALL);
    static final Pattern COLUMN_PATTERN_CONTAINS_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:]+)\\s+contains\\s+", Pattern.DOTALL);
    static final Pattern COLUMN_PATTERN_MATCHES_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:]+)\\s+matches\\s+", Pattern.DOTALL);
    static final Pattern COLUMN_PATTERN_EXCLUDES_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:]+)\\s+excludes\\s+", Pattern.DOTALL);
    static final Pattern COLUMN_PATTERN_COMPARATOR_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:]+)\\s*([<>=!]+)\\s*[^\\s<>!=:]*", Pattern.DOTALL);

	static final int LOCATION_UNKNOWN = 0;
	static final int LOCATION_BEGIN_OF_CONDITION = 1;
	
	static final int LOCATION_INSIDE_CONDITION_START = 100;
	static final int LOCATION_INSIDE_CONDITION_OPERATOR = 101;
	static final int LOCATION_INSIDE_CONDITION_ARGUMENT = 102;

	static final String LOCATION_PROPERTY_CLASS_NAME = "ClassName";
	static final String LOCATION_PROPERTY_PROPERTY_NAME = "PropertyName";
	
    private LocationDeterminator() {
	}
	
    public static class Location {
    	private int type;
    	private Map properties = new HashMap();
    	
    	public Location(int type) {
    		this.type = type;
    	}
    	
    	public int getType() {
    		return type;
    	}
    	
    	public void setProperty(String name, Object value) {
    		properties.put(name, value);
    	}
    	
    	public Object getProperty(String name) {
    		return properties.get(name);
    	}
    }
    
	public static Location getLocationInCondition(String backText) {
		DrlParser parser = new DrlParser();
    	try {
    		PackageDescr packageDescr = parser.parse(backText);
    		List rules = packageDescr.getRules();
    		if (rules != null && rules.size() == 1) {
    			return determineLocationForDescr((RuleDescr) rules.get(0), backText);
    		}
    	} catch (DroolsParserException exc) {
    		// do nothing
    	}
    	return new Location(LOCATION_UNKNOWN);
	}
	
	public static Location determineLocationForDescr(PatternDescr descr, String backText) {
		if (descr instanceof RuleDescr) {
			RuleDescr ruleDescr = (RuleDescr) descr;
			List subDescrs = ruleDescr.getLhs().getDescrs();
			if (subDescrs.size() == 0) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			PatternDescr subDescr = (PatternDescr) subDescrs.get(subDescrs.size() - 1);
			if (subDescr == null) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			if (subDescr.getEndLine() != 0 || subDescr.getEndColumn() != 0) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			return determineLocationForDescr(subDescr, backText);
		} else if (descr instanceof ColumnDescr) {
			ColumnDescr columnDescr = (ColumnDescr) descr;
			// TODO: this is not completely safe, there are rare occasions where this could fail
			Pattern pattern = Pattern.compile(".*(" + columnDescr.getObjectType() + ")\\s*\\((.*)");
			Matcher matcher = pattern.matcher(backText);
			String columnContents = null;
			while (matcher.find()) {
				columnContents = "(" + matcher.group(2);
			}
			if (columnContents == null) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			matcher = COLUMN_PATTERN_OPERATOR.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_OPERATOR);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				return location;
	        }
	        matcher = COLUMN_PATTERN_COMPARATOR_ARGUMENT.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_ARGUMENT);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
				return location;
	        }
	        matcher = COLUMN_PATTERN_CONTAINS_ARGUMENT.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_ARGUMENT);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
				return location;
	        }
	        matcher = COLUMN_PATTERN_EXCLUDES_ARGUMENT.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_ARGUMENT);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
				return location;
	        }
	        matcher = COLUMN_PATTERN_MATCHES_ARGUMENT.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_ARGUMENT);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
				return location;
	        }
			matcher = COLUMN_PATTERN_START.matcher(columnContents);
	        if (matcher.matches()) {
				Location location = new Location(LOCATION_INSIDE_CONDITION_START);
				location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
				return location;
	        }
			Location location = new Location(LOCATION_INSIDE_CONDITION_START);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
			return location;
		} else if (descr instanceof ExistsDescr) {
			List subDescrs = ((ExistsDescr) descr).getDescrs();
			if (subDescrs.size() == 0) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			if (subDescrs.size() == 1) {
				PatternDescr subDescr = (PatternDescr) subDescrs.get(0);
				if (subDescr == null) {
					return new Location(LOCATION_BEGIN_OF_CONDITION);
				}
				if (subDescr.getEndLine() != 0 || subDescr.getEndColumn() != 0) {
					return new Location(LOCATION_BEGIN_OF_CONDITION);
				}
				return determineLocationForDescr(subDescr, backText);
			}
			return determineLocationForDescr(descr, backText);
		} else if (descr instanceof NotDescr) {
			List subDescrs = ((NotDescr) descr).getDescrs();
			if (subDescrs.size() == 0) {
				return new Location(LOCATION_BEGIN_OF_CONDITION);
			}
			if (subDescrs.size() == 1) {
				PatternDescr subDescr = (PatternDescr) subDescrs.get(0);
				if (subDescr == null) {
					return new Location(LOCATION_BEGIN_OF_CONDITION);
				}
				if (subDescr.getEndLine() != 0 || subDescr.getEndColumn() != 0) {
					return new Location(LOCATION_BEGIN_OF_CONDITION);
				}
				return determineLocationForDescr(subDescr, backText);
			}
			return determineLocationForDescr(descr, backText);
			
		}
		
		return new Location(LOCATION_UNKNOWN);
	}
}
