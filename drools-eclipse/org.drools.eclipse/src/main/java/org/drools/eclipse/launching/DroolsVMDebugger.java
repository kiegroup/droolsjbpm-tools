package org.drools.eclipse.launching;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.base.mvel.MVELDebugHandler;
import org.drools.eclipse.debug.core.DroolsDebugModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.LibraryInfo;
import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

public class DroolsVMDebugger extends StandardVMDebugger {
	
	class ConnectRunnable implements Runnable {
		
		private VirtualMachine fVirtualMachine = null;
		private ListeningConnector fConnector = null;
		private Map fConnectionMap = null;
		private Exception fException = null;
		
		public ConnectRunnable(ListeningConnector connector, Map map) {
			fConnector = connector;
			fConnectionMap = map;
		}
		
		public void run() {
			try {
				fVirtualMachine = fConnector.accept(fConnectionMap);
			} catch (IOException e) {
				fException = e;
			} catch (IllegalConnectorArgumentsException e) {
				fException = e;
			}
		}
		
		public VirtualMachine getVirtualMachine() {
			return fVirtualMachine;
		}
		
		public Exception getException() {
			return fException;
		}
	}

	public DroolsVMDebugger(IVMInstall vmInstance) {
		super(vmInstance);
	}

	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.StandardVMDebugger_Launching_VM____1, 4); 
		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Finding_free_socket____2); 

		int port= SocketUtil.findFreePort();
		if (port == -1) {
			abort(LaunchingMessages.StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1, null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE); 
		}
		
		subMonitor.worked(1);
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}		
		
		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Constructing_command_line____3); 
				
		String program= constructProgramString(config);

		List arguments= new ArrayList(12);

		arguments.add(program);

		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first options
		String[] allVMArgs = combineVmArgs(config, fVMInstance);
		addArguments(allVMArgs, arguments);
        arguments.add("-D"+MVELDebugHandler.DEBUG_LAUNCH_KEY+"=true");
        
		addBootClassPathArguments(arguments, config);
		
		String[] cp= config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		double version = getJavaVersion();
		if (version < 1.5) {
			arguments.add("-Xdebug"); //$NON-NLS-1$
			arguments.add("-Xnoagent"); //$NON-NLS-1$
		}
		
		//check if java 1.4 or greater
		if (version < 1.4) {
			arguments.add("-Djava.compiler=NONE"); //$NON-NLS-1$
		}
		if (version < 1.5) { 
			arguments.add("-Xrunjdwp:transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$
		} else {
			arguments.add("-agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:" + port); //$NON-NLS-1$
		}

		arguments.add(config.getClassToLaunch());
		addArguments(config.getProgramArguments(), arguments);
        
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);
		
		String[] envp= config.getEnvironment();
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}		
		
		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Starting_virtual_machine____4); 

		ListeningConnector connector= getConnector();
		if (connector == null) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_find_an_appropriate_debug_connector_2, null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); 
		}
		Map map= connector.defaultArguments();
		
		specifyArguments(map, port);
		Process p= null;
		try {
			try {
				// check for cancellation
				if (monitor.isCanceled()) {
					return;
				}				
				
				connector.startListening(map);
				
				File workingDir = getWorkingDir(config);
				p = exec(cmdLine, workingDir, envp);				
				if (p == null) {
					return;
				}
				
				// check for cancellation
				if (monitor.isCanceled()) {
					p.destroy();
					return;
				}				
				
				IProcess process= newProcess(launch, p, renderProcessLabel(cmdLine), getDefaultProcessMap());
				process.setAttribute(IProcess.ATTR_CMDLINE, StandardVMRunner.renderCommandLine(cmdLine));
				subMonitor.worked(1);
				subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Establishing_debug_connection____5); 
				boolean retry= false;
				do  {
					try {
						
						ConnectRunnable runnable = new ConnectRunnable(connector, map);
						Thread connectThread = new Thread(runnable, "Listening Connector"); //$NON-NLS-1$
                        connectThread.setDaemon(true);
						connectThread.start();
						while (connectThread.isAlive()) {
							if (monitor.isCanceled()) {
								connector.stopListening(map);
								p.destroy();
								return;
							}
							try {
								p.exitValue();
								// process has terminated - stop waiting for a connection
								try {
									connector.stopListening(map); 
								} catch (IOException e) {
									// expected
								}
								checkErrorMessage(process);
							} catch (IllegalThreadStateException e) {
								// expected while process is alive
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
							}
						}

						Exception ex = runnable.getException();
						if (ex instanceof IllegalConnectorArgumentsException)						 {
							throw (IllegalConnectorArgumentsException)ex;
						}
						if (ex instanceof InterruptedIOException) {
							throw (InterruptedIOException)ex;
						}
						if (ex instanceof IOException) {
							throw (IOException)ex;
						}
						
						VirtualMachine vm= runnable.getVirtualMachine();
						if (vm != null) {
							DroolsDebugModel.newDebugTarget(launch, vm, renderDebugTarget(config.getClassToLaunch(), port), process, true, false, config.isResumeOnStartup());
							subMonitor.worked(1);
							subMonitor.done();
						}
						return;
					} catch (InterruptedIOException e) {
						checkErrorMessage(process);
						
						// timeout, consult status handler if there is one
						IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_CONNECT_TIMEOUT, "", e); //$NON-NLS-1$
						IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
						
						retry= false;
						if (handler == null) {
							// if there is no handler, throw the exception
							throw new CoreException(status);
						} 
						Object result = handler.handleStatus(status, this);
						if (result instanceof Boolean) {
							retry = ((Boolean)result).booleanValue();
						}
					}
				} while (retry);
			} finally {
				connector.stopListening(map);
			}
		} catch (IOException e) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_4, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);  
		} catch (IllegalConnectorArgumentsException e) {
			abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_5, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);  
		}
		if (p != null) {
			p.destroy();
		}
	}
	
	private double getJavaVersion() {
		LibraryInfo libInfo = LaunchingPlugin.getLibraryInfo(fVMInstance.getInstallLocation().getAbsolutePath());
		if (libInfo == null) {
		    return 0D;
		}
		String version = libInfo.getVersion();
		int index = version.indexOf("."); //$NON-NLS-1$
		int nextIndex = version.indexOf(".", index+1); //$NON-NLS-1$
		try {
			if (index > 0 && nextIndex>index) {
				return Double.parseDouble(version.substring(0,nextIndex));
			} 
			return Double.parseDouble(version);
		} catch (NumberFormatException e) {
			return 0D;
		}

	}

	protected void checkErrorMessage(IProcess process) throws CoreException {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		if (streamsProxy != null) {
			String errorMessage= streamsProxy.getErrorStreamMonitor().getContents();
			if (errorMessage.length() == 0) {
				errorMessage= streamsProxy.getOutputStreamMonitor().getContents();
			}
			if (errorMessage.length() != 0) {
				abort(errorMessage, null, IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
			}
		}										
	}
		
	protected void specifyArguments(Map map, int portNumber) {
		// XXX: Revisit - allows us to put a quote (") around the classpath
		Connector.IntegerArgument port= (Connector.IntegerArgument) map.get("port"); //$NON-NLS-1$
		port.setValue(portNumber);
		
		Connector.IntegerArgument timeoutArg= (Connector.IntegerArgument) map.get("timeout"); //$NON-NLS-1$
		if (timeoutArg != null) {
			int timeout = JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT);
			timeoutArg.setValue(timeout);
		}
	}

	protected ListeningConnector getConnector() {
		List connectors= Bootstrap.virtualMachineManager().listeningConnectors();
		for (int i= 0; i < connectors.size(); i++) {
			ListeningConnector c= (ListeningConnector) connectors.get(i);
			if ("com.sun.jdi.SocketListen".equals(c.name())) //$NON-NLS-1$
				return c;
		}
		return null;
	}
	
}
