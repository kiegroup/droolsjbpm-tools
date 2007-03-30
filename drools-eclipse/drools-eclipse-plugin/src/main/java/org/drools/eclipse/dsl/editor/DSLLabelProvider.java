package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;
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
