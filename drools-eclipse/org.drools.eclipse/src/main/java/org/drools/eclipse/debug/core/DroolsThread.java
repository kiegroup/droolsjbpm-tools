package org.drools.eclipse.debug.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.mvel2.debug.Debugger;

import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Drools Thread supporting MVEL and Java dialect stackframes
 *
 */
public class DroolsThread extends JDIThread {
    public DroolsThread(JDIDebugTarget target,
                        ThreadReference thread) throws ObjectCollectedException {
        super( target,
               thread );
    }

    protected synchronized List computeStackFrames(boolean refreshChildren) throws DebugException {
        List fStackFrames = getInternalfStackFrames();

        if ( isSuspended() ) {
            if ( isTerminated() ) {
                fStackFrames.clear();
            } else if ( refreshChildren ) {
                List frames = getInternalUnderlyingFrames();
                int oldSize = fStackFrames.size();
                int newSize = frames.size();
                int discard = oldSize - newSize; // number of old frames to discard, if any
                for ( int i = 0; i < discard; i++ ) {
                    DroolsStackFrame invalid = (DroolsStackFrame) fStackFrames.remove( 0 );
                    invalid.bind( null,
                                  -1 );
                }
                int newFrames = newSize - oldSize; // number of frames to create, if any
                int depth = oldSize;
                for ( int i = newFrames - 1; i >= 0; i-- ) {
                    StackFrame currentFrame = (StackFrame) frames.get( i );
                    JDIStackFrame customFrame = createCustomFrame( this,
                                                                   depth,
                                                                   currentFrame );

                    fStackFrames.add( 0,
                                      customFrame );

                    depth++;
                }
                int numToRebind = Math.min( newSize,
                                            oldSize ); // number of frames to attempt to rebind
                int offset = newSize - 1;
                for ( depth = 0; depth < numToRebind; depth++ ) {
                    DroolsStackFrame oldFrame = (DroolsStackFrame) fStackFrames.get( offset );
                    StackFrame frame = (StackFrame) frames.get( offset );
                    DroolsStackFrame newFrame = (DroolsStackFrame) oldFrame.bind( frame,
                                                                                  depth );
                    if ( newFrame != oldFrame ) {
                        fStackFrames.set( offset,
                                          newFrame );
                    }
                    offset--;
                }

            }
            setInternalfRefreshChildren( false );
        } else {
            return Collections.EMPTY_LIST;
        }
        return fStackFrames;
    }

    public final static synchronized DroolsStackFrame createCustomFrame(DroolsThread thread,
                                                                        int depth,
                                                                        StackFrame currentFrame) {
        DroolsStackFrame customFrame;
        Location loc = currentFrame.location();
        if ( loc.declaringType().name().equals( "org.drools.base.mvel.MVELDebugHandler" ) && loc.method().name().equals( "onBreak" ) ) {
            customFrame = new MVELStackFrame( thread,
                                              currentFrame,
                                              depth );
        } else {
            customFrame = new DroolsStackFrame( thread,
                                                currentFrame,
                                                depth );
        }
        return customFrame;
    }

// I don't see the need for any of this custom stepOver stuff, why is it here?
//    public synchronized void stepOver() throws DebugException {
//
//        // Detection for active stackframe
//        if ( !(getTopStackFrame() instanceof MVELStackFrame) ) {
//            super.stepOver();
//            return;
//        }
//
//        //MVEL step over
//        MVELStackFrame mvelStack = (MVELStackFrame) getTopStackFrame();
//
//        if ( !canStepOver() || !mvelStack.canStepOver() ) {
//            return;
//        }
//
//        if ( !setRemoteOnBreakReturn( Debugger.STEP ) ) {
//            return;
//        }
//
//        setRunning( true );
//
//        preserveStackFrames();
//
//        fireEvent( new DebugEvent( this,
//                                   DebugEvent.RESUME,
//                                   DebugEvent.STEP_OVER ) );
//
//        try {
//            getUnderlyingThread().resume();
//        } catch ( RuntimeException e ) {
//            //stepEnd();
//            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_stepping,
//                                                       new String[]{e.toString()} ),
//                                 e );
//        }
//
//    }
//
//    private boolean setRemoteOnBreakReturn(int step_over) throws DebugException {
//
//        JDIStackFrame top = (JDIStackFrame) getTopStackFrame();
//        if ( top == null || (!(top instanceof MVELStackFrame)) ) {
//            return false;
//        }
//
//        Iterator handleriter = getVM().classesByName( "org.drools.base.mvel.MVELDebugHandler" ).iterator();
//        Object debugHandlerClass = handleriter.next();
//
//        int line = step_over;
//
//        ReferenceType refType = (ReferenceType) debugHandlerClass;
//        Method m = (Method) refType.methodsByName( "setOnBreakReturn" ).iterator().next();
//        List args = new ArrayList();
//        IntegerValue lineVal = getVM().mirrorOf( line );
//        //ObjectReference realVal = val.getUnderlyingObject();
//        args.add( lineVal );
//
//        try {
//            ClassType tt = (ClassType) debugHandlerClass;
//            tt.invokeMethod( getUnderlyingThread(),
//                             m,
//                             args,
//                             ObjectReference.INVOKE_SINGLE_THREADED );
//
//        } catch ( Exception e ) {
//            DroolsEclipsePlugin.log( e );
//            return false;
//        }
//        return true;
//    }
//
//    public synchronized void resume() throws DebugException {
//        // clear up the step over flag. step over button never calls this method.
//        setRemoteOnBreakReturn( Debugger.CONTINUE );
//        super.resume();
//    }

    protected synchronized void disposeStackFrames() {
        super.disposeStackFrames();
    }

    protected void terminated() {
        super.terminated();
    }

    protected void removeCurrentBreakpoint(IBreakpoint bp) {
        super.removeCurrentBreakpoint( bp );
    }

    protected synchronized void suspendedByVM() {
        super.suspendedByVM();
    }

    protected synchronized void resumedByVM() throws DebugException {
        super.resumedByVM();
    }

    protected void setRunning(boolean running) {
        super.setRunning( running );
    }

    public void setInternalfRefreshChildren(boolean bool) {
        try {
            java.lang.reflect.Field field = JDIThread.class.getDeclaredField( "fRefreshChildren" );
            field.setAccessible( true );
            field.set( this,
                       bool );
        } catch ( Exception e ) {
        }
    }

    public List getInternalfStackFrames() {
        try {
            java.lang.reflect.Field field = JDIThread.class.getDeclaredField( "fStackFrames" );

            field.setAccessible( true );

            return (List) field.get( this );
        } catch ( Exception e ) {
            return null;
        }
    }

    private List getInternalUnderlyingFrames() throws DebugException {
        try {
            java.lang.reflect.Method method = JDIThread.class.getDeclaredMethod( "getUnderlyingFrames",
                                                                                 null );

            method.setAccessible( true );

            return (List) method.invoke( this,
                                         null );
        } catch ( Exception e ) {
            return null;
        }
    }

}
