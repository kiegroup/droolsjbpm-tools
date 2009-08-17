package org.drools.eclipse.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
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
 * Participant to generate refactoring when a field is renamed.
 * This isn't executed by the RefactoringProcessor when a field is refactored with
 * the rename hotkey into the editor. Only is called when the Rename field dialog is used.
 * 
 * @author Lucas Amador
 *
 */
@SuppressWarnings("restriction")
public class RuleSourceFieldRenameParticipant extends RenameParticipant {

	public static final String NAME = "Rule Source Field Rename Refactoring";

	private static final String FIELD_NAME = "(?<=:\\s)FIELD_NAME|FIELD_NAME(?=\\s=)";
	private static final String VARIABLE_ASSIGNED = "[\\w]*(?=\\s*:\\s*TYPE\\s*\\()";
	private static final String GETTER_NAME = "(?<=VARIABLE_NAME\\.)CURRENT_GETTER_NAME(?=\\s*\\()";
	private static final String SETTER_NAME = "(?<=VARIABLE_NAME\\.)CURRENT_SETTER_NAME(?=\\s*\\()";

	private DRLProjectDetector drlProjectDetector = new DRLProjectDetector();
	private Matcher matcher;

	private RefactoringProcessor processor;
	private List<IFile> drlFiles;
	private SourceField sourceField;
	private String newName;
	private String currentName;

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			IFile file = getSourceFieldIFile();
			if (file==null || file.isReadOnly())
				return status;
			drlFiles = drlProjectDetector.detect(file.getProject());
			String content = null;
			// if at least one file have a reference to the renamed field => apply refactoring
			for (IFile drlFile : drlFiles) {

				if ((content = readFile(drlFile))==null)
					return null;

				Pattern pattern = Pattern.compile("(?<=:\\s)" + currentName + "|" + currentName + "(?=\\s=)");
				matcher = pattern.matcher(content);

				if (matcher.find()) {
					RenameFieldProcessor renameFieldProcessor = (RenameFieldProcessor)processor;
					if (!renameFieldProcessor.getRenameGetter())
						status.addInfo("The getter must be also updated to refactor the DRL files.");
					return status;
				}

			}
		} catch (CoreException e) {
			throw new OperationCanceledException(e.getMessage());
		}
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange changes = null;
		String content;
		changes = new CompositeChange("Fix " + currentName + " field on DRL files");
		IFile file = getSourceFieldIFile();
		String typeName = sourceField.getParent().getElementName();
		if (file!=null) {
			RenameFieldProcessor renameFieldProcessor = (RenameFieldProcessor)processor;
			for (IFile drlFile : drlFiles) {

				if ((content = readFile(drlFile))==null)
					return null;

				TextFileChange change = new TextFileChange(drlFile.getName(), drlFile);
				MultiTextEdit mte = new MultiTextEdit();
				change.setEdit(mte);

				// rename the field name
				Pattern pattern = Pattern.compile(FIELD_NAME.replaceAll("FIELD_NAME", currentName));
				matcher = pattern.matcher(content);
				while (matcher.find()) {
					ReplaceEdit replace = new ReplaceEdit(matcher.start(), currentName.length(), newName);
					mte.addChild(replace);
				}

				// search all the variables of the type to replace the getters/setters
				pattern = Pattern.compile(VARIABLE_ASSIGNED.replace("TYPE", typeName));
				matcher = pattern.matcher(content);
				while (matcher.find()) {
					if (matcher.group().length() > 0) {
						String variableNameAssigned = matcher.group();
						if (renameFieldProcessor.getRenameGetter()) {
							String newGetterName = renameFieldProcessor.getNewGetterName();
							String currentGetterName = renameFieldProcessor.getGetter().getElementName();
							String regexp = GETTER_NAME.replace("VARIABLE_NAME", variableNameAssigned).replace("CURRENT_GETTER_NAME", currentGetterName);
							createFieldRenameChanges(mte, content, regexp, currentGetterName, newGetterName);
						}
						if (renameFieldProcessor.getRenameSetter()) {
							String newSetterName = renameFieldProcessor.getNewSetterName();
							String currentSetterName = renameFieldProcessor.getSetter().getElementName();
							String regexp = SETTER_NAME.replace("VARIABLE_NAME", variableNameAssigned).replace("CURRENT_SETTER_NAME", currentSetterName);
							createFieldRenameChanges(mte, content, regexp, currentSetterName, newSetterName);
						}
					}
				}

				if (change.getEdit().getChildrenSize() > 0)
					changes.add(change);

			}
		}
		return (changes.getChildren().length > 0)?changes:null;
	}

	private void createFieldRenameChanges(MultiTextEdit mte, String content, String regexp, String currentName, String newName) {
		Pattern pattern = Pattern.compile(regexp);
		Matcher setterMatcher = pattern.matcher(content);
		ReplaceEdit replace = null;
		while (setterMatcher.find()) {
			replace = new ReplaceEdit(setterMatcher.start(), currentName.length(), newName);
			mte.addChild(replace);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	// TODO: Search the Native way to find the SourceField IFile
	private IFile getSourceFieldIFile() {
		Field fReferences;
		try {
			fReferences = processor.getClass().getDeclaredField("fReferences");
			fReferences.setAccessible(true);
			SearchResultGroup object[] = (SearchResultGroup[]) fReferences.get(processor);
			for (SearchResultGroup searchResultGroup : object) {
				if (searchResultGroup.getResource() instanceof IFile)
					return (IFile) searchResultGroup.getResource();
			}
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		return null;
	}

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof SourceField) {
			this.sourceField = (SourceField) element;
			this.processor = getProcessor();
			if (this.processor instanceof JavaRenameProcessor) {
				newName = ((JavaRenameProcessor)processor).getNewElementName();
				currentName = ((JavaRenameProcessor)processor).getCurrentElementName();
				return true;
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
