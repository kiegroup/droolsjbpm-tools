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

package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides visible part of the DSL editor table.
 */
public class DSLLabelProvider extends LabelProvider
    implements
    ITableLabelProvider {

    public Image getColumnImage(Object element,
                                int columnIndex) {
        return null;
    }

    public String getColumnText(Object element,
                                int columnIndex) {
        String result = "";
        DSLMappingEntry item = (DSLMappingEntry) element;
        switch (columnIndex) {
            case 0:  
                result = item.getMappingKey();
                break;
            case 1 :
                result = item.getMappingValue();
                break;
            case 2 :
                result = item.getMetaData().getMetaData();
                break;
            case 3 :
                result = item.getSection().getSymbol();
                break;
            default :
                break;
        }
        return result;
    }

}
