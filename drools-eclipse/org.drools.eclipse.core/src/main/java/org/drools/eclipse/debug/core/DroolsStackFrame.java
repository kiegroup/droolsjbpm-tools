package org.drools.eclipse.debug.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;

public class DroolsStackFrame extends JDIStackFrame {

    private static final String CONSEQUENCE_SIGNATURE = "(Lorg/drools/spi/KnowledgeHelper";

    private DroolsThread        fThread;
    private Location            fLocation;
    private List                fVariables;
    private boolean             fRefreshVariables     = true;
    private int                 fDepth                = -2;
    private boolean             initialized           = true;
    private StackFrame          fStackFrame;
    private ObjectReference     fThisObject;
    private String              fReceivingTypeName;
    private boolean             fLocalsAvailable      = true;

    public DroolsStackFrame(DroolsThread thread,
                            StackFrame frame,
                            int depth) {
        super( thread,
               frame,
               depth );
        bind( frame,
              depth );
    }

    public boolean isExecutingRule() {
        try {
            if ( "consequence".equals( getMethodName() ) && getSignature().startsWith( CONSEQUENCE_SIGNATURE ) ) {
                return true;
            }
        } catch ( DebugException exc ) {
            DroolsEclipsePlugin.log( exc );
        }
        return false;
    }

    public RuleInfo getExecutingRuleInfo() {
        try {
            String methodName = getMethodName();
            String signature = getSignature();
            String type = getDeclaringTypeName();
            if ( "consequence".equals( methodName ) && signature.startsWith( CONSEQUENCE_SIGNATURE ) ) {
                return DroolsEclipsePlugin.getDefault().getRuleInfoByClass( type );
            }

        } catch ( DebugException exc ) {
            DroolsEclipsePlugin.log( exc );
        }
        return null;
    }

    public FunctionInfo getExecutingFunctionInfo() {
        try {
            return DroolsEclipsePlugin.getDefault().getFunctionInfoByClass( getDeclaringTypeName() );
        } catch ( DebugException exc ) {
            DroolsEclipsePlugin.log( exc );
        }
        return null;
    }

    public int getLineNumber() throws DebugException {
        synchronized ( fThread ) {
            RuleInfo ruleInfo = getExecutingRuleInfo();
            if ( ruleInfo != null ) {
                return ruleInfo.getConsequenceDrlLineNumber() + (getInternalLineNumber() - ruleInfo.getConsequenceJavaLineNumber() - 1);
            }
            FunctionInfo functionInfo = getExecutingFunctionInfo();
            if ( functionInfo != null ) {
                return functionInfo.getDrlLineNumber() + (getInternalLineNumber() - functionInfo.getJavaLineNumber());
            }
        }

        return getInternalLineNumber();
    }

    private int getInternalLineNumber() throws DebugException {
        try {
            return fLocation.lineNumber();
        } catch ( RuntimeException e ) {
            if ( getThread().isSuspended() ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_line_number,
                                                           new String[]{e.toString()} ),
                                     e );
            }
        }
        return -1;
    }

    public IVariable[] getVariables() throws DebugException {
        IVariable[] variables = super.getVariables();
        List result = new ArrayList( (variables.length - 1) / 2 );
        for ( int i = 0; i < variables.length; i++ ) {
            String name = variables[i].getName();
            if ( !(name.equals( "drools" )) && !(name.endsWith( "__Handle__" )) ) {
                result.add( variables[i] );
            }
        }
        return (IVariable[]) result.toArray( new IVariable[result.size()] );
    }

    protected List getVariables0() throws DebugException {
        synchronized ( fThread ) {
            if ( fVariables == null ) {

                // throw exception if native method, so variable view will update
                // with information message
                if ( isNative() ) {
                    requestFailed( JDIDebugModelMessages.JDIStackFrame_Variable_information_unavailable_for_native_methods,
                                   null );
                }

                Method method = getUnderlyingMethod();
                fVariables = new ArrayList();
                // #isStatic() does not claim to throw any exceptions - so it is not try/catch coded
                if ( method.isStatic() ) {
                    // add statics
                    List allFields = null;
                    ReferenceType declaringType = method.declaringType();
                    try {
                        allFields = declaringType.allFields();
                    } catch ( RuntimeException e ) {
                        targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_fields,
                                                                   new String[]{e.toString()} ),
                                             e );
                        // execution will not reach this line, as 
                        // #targetRequestFailed will throw an exception					
                        return Collections.EMPTY_LIST;
                    }
                    if ( allFields != null ) {
                        Iterator fields = allFields.iterator();
                        while ( fields.hasNext() ) {
                            Field field = (Field) fields.next();
                            if ( field.isStatic() ) {
                                fVariables.add( new JDIFieldVariable( (JDIDebugTarget) getDebugTarget(),
                                                                      field,
                                                                      declaringType ) );
                            }
                        }
                        Collections.sort( fVariables,
                                          new Comparator() {
                                              public int compare(Object a,
                                                                 Object b) {
                                                  JDIFieldVariable v1 = (JDIFieldVariable) a;
                                                  JDIFieldVariable v2 = (JDIFieldVariable) b;
                                                  try {
                                                      return v1.getName().compareToIgnoreCase( v2.getName() );
                                                  } catch ( DebugException de ) {
                                                      logError( de );
                                                      return -1;
                                                  }
                                              }
                                          } );
                    }
                } else {
                    // add "this"
                    ObjectReference t = getUnderlyingThisObject();
                    if ( t != null ) {
                        fVariables.add( new DroolsThisVariable( (JDIDebugTarget) getDebugTarget(),
                                                                t ) );
                    }
                }
                // add locals
                Iterator variables = getUnderlyingVisibleVariables().iterator();
                while ( variables.hasNext() ) {
                    LocalVariable var = (LocalVariable) variables.next();
                    fVariables.add( new DroolsLocalVariable( this,
                                                             var ) );
                }
            } else if ( fRefreshVariables ) {
                updateVariables();
            }
            fRefreshVariables = false;
            return fVariables;
        }
    }

    protected JDIStackFrame bind(StackFrame frame,
                                 int depth) {
        if ( initialized ) {
            synchronized ( fThread ) {
                if ( fDepth == -2 ) {
                    // first initialization
                    fStackFrame = frame;
                    fDepth = depth;
                    fLocation = frame.location();
                    return this;
                } else if ( depth == -1 ) {
                    // mark as invalid
                    fDepth = -1;
                    fStackFrame = null;
                    return null;
                } else if ( fDepth == depth ) {
                    Location location = frame.location();
                    Method method = location.method();
                    if ( method.equals( fLocation.method() ) ) {
                        try {
                            if ( method.declaringType().defaultStratum().equals( "Java" ) || //$NON-NLS-1$
                                 equals( getSourceName( location ),
                                         getSourceName( fLocation ) ) ) {
                                // TODO: what about receiving type being the same?
                                fStackFrame = frame;
                                fLocation = location;
                                clearCachedData();
                                return this;
                            }
                        } catch ( DebugException e ) {
                        }
                    }
                }
                // invalidate this franme
                bind( null,
                      -1 );
                // return a new frame
                return createNewDroolsFrame( frame,
                                             depth );
            }
        } else {
            return null;
        }
    }

    protected DroolsStackFrame createNewDroolsFrame(StackFrame frame,
                                                    int depth) {
        return DroolsThread.createCustomFrame( fThread,
                                               depth,
                                               frame );
    }

    public IThread getThread() {
        return fThread;
    }

    public Method getUnderlyingMethod() {
        synchronized ( fThread ) {
            return fLocation.method();
        }
    }

    protected List getUnderlyingVisibleVariables() throws DebugException {
        synchronized ( fThread ) {
            List variables = Collections.EMPTY_LIST;
            try {
                variables = getUnderlyingStackFrame().visibleVariables();
            } catch ( AbsentInformationException e ) {
                setLocalsAvailable( false );
            } catch ( NativeMethodException e ) {
                setLocalsAvailable( false );
            } catch ( RuntimeException e ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_visible_variables_2,
                                                           new String[]{e.toString()} ),
                                     e );
            }
            return variables;
        }
    }

    protected ObjectReference getUnderlyingThisObject() throws DebugException {
        synchronized ( fThread ) {
            if ( (fStackFrame == null || fThisObject == null) && !isStatic() ) {
                try {
                    fThisObject = getUnderlyingStackFrame().thisObject();
                } catch ( RuntimeException e ) {
                    targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_this,
                                                               new String[]{e.toString()} ),
                                         e );
                    // execution will not reach this line, as 
                    // #targetRequestFailed will throw an exception			
                    return null;
                }
            }
            return fThisObject;
        }
    }

    public String getDeclaringTypeName() throws DebugException {
        synchronized ( fThread ) {
            try {
                if ( isObsolete() ) {
                    return JDIDebugModelMessages.JDIStackFrame__unknown_declaring_type__1;
                }
                return JDIReferenceType.getGenericName( getUnderlyingMethod().declaringType() );
            } catch ( RuntimeException e ) {
                if ( getThread().isSuspended() ) {
                    targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_declaring_type,
                                                               new String[]{e.toString()} ),
                                         e );
                }
                return JDIDebugModelMessages.JDIStackFrame__unknown_declaring_type__1;
            }
        }
    }

    public String getSourceName() throws DebugException {
        synchronized ( fThread ) {
            return getSourceName( fLocation );
        }
    }

    public boolean isObsolete() {
        if ( !JDIDebugPlugin.isJdiVersionGreaterThanOrEqual( new int[]{1, 4} ) || !((JDIDebugTarget) getDebugTarget()).hasHCROccurred() ) {
            // If no hot code replace has occurred, this frame
            // cannot be obsolete.
            return false;
        }
        // if this frame's thread is not suspended, the obsolete status cannot
        // change until it suspends again
        synchronized ( fThread ) {
            if ( getThread().isSuspended() ) {
                return getUnderlyingMethod().isObsolete();
            }
            return false;
        }
    }

    protected boolean exists() {
        synchronized ( fThread ) {
            return fDepth != -1;
        }
    }

    protected StackFrame getUnderlyingStackFrame() throws DebugException {
        synchronized ( fThread ) {
            if ( fStackFrame == null ) {
                if ( fDepth == -1 ) {
                    throw new DebugException( new Status( IStatus.ERROR,
                                                          JDIDebugPlugin.getUniqueIdentifier(),
                                                          IJavaStackFrame.ERR_INVALID_STACK_FRAME,
                                                          JDIDebugModelMessages.JDIStackFrame_25,
                                                          null ) );
                }
                if ( fThread.isSuspended() ) {
                    // re-index stack frames - See Bug 47198
                    fThread.computeStackFrames();
                    if ( fDepth == -1 ) {
                        // If depth is -1, then this is an invalid frame
                        throw new DebugException( new Status( IStatus.ERROR,
                                                              JDIDebugPlugin.getUniqueIdentifier(),
                                                              IJavaStackFrame.ERR_INVALID_STACK_FRAME,
                                                              JDIDebugModelMessages.JDIStackFrame_25,
                                                              null ) );
                    }
                } else {
                    throw new DebugException( new Status( IStatus.ERROR,
                                                          JDIDebugPlugin.getUniqueIdentifier(),
                                                          IJavaThread.ERR_THREAD_NOT_SUSPENDED,
                                                          JDIDebugModelMessages.JDIStackFrame_25,
                                                          null ) );
                }
            }
            return fStackFrame;
        }
    }

    protected void setUnderlyingStackFrame(StackFrame frame) {
        synchronized ( fThread ) {
            fStackFrame = frame;
            if ( frame == null ) {
                fRefreshVariables = true;
            }
        }
    }

    protected void setThread(JDIThread thread) {
        fThread = (DroolsThread) thread;
    }

    public String getSourcePath(String stratum) throws DebugException {
        synchronized ( fThread ) {
            try {
                return fLocation.sourcePath( stratum );
            } catch ( AbsentInformationException e ) {
            } catch ( RuntimeException e ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_path,
                                                           new String[]{e.toString()} ),
                                     e );
            }
        }
        return null;
    }

    public String getSourcePath() throws DebugException {
        synchronized ( fThread ) {
            try {
                return fLocation.sourcePath();
            } catch ( AbsentInformationException e ) {
            } catch ( RuntimeException e ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_path,
                                                           new String[]{e.toString()} ),
                                     e );
            }
        }
        return null;
    }

    public int getLineNumber(String stratum) throws DebugException {
        synchronized ( fThread ) {
            try {
                return fLocation.lineNumber( stratum );
            } catch ( RuntimeException e ) {
                if ( getThread().isSuspended() ) {
                    targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_line_number,
                                                               new String[]{e.toString()} ),
                                         e );
                }
            }
        }
        return -1;
    }

    public String getSourceName(String stratum) throws DebugException {
        synchronized ( fThread ) {
            try {
                return fLocation.sourceName( stratum );
            } catch ( AbsentInformationException e ) {
            } catch ( NativeMethodException e ) {
            } catch ( RuntimeException e ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_name,
                                                           new String[]{e.toString()} ),
                                     e );
            }
        }
        return null;
    }

    protected void updateVariables() throws DebugException {
        if ( fVariables == null ) {
            return;
        }

        Method method = getUnderlyingMethod();
        int index = 0;
        if ( !method.isStatic() ) {
            // update "this"
            ObjectReference thisObject;
            try {
                thisObject = getUnderlyingThisObject();
            } catch ( DebugException exception ) {
                if ( !getThread().isSuspended() ) {
                    thisObject = null;
                } else {
                    throw exception;
                }
            }
            DroolsThisVariable oldThisObject = null;
            if ( !fVariables.isEmpty() && fVariables.get( 0 ) instanceof DroolsThisVariable ) {
                oldThisObject = (DroolsThisVariable) fVariables.get( 0 );
            }
            if ( thisObject == null && oldThisObject != null ) {
                // removal of 'this'
                fVariables.remove( 0 );
                index = 0;
            } else {
                if ( oldThisObject == null && thisObject != null ) {
                    // creation of 'this'
                    oldThisObject = new DroolsThisVariable( (JDIDebugTarget) getDebugTarget(),
                                                            thisObject );
                    fVariables.add( 0,
                                    oldThisObject );
                    index = 1;
                } else {
                    if ( oldThisObject != null ) {
                        // 'this' still exists, replace with new 'this' if a different receiver
                        if ( !oldThisObject.retrieveValue().equals( thisObject ) ) {
                            fVariables.remove( 0 );
                            fVariables.add( 0,
                                            new DroolsThisVariable( (JDIDebugTarget) getDebugTarget(),
                                                                    thisObject ) );
                        }
                        index = 1;
                    }
                }
            }
        }

        List locals = null;
        try {
            locals = getUnderlyingStackFrame().visibleVariables();
        } catch ( AbsentInformationException e ) {
            locals = Collections.EMPTY_LIST;
        } catch ( NativeMethodException e ) {
            locals = Collections.EMPTY_LIST;
        } catch ( RuntimeException e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_visible_variables,
                                                       new String[]{e.toString()} ),
                                 e );
            // execution will not reach this line, as 
            // #targetRequestFailed will throw an exception			
            return;
        }
        int localIndex = -1;
        while ( index < fVariables.size() ) {
            Object var = fVariables.get( index );
            if ( var instanceof JDILocalVariable ) {
                DroolsLocalVariable local = (DroolsLocalVariable) fVariables.get( index );
                localIndex = locals.indexOf( local.getLocal() );
                if ( localIndex >= 0 ) {
                    // update variable with new underling JDI LocalVariable
                    local.setLocal( (LocalVariable) locals.get( localIndex ) );
                    locals.remove( localIndex );
                    index++;
                } else {
                    // remove variable
                    fVariables.remove( index );
                }
            } else {
                //field variable of a static frame
                index++;
            }
        }

        // add any new locals
        Iterator newOnes = locals.iterator();
        while ( newOnes.hasNext() ) {
            DroolsLocalVariable local = new DroolsLocalVariable( this,
                                                                 (LocalVariable) newOnes.next() );
            fVariables.add( local );
        }
    }

    protected void setVariables(List variables) {
        fVariables = variables;
    }

    public String getReceivingTypeName() throws DebugException {
        if ( fStackFrame == null || fReceivingTypeName == null ) {
            try {
                if ( isObsolete() ) {
                    fReceivingTypeName = JDIDebugModelMessages.JDIStackFrame__unknown_receiving_type__2;
                } else {
                    ObjectReference thisObject = getUnderlyingThisObject();
                    if ( thisObject == null ) {
                        fReceivingTypeName = getDeclaringTypeName();
                    } else {
                        fReceivingTypeName = JDIReferenceType.getGenericName( thisObject.referenceType() );
                    }
                }
            } catch ( RuntimeException e ) {
                if ( getThread().isSuspended() ) {
                    targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_receiving_type,
                                                               new String[]{e.toString()} ),
                                         e );
                }
                return JDIDebugModelMessages.JDIStackFrame__unknown_receiving_type__2;
            }
        }
        return fReceivingTypeName;
    }

    private String getSourceName(Location location) throws DebugException {
        try {
            return location.sourceName();
        } catch ( AbsentInformationException e ) {
            return null;
        } catch ( NativeMethodException e ) {
            return null;
        } catch ( RuntimeException e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_name,
                                                       new String[]{e.toString()} ),
                                 e );
        }
        return null;
    }

    private boolean equals(Object o1,
                           Object o2) {
        if ( o1 == null ) {
            return o2 == null;
        } else {
            return o1.equals( o2 );
        }
    }

    protected void clearCachedData() {
        fThisObject = null;
        fReceivingTypeName = null;
    }

    private void setLocalsAvailable(boolean available) {
        if ( available != fLocalsAvailable ) {
            fLocalsAvailable = available;
            fireChangeEvent( DebugEvent.STATE );
        }
    }

    public boolean wereLocalsAvailable() {
        return fLocalsAvailable;
    }

    public IJavaVariable[] getLocalVariables() throws DebugException {
        List list = getUnderlyingVisibleVariables();
        IJavaVariable[] locals = new IJavaVariable[list.size()];
        for ( int i = 0; i < list.size(); i++ ) {
            locals[i] = new DroolsLocalVariable( this,
                                                 (LocalVariable) list.get( i ) );
        }
        return locals;
    }

}
