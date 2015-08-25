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
 */
@SuppressWarnings("restriction")
public class RuleSourceFieldRenameParticipant extends RenameParticipant {

    public static final String NAME = "Rule Source Field Rename Refactoring";

    private static final String FIELD_NAME = "(?<=[!\\(:\\s])FIELD_NAME(?=[\\s=\\),\\.<>!])";
    private static final String VARIABLE_ASSIGNED = "[\\w]*(?=\\s*:\\s*TYPE\\s*\\()";
    private static final String GETTER_NAME = "(?<=VARIABLE_NAME\\.)CURRENT_GETTER_NAME(?=\\s*\\()";
    private static final String SETTER_NAME = "(?<=VARIABLE_NAME\\.)CURRENT_SETTER_NAME(?=\\s*\\()";

    private DRLProjectDetector drlProjectDetector = new DRLProjectDetector();

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

                if ((content = FileUtil.readFile(drlFile))==null)
                    return null;

                Pattern pattern = Pattern.compile("(?<=:\\s)" + currentName + "|" + currentName + "(?=\\s=)");
                Matcher matcher = pattern.matcher(content);

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

        Pattern fieldPattern = Pattern.compile(FIELD_NAME.replaceAll("FIELD_NAME", currentName));
        Pattern typePattern = Pattern.compile(VARIABLE_ASSIGNED.replace("TYPE", typeName));

        if (file!=null) {
            RenameFieldProcessor renameFieldProcessor = (RenameFieldProcessor)processor;
            for (IFile drlFile : drlFiles) {

                if ((content = FileUtil.readFile(drlFile))==null)
                    return null;

                TextFileChange change = new TextFileChange(drlFile.getName(), drlFile);
                MultiTextEdit mte = new MultiTextEdit();
                change.setEdit(mte);

                // rename the field name
                Matcher matcher = fieldPattern.matcher(content);
                while (matcher.find()) {
                	if (isFieldInRightType(content, typeName, matcher.start())) {
                		ReplaceEdit replace = new ReplaceEdit(matcher.start(), currentName.length(), newName);
                		mte.addChild(replace);
                	}
                }

                // search all the variables of the type to replace the getters/setters
                matcher = typePattern.matcher(content);
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
    
    private boolean isFieldInRightType(String content, String typeName, int offset) {
    	int lastTypeOccurence = content.lastIndexOf(typeName, offset);
    	if (lastTypeOccurence < 0) {
    		return false;
    	}
    	
    	int typeConstraintsStart = content.indexOf('(', lastTypeOccurence);
    	if (typeConstraintsStart < 0 || typeConstraintsStart > offset) {
    		return false;
    	}
    	
    	String s = content.substring(lastTypeOccurence, typeConstraintsStart);
    	if (s.indexOf("global") >= 0 || s.indexOf("function") >= 0) {
    		return false;
    	}
    	
    	int parenthesisCounter = 1;
    	int typeConstraintsEnd = typeConstraintsStart+1;
    	while (parenthesisCounter != 0 && typeConstraintsEnd < content.length()) {
    		if (content.charAt(typeConstraintsEnd) == '(') {
    			parenthesisCounter++;
    		} else if (content.charAt(typeConstraintsEnd) == ')') {
    			parenthesisCounter--;
    		}
    		typeConstraintsEnd++;	
    	}
    	
    	return typeConstraintsEnd < content.length() && typeConstraintsEnd > offset;
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
}
