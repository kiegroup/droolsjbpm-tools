package org.drools.eclipse.editors.completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.descr.AccumulateDescr;
import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.CollectDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.EvalDescr;
import org.drools.lang.descr.ExistsDescr;
import org.drools.lang.descr.FieldConstraintDescr;
import org.drools.lang.descr.FromDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RestrictionConnectiveDescr;
import org.drools.lang.descr.RuleDescr;

public class LocationDeterminator {

    static final Pattern PATTERN_PATTERN_START = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_OPERATOR = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_CONTAINS_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+contains\\s+[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_MATCHES_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+matches\\s+[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_EXCLUDES_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+excludes\\s+[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_IN_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+(not\\s+)?in\\s+[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_MEMBER_OF_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+(not\\s+)?memberOf\\s+[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_COMPARATOR_ARGUMENT = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s*([<>=!]+)\\s*[^\\s<>!=:]*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_CONTAINS_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+contains\\s+[^\\s<>!=:,]+\\s+", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_MATCHES_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+matches\\s+[^\\s<>!=:,]+\\s+", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_EXCLUDES_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+excludes\\s+[^\\s<>!=:,]+\\s+", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_IN_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+(not\\s+)?in\\s+\\([^\\)]+\\)\\s*", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_MEMBER_OF_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s+(not\\s+)?memberOf\\s+[^\\s<>!=:,]+\\s+", Pattern.DOTALL);
    static final Pattern PATTERN_PATTERN_COMPARATOR_END = Pattern.compile(".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s*([<>=!]+)\\s*[^\\s<>!=:,]+\\s+", Pattern.DOTALL);

    static final Pattern PATTERN_PATTERN = Pattern.compile("((\\S+)\\s*:\\s*)?(\\S+)\\s*(\\(.*)", Pattern.DOTALL);
    static final Pattern EXISTS_PATTERN = Pattern.compile(".*\\s+exists\\s*\\(?\\s*((\\S*)\\s*:)?\\s*\\S*", Pattern.DOTALL);
    static final Pattern NOT_PATTERN = Pattern.compile(".*\\s+not\\s*\\(?\\s*((\\S*)\\s*:)?\\s*\\S*", Pattern.DOTALL);
    static final Pattern EVAL_PATTERN = Pattern.compile(".*\\s+eval\\s*\\(\\s*([(^\\))(\\([^\\)]*\\)?)]*)", Pattern.DOTALL);
    static final Pattern FROM_PATTERN = Pattern.compile(".*\\)\\s+from\\s+", Pattern.DOTALL);
    static final Pattern ACCUMULATE_PATTERN = Pattern.compile(".*\\)\\s+from\\s+accumulate\\s*\\(\\s*", Pattern.DOTALL);
    static final Pattern ACCUMULATE_PATTERN_INIT = Pattern.compile(".*,\\s*init\\s*\\(\\s*(.*)", Pattern.DOTALL);
    static final Pattern ACCUMULATE_PATTERN_ACTION = Pattern.compile(".*,\\s*init\\s*\\(\\s*(.*)\\)\\s*,\\s*action\\s*\\(\\s*(.*)", Pattern.DOTALL);
    static final Pattern ACCUMULATE_PATTERN_RESULT = Pattern.compile(".*,\\s*init\\s*\\(\\s*(.*)\\)\\s*,\\s*action\\s*\\(\\s*(.*)\\)\\s*,\\s*result\\s*\\(\\s*(.*)", Pattern.DOTALL);
    static final Pattern COLLECT_PATTERN = Pattern.compile(".*\\)\\s+from\\s+collect\\s*\\(\\s*", Pattern.DOTALL);
    
    static final Pattern THEN_PATTERN = Pattern.compile(".*\n\\s*when\\s*(.*)\n\\s*then\\s*(.*)", Pattern.DOTALL);

    static final int LOCATION_UNKNOWN = 0;
	
	static final int LOCATION_LHS_BEGIN_OF_CONDITION = 1;
	static final int LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS = 2;
	static final int LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR = 3;
	static final int LOCATION_LHS_BEGIN_OF_CONDITION_NOT = 4;
	
	static final int LOCATION_LHS_INSIDE_CONDITION_START = 100;
	static final int LOCATION_LHS_INSIDE_CONDITION_OPERATOR = 101;
	static final int LOCATION_LHS_INSIDE_CONDITION_ARGUMENT = 102;
	static final int LOCATION_LHS_INSIDE_CONDITION_END = 103;

	static final int LOCATION_LHS_INSIDE_EVAL = 200;
	
	static final int LOCATION_LHS_FROM = 300;
	static final int LOCATION_LHS_FROM_COLLECT = 301;
	static final int LOCATION_LHS_FROM_ACCUMULATE = 302;
	static final int LOCATION_LHS_FROM_ACCUMULATE_INIT = 303;
	static final int LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE = 304;
	static final int LOCATION_LHS_FROM_ACCUMULATE_ACTION = 305;
	static final int LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE = 306;
	static final int LOCATION_LHS_FROM_ACCUMULATE_RESULT = 307;
	static final int LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE = 308;
	
	static final int LOCATION_RHS = 1000;
	static final int LOCATION_RULE_HEADER = 2000;
	
	static final String LOCATION_PROPERTY_CLASS_NAME = "ClassName";
	static final String LOCATION_PROPERTY_PROPERTY_NAME = "PropertyName";
	static final String LOCATION_PROPERTY_OPERATOR = "Operator";
	static final String LOCATION_EVAL_CONTENT = "EvalContent";
	static final String LOCATION_FROM_CONTENT = "FromContent";
	static final String LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT = "FromAccumulateInitContent";
	static final String LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT = "FromAccumulateActionContent";
	static final String LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT = "FromAccumulateResultContent";
	static final String LOCATION_LHS_CONTENT = "LHSContent";
	static final String LOCATION_RHS_CONTENT = "RHSContent";
	static final String LOCATION_HEADER_CONTENT = "HeaderContent";
	
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
    	
    	void setType(int type) {
    		this.type = type;
    	}
    }
    
	public static Location getLocation(String backText) {
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
	
	public static Location determineLocationForDescr(BaseDescr descr, String backText) {
		if (descr instanceof RuleDescr) {
			RuleDescr ruleDescr = (RuleDescr) descr;
			Object o = ruleDescr.getConsequence();
			if (o == null) {
				Matcher matcher = THEN_PATTERN.matcher(backText);
				if (matcher.matches()) {
					Location location = new Location(LOCATION_RHS);
					location.setProperty(LOCATION_LHS_CONTENT, matcher.group(1));
					location.setProperty(LOCATION_RHS_CONTENT, matcher.group(2));
					return location;
				}
			}
			AndDescr lhs = ruleDescr.getLhs();
			if (lhs == null) {
				return new Location(LOCATION_RULE_HEADER);
			}
			List subDescrs = lhs.getDescrs();
			if (subDescrs.size() == 0) {
				Matcher matcher = EXISTS_PATTERN.matcher(backText);
				if (matcher.matches()) {
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
				}
				matcher = NOT_PATTERN.matcher(backText);
				if (matcher.matches()) {
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
				}
				matcher = FROM_PATTERN.matcher(backText);
				if (matcher.matches()) {
					Location location = new Location(LOCATION_LHS_FROM);
					location.setProperty(LOCATION_FROM_CONTENT, "");
					return location;
				}
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION);
			}
			BaseDescr subDescr = (BaseDescr) subDescrs.get(subDescrs.size() - 1);
			if (subDescr == null) {
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION);
			}
			if (endReached(subDescr)) {
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION);
			}
			return determineLocationForDescr(subDescr, backText);
		} else if (descr instanceof PatternDescr) {
			PatternDescr patternDescr = (PatternDescr) descr;
//			int locationType;
//			String propertyName = null;
//			String evaluator = null;
//			boolean endOfConstraint = false;
//			List subDescrs = columnDescr.getDescrs();
//			if (subDescrs.size() > 0) {
//				BaseDescr lastDescr = (BaseDescr) subDescrs.get(subDescrs.size() - 1);
//				if (lastDescr.getEndCharacter() != -1) {
//					endOfConstraint = true;
//				}
//				if (lastDescr instanceof FieldConstraintDescr) {
//					FieldConstraintDescr lastFieldDescr = (FieldConstraintDescr) lastDescr;
//					propertyName = lastFieldDescr.getFieldName();
//					List restrictions = lastFieldDescr.getRestrictions();
//					if (restrictions.size() > 0) {
//						RestrictionDescr restriction = (RestrictionDescr) restrictions.get(restrictions.size() - 1);
//						if (restriction instanceof LiteralRestrictionDescr) {
//							LiteralRestrictionDescr literal = (LiteralRestrictionDescr) restriction;
//							evaluator = literal.getEvaluator();
//						} else if (restriction instanceof VariableRestrictionDescr) {
//							VariableRestrictionDescr variable = (VariableRestrictionDescr) restriction;
//							evaluator = variable.getEvaluator();
//						}
//					}
//				}
//			}
//			if (endOfConstraint) {
//				locationType = LOCATION_INSIDE_CONDITION_END;
//			} else if (evaluator != null) {
//				locationType = LOCATION_INSIDE_CONDITION_ARGUMENT;
//			} else if (propertyName != null) {
//				locationType = LOCATION_INSIDE_CONDITION_OPERATOR;
//			} else {
//				locationType = LOCATION_INSIDE_CONDITION_START;
//			}
//			Location location = new Location(locationType);
//			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, columnDescr.getObjectType());
//			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, propertyName);
//			location.setProperty(LOCATION_PROPERTY_OPERATOR, evaluator); 
//			return location;
			// TODO: this is not completely safe, there are rare occasions where this could fail
			Pattern pattern = Pattern.compile(".*(" + patternDescr.getObjectType() + ")\\s*\\((.*)", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(backText);
			String patternContents = null;
			while (matcher.find()) {
				patternContents = "(" + matcher.group(2);
			}
			if (patternContents == null) {
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION);
			}
			List subDescrs = patternDescr.getDescrs();
			if (subDescrs.size() > 0) {
				Object lastDescr = subDescrs.get(subDescrs.size() - 1);
				if (lastDescr instanceof FieldConstraintDescr) {
					FieldConstraintDescr lastFieldDescr = (FieldConstraintDescr) lastDescr;
					List restrictions = lastFieldDescr.getRestrictions();
					// if there are multiple restrictions, filter out all the rest so that
					// only the last one remains
					if (restrictions.size() > 2) {
						Object last = restrictions.get(restrictions.size() - 2);
						if (last instanceof RestrictionConnectiveDescr) {
							RestrictionConnectiveDescr lastRestr = (RestrictionConnectiveDescr) last;
							char connective = '&';
							if (lastRestr.getConnective() == RestrictionConnectiveDescr.OR) {
								connective = '|';
							}
							int connectiveLocation = patternContents.lastIndexOf(connective);
							patternContents = "( " + lastFieldDescr.getFieldName() + " " + patternContents.substring(connectiveLocation + 1);
						}
					}
					if (restrictions.size() > 1) {
						Object last = restrictions.get(restrictions.size() - 1);
						if (last instanceof RestrictionConnectiveDescr) {
							RestrictionConnectiveDescr lastRestr = (RestrictionConnectiveDescr) last;
							char connective = '&';
							if (lastRestr.getConnective() == RestrictionConnectiveDescr.OR) {
								connective = '|';
							}
							int connectiveLocation = patternContents.lastIndexOf(connective);
							patternContents = "( " + lastFieldDescr.getFieldName() + " " + patternContents.substring(connectiveLocation + 1);
						}
					}
				}
			}
			return getLocationForPatttern(patternContents, patternDescr.getObjectType());
		} else if (descr instanceof ExistsDescr) {
			List subDescrs = ((ExistsDescr) descr).getDescrs();
			if (subDescrs.size() == 0) {
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
			}
			if (subDescrs.size() == 1) {
				BaseDescr subDescr = (BaseDescr) subDescrs.get(0);
				if (subDescr == null) {
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
				}
				Location result = determineLocationForDescr(subDescr, backText);
				if (result.getType() == LOCATION_LHS_BEGIN_OF_CONDITION) {
					result.setType(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
				}
				return result;
			}
			return determineLocationForDescr(descr, backText);
		} else if (descr instanceof NotDescr) {
			List subDescrs = ((NotDescr) descr).getDescrs();
			if (subDescrs.size() == 0) {
				return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
			}
			if (subDescrs.size() == 1) {
				BaseDescr subDescr = (BaseDescr) subDescrs.get(0);
				if (subDescr == null) {
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
				}
				Location location = determineLocationForDescr(subDescr, backText);
				if (location.getType() == LOCATION_LHS_BEGIN_OF_CONDITION) {
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
				}
				return location;
			}
			return determineLocationForDescr(descr, backText);
		} else if (descr instanceof AndDescr) {
			List subDescrs = ((AndDescr) descr).getDescrs();
			int size = subDescrs.size();
			if (size == 2) {
				BaseDescr subDescr = (BaseDescr) subDescrs.get(1);
				if (subDescr == null) {
					Matcher matcher = EXISTS_PATTERN.matcher(backText);
					if (matcher.matches()) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
					}
					matcher = NOT_PATTERN.matcher(backText);
					if (matcher.matches()) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
					}
					return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR);
				} else {
					Location location = determineLocationForDescr(subDescr, backText);
					if (location.getType() == LOCATION_LHS_BEGIN_OF_CONDITION) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR);
					}
					return location;
				}
			}
			return new Location(LOCATION_UNKNOWN);
		} else if (descr instanceof OrDescr) {
			List subDescrs = ((OrDescr) descr).getDescrs();
			int size = subDescrs.size();
			if (size == 2) {
				BaseDescr subDescr = (BaseDescr) subDescrs.get(1);
				if (subDescr == null) {
					Matcher matcher = EXISTS_PATTERN.matcher(backText);
					if (matcher.matches()) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_EXISTS);
					}
					matcher = NOT_PATTERN.matcher(backText);
					if (matcher.matches()) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_NOT);
					}return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR);
				} else {
					Location location = determineLocationForDescr(subDescr, backText);
					if (location.getType() == LOCATION_LHS_BEGIN_OF_CONDITION) {
						return new Location(LOCATION_LHS_BEGIN_OF_CONDITION_AND_OR);
					}
					return location;
				}
			}
			return new Location(LOCATION_UNKNOWN);
		} else if (descr instanceof FromDescr) {
			Location location = new Location(LOCATION_LHS_FROM);
			String content = CompletionUtil.stripWhiteSpace(backText);
			location.setProperty(LOCATION_FROM_CONTENT, content);
			return location;
		} else if (descr instanceof AccumulateDescr) {
			Matcher matcher = ACCUMULATE_PATTERN.matcher(backText);
			int end = -1;
			while (matcher.find()) {
				end = matcher.end();
			}
			String accumulateText = backText.substring(end);
			matcher = ACCUMULATE_PATTERN_RESULT.matcher(accumulateText);
			if (matcher.matches()) {
				Location location = new Location(LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE);
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, matcher.group(1));
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT, matcher.group(2));
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT, matcher.group(3));
				return location;
			}
			matcher = ACCUMULATE_PATTERN_ACTION.matcher(accumulateText);
			if (matcher.matches()) {
				Location location =  new Location(LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE);
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, matcher.group(1));
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT, matcher.group(2));
				return location;
			}
			matcher = ACCUMULATE_PATTERN_INIT.matcher(accumulateText);
			if (matcher.matches()) {
				Location location =  new Location(LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE);
				location.setProperty(LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT, matcher.group(1));
				return location;
			}
			matcher = PATTERN_PATTERN.matcher(accumulateText);
			if (matcher.matches()) {
				String className = matcher.group(3);
				String patternContents = matcher.group(4);
				return getLocationForPatttern(patternContents, className);
			}
			return new Location(LOCATION_LHS_FROM_ACCUMULATE);
		} else if (descr instanceof CollectDescr) {
			Matcher matcher = COLLECT_PATTERN.matcher(backText);
			int end = -1;
			while (matcher.find()) {
				end = matcher.end();
			}
			String collectText = backText.substring(end);
			matcher = PATTERN_PATTERN.matcher(collectText);
			if (matcher.matches()) {
				String className = matcher.group(3);
				String columnContents = matcher.group(4);
				return getLocationForPatttern(columnContents, className);
			}
			return new Location(LOCATION_LHS_FROM_COLLECT);
		} else if (descr instanceof EvalDescr) {
			Matcher matcher = EVAL_PATTERN.matcher(backText);
			if (matcher.matches()) {
				String content = matcher.group(1);
				Location location = new Location(LOCATION_LHS_INSIDE_EVAL);
				location.setProperty(LOCATION_EVAL_CONTENT, content);
				return location;
			}
		}
		
		return new Location(LOCATION_UNKNOWN);
	}
	
	private static boolean endReached(BaseDescr descr) {
		if (descr == null) {
			return false;
		}
		if (descr instanceof PatternDescr) {
			return descr.getEndCharacter() != -1;
		} else if (descr instanceof ExistsDescr) {
			List descrs = ((ExistsDescr) descr).getDescrs();
			if (descrs.isEmpty()) {
				return false;
			}
			return endReached((BaseDescr) descrs.get(0));
		} else if (descr instanceof NotDescr) {
			List descrs = ((NotDescr) descr).getDescrs();
			if (descrs.isEmpty()) {
				return false;
			}
			return endReached((BaseDescr) descrs.get(0));
		} else if (descr instanceof NotDescr) {
			List descrs = ((NotDescr) descr).getDescrs();
			if (descrs.isEmpty()) {
				return false;
			}
			return endReached((BaseDescr) descrs.get(0));
		} else if (descr instanceof AndDescr) {
			List descrs = ((AndDescr) descr).getDescrs();
			if (descrs.size() != 2) {
				return false;
			}
			return endReached((BaseDescr) descrs.get(0))
				&& endReached((BaseDescr) descrs.get(1));
		} else if (descr instanceof OrDescr) {
			List descrs = ((OrDescr) descr).getDescrs();
			if (descrs.size() != 2) {
				return false;
			}
			return endReached((BaseDescr) descrs.get(0))
				&& endReached((BaseDescr) descrs.get(1));
		} else if (descr instanceof EvalDescr) {
			return ((EvalDescr) descr).getContent() != null;
		}
		return descr.getEndCharacter() != -1;
//		else if (descr instanceof AccumulateDescr) {
//			return ((AccumulateDescr) descr).getResultCode() != null;
//		} else if (descr instanceof CollectDescr) {
//			return ((CollectDescr) descr).getSourceColumn() != null;
//		}
//		return false;
	}
	
	private static Location getLocationForPatttern(String patternContents, String className) {
		Matcher matcher = PATTERN_PATTERN_OPERATOR.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_OPERATOR);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			return location;
        }
        matcher = PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, matcher.group(4));
			return location;
        }
        matcher = PATTERN_PATTERN_CONTAINS_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, "contains");
			return location;
        }
        matcher = PATTERN_PATTERN_EXCLUDES_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, "excludes");
			return location;
        }
        matcher = PATTERN_PATTERN_IN_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, "in");
			return location;
        }
        matcher = PATTERN_PATTERN_MEMBER_OF_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, "memberOf");
			return location;
        }
        matcher = PATTERN_PATTERN_MATCHES_ARGUMENT.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_ARGUMENT);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			location.setProperty(LOCATION_PROPERTY_PROPERTY_NAME, matcher.group(3));
			location.setProperty(LOCATION_PROPERTY_OPERATOR, "matches");
			return location;
        }
        matcher = PATTERN_PATTERN_CONTAINS_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
        matcher = PATTERN_PATTERN_MATCHES_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
        matcher = PATTERN_PATTERN_EXCLUDES_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
        matcher = PATTERN_PATTERN_IN_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
        matcher = PATTERN_PATTERN_MEMBER_OF_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
        matcher = PATTERN_PATTERN_COMPARATOR_END.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
		matcher = PATTERN_PATTERN_START.matcher(patternContents);
        if (matcher.matches()) {
			Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_START);
			location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
			return location;
        }
		Location location = new Location(LOCATION_LHS_INSIDE_CONDITION_END);
		location.setProperty(LOCATION_PROPERTY_CLASS_NAME, className);
		return location;
	}
}
