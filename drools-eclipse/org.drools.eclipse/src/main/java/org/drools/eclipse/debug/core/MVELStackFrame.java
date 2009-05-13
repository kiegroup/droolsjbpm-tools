package org.drools.eclipse.debug.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.debug.DebugUtil;
import org.drools.eclipse.debug.VariableWrapper;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

/**
 * Stack Frame for MVEL Dialect
 * 
 * @author Ahti Kitsik
 *
 */
public class MVELStackFrame extends DroolsStackFrame {

    //private final MVELStackFrameContext ctxCache = new MVELStackFrameContext();

    public MVELStackFrame(DroolsThread thread,
                          StackFrame frame,
                          int depth) {
        super( thread,
               frame,
               depth );

    }

    @Override
    public RuleInfo getExecutingRuleInfo() {
        try {
            String type = getDeclaringTypeName();

            return DroolsEclipsePlugin.getDefault().getRuleInfoByClass( type );

        } catch ( DebugException exc ) {
            DroolsEclipsePlugin.log( exc );
        }
        return null;
    }

    @Override
    public IVariable[] getVariables() throws DebugException {
        synchronized ( getThread() ) {
            if ( !isSuspended() ) {
                return new IVariable[0];
            }

//            IVariable[] cache = ctxCache.getCacheVariables();
//            if ( cache != null ) {
//                return cache;
//            }

            List<IVariable> result = new ArrayList<IVariable>( 0 );

            Method method = getUnderlyingMethod(); // onBreak
            ReferenceType declaringType = method.declaringType(); // org.drools.base.mvel.MVELDebugHandler

            try {
                Object var = method.variables().get( 0 );
                LocalVariable v2 = (LocalVariable) var;
                JDILocalVariable frameLocal = new JDILocalVariable( this,
                                                                    v2 );

                IValue knownVars = DebugUtil.getValueByExpression( "return getFactory().getKnownVariables().toArray(new String[0]);",
                                                                   frameLocal.getValue() );

                JDIObjectValue vvv = (JDIObjectValue) knownVars;

                if ( vvv != null && ((ArrayReference) vvv.getUnderlyingObject()).length() > 0 ) {
                    ArrayReference arr = (ArrayReference) vvv.getUnderlyingObject();

                    Iterator varIter = arr.getValues().iterator();

                    while ( varIter.hasNext() ) {
                        final String varName = ((StringReference) varIter.next()).value();

                        IJavaValue val = (IJavaValue) DebugUtil.getValueByExpression( "return getFactory().getVariableResolver(\"" + varName + "\").getValue();",
                                                                                      frameLocal.getValue() );
                        if ( val != null ) {
                            final ObjectReference valRef = ((JDIObjectValue) val).getUnderlyingObject();
                            VariableWrapper local = new VariableWrapper( varName,
                                                                         val );

                            local.setPublic( true );
                            result.add( local );
                        } else {
                            DroolsEclipsePlugin.log( new Exception( "Unable to get value for variable named '" + varName + "' suspend=" + isSuspended() ) );
                        }
                    }

                }

                IVariable[] vararr = result.toArray( new IVariable[result.size()] );
                Arrays.sort( vararr,
                             new Comparator<IVariable>() {
                                 public int compare(IVariable var1,
                                                    IVariable var2) {
                                     try {
                                         return var1.getName().compareTo( var2.getName() );
                                     } catch ( DebugException e ) {
                                         return 0;
                                     }
                                 }
                             } );
//                ctxCache.setCacheVariables( vararr );
                return vararr;

            } catch ( Throwable t ) {
                DroolsEclipsePlugin.log( t );
            }
            return new IVariable[0];
        }
    }

    @Override
    public int getLineNumber() throws DebugException {
        synchronized ( getThread() ) {
//            int cache = ctxCache.getCacheLineNumber();
//            if ( cache != -1 ) {
//                return cache;
//            }

            DroolsDebugTarget t = (DroolsDebugTarget) getDebugTarget();
            String sourceName = getMVELName();
            DroolsLineBreakpoint bpoint = (DroolsLineBreakpoint) t.getDroolsBreakpoint( sourceName );
            if ( bpoint == null ) {
                return -1;
            }

            int line;
            try {
                line = Integer.parseInt( bpoint.getFileRuleMappings().get( sourceName ).toString() );
            } catch ( Throwable t2 ) {
                DroolsEclipsePlugin.log( t2 );
                return -1;
            }

            int fragmentLine = getBreakpointLineNumber(); // 4->5 for step over
            int res = line + fragmentLine;

            // System.out.println("Resolved line to line:"+line+"; fragment:"+fragmentLine);

            if ( fragmentLine == -1 ) {
                System.err.println( "Unable to retrieve fragment line!" );
                return -1;
            }
//            ctxCache.setCacheLineNumber( res );
            return res;
        }
    }

    private int getBreakpointLineNumber() {
//        if ( ctxCache.getCacheBreakpointLineNumber() != -1 ) {
//            return ctxCache.getCacheBreakpointLineNumber();
//        }

        // Drools 4
        try {
            Object o = getRemoteVar( "lineNumber" );
            if ( o == null ) {
                return -1;
            }
            IntegerValue val = (IntegerValue) o;
            int realval = val.value();
//            ctxCache.setCacheBreakpointLineNumber( realval );
            return realval;
        } catch ( NullPointerException e ) {
            // Drools 5+
        } catch ( Throwable e ) {
            DroolsEclipsePlugin.log( e );
        }

        // Drools 5
        try {
            Object o = getRemoteVar( "label" );
            if ( o == null ) {
                return -1;
            }
            ObjectReference obj = (ObjectReference) o;
            ClassType frameType = (ClassType) obj.type();
            Field field = frameType.fieldByName( "lineNumber" );
            o = obj.getValue( field );
            if ( o == null ) {
                return -1;
            }
            IntegerValue val = (IntegerValue) o;
            int realval = val.value();
//            ctxCache.setCacheBreakpointLineNumber( realval );
            return realval;
        } catch ( NullPointerException e ) {
            // Drools 5+
        } catch ( Throwable e ) {
            DroolsEclipsePlugin.log( e );
        }

        return -1;
    }

    public String getMVELName() {
        synchronized ( getThread() ) {
            if ( !isSuspended() ) {
                return null;
            }

//            String cache = ctxCache.getCacheMVELName();
//            if ( cache != null ) {
//                return cache;
//            }

            // Drools 4
            try {
                Object rem = getRemoteVar( "sourceName" );
                if ( rem == null ) {
                    return null;
                }
                StringReference res = (StringReference) rem;
                String realres = res.value();
//                ctxCache.setCacheMVELName( realres );
                return realres;
            } catch ( NullPointerException e ) {
                // Drools 5
            } catch ( Throwable e ) {
                DroolsEclipsePlugin.log( e );
            }

            // Drools 5
            try {
                Object rem = getRemoteVar( "label" );
                if ( rem == null ) {
                    return null;
                }
                ObjectReference obj = (ObjectReference) rem;
                ClassType frameType = (ClassType) obj.type();
                Field field = frameType.fieldByName( "sourceFile" );
                rem = obj.getValue( field );
                if ( rem == null ) {
                    return null;
                }
                StringReference res = (StringReference) rem;
                String realres = res.value();
//                ctxCache.setCacheMVELName( realres );
                return realres;
            } catch ( Throwable e ) {
                DroolsEclipsePlugin.log( e );
            }

            return "Unavailable";
        }
    }

    private void evalEnd() {
        fireChangeEvent( DebugEvent.STATE );
        //fireChangeEvent( DebugEvent.CONTENT );
    }

    private Object getRemoteVar(String methodName) throws AbsentInformationException,
                                                  ClassNotLoadedException,
                                                  DebugException,
                                                  InvalidTypeException,
                                                  IncompatibleThreadStateException,
                                                  InvocationException {

        //frame arg
        Method method = getUnderlyingMethod(); // onBreak
        //ReferenceType declaringType = method.declaringType(); // org.drools.base.mvel.MVELDebugHandler

        LocalVariable var = (LocalVariable) method.variables().get( 0 );//frame

        ClassType frameType = (ClassType) var.type();

        StackFrame frame = getUnderlyingStackFrame();
        Value value = frame.getValue( var );
        //getThread().getTopStackFrame().get

        //IValue value = jdivar.getValue();
        ObjectReferenceImpl o = (ObjectReferenceImpl) value;

        //if ( value instanceof JDINullValue ) {
        // return null;
        // }

        //ObjectReference o = (ObjectReference) ((JDIObjectValue) value).getUnderlyingObject();
        if ( o == null ) {
            return null;
        }

        Field field = frameType.fieldByName( methodName );
        Value val = o.getValue( field );
        return val;
    }

    public String getSourcePath() throws DebugException {
        return getMVELName();
    }

    public String getSourceName() throws DebugException {
        return getMVELName();
    }

    public boolean canStepInto() {
        return false;
    }

    public boolean canStepOver() {
        // while not synchronised, this is thread safe due to the atomic evaluating.
        return exists() && !isObsolete();
    }

    public boolean canDropToFrame() {
        return false;
    }

    public boolean canStepReturn() {
        return false;
    }

    public boolean canStepWithFilters() {
        return false;
    }

    public IJavaReferenceType getReferenceType() throws DebugException {
        return REF_TYPE;
    }

    public static class MVELStackFrameContext {
        private int         cacheLineNumber           = -1;
        private int         cacheBreakpointLineNumber = -1;
        private String      cacheMVELName             = null;
        private IVariable[] cacheVariables            = null;

        public synchronized void clear() {
            cacheLineNumber = -1;
            cacheBreakpointLineNumber = -1;
            cacheMVELName = null;
            cacheVariables = null;
        }

        public synchronized int getCacheLineNumber() {
            return cacheLineNumber;
        }

        public synchronized void setCacheLineNumber(int cacheLineNumber) {
            this.cacheLineNumber = cacheLineNumber;
        }

        public synchronized int getCacheBreakpointLineNumber() {
            return cacheBreakpointLineNumber;
        }

        public synchronized void setCacheBreakpointLineNumber(int cacheBreakpointLineNumber) {
            this.cacheBreakpointLineNumber = cacheBreakpointLineNumber;
        }

        public synchronized String getCacheMVELName() {
            return cacheMVELName;
        }

        public synchronized void setCacheMVELName(String cacheMVELName) {
            this.cacheMVELName = cacheMVELName;
        }

        public synchronized IVariable[] getCacheVariables() {
            return cacheVariables;
        }

        public synchronized void setCacheVariables(IVariable[] cacheVariables) {
            this.cacheVariables = cacheVariables;
        }
    }

    /**
      * Dummy type with changed stratum to force debugger's LaunchView to show proper stackframe name
      */
    private static final IJavaReferenceType REF_TYPE = new IJavaReferenceType() {

                                                         public IJavaFieldVariable getField(String name) throws DebugException {
                                                             return null;
                                                         }

                                                         public IJavaClassObject getClassObject() throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getAvailableStrata() throws DebugException {
                                                             return null;
                                                         }

                                                         public String getDefaultStratum() throws DebugException {
                                                             return "MVEL";
                                                         }

                                                         public String[] getDeclaredFieldNames() throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getAllFieldNames() throws DebugException {
                                                             return null;
                                                         }

                                                         public IJavaObject getClassLoaderObject() throws DebugException {
                                                             return null;
                                                         }

                                                         public String getGenericSignature() throws DebugException {
                                                             return null;
                                                         }

                                                         public String getSourceName() throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getSourceNames(String stratum) throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getSourcePaths(String stratum) throws DebugException {
                                                             return null;
                                                         }

                                                         public IJavaObject[] getInstances(long max) throws DebugException {
                                                             return null;
                                                         }

                                                         public String getName() throws DebugException {
                                                             return null;
                                                         }

                                                         public String getSignature() throws DebugException {
                                                             return null;
                                                         }

                                                         public IDebugTarget getDebugTarget() {
                                                             return null;
                                                         }

                                                         public ILaunch getLaunch() {
                                                             return null;
                                                         }

                                                         public String getModelIdentifier() {
                                                             return null;
                                                         }

                                                         public Object getAdapter(Class adapter) {
                                                             return null;
                                                         }

                                                     };

}