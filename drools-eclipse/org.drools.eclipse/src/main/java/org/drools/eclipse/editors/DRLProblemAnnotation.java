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

package org.drools.eclipse.editors;

import org.eclipse.jface.text.source.Annotation;

/**
 * Problem annotation for Drools.
 */
public class DRLProblemAnnotation extends Annotation {

    public static final String ERROR = "org.drools.eclipse.editors.error_annotation";

    public DRLProblemAnnotation(String text) {
        super(ERROR, false, text);
    }

}
