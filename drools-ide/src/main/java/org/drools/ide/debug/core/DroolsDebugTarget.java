package org.drools.ide.debug.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class DroolsDebugTarget extends JDIDebugTarget {

	private ArrayList fThreads;
	private ThreadStartHandler fThreadStartHandler = null;
	private boolean fSuspended = true;
	
	public DroolsDebugTarget(ILaunch launch, VirtualMachine jvm, String name, boolean supportTerminate, boolean supportDisconnect, IProcess process, boolean resume) {
		super(launch, jvm, name, supportTerminate, supportDisconnect, process, resume);
	}

	protected synchronized void initialize() {
		setThreadList(new ArrayList(5));
		super.initialize();
	}
	
	protected JDIThread createThread(ThreadReference thread) {
		JDIThread jdiThread= null;
		try {
			jdiThread= new DroolsThread(this, thread);
		} catch (ObjectCollectedException exception) {
			// ObjectCollectionException can be thrown if the thread has already
			// completed (exited) in the VM.
			return null;
		}
		if (isDisconnected()) {
			return null;
		}
		synchronized (fThreads) {
			fThreads.add(jdiThread);
		}
		jdiThread.fireCreationEvent();
		return jdiThread;
	}
	
	private Iterator getThreadIterator() {
		List threadList;
		synchronized (fThreads) {
			threadList= (List) fThreads.clone();
		}
		return threadList.iterator();
	}

	private boolean hasSuspendedThreads() {
		Iterator it = getThreadIterator();
		while(it.hasNext()){
			IThread thread = (IThread)it.next();
			if(thread.isSuspended())
				return true;
		}
		return false;
	}

	public boolean canResume() {
		return (isSuspended() || hasSuspendedThreads())
		    && isAvailable() && !isPerformingHotCodeReplace();
	}
	
	protected void resume(boolean fireNotification) throws DebugException {
		if ((!isSuspended() && !hasSuspendedThreads()) 
			|| !isAvailable()) {
			return;
		}
		try {
			setSuspended(false);
			resumeThreads();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.resume();
			}
			if (fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (RuntimeException e) {
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIDebugTarget_exception_resume, new String[] {e.toString()}), e); 
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
		synchronized (fThreads) {
			return (IThread[])fThreads.toArray(new IThread[0]);
		}
	}

	protected void removeAllThreads() {
		Iterator itr= getThreadIterator();
		while (itr.hasNext()) {
			DroolsThread child= (DroolsThread) itr.next();
			child.terminated();
		}
		synchronized (fThreads) {
		    fThreads.clear();
		}
	}
	
	protected void initializeRequests() {
		setThreadStartHandler(new ThreadStartHandler());
		new ThreadDeathHandler();		
	}
	
	class ThreadDeathHandler implements IJDIEventListener {
		
		protected ThreadDeathHandler() {
			createRequest();
		}
		
		/**
		 * Creates and registers a request to listen to thread
		 * death events.
		 */
		protected void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if (manager != null) {
				try {
					EventRequest req= manager.createThreadDeathRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addJDIEventListener(this, req);	
				} catch (RuntimeException e) {
					logError(e);
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
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			ThreadReference ref= ((ThreadDeathEvent)event).thread();
			DroolsThread thread= (DroolsThread) findThread(ref);
			if (thread != null) {
				synchronized (fThreads) {
					fThreads.remove(thread);
				}
				thread.terminated();
			}
			return true;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.debug.core.IJDIEventListener#wonSuspendVote(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
		 */
		public void wonSuspendVote(Event event, JDIDebugTarget target) {
			// do nothing
		}
	
	}
	
	class ThreadStartHandler implements IJDIEventListener {
		
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
			if (manager != null) {			
				try {
					EventRequest req= manager.createThreadStartRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addJDIEventListener(this, req);
					setRequest(req);
				} catch (RuntimeException e) {
					logError(e);
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
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			ThreadReference thread= ((ThreadStartEvent)event).thread();
			try {
				if (thread.isCollected()) {
					return false;
				}
			} catch (VMDisconnectedException exception) {
				return false;
			} catch (ObjectCollectedException e) {
				return false;
			} catch (TimeoutException e) {
				// continue - attempt to create the thread
			}
			DroolsThread jdiThread= (DroolsThread) findThread(thread);
			if (jdiThread == null) {
				jdiThread = (DroolsThread) createThread(thread);
				if (jdiThread == null) {
					return false;
				}
			} else {
				jdiThread.disposeStackFrames();
				jdiThread.fireChangeEvent(DebugEvent.CONTENT);
			}
			return !jdiThread.isSuspended();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.debug.core.IJDIEventListener#wonSuspendVote(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
		 */
		public void wonSuspendVote(Event event, JDIDebugTarget target) {
			// do nothing
		}
		
		/**
		 * Deregisters this event listener.
		 */
		protected void deleteRequest() {
			if (getRequest() != null) {
				removeJDIEventListener(this, getRequest());
				setRequest(null);
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
		if (handler != null) {
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
		Iterator threads= getThreadIterator();
		while (threads.hasNext()) {
			JDIThread thread= (JDIThread)threads.next();
			if (thread.isOutOfSynch()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean mayBeOutOfSynch() {
		Iterator threads= getThreadIterator();
		while (threads.hasNext()) {
			JDIThread thread= (JDIThread)threads.next();
			if (thread.mayBeOutOfSynch()) {
				return true;
			}
		}
		return false;
	}
	
	public JDIThread findThread(ThreadReference tr) {
		Iterator iter= getThreadIterator();
		while (iter.hasNext()) {
			JDIThread thread = (JDIThread) iter.next();
			if (thread.getUnderlyingThread().equals(tr))
				return thread;
		}
		return null;
	}
	
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (!isAvailable()) {
			return;
		}		
		if (supportsBreakpoint(breakpoint)) {
			try {
				((JavaBreakpoint)breakpoint).removeFromTarget(this);
				getBreakpoints().remove(breakpoint);
				Iterator threads = getThreadIterator();
				while (threads.hasNext()) {
					((DroolsThread)threads.next()).removeCurrentBreakpoint(breakpoint);
				}
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	protected void suspendThreads() {
		Iterator threads = getThreadIterator();
		while (threads.hasNext()) {
			((DroolsThread)threads.next()).suspendedByVM();
		}
	}

	protected void resumeThreads() throws DebugException {
		Iterator threads = getThreadIterator();
		while (threads.hasNext()) {
			((DroolsThread)threads.next()).resumedByVM();
		}
	}
	
	public void disconnect() throws DebugException {

		if (!isAvailable()) {
			// already done
			return;
		}

		if (!canDisconnect()) {
			notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_disconnect); 
		}

		try {
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.dispose();
			}
		} catch (VMDisconnectedException e) {
			// if the VM disconnects while disconnecting, perform
			// normal disconnect handling
			disconnected();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIDebugTarget_exception_disconnecting, new String[] {e.toString()}), e); 
		}

	}

	public void terminate() throws DebugException {
		if (!isAvailable()) {
			return;
		}
		if (!supportsTerminate()) {
			notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_termination); 
		}
		try {
			setTerminating(true);
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.exit(1);
			}
			IProcess process= getProcess();
			if (process != null) {
				process.terminate();
			}
		} catch (VMDisconnectedException e) {
			// if the VM disconnects while exiting, perform 
			// normal termination processing
			terminated();
		} catch (TimeoutException exception) {
			// if there is a timeout see if the associated process is terminated
			IProcess process = getProcess();
			if (process != null && process.isTerminated()) {
				terminated();
			} else {
				// All we can do is disconnect
				disconnected();
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIDebugTarget_exception_terminating, new String[] {e.toString()}), e); 
		}
	}
	
	public void handleVMStart(VMStartEvent event) {
		if (isResumeOnStartup()) {
			try {
				setSuspended(true);
				resume();
			} catch (DebugException e) {
				logError(e);
			}
		}
		// If any threads have resumed since thread collection was initialized,
		// update their status (avoid concurrent modification - use #getThreads())
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			DroolsThread thread = (DroolsThread) threads[i];
			if (thread.isSuspended()) {
				try {
					boolean suspended = thread.getUnderlyingThread().isSuspended();
					if (!suspended) {
						thread.setRunning(true);
						thread.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
					}
				} catch (VMDisconnectedException e) {
				} catch (ObjectCollectedException e){
				} catch (RuntimeException e) {
					logError(e);
				}				
			}
		}
	}
	
	protected void initializeState() {

		List threads= null;
		VirtualMachine vm = getVM();
		if (vm != null) {
			try {
				threads= vm.allThreads();
			} catch (RuntimeException e) {
				internalError(e);
			}
			if (threads != null) {
				Iterator initialThreads= threads.iterator();
				while (initialThreads.hasNext()) {
					createThread((ThreadReference) initialThreads.next());
				}
			}			
		}
		
		if (isResumeOnStartup()) {
			setSuspended(false);
		}
	}
	
	public void suspend() throws DebugException {
		if (isSuspended()) {
			return;
		}
		try {
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.suspend();
			}
			suspendThreads();
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		} catch (RuntimeException e) {
			setSuspended(false);
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIDebugTarget_exception_suspend, new String[] {e.toString()}), e); 
		}
	}
	
	public void prepareToSuspendByBreakpoint(JavaBreakpoint breakpoint) {
		setSuspended(true);
		suspendThreads();
	}
	
	protected void cancelSuspendByBreakpoint(JavaBreakpoint breakpoint) throws DebugException {
		setSuspended(false);
		resumeThreads();
	}	
}
