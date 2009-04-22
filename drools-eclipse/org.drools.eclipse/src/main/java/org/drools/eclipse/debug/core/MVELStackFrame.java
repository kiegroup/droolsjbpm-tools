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
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * Stack Frame for MVEL Dialect
 * 
 * @author Ahti Kitsik
 *
 */
public class MVELStackFrame extends DroolsStackFrame {

    private int                             cacheLineNumber           = -1;
    private int                             cacheBreakpointLineNumber = -1;
    private String                          cacheMVELName             = null;
    private IVariable[]                     cacheVariables            = null;

    private boolean                         evaluating                = false;

    /**
     * Dummy type with changed stratum to force debugger's LaunchView to show proper stackframe name
     */
    private static final IJavaReferenceType REF_TYPE                  = new IJavaReferenceType() {

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

    public MVELStackFrame(DroolsThread thread,
                          StackFrame frame,
                          int depth) {
        super( thread,
               frame,
               depth );

    }

    public IVariable[] getVariables() throws DebugException {

        if ( !isSuspended() ) {
            return null;
        }

        if ( cacheVariables != null ) {
            return cacheVariables;
        }

        evaluating = true;
        try {
            List result = new ArrayList( 0 );

            Method method = getUnderlyingMethod(); // onBreak
            ReferenceType declaringType = method.declaringType(); // org.drools.base.mvel.MVELDebugHandler

            try {

                Object var = method.variables().get( 0 );
                LocalVariable v2 = (LocalVariable) var;
                DroolsLocalVariable frameLocal = new DroolsLocalVariable( this,
                                                                          v2 );

                IValue knownVars = DebugUtil.getValueByExpression( "return getFactory().getKnownVariables().toArray(new String[0]);",
                                                                   frameLocal.getValue() );

                IValue factory = DebugUtil.getValueByExpression( "return getFactory();",
                                                                 frameLocal.getValue() );

                IValue vars2 = DebugUtil.getValueByExpression( "return getFactory().getKnownVariables();",
                                                               frameLocal.getValue() );

                JDIObjectValue vvv = (JDIObjectValue) knownVars;

                if ( vvv != null && ((ArrayReference) vvv.getUnderlyingObject()).length() > 0 ) {
                    ArrayReference arr = (ArrayReference) vvv.getUnderlyingObject();

                    Iterator varIter = arr.getValues().iterator();

                    while ( varIter.hasNext() ) {
                        final String varName = ((StringReference) varIter.next()).value();

                        IJavaValue val = (IJavaValue) DebugUtil.getValueByExpression( "return getVariableResolver(\"" + varName + "\").getValue();",
                                                                                      factory );
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

                IVariable[] vararr = (IVariable[]) result.toArray( new IVariable[result.size()] );
                cacheVariables = vararr;
                return vararr;

            } catch ( Throwable t ) {
                DroolsEclipsePlugin.log( t );
            }

            IVariable[] vararr = (IVariable[]) result.toArray( new IVariable[result.size()] );

            Arrays.sort( vararr,
                         new Comparator() {

                             public int compare(Object var1,
                                                Object var2) {
                                 try {
                                     return ((IVariable) var1).getName().compareTo( ((IVariable) var2).getName() );
                                 } catch ( DebugException e ) {
                                     return 0;
                                 }
                             }

                         } );
            cacheVariables = vararr;
            return vararr;
        } finally {
            evaluating = false;
            evalEnd();
        }
    }

    private boolean internalHasNext(Value iter) throws InvalidTypeException,
                                               ClassNotLoadedException,
                                               IncompatibleThreadStateException,
                                               InvocationException,
                                               DebugException {
        BooleanValue hasNext = (BooleanValue) runMethod( iter,
                                                         "hasNext" );
        return hasNext.booleanValue();
    }

    private Value fetchField(Value factoryVar,
                             String fieldName) throws ClassNotLoadedException,
                                              DebugException {
        return fetchField( (ObjectReference) factoryVar,
                           fieldName );
    }

    private Value runMethod(Value val,
                            String methodName) throws InvalidTypeException,
                                              ClassNotLoadedException,
                                              IncompatibleThreadStateException,
                                              InvocationException,
                                              DebugException {

        ObjectReference refObj = (ObjectReference) val;
        ReferenceType t = refObj.referenceType();
        Method m2 = (Method) t.methodsByName( methodName ).iterator().next();
        ThreadReference thread = ((DroolsThread) getThread()).getUnderlyingThread();

        Value res = refObj.invokeMethod( thread,
                                         m2,
                                         new ArrayList(),
                                         0 );

        return res;
    }

    private Value fetchField(DroolsLocalVariable frameLocal,
                             String fieldName) throws DebugException,
                                              ClassNotLoadedException {
        ObjectReference objRef = ((JDIObjectValue) frameLocal.getValue()).getUnderlyingObject();
        return fetchField( objRef,
                           fieldName );
    }

    private Value fetchField(ObjectReference ref,
                             String fieldName) throws ClassNotLoadedException,
                                              DebugException {
        ClassType varType = (ClassType) ref.type();
        Field field = varType.fieldByName( fieldName );
        Value res = ref.getValue( field );
        return res;
    }

    protected DroolsStackFrame createNewDroolsFrame(StackFrame frame,
                                                    int depth) {
        return new MVELStackFrame( (DroolsThread) getThread(),
                                   frame,
                                   depth );
    }

    public int getLineNumber() throws DebugException {

        if ( cacheLineNumber != -1 ) {
            return cacheLineNumber;
        }

        if ( !isSuspended() ) {
            return -1;
        }

        evaluating = true;
        try {
            DroolsDebugTarget t = (DroolsDebugTarget) getDebugTarget();

            //int lineNr = getBreakpointLineNumber();
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

            cacheLineNumber = res;
            return res;
        } finally {
            evaluating = false;
            evalEnd();
        }
    }

    public RuleInfo getExecutingRuleInfo() {
        try {
            String type = getDeclaringTypeName();

            return DroolsEclipsePlugin.getDefault().getRuleInfoByClass( type );

        } catch ( DebugException exc ) {
            DroolsEclipsePlugin.log( exc );
        }
        return null;
    }

    public int getBreakpointLineNumber() {

        if ( cacheBreakpointLineNumber != -1 ) {
            return cacheBreakpointLineNumber;
        }

        if ( !isSuspended() ) {
            return -1;
        }

        evaluating = true;
        try {
        	
        	// Drools 4
            try {
                Object o = getRemoteVar( "lineNumber" );
                if ( o == null ) {
                    return -1;
                }
                IntegerValue val = (IntegerValue) o;
                int realval = val.value();
                cacheBreakpointLineNumber = realval;
                return realval;
            } catch ( NullPointerException e ) {
                // Drools 5+
            } catch (Throwable e) {
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
                cacheBreakpointLineNumber = realval;
                return realval;
            } catch ( NullPointerException e ) {
                // Drools 5+
            } catch (Throwable e) {
            	DroolsEclipsePlugin.log( e );
            }
            
            return -1;
        } finally {
            evaluating = false;
            evalEnd();
        }
    }

    public String getMVELName() {

        if ( cacheMVELName != null ) {
            return cacheMVELName;
        }

        if ( !isSuspended() ) {
            return null;
        }

        evaluating = true;
        try {
        	
        	// Drools 4
            try {
                Object rem = getRemoteVar( "sourceName" );
                if ( rem == null ) {
                    return null;
                }
                StringReference res = (StringReference) rem;
                String realres = res.value();
                cacheMVELName = realres;
                return realres;
            } catch ( NullPointerException e) {
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
                cacheMVELName = realres;
                return realres;
            } catch ( Throwable e ) {
                DroolsEclipsePlugin.log( e );
            }

            return "Unavailable";
        } finally {
            evaluating = false;
            evalEnd();
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

        /*        if ( value instanceof JDINullValue ) {
         return null;
         }
         */

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

    public boolean canStepInto() {
        return false;
    }

    public boolean canStepOver() {
        return exists() && !isObsolete() && !evaluating;
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

    public String getSourceName() throws DebugException {
        return getMVELName();
    }

    protected JDIStackFrame bind(StackFrame frame,
                                 int depth) {
        clearCachedData();
        return super.bind( frame,
                           depth );
    }

    protected void clearCachedData() {
        super.clearCachedData();
        clearFrameCache();
        if ( !isSuspended() ) {
            initMVELinfo();
        }
    }

    private void initMVELinfo() {
        try {
            getLineNumber();
        } catch ( DebugException e ) {
            // no luck this time. will be initialized later
        }
        getBreakpointLineNumber();
        getMVELName();
        try {
            getVariables();
        } catch ( DebugException e1 ) {
            // no luck this time. will be initialized later
        }
    }

    private void clearFrameCache() {
        cacheLineNumber = -1;
        cacheBreakpointLineNumber = -1;
        cacheMVELName = null;
        cacheVariables = null;
    }

}
