package org.drools.eclipse.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.processes.RuleFlowMigrator;
import org.jbpm.ruleflow.core.RuleFlowProcess;

public class ConvertToBPMN2ProcessAction implements IObjectActionDelegate {

    private IFile file;
    private IWorkbenchPart targetPart;
    
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		if (file != null && file.exists()) {
			try {
				convertToBPMN2();
			} catch (Throwable t) {
				DroolsEclipsePlugin.log(t);
			}
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			if (structured.size() == 1) {
				Object element = structured.getFirstElement();
				if (element instanceof IFile) {
					file = (IFile) element;
				}
			}
		}
	}
	
	public void convertToBPMN2() {
		try {
			InputStreamReader isr = new InputStreamReader(((File) file).getContents());
			PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
			XmlProcessReader xmlReader = new XmlProcessReader( configuration.getSemanticModules() );
			String xml = RuleFlowMigrator.convertReaderToString(isr);
			Reader reader = new StringReader(xml);
			RuleFlowProcess process = (RuleFlowProcess) xmlReader.read(reader);
			final String output = XmlBPMNProcessDumper.INSTANCE.dump(process, true);
			
			ConvertToBPMN2ProcessDialog dialog = new ConvertToBPMN2ProcessDialog(targetPart.getSite().getShell());
			dialog.setOriginalFile(file);
			dialog.open();
			IPath path = dialog.getResult();
	
			if (path == null) {
				return;
			}
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IFile newFile = workspace.getRoot().getFile(path);
			
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				public void execute(final IProgressMonitor monitor)
						throws CoreException {
					try {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						newFile.create(new ByteArrayInputStream(output.getBytes()), true, monitor);
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			
			try {
				new ProgressMonitorDialog(targetPart.getSite().getShell()).run(false, true, op);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
}
