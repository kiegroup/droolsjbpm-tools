package org.drools.eclipse.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class DroolsLineBreakpoint extends JavaLineBreakpoint {

    private static final String DIALECT = "org.drools.eclipse.debug.core.breakpointDialect";

    /**
     * Default constructor is required for the breakpoint manager
     * to re-create persisted breakpoints. After instantiating a breakpoint,
     * the <code>setMarker(...)</code> method is called to restore
     * this breakpoint's attributes.
     */
    public DroolsLineBreakpoint() {
        super();
    }

    /**
     * Constructs a line breakpoint on the given resource at the given
     * line number.
     *
     * @param resource file on which to set the breakpoint
     * @param lineNumber line number of the breakpoint
     * @throws CoreException if unable to create the breakpoint
     */
    public DroolsLineBreakpoint(IResource resource, int lineNumber)
			throws CoreException {
        super( resource, "", -1, -1, -1, 0, true,
            createAttributesMap( lineNumber ), IDroolsDebugConstants.DROOLS_MARKER_TYPE );
        setJavaBreakpointProperties();
    }

    private static Map createAttributesMap(int lineNumber) {
        Map map = new HashMap();
        map.put( IDroolsDebugConstants.DRL_LINE_NUMBER, new Integer( lineNumber ) );
        return map;
    }

    public int getDRLLineNumber() {
        return getMarker().getAttribute( IDroolsDebugConstants.DRL_LINE_NUMBER, -1 );
    }

    public String getModelIdentifier() {
        return IDroolsDebugConstants.ID_DROOLS_DEBUG_MODEL;
    }

    public String getDialectName() {
        return getMarker().getAttribute( DIALECT, "Unknown");
    }

    public Map getFileRuleMappings() {
        String packedInfo = getMarker().getAttribute( IDroolsDebugConstants.DRL_RULES, "");
        return unpackRuleMapping( packedInfo );
    }

    public void setJavaBreakpointProperties() throws CoreException {
        IMarker marker = getMarker();
        int drlLineNumber = getDRLLineNumber();
        if ( marker.exists() ) {
            try {
                DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource( marker.getResource(), true );

                RuleInfo[] ruleInfos = drlInfo.getRuleInfos();

                StringBuffer rb = new StringBuffer();
                for (int i=0;i<ruleInfos.length; i++) {
                    int line = ruleInfos[i].getConsequenceDrlLineNumber();
                    String ruleid = ruleInfos[i].getClassName()+":"+line;
                    rb.append(ruleid);
                    if (i<ruleInfos.length-1) {
                        rb.append(";");
                    }
                }

                marker.setAttribute( IDroolsDebugConstants.DRL_RULES, rb.toString());

                marker.setAttribute( TYPE_NAME, getRuleClassName( drlInfo, marker.getResource().toString(), drlLineNumber ) );
                int ruleLineNumber = getRuleLineNumber( drlInfo, marker.getResource().toString(), drlLineNumber );
                marker.setAttribute( IMarker.LINE_NUMBER, ruleLineNumber );
                marker.setAttribute( DIALECT, getDialect( drlInfo, drlLineNumber ) );

            } catch ( Throwable t ) {
                throw new CoreException( new Status( IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
                                                     "Cannot determine ruleInfo " + marker.getResource() + " " + drlLineNumber, t ) );
            }
        }
    }

    private String getDialect(DRLInfo info, int drlLineNumber) {
        if ( info != null ) {
            return info.getRuleInfo( drlLineNumber ).getDialectName();
        }
        return null;
    }

    private String getRuleClassName(DRLInfo drlInfo, String resource, int lineNumber) throws CoreException {
        if ( drlInfo != null ) {
            RuleInfo ruleInfo = drlInfo.getRuleInfo( lineNumber );
            if ( ruleInfo != null ) {
                return ruleInfo.getClassName();
            }
            FunctionInfo functionInfo = drlInfo.getFunctionInfo( lineNumber );
            if ( functionInfo != null ) {
                return functionInfo.getClassName();
            }
        }
        throw new CoreException( new Status( IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
                                             "Cannot determine ruleClassName for " + resource + " " + lineNumber, null ) );
    }

    private int getRuleLineNumber(DRLInfo drlInfo, String resource, int lineNumber) throws CoreException {
        if ( drlInfo != null ) {
            RuleInfo ruleInfo = drlInfo.getRuleInfo( lineNumber );
            if ( ruleInfo != null ) {
                if ( ruleInfo.getConsequenceDrlLineNumber() < lineNumber ) {

                    int line = ruleInfo.getConsequenceJavaLineNumber()
                                        	+ (lineNumber - ruleInfo.getConsequenceDrlLineNumber());
                    if (ruleInfo.getDialectName() != null && ruleInfo.getDialectName().equals( "mvel" )) {
                        return line;
                    }
                    return line+1;
                }
            }
            FunctionInfo functionInfo = drlInfo.getFunctionInfo( lineNumber );
            if ( functionInfo != null ) {
                return functionInfo.getJavaLineNumber()
                	+ (lineNumber - functionInfo.getDrlLineNumber());
            }
        }
        throw new CoreException( new Status( IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
                                             "Cannot determine ruleLineNumber for " + resource + " " + lineNumber, null ) );
    }

    public String getRuleName() {
        IMarker marker = getMarker();
        if ( marker.exists() ) {
                try {
                    return (String) marker.getAttribute( TYPE_NAME);
                } catch ( CoreException e ) {
                    DroolsEclipsePlugin.log( e );
                }
        }
        return null;
    }

    private final static Map unpackRuleMapping(String input) {
        Map map = new HashMap();
        String[] rules = input.split( "\\;");
        for (int i=0; i<rules.length; i++) {
            if (rules[i].length()>0) {
                String[] inf = rules[i].split( "\\:" );
                map.put( inf[0], Integer.valueOf( inf[1] ) );
            }
        }
        return map;
    }


}