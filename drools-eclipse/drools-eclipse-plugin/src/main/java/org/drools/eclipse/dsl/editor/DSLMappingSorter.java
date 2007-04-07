package org.drools.eclipse.dsl.editor;

import org.drools.lang.dsl.DSLMappingEntry;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/** 
 * Provides sorting for the natural language table viewer.
 * @author Michael Neale
 */
public class DSLMappingSorter extends ViewerSorter {

	public final static int OBJECT = 0;
	public final static int EXPRESSION = 1;
	public final static int MAPPING = 2;
	public final static int SCOPE = 3;

	// Criteria that the instance uses 
	private int criteria;

	/**
	 * Creates a resource sorter that will use the given sort criteria.
	 *
	 * @param criteria the sort criterion to use: one of <code>NAME</code> or 
	 *   <code>TYPE</code>
	 */
	public DSLMappingSorter(int criteria) {
		super();
		this.criteria = criteria;
	}

	/* (non-Javadoc)
	 * Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {

        DSLMappingEntry item1 = (DSLMappingEntry) o1;
        DSLMappingEntry item2 = (DSLMappingEntry) o2;

		switch (criteria) {
			case OBJECT:
				return compareObject(item1, item2);
			case EXPRESSION :
				return compareExpressions(item1, item2);
			case MAPPING :
				return compareMappings(item1, item2);
			case SCOPE :
				return compareScope(item1, item2);
			default:
				return 0;
		}
	}

	

	private int compareScope(DSLMappingEntry item1,
                             DSLMappingEntry item2) {
        return item1.getSection().compareTo( item2.getSection() );
    }

    private int compareMappings(DSLMappingEntry item1,
                                DSLMappingEntry item2) {
        return item1.getMappingValue().compareTo( item2.getMappingValue() );
    }

    private int compareExpressions(DSLMappingEntry item1,
                                   DSLMappingEntry item2) {
        return item1.getMappingKey().compareTo( item2.getMappingKey() );
    }

    private int compareObject(DSLMappingEntry item1, 
                              DSLMappingEntry item2) {
		return item1.getMetaData().compareTo(item2.getMetaData());
	}
    
    /**
	 * @return the sort criterion
	 */
	public int getCriteria() {
		return criteria;
	}
}
