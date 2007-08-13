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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.StackFrameImpl;
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

public class MVELStackFrame extends DroolsStackFrame {

    public MVELStackFrame(DroolsThread thread,
                          StackFrame frame,
                          int depth) {
        super( thread,
               frame,
               depth );
        //MVEL:Logging
       try {
        Iterator i = thread.getUnderlyingThread().frames().iterator();
        while ( i.hasNext() ) {
            StackFrameImpl o = (StackFrameImpl) i.next();
        }
    } catch ( IncompatibleThreadStateException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    }

    public IVariable[] getVariables() throws DebugException {
        /*        IVariable[] variables = super.getVariables();
         List result = new ArrayList((variables.length - 1)/2);
         for (int i = 0; i < variables.length; i++) {
         String name = variables[i].getName();
         if (!(name.equals("drools")) && !(name.endsWith("__Handle__"))) {
         result.add(variables[i]);
         }
         }
         */

        List result = new ArrayList( 0 );

        Method method = getUnderlyingMethod(); // onBreak
        ReferenceType declaringType = method.declaringType(); // org.drools.base.mvel.MVELDebugHandler

        //ReferenceType declaringType = .location().declaringType();
        try {

            Object var = method.variables().get( 0 );
            LocalVariable v2 = (LocalVariable) var;
            DroolsLocalVariable frameLocal = new DroolsLocalVariable( this,
                                                                      v2 );
            // result.add( frameLocal );

            IValue knownVars = DebugUtil.getValueByExpression( "return getFactory().getKnownVariables().toArray(new String[0]);", frameLocal.getValue());
            
            IValue factory = DebugUtil.getValueByExpression( "return getFactory();", frameLocal.getValue());

            JDIObjectValue vvv = (JDIObjectValue) knownVars;

            if (vvv!=null) {
            ArrayReference arr = (ArrayReference) vvv.getUnderlyingObject();
            Iterator varIter = arr.getValues().iterator();
            
            while ( varIter.hasNext() ) {
                    final String varName = ((StringReference) varIter.next()).value();
                    IJavaValue val = (IJavaValue) DebugUtil.getValueByExpression( "return getVariableResolver(\"" + varName + "\").getValue();",
                                                                                  factory );
                    if ( val != null ) {
                        final ObjectReference valRef = ((JDIObjectValue) val).getUnderlyingObject();
                        //MVELThisVariable t2 = new MVELThisVariable((JDIDebugTarget) getDebugTarget(),valRef,varName);

                        VariableWrapper local = new VariableWrapper( varName,
                                                                     val );

                        //IValue isLocal = DebugUtil.getValueByExpression( "return getParserContext().getVariables();",
                        //                                                 frameLocal.getValue() );
                        local.setPublic( true );
                        //local.setLocal( true );
                        result.add( local );
                    } else {
                        DroolsEclipsePlugin.log( new Exception("Unable to get value for variable named '"+varName+"'") );
                    }
                }
            
            //IValue localVars = DebugUtil.getValueByExpression( "return getParserContext().getVariables();", frameLocal.getValue());
            //IValue globalVars = DebugUtil.getValueByExpression( "return getParserContext().getInputs();", frameLocal.getValue());
            
            //result.add(new VariableWrapper("LocalVariables", (IJavaValue) localVars));
            //result.add(new VariableWrapper("GlobalVariables", (IJavaValue) globalVars));
            
            
            
            //now iterate over localVars and add all that have getValue() non-null
            
            //MVELThisVariable t2 = new MVELThisVariable((JDIDebugTarget) getDebugTarget(),((JDIObjectValue)variables).getUnderlyingObject(),"parserContext variables");
            //result.add(t2);
            }
            
            IVariable[] vararr = (IVariable[]) result.toArray( new IVariable[result.size()] );
            return vararr;

        } catch ( Throwable t ) {
            t.printStackTrace();
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

    private boolean internalHasNext(Value iter) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, DebugException {
        BooleanValue hasNext = (BooleanValue) runMethod( iter,
                                "hasNext" );
        return hasNext.booleanValue();
    }

    private Value fetchField(Value factoryVar,
                             String fieldName) throws ClassNotLoadedException, DebugException {
        return fetchField((ObjectReference)factoryVar, fieldName );
    }

    private Value runMethod(Value val,
                            String methodName) throws InvalidTypeException,
                                              ClassNotLoadedException,
                                              IncompatibleThreadStateException,
                                              InvocationException, DebugException {
        
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
                             String fieldName) throws ClassNotLoadedException, DebugException {
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

    public String getReceivingTypeName() throws DebugException {
        return "getReceivingTypeName";
    }

    public String getMethodName() throws DebugException {
        return "getMethodName";
    }

    /*    public String getDeclaringTypeName() throws DebugException {
     return "MVELRunner";
     }
     */
    public int getLineNumber() throws DebugException {
        /*        DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource( marker.getResource(),
         true );*/

        //RuleInfo ruleINF = getExecutingRuleInfo();
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
        return line;
        //return getUnderlyingStackFrame().location().lineNumber();
    }

    public RuleInfo getExecutingRuleInfo() {
        try {
            String methodName = getMethodName();
            String signature = getSignature();
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
            //getLineNumber
            IntegerValue val = (IntegerValue) o;
            return val.value();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public String getMVELName() {
        try {
            Object rem = getRemoteVar( "sourceName" );
            if ( rem == null ) {
                return null;
            }
            //getSourceName
            StringReference res = (StringReference) rem;
            return res.value();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
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
        
        JDILocalVariable jdivar = new JDILocalVariable(this, (LocalVariable) var);

        ClassType frameType = (ClassType) var.type();

        IValue value = jdivar.getValue();
        if (value instanceof JDINullValue) {
            return null;
        }
        ObjectReference o = (ObjectReference) ((JDIObjectValue)value).getUnderlyingObject();
        
        if ( o == null ) {
            return null;
        }
        Field field = frameType.fieldByName( methodName );
        Value val = o.getValue( field );

        return val;
    }

    public String getSourceName() throws DebugException {
        return getMVELName();
    }

    public String getSourcePath() throws DebugException {
        return "";
    }

    public boolean canStepInto() {
    	return false;
    }
    
    public boolean canStepOver() {
    	return false;
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
    
}
