package org.drools.eclipse.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.MoveModifications;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Participant to generate refactoring when types are moved or a package is renamed
 * 
 * @author Lucas Amador
 *
 */
@SuppressWarnings("restriction")
public class RuleIFileMoveParticipant extends MoveParticipant {

	public static final String NAME = "Rule Move Refactoring";

	private static RefactoringContent refactoringContent = new RefactoringContent();
	private DRLProjectDetector drlProjectDetector = new DRLProjectDetector();
	private Matcher matcher;

	private IFile file;
	private RefactoringProcessor processor;
	private String newName;
	private String currentName;

	private String className;
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if (file==null || file.isReadOnly())
			status.addFatalError("File don't exists or is read only");
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange changes = new CompositeChange("Reorganize DRL " + currentName + "." + className + " imports ");
		String content;
		for (IFile drlFile : drlProjectDetector.detect(file.getProject())) {

			content = refactoringContent.getIFileContent(drlFile);
			if (content==null && (content = readFile(drlFile))==null)
				continue;

			String toReplace = currentName + "." + className;
			String replaceWith = newName + "." + className;

			Pattern pattern = Pattern.compile(toReplace);
			matcher = pattern.matcher(content);

			if (matcher.find()) {
				TextFileChange change = new TextFileChange(drlFile.getName(), drlFile);
				MultiTextEdit mte = new MultiTextEdit();
				change.setEdit(mte);
				ReplaceEdit replace = new ReplaceEdit(matcher.start(), toReplace.length(), replaceWith);
				mte.addChild(replace);
				changes.add(change);
				refactoringContent.updateContent(drlFile, content.replace(toReplace, replaceWith));
			}
		}
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
					this.className = file.getName().replace("."+file.getFileExtension(), "");
					if (processor.hashCode()!=refactoringContent.getProcessorHashcode()) {
						refactoringContent.setProcessorHashcode(processor.hashCode());
						refactoringContent.clear();
					}
					if (processor instanceof JavaRenameProcessor) {
						newName = ((JavaRenameProcessor)processor).getNewElementName();
						currentName = ((JavaRenameProcessor)processor).getCurrentElementName();
						return true;
					}
					else if (processor instanceof JavaMoveProcessor) {
						try {
							MoveModifications moveModifications = getNewName();
							getCurrentName(moveModifications);
						} catch (SecurityException e) {
							return false;
						} catch (NoSuchFieldException e) {
							return false;
						} catch (IllegalArgumentException e) {
							return false;
						} catch (IllegalAccessException e) {
							return false;
						} catch (JavaModelException e) {
							return false;
						}
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

	@SuppressWarnings("unchecked")
	private MoveModifications getNewName() throws NoSuchFieldException, IllegalAccessException {
		Field fMovePolicyPrivateField = processor.getClass().getDeclaredField("fMovePolicy");
		fMovePolicyPrivateField.setAccessible(true);
		Object movePolicy = fMovePolicyPrivateField.get(processor);

		Field fModificationsPrivateField = movePolicy.getClass().getDeclaredField("fModifications");
		fModificationsPrivateField.setAccessible(true);
		MoveModifications moveModifications = (MoveModifications) fModificationsPrivateField.get(movePolicy);
		Field fMoveArgumentsPrivateField = moveModifications.getClass().getDeclaredField("fMoveArguments");
		fMoveArgumentsPrivateField.setAccessible(true);
		ArrayList<MoveArguments> moveArguments = (ArrayList<MoveArguments>) fMoveArgumentsPrivateField.get(moveModifications);
		PackageFragment packageFragment = (PackageFragment) moveArguments.get(0).getDestination();

		String[] names = packageFragment.names;
		String newPackageName = "";
		for (int i = 0; i < names.length; i++)
			newPackageName = newPackageName.concat(names[i]+".");

		newName = newPackageName.substring(0, newPackageName.length()-1);
		return moveModifications;
	}

	@SuppressWarnings("unchecked")
	private void getCurrentName(MoveModifications moveModifications) throws NoSuchFieldException, IllegalAccessException, JavaModelException {
		Field fMovesPrivateField = moveModifications.getClass().getDeclaredField("fMoves");
		fMovesPrivateField.setAccessible(true);
		ArrayList<Object> fmoves = (ArrayList<Object>) fMovesPrivateField.get(moveModifications);
		for (Object fmove : fmoves) {
			if (fmove instanceof CompilationUnit) {
				CompilationUnit cu = (CompilationUnit)fmove;
				IPackageDeclaration[] packageDeclarations = cu.getPackageDeclarations();
				for (int i = 0; i < packageDeclarations.length; i++)
					currentName = packageDeclarations[i].getElementName();
			}
		}
	}

}
