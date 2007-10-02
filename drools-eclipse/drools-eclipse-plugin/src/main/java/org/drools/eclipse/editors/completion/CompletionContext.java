package org.drools.eclipse.editors.completion;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.lang.Location;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;

/**
 * A utility class that invokes the DRLParser on some partial drl text, and provides
 * information back about the context of that parserd drl,
 * such as a location type, a dialect, and so on.
 *
 */
public class CompletionContext {

    static final Pattern PATTERN_PATTERN_OPERATOR            = Pattern.compile( ".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\),]+)(\\s*([<>=!]+)\\s*[^\\s<>!=:]*\\s*(&&|\\|\\|))*\\s+",
                                                                                Pattern.DOTALL );

    static final Pattern PATTERN_PATTERN_COMPARATOR_ARGUMENT = Pattern.compile( ".*[(,](\\s*(\\S*)\\s*:)?\\s*([^\\s<>!=:\\(\\)]+)\\s*(([<>=!]+)\\s*[^\\s<>!=:]+\\s*(&&|\\|\\|)\\s*)*([<>=!]+)\\s*[^\\s<>!=:]*",
                                                                                Pattern.DOTALL );

    static final Pattern EVAL_PATTERN                        = Pattern.compile( ".*\\s+eval\\s*\\(\\s*([(^\\))(\\([^\\)]*\\)?)]*)",
                                                                                Pattern.DOTALL );

    static final Pattern ACCUMULATE_PATTERN_INIT             = Pattern.compile( ".*,?\\s*init\\s*\\(\\s*(.*)",
                                                                                Pattern.DOTALL );

    static final Pattern ACCUMULATE_PATTERN_ACTION           = Pattern.compile( ".*,?\\s*init\\s*\\(\\s*(.*)\\)\\s*,?\\s*action\\s*\\(\\s*(.*)",
                                                                                Pattern.DOTALL );

    static final Pattern ACCUMULATE_PATTERN_REVERSE          = Pattern.compile( ".*,?\\s*init\\s*\\(\\s*(.*)\\)\\s*,?\\s*action\\s*\\(\\s*(.*)\\)\\s*,?\\s*reverse\\s*\\(\\s*(.*)",
                                                                                Pattern.DOTALL );

    static final Pattern ACCUMULATE_PATTERN_RESULT           = Pattern.compile( ".*,?\\s*init\\s*\\(\\s*(.*)\\)\\s*,?\\s*action\\s*\\(\\s*(.*)\\)\\s*,?(\\s*reverse\\s*\\(\\s*(.*)\\)\\s*,?)?\\s*result\\s*\\(\\s*(.*)",
                                                                                Pattern.DOTALL );

    static final Pattern THEN_PATTERN                        = Pattern.compile( ".*\n\\s*when\\s*(.*)\n\\s*then\\s*(.*)",
                                                                                Pattern.DOTALL );

    static final Pattern ENDS_WITH_SPACES                    = Pattern.compile( ".*\\s+",
                                                                                Pattern.DOTALL );

    static final Pattern ENDS_WITH_COLON                     = Pattern.compile( ".*:\\s*",
                                                                                Pattern.DOTALL );

    static final Pattern ENDS_WITH_BRACKET                   = Pattern.compile( ".*\\)\\s*",
                                                                                Pattern.DOTALL );

    static final Pattern MVEL_DIALECT_PATTERN                = Pattern.compile( ".*dialect\\s+\"mvel\".*",
                                                                                Pattern.DOTALL );

    static final Pattern JAVA_DIALECT_PATTERN                = Pattern.compile( ".*dialect\\s+\"java\".*",
                                                                                Pattern.DOTALL );

    static final String  MVEL_DIALECT                        = "mvel";
    static final String  JAVA_DIALECT                        = "java";

    private String       backText;
    private DrlParser    parser;
    private RuleDescr    rule;
    private PackageDescr packageDescr;
    private String       dialect;

    public CompletionContext(String backText) {
        this.backText = backText;
        this.parser = new DrlParser();

        try {
            packageDescr = parser.parse( backText );
            List rules = packageDescr.getRules();
            if ( rules != null && rules.size() == 1 ) {
                this.rule = (RuleDescr) rules.get( 0 );
            }

        } catch ( DroolsParserException exc ) {
            // do nothing
        }

        //FIXME: the whole story of dialect determination for completion needs beefing up
        determineDialect( backText );
    }

    public boolean isJavaDialect() {
        return JAVA_DIALECT.equalsIgnoreCase( dialect );
    }

    public boolean isMvelDialect() {
        return MVEL_DIALECT.equalsIgnoreCase( dialect );
    }

    public boolean isDefaultDialect() {
        return !isJavaDialect() && !isMvelDialect();
    }

    public PackageDescr getPackageDescr() {
        return packageDescr;
    }

    //note: this is a crude but reasonably fast way to determine the dialect,
    //especially when parsing imcomplete rules
    private void determineDialect(String backText) {
        dialect = null;
        boolean mvel = MVEL_DIALECT_PATTERN.matcher( backText ).matches();
        boolean java = JAVA_DIALECT_PATTERN.matcher( backText ).matches();
        //which dialect may be defined for this rule?
        if ( mvel ) {
            dialect = MVEL_DIALECT;
        }
        if ( java ) {
            dialect = JAVA_DIALECT;
        }
    }

    public Location getLocation() {
        if ( backText == null || rule == null ) {
            return new Location( Location.LOCATION_UNKNOWN );
        }
        return determineLocationForDescr( rule,
                                          parser.getLocation(),
                                          backText );
    }

    public RuleDescr getRule() {
        return rule;
    }

    private static Location determineLocationForDescr(BaseDescr descr,
                                                      Location location,
                                                      String backText) {
        if ( location.getType() == Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR ) {
            if ( !ENDS_WITH_SPACES.matcher( backText ).matches() || ENDS_WITH_COLON.matcher( backText ).matches() ) {
                location.setType( Location.LOCATION_LHS_INSIDE_CONDITION_START );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_INSIDE_CONDITION_END ) {
            if ( !backText.endsWith( " " ) ) {
                location.setType( Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_INSIDE_EVAL ) {
            Matcher matcher = EVAL_PATTERN.matcher( backText );
            if ( matcher.matches() ) {
                String content = matcher.group( 1 );
                location.setProperty( Location.LOCATION_EVAL_CONTENT,
                                      content );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_INSIDE_CONDITION_START ) {
            Matcher matcher = PATTERN_PATTERN_COMPARATOR_ARGUMENT.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_INSIDE_CONDITION_ARGUMENT );
                location.setProperty( Location.LOCATION_PROPERTY_OPERATOR,
                                      matcher.group( 7 ) );
                return location;
            }

            matcher = PATTERN_PATTERN_OPERATOR.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_INSIDE_CONDITION_OPERATOR );
                return location;
            }
        } else if ( location.getType() == Location.LOCATION_LHS_FROM ) {
            if ( location.getProperty( Location.LOCATION_FROM_CONTENT ) == null ) {
                location.setProperty( Location.LOCATION_FROM_CONTENT,
                                      "" );
            } else if ( ((String) location.getProperty( Location.LOCATION_FROM_CONTENT )).length() > 0 && (ENDS_WITH_SPACES.matcher( backText ).matches() || ENDS_WITH_BRACKET.matcher( backText ).matches()) ) {
                location.setType( Location.LOCATION_LHS_BEGIN_OF_CONDITION );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_FROM_ACCUMULATE_INIT ) {
            Matcher matcher = ACCUMULATE_PATTERN_INIT.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_FROM_ACCUMULATE_INIT_INSIDE );
                location.setProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_INIT_CONTENT,
                                      matcher.group( 1 ) );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION ) {
            Matcher matcher = ACCUMULATE_PATTERN_ACTION.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_FROM_ACCUMULATE_ACTION_INSIDE );
                location.setProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_ACTION_CONTENT,
                                      matcher.group( 2 ) );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_FROM_ACCUMULATE_REVERSE ) {
            Matcher matcher = ACCUMULATE_PATTERN_REVERSE.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_FROM_ACCUMULATE_REVERSE_INSIDE );
                location.setProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_REVERSE_CONTENT,
                                      matcher.group( 3 ) );
            }
            matcher = ACCUMULATE_PATTERN_RESULT.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE );
                location.setProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT,
                                      matcher.group( 5 ) );
            }
        } else if ( location.getType() == Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT ) {
            Matcher matcher = ACCUMULATE_PATTERN_RESULT.matcher( backText );
            if ( matcher.matches() ) {
                location.setType( Location.LOCATION_LHS_FROM_ACCUMULATE_RESULT_INSIDE );
                location.setProperty( Location.LOCATION_PROPERTY_FROM_ACCUMULATE_RESULT_CONTENT,
                                      matcher.group( 5 ) );
            }
        } else if ( location.getType() == Location.LOCATION_RHS ) {
            Matcher matcher = THEN_PATTERN.matcher( backText );
            if ( matcher.matches() ) {
                location.setProperty( Location.LOCATION_LHS_CONTENT,
                                      matcher.group( 1 ) );
                location.setProperty( Location.LOCATION_RHS_CONTENT,
                                      matcher.group( 2 ) );
                return location;
            }
        }

        return location;
    }
}