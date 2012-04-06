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
        Pattern pattern = Pattern.compile("(?<=[\\(\\.\\s])" + currentName + "(?=[\\(\\r\\n\\s])");
        for (IFile drlFile : drlFiles) {

            if ((content = FileUtil.readFile(drlFile))==null)
                return null;

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
                if (file.getFileExtension() != null && file.getFileExtension().equalsIgnoreCase("java")) {
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
}
