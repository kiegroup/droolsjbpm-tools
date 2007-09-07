package org.drools.eclipse.debug.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.rule.builder.dialect.mvel.MVELDialect;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ClassType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

public class DroolsDebugTarget extends JDIDebugTarget {

    private ArrayList          fThreads;
    private ThreadStartHandler fThreadStartHandler = null;
    private boolean            fSuspended          = true;

    public DroolsDebugTarget(ILaunch launch,
                             VirtualMachine jvm,
                             String name,
                             boolean supportTerminate,
                             boolean supportDisconnect,
                             IProcess process,
                             boolean resume) {
        super( launch,
               jvm,
               name,
               supportTerminate,
               supportDisconnect,
               process,
               resume );
    }

    public void breakpointAdded(IBreakpoint breakpoint) {

        try {
            if ( breakpoint instanceof DroolsLineBreakpoint ) {
                ((DroolsLineBreakpoint) breakpoint).setJavaBreakpointProperties();

                final DroolsLineBreakpoint d = (DroolsLineBreakpoint) breakpoint;

                if ( d.getDialectName().equals( MVELDialect.ID ) ) {
                    //getBreakpoints().add( breakpoint );
                    //super.breakpointAdded(breakpoint);

                    Iterator handleriter = getVM().classesByName( "org.drools.base.mvel.MVELDebugHandler" ).iterator();
                    if ( !handleriter.hasNext() ) {
                        // Create class prepare request to add breakpoint after MVELDebugHanlder is loaded
                        ClassPrepareRequest req = getEventRequestManager().createClassPrepareRequest();
                        req.addClassFilter( "org.drools.base.mvel.MVELDebugHandler" );
                        req.setSuspendPolicy( EventRequest.SUSPEND_ALL );

                        addJDIEventListener( new IJDIEventListener() {

                                                 public boolean handleEvent(Event event,
                                                                            JDIDebugTarget target) {
                                                     addRemoteBreakpoint( d );
                                                     return true;
                                                 }

                                                 public void wonSuspendVote(Event event,
                                                                            JDIDebugTarget target) {
                                                 }

                                             },
                                             req );

                        req.enable();
                        return;
                    }

                    addRemoteBreakpoint( d );
                } else {
                    // only add breakpoint if setting Java properties of DRL
                    // breakpoint does not generate an error
                    super.breakpointAdded( breakpoint );
                }
            } else {
                super.breakpointAdded( breakpoint );
            }
        } catch ( Throwable t ) {
            // Exception will be thrown when trying to use breakpoint
            // on drl that is incorrect (cannot be parsed or compiled)
            DroolsEclipsePlugin.log( t );
        }
    }

    protected synchronized void initialize() {
        setThreadList( new ArrayList( 5 ) );
        super.initialize();
    }

    protected JDIThread createThread(ThreadReference thread) {
        JDIThread jdiThread = null;
        try {
            jdiThread = new DroolsThread( this,
                                          thread );
        } catch ( ObjectCollectedException exception ) {
            // ObjectCollectionException can be thrown if the thread has already
            // completed (exited) in the VM.
            return null;
        }
        if ( isDisconnected() ) {
            return null;
        }
        synchronized ( fThreads ) {
            fThreads.add( jdiThread );
        }
        jdiThread.fireCreationEvent();
        return jdiThread;
    }

    private Iterator getThreadIterator() {
        List threadList;
        synchronized ( fThreads ) {
            threadList = (List) fThreads.clone();
        }
        return threadList.iterator();
    }

    private boolean hasSuspendedThreads() {
        Iterator it = getThreadIterator();
        while ( it.hasNext() ) {
            IThread thread = (IThread) it.next();
            if ( thread.isSuspended() ) return true;
        }
        return false;
    }

    public boolean canResume() {
        return (isSuspended() || hasSuspendedThreads()) && isAvailable() && !isPerformingHotCodeReplace();
    }

    protected void resume(boolean fireNotification) throws DebugException {
        if ( (!isSuspended() && !hasSuspendedThreads()) || !isAvailable() ) {
            return;
        }
        try {
            setSuspended( false );
            resumeThreads();
            VirtualMachine vm = getVM();
            if ( vm != null ) {
                vm.resume();
            }
            if ( fireNotification ) {
                fireResumeEvent( DebugEvent.CLIENT_REQUEST );
            }
        } catch ( VMDisconnectedException e ) {
            disconnected();
            return;
        } catch ( RuntimeException e ) {
            setSuspended( true );
            fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIDebugTarget_exception_resume,
                                                       new String[]{e.toString()} ),
                                 e );
        }
    }

    private void setSuspended(boolean suspended) {
        fSuspended = suspended;
    }

    public boolean isSuspended() {
        return fSuspended;
    }

    private void setThreadList(ArrayList threads) {
        fThreads = threads;
    }

    public IThread[] getThreads() {
        synchronized ( fThreads ) {
            return (IThread[]) fThreads.toArray( new IThread[0] );
        }
    }

    protected void removeAllThreads() {
        Iterator itr = getThreadIterator();
        while ( itr.hasNext() ) {
            DroolsThread child = (DroolsThread) itr.next();
            child.terminated();
        }
        synchronized ( fThreads ) {
            fThreads.clear();
        }
    }

    protected void initializeRequests() {
        setThreadStartHandler( new ThreadStartHandler() );
        new ThreadDeathHandler();
        new MVELTraceHandler();
    }

    class ThreadDeathHandler
        implements
        IJDIEventListener {

        protected ThreadDeathHandler() {
            createRequest();
        }

        /**
         * Creates and registers a request to listen to thread
         * death events.
         */
        protected void createRequest() {
            EventRequestManager manager = getEventRequestManager();
            if ( manager != null ) {
                try {
                    EventRequest req = manager.createThreadDeathRequest();
                    req.setSuspendPolicy( EventRequest.SUSPEND_NONE );
                    req.enable();
                    addJDIEventListener( this,
                                         req );
                } catch ( RuntimeException e ) {
                    logError( e );
                }
            }
        }

        /**
         * Locates the model thread associated with the underlying JDI thread
         * that has terminated, and removes it from the collection of
         * threads belonging to this debug target. A terminate event is
         * fired for the model thread.
         *
         * @param event a thread death event
         * @param target the target in which the thread died
         * @return <code>true</code> - the thread should be resumed
         */
        public boolean handleEvent(Event event,
                                   JDIDebugTarget target) {
            ThreadReference ref = ((ThreadDeathEvent) event).thread();
            DroolsThread thread = (DroolsThread) findThread( ref );
            if ( thread != null ) {
                synchronized ( fThreads ) {
                    fThreads.remove( thread );
                }
                thread.terminated();
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jdt.internal.debug.core.IJDIEventListener#wonSuspendVote(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
         */
        public void wonSuspendVote(Event event,
                                   JDIDebugTarget target) {
            // do nothing
        }

    }

    class ThreadStartHandler
        implements
        IJDIEventListener {

        protected EventRequest fRequest;

        protected ThreadStartHandler() {
            createRequest();
        }

        /**
         * Creates and registers a request to handle all thread start
         * events
         */
        protected void createRequest() {
            EventRequestManager manager = getEventRequestManager();
            if ( manager != null ) {
                try {
                    EventRequest req = manager.createThreadStartRequest();
                    req.setSuspendPolicy( EventRequest.SUSPEND_NONE );
                    req.enable();
                    addJDIEventListener( this,
                                         req );
                    setRequest( req );
                } catch ( RuntimeException e ) {
                    logError( e );
                }
            }
        }

        /**
         * Creates a model thread for the underlying JDI thread
         * and adds it to the collection of threads for this
         * debug target. As a side effect of creating the thread,
         * a create event is fired for the model thread.
         * The event is ignored if the underlying thread is already
         * marked as collected.
         *
         * @param event a thread start event
         * @param target the target in which the thread started
         * @return <code>true</code> - the thread should be resumed
         */
        public boolean handleEvent(Event event,
                                   JDIDebugTarget target) {
            ThreadReference thread = ((ThreadStartEvent) event).thread();
            try {
                if ( thread.isCollected() ) {
                    return false;
                }
            } catch ( VMDisconnectedException exception ) {
                return false;
            } catch ( ObjectCollectedException e ) {
                return false;
            } catch ( TimeoutException e ) {
                // continue - attempt to create the thread
            }
            DroolsThread jdiThread = (DroolsThread) findThread( thread );
            if ( jdiThread == null ) {
                jdiThread = (DroolsThread) createThread( thread );
                if ( jdiThread == null ) {
                    return false;
                }
            } else {
                jdiThread.disposeStackFrames();
                jdiThread.fireChangeEvent( DebugEvent.CONTENT );
            }
            return !jdiThread.isSuspended();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jdt.internal.debug.core.IJDIEventListener#wonSuspendVote(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
         */
        public void wonSuspendVote(Event event,
                                   JDIDebugTarget target) {
            // do nothing
        }

        /**
         * Deregisters this event listener.
         */
        protected void deleteRequest() {
            if ( getRequest() != null ) {
                removeJDIEventListener( this,
                                        getRequest() );
                setRequest( null );
            }
        }

        protected EventRequest getRequest() {
            return fRequest;
        }

        protected void setRequest(EventRequest request) {
            fRequest = request;
        }
    }

    private void disposeThreadHandler() {
        ThreadStartHandler handler = getThreadStartHandler2();
        if ( handler != null ) {
            handler.deleteRequest();
        }
    }

    public boolean hasThreads() {
        return fThreads.size() > 0;
    }

    protected ThreadStartHandler getThreadStartHandler2() {
        return fThreadStartHandler;
    }

    protected void setThreadStartHandler(ThreadStartHandler threadStartHandler) {
        fThreadStartHandler = threadStartHandler;
    }

    public boolean isOutOfSynch() throws DebugException {
        Iterator threads = getThreadIterator();
        while ( threads.hasNext() ) {
            JDIThread thread = (JDIThread) threads.next();
            if ( thread.isOutOfSynch() ) {
                return true;
            }
        }
        return false;
    }

    public boolean mayBeOutOfSynch() {
        Iterator threads = getThreadIterator();
        while ( threads.hasNext() ) {
            JDIThread thread = (JDIThread) threads.next();
            if ( thread.mayBeOutOfSynch() ) {
                return true;
            }
        }
        return false;
    }

    public JDIThread findThread(ThreadReference tr) {
        Iterator iter = getThreadIterator();
        while ( iter.hasNext() ) {
            JDIThread thread = (JDIThread) iter.next();
            if ( thread.getUnderlyingThread().equals( tr ) ) return thread;
        }
        return null;
    }

    public void breakpointRemoved(IBreakpoint breakpoint,
                                  IMarkerDelta delta) {
        if ( !isAvailable() ) {
            return;
        }
        if ( supportsBreakpoint( breakpoint ) ) {
            try {

                if ( breakpoint instanceof DroolsLineBreakpoint ) {
                    ((DroolsLineBreakpoint) breakpoint).setJavaBreakpointProperties();

                    final DroolsLineBreakpoint d = (DroolsLineBreakpoint) breakpoint;

                    if ( d.getDialectName().equals( MVELDialect.ID ) ) {
                        removeRemoteBreakpoint( (DroolsLineBreakpoint) breakpoint,
                                                delta );
                    }
                }

                ((JavaBreakpoint) breakpoint).removeFromTarget( this );
                getBreakpoints().remove( breakpoint );
                Iterator threads = getThreadIterator();
                while ( threads.hasNext() ) {
                    ((DroolsThread) threads.next()).removeCurrentBreakpoint( breakpoint );
                }
            } catch ( CoreException e ) {
                logError( e );
            }
        }
    }

    protected void suspendThreads() {
        Iterator threads = getThreadIterator();
        while ( threads.hasNext() ) {
            ((DroolsThread) threads.next()).suspendedByVM();
        }
    }

    protected void resumeThreads() throws DebugException {
        Iterator threads = getThreadIterator();
        while ( threads.hasNext() ) {
            ((DroolsThread) threads.next()).resumedByVM();
        }
    }

    public void disconnect() throws DebugException {

        if ( !isAvailable() ) {
            // already done
            return;
        }

        if ( !canDisconnect() ) {
            notSupported( JDIDebugModelMessages.JDIDebugTarget_does_not_support_disconnect );
        }

        try {
            disposeThreadHandler();
            VirtualMachine vm = getVM();
            if ( vm != null ) {
                vm.dispose();
            }
        } catch ( VMDisconnectedException e ) {
            // if the VM disconnects while disconnecting, perform
            // normal disconnect handling
            disconnected();
        } catch ( RuntimeException e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIDebugTarget_exception_disconnecting,
                                                       new String[]{e.toString()} ),
                                 e );
        }

    }

    public void terminate() throws DebugException {
        if ( !isAvailable() ) {
            return;
        }
        if ( !supportsTerminate() ) {
            notSupported( JDIDebugModelMessages.JDIDebugTarget_does_not_support_termination );
        }
        try {
            setTerminating( true );
            disposeThreadHandler();
            VirtualMachine vm = getVM();
            if ( vm != null ) {
                vm.exit( 1 );
            }
            IProcess process = getProcess();
            if ( process != null ) {
                process.terminate();
            }
        } catch ( VMDisconnectedException e ) {
            // if the VM disconnects while exiting, perform
            // normal termination processing
            terminated();
        } catch ( TimeoutException exception ) {
            // if there is a timeout see if the associated process is terminated
            IProcess process = getProcess();
            if ( process != null && process.isTerminated() ) {
                terminated();
            } else {
                // All we can do is disconnect
                disconnected();
            }
        } catch ( RuntimeException e ) {
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIDebugTarget_exception_terminating,
                                                       new String[]{e.toString()} ),
                                 e );
        }
    }

    public void handleVMStart(VMStartEvent event) {
        if ( isResumeOnStartup() ) {
            try {
                setSuspended( true );
                resume();
            } catch ( DebugException e ) {
                logError( e );
            }
        }
        // If any threads have resumed since thread collection was initialized,
        // update their status (avoid concurrent modification - use #getThreads())
        IThread[] threads = getThreads();
        for ( int i = 0; i < threads.length; i++ ) {
            DroolsThread thread = (DroolsThread) threads[i];
            if ( thread.isSuspended() ) {
                try {
                    boolean suspended = thread.getUnderlyingThread().isSuspended();
                    if ( !suspended ) {
                        thread.setRunning( true );
                        thread.fireResumeEvent( DebugEvent.CLIENT_REQUEST );
                    }
                } catch ( VMDisconnectedException e ) {
                } catch ( ObjectCollectedException e ) {
                } catch ( RuntimeException e ) {
                    logError( e );
                }
            }
        }
    }

    protected void initializeState() {

        List threads = null;
        VirtualMachine vm = getVM();
        if ( vm != null ) {
            try {
                threads = vm.allThreads();
            } catch ( RuntimeException e ) {
                internalError( e );
            }
            if ( threads != null ) {
                Iterator initialThreads = threads.iterator();
                while ( initialThreads.hasNext() ) {
                    createThread( (ThreadReference) initialThreads.next() );
                }
            }
        }

        if ( isResumeOnStartup() ) {
            setSuspended( false );
        }
    }

    public void suspend() throws DebugException {
        if ( isSuspended() ) {
            return;
        }
        try {
            VirtualMachine vm = getVM();
            if ( vm != null ) {
                vm.suspend();
            }
            suspendThreads();
            setSuspended( true );
            fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
        } catch ( RuntimeException e ) {
            setSuspended( false );
            fireResumeEvent( DebugEvent.CLIENT_REQUEST );
            targetRequestFailed( MessageFormat.format( JDIDebugModelMessages.JDIDebugTarget_exception_suspend,
                                                       new String[]{e.toString()} ),
                                 e );
        }
    }

    public void prepareToSuspendByBreakpoint(JavaBreakpoint breakpoint) {
        setSuspended( true );
        suspendThreads();
    }

    protected void cancelSuspendByBreakpoint(JavaBreakpoint breakpoint) throws DebugException {
        setSuspended( false );
        resumeThreads();
    }

    class MVELTraceHandler
        implements
        IJDIEventListener {

        protected MVELTraceHandler() {
            createRequest();
        }

        protected void createRequest() {
            EventRequestManager manager = getEventRequestManager();
            if ( manager != null ) {
                try {
                    ClassPrepareRequest req = manager.createClassPrepareRequest();
                    req.addClassFilter( "org.drools.base.mvel.MVELDebugHandler" );
                    req.setSuspendPolicy( EventRequest.SUSPEND_ALL );
                    addJDIEventListener( MVELTraceHandler.this,
                                         req );
                    req.enable();

                } catch ( RuntimeException e ) {
                    logError( e );
                }
            }
        }

        /**
         * Locates the model thread associated with the underlying JDI thread
         * that has terminated, and removes it from the collection of
         * threads belonging to this debug target. A terminate event is
         * fired for the model thread.
         *
         * @param event a thread death event
         * @param target the target in which the thread died
         * @return <code>true</code> - the thread should be resumed
         */
        public boolean handleEvent(Event event,
                                   JDIDebugTarget target) {
            String name = ((ClassPrepareEvent) event).referenceType().name();

            MethodEntryRequest req = getEventRequestManager().createMethodEntryRequest();
            req.addClassFilter( ((ClassPrepareEvent) event).referenceType() );

            //breakpointCatched

            /*field= type.fieldByName(getFieldName());
             Field field;
             EventRequest req= manager.createModificationWatchpointRequest(field);
             */
            req.setSuspendPolicy( EventRequest.SUSPEND_EVENT_THREAD );
            addJDIEventListener( new IJDIEventListener() {

                                     public boolean handleEvent(Event event,
                                                                JDIDebugTarget target) {
                                         MethodEntryEvent entryEvent = (MethodEntryEvent) event;

                                         String name2 = entryEvent.method().name();

                                         if ( !name2.equals( "onBreak" ) && !name2.equals( "receiveBreakpoints" ) ) {
                                             //event.virtualMachine().resume();
                                             return true;
                                         }

                                         try {
                                             IThread[] tharr = getThreads();

                                             ThreadReference t = null;
                                             DroolsThread t2 = null;

                                             for ( int i = 0; i < tharr.length; i++ ) {
                                                 DroolsThread th2 = (DroolsThread) tharr[i];
                                                 ThreadReference th2real = ((DroolsThread) tharr[i]).getUnderlyingThread();

                                                 if ( th2real.suspendCount() == 1 && th2.getName().equals( "main" ) ) {
                                                     t = th2real;
                                                     t2 = (DroolsThread) th2;

                                                     th2real.suspend();
                                                     th2.setRunning( false );
                                                     th2.fireSuspendEvent( DebugEvent.CLIENT_REQUEST );

                                                     return true;
                                                 }
                                             }
                                         } catch ( Exception t ) {
                                             logError( t );
                                         }
                                         return true;

                                     }

                                     public void wonSuspendVote(Event event,
                                                                JDIDebugTarget target) {

                                     }

                                 },
                                 req );

            req.enable();

            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jdt.internal.debug.core.IJDIEventListener#wonSuspendVote(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
         */
        public void wonSuspendVote(Event event,
                                   JDIDebugTarget target) {
            // do nothing
        }

    }

    /**
     * Tries to find a match for the provided breakpoint information from the list of registered breakpoints.
     * For stepping and possibly other purposes it returns also a breakpoint for cases where exactly the same line was not found.
     * 
     * If breakpoint is not found for <code>line</code> at <code>source</code> then it takes the first line that is above the
     * specified line at the same file.
     * 
     * @param source
     * @param line
     * @return
     */
    public DroolsLineBreakpoint getDroolsBreakpoint(String source) {

        if ( source == null ) {
            return null;
        }

        Iterator iterator = getBreakpoints().iterator();
        while ( iterator.hasNext() ) {
            IJavaBreakpoint element = (IJavaBreakpoint) iterator.next();
            if ( element instanceof DroolsLineBreakpoint && ((DroolsLineBreakpoint) element).getDialectName().equals( MVELDialect.ID ) ) {
                DroolsLineBreakpoint l = (DroolsLineBreakpoint) element;
                try {

                    int matchLine = l.getLineNumber();
                    String matchSource = l.getRuleName();

                    if ( source.equals( matchSource ) || l.getFileRuleMappings().containsKey( source ) ) {
                        return l;
                    }

                } catch ( CoreException e ) {
                    logError( e );
                }
            }
        }

        return null;
    }

    private void addRemoteBreakpoint(DroolsLineBreakpoint d) {

        try {
            if ( !d.isEnabled() ) {
                return; // No need to install disabled breakpoints
            }
        } catch ( CoreException e2 ) {
            logError( e2 );
            return; // No need to install breakpoints that are this much broken
        }

        Iterator handleriter = getVM().classesByName( "org.drools.base.mvel.MVELDebugHandler" ).iterator();
        Object debugHandlerClass = handleriter.next();

        int line;
        String sourceName;

        try {
            line = d.getLineNumber();
            sourceName = d.getTypeName();
        } catch ( CoreException e1 ) {
            logError( e1 );
            return;
        }

        ReferenceType refType = (ReferenceType) debugHandlerClass;
        Method m = (Method) refType.methodsByName( "registerBreakpoint" ).iterator().next();
        List args = new ArrayList();
        IntegerValue lineVal = getVM().mirrorOf( line );
        StringReference nameVal = getVM().mirrorOf( sourceName );
        JDIObjectValue val = (JDIObjectValue) newValue( sourceName );
        ObjectReference realVal = val.getUnderlyingObject();
        args.add( nameVal );
        args.add( lineVal );

        try {
            ClassType tt = (ClassType) debugHandlerClass;
            IThread[] tharr = getThreads();
            ThreadReference t = null;
            DroolsThread t2 = null;

            for ( int i = 0; i < tharr.length; i++ ) {
                IThread th2 = tharr[i];
                ThreadReference th2real = ((DroolsThread) tharr[i]).getUnderlyingThread();

                if ( th2real.suspendCount() == 1 && th2.getName().equals( "main" ) ) {
                    t = th2real;
                    t2 = (DroolsThread) th2;
                }
            }

            tt.invokeMethod( t,
                             m,
                             args,
                             ObjectReference.INVOKE_SINGLE_THREADED );
            //t2.computeNewStackFrames();

            super.breakpointAdded( d );

        } catch ( Exception e ) {
            logError( e );
        }

    }

    private void removeRemoteBreakpoint(DroolsLineBreakpoint d,
                                        IMarkerDelta delta) {
        Iterator handleriter = getVM().classesByName( "org.drools.base.mvel.MVELDebugHandler" ).iterator();
        Object debugHandlerClass = handleriter.next();

        int line;
        String sourceName;

        try {
            line = d.getLineNumber();
            sourceName = d.getTypeName();
        } catch ( CoreException e1 ) {
            logError( e1 );
            return;
        }

        ReferenceType refType = (ReferenceType) debugHandlerClass;
        Method m = (Method) refType.methodsByName( "removeBreakpoint" ).iterator().next();
        List args = new ArrayList();
        IntegerValue lineVal = getVM().mirrorOf( line );
        StringReference nameVal = getVM().mirrorOf( sourceName );
        JDIObjectValue val = (JDIObjectValue) newValue( sourceName );
        ObjectReference realVal = val.getUnderlyingObject();
        args.add( nameVal );
        args.add( lineVal );

        try {
            ClassType tt = (ClassType) debugHandlerClass;
            IThread[] tharr = getThreads();
            ThreadReference t = null;
            DroolsThread t2 = null;

            for ( int i = 0; i < tharr.length; i++ ) {
                IThread th2 = tharr[i];
                ThreadReference th2real = ((DroolsThread) tharr[i]).getUnderlyingThread();

                if ( th2real.suspendCount() == 1 && th2.getName().equals( "main" ) ) {
                    t = th2real;
                    t2 = (DroolsThread) th2;
                }
            }

            tt.invokeMethod( t,
                             m,
                             args,
                             ObjectReference.INVOKE_SINGLE_THREADED );

        } catch ( Exception e ) {
            logError( e );
        }

    }

}
