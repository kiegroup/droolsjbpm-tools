package org.drools.ide.editors.completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.ColumnDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.RuleDescr;

public class LocationDeterminator {

	static final int LOCATION_UNKNOWN = 0;
	static final int LOCATION_BEGIN_OF_CONDITION = 1;
	
	static final int LOCATION_INSIDE_CONDITION_START = 100;

	static final String LOCATION_PROPERTY_CLASS_NAME = "ClassName";
	
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
    			return determineLocationForDescr((RuleDescr) rules.get(0));
    		}
    	} catch (DroolsParserException exc) {
    		// do nothing
    	}
    	return new Location(LOCATION_UNKNOWN);
	}
	
	public static Location determineLocationForDescr(PatternDescr descr) {
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
			return determineLocationForDescr(subDescr);
		} else if (descr instanceof ColumnDescr) {
			Location location = new Location(LOCATION_INSIDE_CONDITION_START);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, ((ColumnDescr) descr).getObjectType());
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
				return determineLocationForDescr(subDescr);
			}
			return determineLocationForDescr(descr);
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
				return determineLocationForDescr(subDescr);
			}
			return determineLocationForDescr(descr);
			
		}
		
		return new Location(LOCATION_UNKNOWN);
	}
}
