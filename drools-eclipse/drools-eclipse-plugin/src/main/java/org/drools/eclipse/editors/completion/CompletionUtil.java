package org.drools.eclipse.editors.completion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Signature;

public class CompletionUtil {

    protected static final Pattern INCOMPLETED_MVEL_EXPRESSION = Pattern.compile( "[\\.\\(\\{\\[]\\Z",
                                                                                  Pattern.DOTALL );

    protected static final Pattern COMPLETED_MVEL_EXPRESSION   = Pattern.compile( "]\\)\\}\\]\\Z",
                                                                                  Pattern.DOTALL );

    protected static final Pattern MODIFY_PATTERN              = Pattern.compile( ".*modify\\s*\\(\\s*(.*)\\s*\\)(\\s*\\{(.*)\\s*\\}?)?",
                                                                                  Pattern.DOTALL );

    protected static final Pattern START_OF_NEW_JAVA_STATEMENT = Pattern.compile( ".*[;{}]\\s*",
                                                                                  Pattern.DOTALL );
    protected static final Pattern START_OF_METHOD_ARGS        = Pattern.compile( ".*[\\(]\\s*",
                                                                                  Pattern.DOTALL );

    private CompletionUtil() {
    }

    /**
     * Looks behind, gets stuff after the white space. Basically ripping out the
     * last word.
     */
    public static String stripLastWord(String prefix) {
        if ( "".equals( prefix ) ) {
            return prefix;
        }
        if ( prefix.charAt( prefix.length() - 1 ) == ' ' ) {
            return "";
        } else {
            char[] c = prefix.toCharArray();
            int start = 0;
            for ( int i = c.length - 1; i >= 0; i-- ) {
                if ( Character.isWhitespace( c[i] ) || c[i] == '(' || c[i] == ':' || c[i] == ';' || c[i] == '=' || c[i] == '<' || c[i] == '>' || c[i] == '.' || c[i] == '{' || c[i] == '}' ) {
                    start = i + 1;
                    break;
                }
            }
            prefix = prefix.substring( start,
                                       prefix.length() );
            return prefix;
        }
    }

    public static String getPreviousExpression(String backText) {
        int separator = backText.lastIndexOf( ';' );
        if ( separator < 0 ) {
            return backText;
        }
        return backText.substring( 0,
                                   separator + 1 );
    }

    public static String getLastExpression(String backText) {
        StringTokenizer st = new StringTokenizer( backText,
                                                  ";" );
        String last = "";
        while ( st.hasMoreTokens() ) {
            last = st.nextToken();
        }
        if ( last.trim().length() == 0 ) {
            return backText;
        }
        return last;
    }

    public static String getInnerExpression(String backText) {
        String last = getLastExpression( backText ).trim();

        char[] c = last.toCharArray();
        int start = 0;
        for ( int i = c.length - 1; i >= 0; i-- ) {
            if ( Character.isWhitespace( c[i] ) || c[i] == '(' || c[i] == '+' || c[i] == ')' || c[i] == '[' || c[i] == ']' || c[i] == ':' || c[i] == '=' || c[i] == '<' || c[i] == '>' || c[i] == ',' || c[i] == '{' || c[i] == '}' ) {
                start = i + 1;
                break;
            }
        }
        last = last.substring( start );
        return last;
    }

    public static int nestedExpressionIndex(char[] chars,
                                            int start,
                                            char type) {
        int depth = 1;
        char term = type;
        switch ( type ) {
            case '[' :
                term = ']';
                break;
            case '{' :
                term = '}';
                break;
            case '(' :
                term = ')';
                break;
        }

        if ( type == term ) {
            for ( start++; start < chars.length; start++ ) {
                if ( chars[start] == type ) {
                    return start;
                }
            }
        } else {
            for ( start++; start < chars.length; start++ ) {
                if ( chars[start] == '\'' || chars[start] == '"' ) {
                    //start = captureStringLiteral(chars[start], chars, start, chars.length);
                } else if ( chars[start] == type ) {
                    depth++;
                } else if ( chars[start] == term && --depth == 0 ) {
                    return start;
                }
            }
        }

        return -1;
    }

    public static String stripWhiteSpace(String prefix) {
        if ( "".equals( prefix ) ) {
            return prefix;
        }
        if ( prefix.charAt( prefix.length() - 1 ) == ' ' ) {
            return "";
        } else {
            char[] c = prefix.toCharArray();
            int start = 0;
            for ( int i = c.length - 1; i >= 0; i-- ) {
                if ( Character.isWhitespace( c[i] ) ) {
                    start = i + 1;
                    break;
                }
            }
            prefix = prefix.substring( start,
                                       prefix.length() );
            return prefix;
        }
    }

    /**
     * Attempt to enhance a consequence backtext such that it should compile in MVEL
     * @param backText
     * @return a substring of the back text, that should be compilable without
     *         syntax errors by the mvel compiler
     *
     *         TODO: add tests and more use
     *         cases
     */
    public static String getCompilableText(String backText) {
        String trimed = backText.trim();
        if ( trimed.endsWith( ";" ) ) {
            // RHS expression should compile if it ends with ; but to get the last object,
            // we do no want it, to simulate a return statement
            return backText.substring( 0,
                                       backText.length() - 1 );
        } else if ( trimed.endsWith( "." ) || trimed.endsWith( "," ) ) {
            // RHS expression should compile if it ends with no dot or comma
            return backText.substring( 0,
                                       backText.length() - 1 );
        } else if ( CompletionUtil.COMPLETED_MVEL_EXPRESSION.matcher( backText ).matches() ) {
            // RHS expression should compile if closed. just need to close the
            // statement
            return backText + ";";
            //        } else if ( INCOMPLETED_MVEL_EXPRESSION.matcher( backText ).matches() ) {
            //            // remove the last char and close the statement
            //            return backText.substring( 0,
            //                                       backText.length() - 1 );
        } else {
            //TODO: support completion within with {} blocks
            //TODO: support completion within nested expression.

            return backText;
        }
    }

    /*
     * propertyname extraction and bean convention methods names checks
     */

    public static boolean isGetter(String methodName,
                                   int argCount,
                                   String returnedType) {
        return isAccessor( methodName,
                           argCount,
                           0,
                           "get",
                           returnedType,
                           Signature.SIG_VOID,
                           false );
    }

    public static boolean isSetter(String methodName,
                                   int argCount,
                                   String returnedType) {
        return isAccessor( methodName,
                           argCount,
                           1,
                           "set",
                           returnedType,
                           Signature.SIG_VOID,
                           true );
    }

    public static boolean isIsGetter(String methodName,
                                     int argCount,
                                     String returnedType) {
        return isAccessor( methodName,
                           argCount,
                           0,
                           "is",
                           returnedType,
                           Signature.SIG_BOOLEAN,
                           true );
    }

    /**
     * Given a data depicting a method (name, # or params/args, returned type key), tries to return a  bean property name derived from that method.
     * If a bean property name is not found, the initial method name is returned
     * @param methodName
     * @param parameterCount
     * @param returnType
     * @return a bean property name
     */
    public static String getPropertyName(String methodName,
                                         int parameterCount,
                                         String returnType) {
        if ( methodName == null ) {
            return null;
        }
        String simpleName = methodName.replaceAll( "\\(\\)",
                                                   "" );
        int prefixLength = 0;
        if ( isIsGetter( simpleName,
                         parameterCount,
                         returnType ) ) {

            prefixLength = 2;

        } else if ( isGetter( simpleName,
                              parameterCount,
                              returnType ) //
                    || isSetter( simpleName,
                                 parameterCount,
                                 returnType ) ) {

            prefixLength = 3;
        } else {
            return methodName;
        }

        char firstChar = Character.toLowerCase( simpleName.charAt( prefixLength ) );
        String propertyName = firstChar + simpleName.substring( prefixLength + 1 );
        return propertyName;
    }

    public static String getPropertyName(String methodName,
                                         char[] signature) {
        if ( signature == null || methodName == null ) {
            return methodName;
        }

        int parameterCount = Signature.getParameterCount( signature );
        String returnType = new String( Signature.getReturnType( signature ) );

        return getPropertyName( methodName,
                                parameterCount,
                                returnType );
    }

    /**
     * Given a data depicting a method (name, # or params/args, returned type key), tries to return a  writable bean property name derived from that method.
     * If a writable (ie setter) bean property name is not found, the initial method name is returned
     * @param methodName
     * @param parameterCount
     * @param returnType
     * @return a bean property name
     */
    public static String getWritablePropertyName(String methodName,
                                                 int parameterCount,
                                                 String returnType) {
        if ( methodName == null ) {
            return null;
        }
        String simpleName = methodName.replaceAll( "\\(\\)",
                                                   "" );
        if ( !isSetter( simpleName,
                        parameterCount,
                        returnType ) ) {
            return methodName;
        }

        int prefixLength = 3;

        char firstChar = Character.toLowerCase( simpleName.charAt( prefixLength ) );
        String propertyName = firstChar + simpleName.substring( prefixLength + 1 );
        return propertyName;
    }

    public static String getWritablePropertyName(String methodName,
                                                 char[] signature) {
        if ( signature == null || methodName == null ) {
            return methodName;
        }

        int parameterCount = Signature.getParameterCount( signature );
        String returnType = new String( Signature.getReturnType( signature ) );

        return getWritablePropertyName( methodName,
                                        parameterCount,
                                        returnType );
    }

    /**
     * Determine if the given method is a bean accessor (ie getter/setter)
     * @param methodName
     * @param actualParameterCount
     * @param requiredParameterCount
     * @param prefix
     * @param returnType
     * @param requiredReturnType
     * @param includeType
     * @return true if the method is a bean accessor, false otherwise
     */
    private static boolean isAccessor(String methodName,
                                      int actualParameterCount,
                                      int requiredParameterCount,
                                      String prefix,
                                      String returnType,
                                      String requiredReturnType,
                                      boolean includeType) {

        //must be longer than the accessor prefix
        if ( methodName.length() < prefix.length() + 1 ) {
            return false;
        }

        //start with get, set or is
        if ( !methodName.startsWith( prefix ) ) {
            return false;
        }

        if ( actualParameterCount != requiredParameterCount ) {
            return false;
        }

        //if we check for the returned type, verify that the returned type is of the cirrect type signature
        if ( includeType ) {
            if ( !requiredReturnType.equals( returnType ) ) {
                return false;
            }
        } else {
            if ( requiredReturnType.equals( returnType ) ) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStartOfNewStatement(String text,
                                                String prefix) {
        String javaTextWithoutPrefix = text.substring( 0,
                                                       text.length() - prefix.length() );

        if ( "".equals( javaTextWithoutPrefix.trim() ) || CompletionUtil.START_OF_NEW_JAVA_STATEMENT.matcher( javaTextWithoutPrefix ).matches() ) {
            return true;
        }
        return false;
    }

    public static String getLastLine(String text) {
        final BufferedReader reader = new BufferedReader( new StringReader( text ) );
        String line = null;
        String lastLine = null;
        try {
            while ( (line = reader.readLine()) != null ) {
                if ( line.trim().length() > 0 ) {
                    lastLine = line;
                }
            }
        } catch ( final IOException e ) {
            // should never happen, it's just reading over a string.
        }
        return lastLine;
    }

    /**
     * COMPENSATES FOR LACK OF getSimpleName IN java.lang.Class
     * Borrowed and adpated from MVEL's org.mvel.util.ParseTools.getSimpleClassName(Class)
     * @param cls -- class reference
     * @return Simple name of class
     */
    public static String getSimpleClassName(Class cls) {
        int lastIndex = cls.getName().lastIndexOf( '$' );
        if ( lastIndex < 0 ) {
            lastIndex = cls.getName().lastIndexOf( '.' );
        }
        if ( cls.isArray() ) {
            return cls.getName().substring( lastIndex + 1 ) + "[]";
        } else {
            return cls.getName().substring( lastIndex + 1 );
        }
    }

    public static String getTextWithoutPrefix(final String javaText,
                                              final String prefix) {
        int endIndex = javaText.length() - prefix.length();
        String javaTextWithoutPrefix = javaText;
        //javaText can be an empty string.
        if ( endIndex >= 0 ) {
            javaTextWithoutPrefix = javaText.substring( 0,
                                                        endIndex );
        }
        return javaTextWithoutPrefix;
    }

    public static boolean isStartOfDialectExpression(String text) {
        return "".equals( text.trim() ) || CompletionUtil.START_OF_NEW_JAVA_STATEMENT.matcher( text ).matches();
    }

    public static boolean isStartOfMethodArgsExpression(String text) {
        return CompletionUtil.START_OF_NEW_JAVA_STATEMENT.matcher( text ).matches();
    }

}
