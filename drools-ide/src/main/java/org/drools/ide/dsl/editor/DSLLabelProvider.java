package org.drools.ide.dsl.editor;

import org.drools.lang.dsl.template.NLMappingItem;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides visible part of the DSL editor table.
 * 
 * @author Michael Neale
 *
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
        NLMappingItem item = (NLMappingItem) element;
        switch (columnIndex) {
            case 0:  
                result = item.getNaturalTemplate();
                break;
            case 1 :
                result = item.getTargetTemplate();
                break;
            case 2 :
            	result = item.getObjectName();
            	break;
            case 3 :
                result = item.getScope();
                break;
            default :
                break;  
        }
        return result;
    }

}
