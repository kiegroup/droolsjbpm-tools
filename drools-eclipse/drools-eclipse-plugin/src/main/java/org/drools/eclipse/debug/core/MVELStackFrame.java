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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.StackFrameImpl;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

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

    /**
     * Dummy type with changed stratum to force debugger's LaunchView to show proper stackframe name
     */
    private static final IJavaReferenceType REF_TYPE = new IJavaReferenceType() {

                                                         public String[] getAllFieldNames() throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getAvailableStrata() throws DebugException {
                                                             return null;
                                                         }

                                                         public IJavaObject getClassLoaderObject() throws DebugException {
                                                             return null;
                                                         }

                                                         public IJavaClassObject getClassObject() throws DebugException {
                                                             return null;
                                                         }

                                                         public String[] getDeclaredFieldNames() throws DebugException {
                                                             return null;
                                                         }

                                                         public String getDefaultStratum() throws DebugException {
                                                             return "MVEL";
                                                         }

                                                         public IJavaFieldVariable getField(String name) throws DebugException {
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

                                                     }; ;

    public MVELStackFrame(DroolsThread thread,
                          StackFrame frame,
                          int depth) {
        super( thread,
               frame,
               depth );

        try {
            Iterator i = thread.getUnderlyingThread().frames().iterator();
            while ( i.hasNext() ) {
                StackFrameImpl o = (StackFrameImpl) i.next();
            }
        } catch ( IncompatibleThreadStateException e ) {
            DroolsEclipsePlugin.log( e );
        }

    }

    public IVariable[] getVariables() throws DebugException {

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

            JDIObjectValue vvv = (JDIObjectValue) knownVars;

            if ( vvv != null ) {
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
                        DroolsEclipsePlugin.log( new Exception( "Unable to get value for variable named '" + varName + "'" ) );
                    }
                }

            }

            IVariable[] vararr = (IVariable[]) result.toArray( new IVariable[result.size()] );
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
        return vararr;
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

        DroolsDebugTarget t = (DroolsDebugTarget) getDebugTarget();

        int lineNr = getBreakpointLineNumber();
        String sourceName = getMVELName();

        DroolsLineBreakpoint bpoint = (DroolsLineBreakpoint) t.getDroolsBreakpoint( sourceName,
                                                                                    lineNr );

        if ( bpoint == null ) {
            return -1;
        }

        int line;
        line = bpoint.getDRLLineNumber();

        int fragmentLine = getBreakpointLineNumber(); // 4->5 for step over

        int delta = 0;
        try {
            delta = fragmentLine - bpoint.getLineNumber();
        } catch ( CoreException e ) {
            DroolsEclipsePlugin.log( e );
        }
        return line + delta;
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
        try {
            Object o = getRemoteVar( "lineNumber" );
            if ( o == null ) {
                return -1;
            }
            IntegerValue val = (IntegerValue) o;
            return val.value();
        } catch ( Throwable e ) {
            DroolsEclipsePlugin.log( e );
        }
        return -1;
    }

    public String getMVELName() {
        try {
            Object rem = getRemoteVar( "sourceName" );
            if ( rem == null ) {
                return null;
            }
            StringReference res = (StringReference) rem;
            return res.value();
        } catch ( Throwable e ) {
            DroolsEclipsePlugin.log( e );
        }

        return "Unavailable";
    }

    private Object getRemoteVar(String methodName) throws AbsentInformationException,
                                                  ClassNotLoadedException,
                                                  DebugException,
                                                  InvalidTypeException,
                                                  IncompatibleThreadStateException,
                                                  InvocationException {

        //frame arg
        Method method = getUnderlyingMethod(); // onBreak
        ReferenceType declaringType = method.declaringType(); // org.drools.base.mvel.MVELDebugHandler

        LocalVariable var = (LocalVariable) method.variables().get( 0 );//frame

        JDILocalVariable jdivar = new JDILocalVariable( this,
                                                        (LocalVariable) var );

        ClassType frameType = (ClassType) var.type();

        IValue value = jdivar.getValue();
        if ( value instanceof JDINullValue ) {
            return null;
        }
        ObjectReference o = (ObjectReference) ((JDIObjectValue) value).getUnderlyingObject();

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
        return true;
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
    
}
