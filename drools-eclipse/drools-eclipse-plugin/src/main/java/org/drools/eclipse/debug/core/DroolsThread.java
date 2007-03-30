package org.drools.eclipse.debug.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public class DroolsThread extends JDIThread {

	private List fStackFrames;
	private boolean fRefreshChildren = true;
	
	public DroolsThread(JDIDebugTarget target, ThreadReference thread) throws ObjectCollectedException {
		super(target, thread);
	}

	protected void initialize() throws ObjectCollectedException {
		super.initialize();
		fStackFrames = new ArrayList();
	}
	
	public synchronized List computeStackFrames() throws DebugException {
		return computeStackFrames(fRefreshChildren);
	}
	
	protected synchronized List computeStackFrames(boolean refreshChildren) throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames.clear();
			} else if (refreshChildren) {
				List frames = getUnderlyingFrames();
				int oldSize = fStackFrames.size();
				int newSize = frames.size();
				int discard = oldSize - newSize; // number of old frames to discard, if any
				for (int i = 0; i < discard; i++) {
					DroolsStackFrame invalid = (DroolsStackFrame) fStackFrames.remove(0);
					invalid.bind(null, -1);
				}
				int newFrames = newSize - oldSize; // number of frames to create, if any
				int depth = oldSize;
				for (int i = newFrames - 1; i >= 0; i--) {
					fStackFrames.add(0, new DroolsStackFrame(this, (StackFrame) frames.get(i), depth));
					depth++;
				}
				int numToRebind = Math.min(newSize, oldSize); // number of frames to attempt to rebind
				int offset = newSize - 1;
				for (depth = 0; depth < numToRebind; depth++) {
					DroolsStackFrame oldFrame = (DroolsStackFrame) fStackFrames.get(offset);
					StackFrame frame = (StackFrame) frames.get(offset);
					DroolsStackFrame newFrame = (DroolsStackFrame) oldFrame.bind(frame, depth);
					if (newFrame != oldFrame) {
						fStackFrames.set(offset, newFrame);
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
	
	private List getUnderlyingFrames() throws DebugException {
		if (!isSuspended()) {
			// Checking isSuspended here eliminates a race condition in resume
			// between the time stack frames are preserved and the time the
			// underlying thread is actually resumed.
			requestFailed(JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1, null, IJavaThread.ERR_THREAD_NOT_SUSPENDED); 
		}
		try {
			return getUnderlyingThread().frames();
		} catch (IncompatibleThreadStateException e) {
			requestFailed(JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1, e, IJavaThread.ERR_THREAD_NOT_SUSPENDED); 
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2, new String[] {e.toString()}), e); 
		} catch (InternalError e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2, new String[] {e.toString()}), e); 
		}
		// execution will not reach this line, as
		// #targetRequestFailed will thrown an exception
		return null;
	}

	protected synchronized void preserveStackFrames() {
		fRefreshChildren = true;
		Iterator frames = fStackFrames.iterator();
		while (frames.hasNext()) {
			((DroolsStackFrame) frames.next()).setUnderlyingStackFrame(null);
		}
	}

	protected synchronized void disposeStackFrames() {
		fStackFrames.clear();
		fRefreshChildren = true;
	}
	
	protected void popFrame(IStackFrame frame) throws DebugException {
		JDIDebugTarget target= (JDIDebugTarget)getDebugTarget();
		if (target.canPopFrames()) {
			// JDK 1.4 support
			try {
				// Pop the frame and all frames above it
				StackFrame jdiFrame= null;
				int desiredSize= fStackFrames.size() - fStackFrames.indexOf(frame) - 1;
				int lastSize= fStackFrames.size() + 1; // Set up to pass the first test
				int size= fStackFrames.size();
				while (size < lastSize && size > desiredSize) {
					// Keep popping frames until the stack stops getting smaller
					// or popFrame is gone.
					// see Bug 8054
					jdiFrame = ((DroolsStackFrame) frame).getUnderlyingStackFrame();
					preserveStackFrames();
					getUnderlyingThread().popFrames(jdiFrame);
					lastSize= size;
					size= computeStackFrames().size();
				}
			} catch (IncompatibleThreadStateException exception) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThread_exception_popping, new String[] {exception.toString()}),exception); 
			} catch (InvalidStackFrameException exception) {
				// InvalidStackFrameException can be thrown when all but the
				// deepest frame were popped. Fire a changed notification
				// in case this has occured.
				fireChangeEvent(DebugEvent.CONTENT);
				targetRequestFailed(exception.toString(),exception); 
			} catch (RuntimeException exception) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIThread_exception_popping, new String[] {exception.toString()}),exception); 
			}
		}
	}
	
	protected void terminated() {
		super.terminated();
	}
	
	protected void removeCurrentBreakpoint(IBreakpoint bp) {
		super.removeCurrentBreakpoint(bp);
	}
	
	protected synchronized void suspendedByVM() {
		super.suspendedByVM();
	}

	protected synchronized void resumedByVM() throws DebugException {
		super.resumedByVM();
	}
	
	protected void setRunning(boolean running) {
		super.setRunning(running);
	}
	
	protected void dropToFrame(IStackFrame frame) throws DebugException {
		super.dropToFrame(frame);
	}
	
	protected synchronized void stepToFrame(IStackFrame frame) throws DebugException {
		super.stepToFrame(frame);
	}

}
