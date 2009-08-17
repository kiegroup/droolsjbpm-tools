package org.drools.eclipse.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Participant to generate refactoring when a type is renamed.
 * 
 * @author Lucas Amador
 *
 */
@SuppressWarnings("restriction")
public class RuleIFileRenameParticipant extends RenameParticipant {

	public static final String NAME = "Rule File Rename Refactoring";

	private DRLProjectDetector drlProjectDetector = new DRLProjectDetector();
	private Matcher matcher;

	private RefactoringProcessor processor;
	private List<IFile> drlFiles;
	private IFile file;
	private String newName;
	private String currentName;

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if (file==null || file.isReadOnly())
			status.addFatalError("File don't exists or is read only");
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange changes = null;
		String content;
		changes = new CompositeChange("Reorganize DRL " + currentName + " Type ");
		drlFiles = drlProjectDetector.detect(file.getProject());
		for (IFile drlFile : drlFiles) {

			if ((content = readFile(drlFile))==null)
				return null;

			Pattern pattern = Pattern.compile("(?<=\\.|\\s)" + currentName + "(?=\\(|\\n|\\s)");
			matcher = pattern.matcher(content);

			TextFileChange change = new TextFileChange(drlFile.getName(), drlFile);
			MultiTextEdit mte = new MultiTextEdit();
			change.setEdit(mte);
			while (matcher.find()) {
				ReplaceEdit replace = new ReplaceEdit(matcher.start(), currentName.length(), newName);
				mte.addChild(replace);
			}
			if (change.getEdit().getChildrenSize() > 0)
				changes.add(change);
		}
		if (changes.getChildren().length==0)
			return null;
		return (changes.getChildren().length > 0)?changes:null;
	}

	@Override
	public String getName() {
		return NAME;
	}


	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile)element;
			if (file.getType()==IFile.FILE) {
				if (file.getFileExtension().equalsIgnoreCase("java")) {
					this.processor = getProcessor();
					this.file = file;
					if (this.processor instanceof JavaRenameProcessor) {
						newName = ((JavaRenameProcessor)processor).getNewElementName().replace(".java", "");
						currentName = ((JavaRenameProcessor)processor).getCurrentElementName();
						return true;
					}
				}
			}
		}
		return false;
	}

	private String readFile(IFile file) throws CoreException {
		InputStream inputStream = file.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder();
		String buffer = null;
		try {
			while ((buffer = reader.readLine()) != null)
				sb.append(buffer + "\n");
		}
		catch (IOException e) {
			return null;
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				// Nothing
			}
		}
		return sb.toString();
	}

}
