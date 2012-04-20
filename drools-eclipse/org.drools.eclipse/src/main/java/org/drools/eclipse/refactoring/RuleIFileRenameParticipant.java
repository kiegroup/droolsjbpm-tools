/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.refactoring;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.DroolsParserException;
import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.PatternInfo;
import org.drools.lang.descr.ImportDescr;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
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
 */
@SuppressWarnings("restriction")
public class RuleIFileRenameParticipant extends RenameParticipant {

    public static final String NAME = "Rule File Rename Refactoring";

    private DRLProjectDetector drlProjectDetector = new DRLProjectDetector();

    private RefactoringProcessor processor;
    private List<IFile> drlFiles;
    private IFile file;
    private String newName;
    private String currentName;
    private String packageName;
    private String className;
    private Pattern classPattern;
    
    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();
        if (file==null || file.isReadOnly())
            status.addFatalError("File don't exists or is read only");
        return status;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        CompositeChange changes = new CompositeChange("Reorganize DRL " + currentName + " Type ");
        drlFiles = drlProjectDetector.detect(file.getProject());
        classPattern = Pattern.compile("(?<=\\W)" + currentName + "(?=\\W)");
        for (IFile drlFile : drlFiles) {
        	TextFileChange change = createChangesForFile(drlFile);
            if ( change != null && change.getEdit().getChildrenSize() > 0 ) {
                changes.add(change);
            }
        }
        
        if (changes.getChildren().length == 0) {
        	return null;
        }
        DroolsEclipsePlugin.getDefault().setForceFullBuild();
        return changes;
    }

	private TextFileChange createChangesForFile(IFile drlFile) throws CoreException {
		DRLInfo drlInfo = null;
		try {
			drlInfo = DroolsEclipsePlugin.getDefault().parseResource( drlFile, false );
		} catch (DroolsParserException e) { }
		if ( drlInfo == null ) {
			return null;
		}

		String content = FileUtil.readFile(drlFile);
		if ( content == null ) {
		    return null;
		}
		
		TextFileChange change = new TextFileChange(drlFile.getName(), drlFile);
		MultiTextEdit mte = new MultiTextEdit();
		change.setEdit(mte);
		
		boolean isImported = false;
		for (ImportDescr importDescr : drlInfo.getPackageDescr().getImports()) {
			isImported |= importDescr.getTarget().equals(className) || importDescr.getTarget().equals(packageName + ".*");
			addReplace(mte, importDescr.getTarget(), content, importDescr.getStartCharacter(), importDescr.getEndCharacter());
		}
		if (!isImported) {
			return change;
		}

		for (DRLInfo.RuleInfo ruleInfo : drlInfo.getRuleInfos()) {
			List<PatternInfo> patternInfos = ruleInfo.getPatternInfos();
			if (patternInfos != null) {
				for (DRLInfo.PatternInfo patternInfo : patternInfos) {
					addReplace(mte, patternInfo.getPatternTypeName(), content, patternInfo.getStart(), patternInfo.getEnd());
				}
				addReplace(mte, className, content, ruleInfo.getConsequenceStart(), ruleInfo.getConsequenceEnd());
			} else {
				addReplace(mte, className, content, ruleInfo.getRuleStart(), ruleInfo.getRuleEnd());
			}
		}

		for (DRLInfo.FunctionInfo functionInfo : drlInfo.getFunctionInfos()) {
			addReplace(mte, className, content, functionInfo.getFunctionStart(), functionInfo.getFunctionEnd());
		}
		
		return change;
	}
	
	private void addReplace(MultiTextEdit mte, String descrClassName, String content, int start, int end) {
		if (className.equals(descrClassName)) {
			String text = content.substring(start-1, end+1);
			Matcher matcher = classPattern.matcher(text);
			while (matcher.find()) {
				mte.addChild(new ReplaceEdit(matcher.start() + start - 1, currentName.length(), newName));
			}
		}
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
                if (file.getFileExtension() != null && file.getFileExtension().equalsIgnoreCase("java")) {
                    this.processor = getProcessor();
                    this.file = file;
                    if (this.processor instanceof JavaRenameProcessor) {
                    	JavaRenameProcessor javaProcessor = (JavaRenameProcessor)processor;
                    	newName = javaProcessor.getNewElementName().replace(".java", "");
                        currentName = javaProcessor.getCurrentElementName();

                    	try {
                        	ICompilationUnit compilationUnit = (ICompilationUnit)javaProcessor.getElements()[0];
							packageName = compilationUnit.getPackageDeclarations()[0].getElementName();
							className = packageName + "." + currentName;
						} catch (Exception e) {
							return false;
						}
                        
                    	return true;
                    }
                }
            }
        }
        return false;
    }
}
