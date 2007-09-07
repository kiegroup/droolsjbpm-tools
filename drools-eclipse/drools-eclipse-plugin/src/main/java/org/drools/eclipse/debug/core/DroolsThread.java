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
import org.mvel.debug.Debugger;

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

    private List    fStackFrames;
    private boolean fRefreshChildren = true;

    public DroolsThread(JDIDebugTarget target,
                        ThreadReference thread) throws ObjectCollectedException {
        super( target,
               thread );
    }

    protected void initialize() throws ObjectCollectedException {
        super.initialize();
        fStackFrames = new ArrayList();
    }

    public synchronized List computeStackFrames() throws DebugException {
        return computeStackFrames( fRefreshChildren );
    }

    protected synchronized List computeStackFrames(boolean refreshChildren) throws DebugException {
        if ( isSuspended() ) {
            if ( isTerminated() ) {
                fStackFrames.clear();
            } else if ( refreshChildren ) {
                List frames = getUnderlyingFrames();
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
                    //MVEL: create an mvel stack frame when the declaring type is our debugger?

                    DroolsStackFrame customFrame;

                    customFrame = createCustomFrame( this,
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
            fRefreshChildren = false;
        } else {
            return Collections.EMPTY_LIST;
        }
        return fStackFrames;
    }

    public final static DroolsStackFrame createCustomFrame(DroolsThread thread,
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

    private List getUnderlyingFrames() throws DebugException {
        if ( !isSuspended() ) {
            // Checking isSuspended here eliminates a race condition in resume
            // between the time stack frames are preserved and the time the
            // underlying thread is actually resumed.
            requestFailed( JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1,
                           null,
                           IJavaThread.ERR_THREAD_NOT_SUSPENDED );
        }
        try {
            return getUnderlyingThread().frames();
        } catch ( IncompatibleThreadStateException e ) {
            requestFailed( JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1,
                           e,
                           IJavaThread.ERR_THREAD_NOT_SUSPENDED );
        } catch ( RuntimeException e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2,
                                                       new String[]{e.toString()} ),
                                 e );
        } catch ( InternalError e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2,
                                                       new String[]{e.toString()} ),
                                 e );
        }
        // execution will not reach this line, as
        // #targetRequestFailed will thrown an exception
        return null;
    }

    protected synchronized void preserveStackFrames() {
        fRefreshChildren = true;
        Iterator frames = fStackFrames.iterator();
        while ( frames.hasNext() ) {
            ((DroolsStackFrame) frames.next()).setUnderlyingStackFrame( null );
        }
    }

    protected synchronized void disposeStackFrames() {
        fStackFrames.clear();
        fRefreshChildren = true;
    }

    protected void popFrame(IStackFrame frame) throws DebugException {
        JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
        if ( target.canPopFrames() ) {
            // JDK 1.4 support
            try {
                // Pop the frame and all frames above it
                StackFrame jdiFrame = null;
                int desiredSize = fStackFrames.size() - fStackFrames.indexOf( frame ) - 1;
                int lastSize = fStackFrames.size() + 1; // Set up to pass the first test
                int size = fStackFrames.size();
                while ( size < lastSize && size > desiredSize ) {
                    // Keep popping frames until the stack stops getting smaller
                    // or popFrame is gone.
                    // see Bug 8054
                    jdiFrame = ((DroolsStackFrame) frame).getUnderlyingStackFrame();
                    preserveStackFrames();
                    getUnderlyingThread().popFrames( jdiFrame );
                    lastSize = size;
                    size = computeStackFrames().size();
                }
            } catch ( IncompatibleThreadStateException exception ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_popping,
                                                           new String[]{exception.toString()} ),
                                     exception );
            } catch ( InvalidStackFrameException exception ) {
                // InvalidStackFrameException can be thrown when all but the
                // deepest frame were popped. Fire a changed notification
                // in case this has occured.
                fireChangeEvent( DebugEvent.CONTENT );
                targetRequestFailed( exception.toString(),
                                     exception );
            } catch ( RuntimeException exception ) {
                targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_popping,
                                                           new String[]{exception.toString()} ),
                                     exception );
            }
        }
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

    protected void dropToFrame(IStackFrame frame) throws DebugException {
        super.dropToFrame( frame );
    }

    protected synchronized void stepToFrame(IStackFrame frame) throws DebugException {
        super.stepToFrame( frame );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.core.model.JDIThread#newInstance(com.sun.jdi.ClassType, com.sun.jdi.Method, java.util.List)
     */
    public ObjectReference newInstance(ClassType receiverClass,
                                       Method constructor,
                                       List args) throws DebugException {
        return super.newInstance( receiverClass,
                                  constructor,
                                  args );
    }

    public synchronized void stepOver() throws DebugException {

        // Detection for active stackframe
        if ( !(getTopStackFrame() instanceof MVELStackFrame) ) {
            super.stepOver();
            return;
        }

        //MVEL step over
        MVELStackFrame mvelStack = (MVELStackFrame) getTopStackFrame();

        if ( !canStepOver() || !mvelStack.canStepOver() ) {
            return;
        }

        if ( !setRemoteOnBreakReturn( Debugger.STEP ) ) {
            return;
        }

        preserveStackFrames();

        setRunning( true );

        try {
            getUnderlyingThread().resume();
        } catch ( RuntimeException e ) {
            //stepEnd();
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIThread_exception_stepping,
                                                       new String[]{e.toString()} ),
                                 e );
        }

    }

    private boolean setRemoteOnBreakReturn(int step_over) throws DebugException {

        JDIStackFrame top = (JDIStackFrame) getTopStackFrame();
        if ( top == null || (!(top instanceof MVELStackFrame)) ) {
            return false;
        }

        Iterator handleriter = getVM().classesByName( "org.drools.base.mvel.MVELDebugHandler" ).iterator();
        Object debugHandlerClass = handleriter.next();

        int line = step_over;

        ReferenceType refType = (ReferenceType) debugHandlerClass;
        Method m = (Method) refType.methodsByName( "setOnBreakReturn" ).iterator().next();
        List args = new ArrayList();
        IntegerValue lineVal = getVM().mirrorOf( line );
        //ObjectReference realVal = val.getUnderlyingObject();
        args.add( lineVal );

        try {
            ClassType tt = (ClassType) debugHandlerClass;
            tt.invokeMethod( getUnderlyingThread(),
                             m,
                             args,
                             ObjectReference.INVOKE_SINGLE_THREADED );

        } catch ( Exception e ) {
            DroolsEclipsePlugin.log( e );
            return false;
        }
        return true;
    }

    public synchronized void resume() throws DebugException {
        // clear up the step over flag. step over button never calls this method.
        setRemoteOnBreakReturn( Debugger.CONTINUE );
        super.resume();
    }

    public void setInvokingMethod(boolean invoking) {
        super.setInvokingMethod( invoking );
    }

}
