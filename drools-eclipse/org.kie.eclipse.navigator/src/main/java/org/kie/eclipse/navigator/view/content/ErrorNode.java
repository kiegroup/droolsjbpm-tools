/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.kie.eclipse.navigator.view.content;

import java.util.List;

import org.kie.eclipse.server.IKieResourceHandler;
import org.kie.eclipse.server.KieResourceHandler;

/**
 * Represents an error that occurred while attempting to retrieve a resource's
 * children.
 */
public class ErrorNode extends ContentNode<IContainerNode<?>> implements IErrorNode {

    public static final String ERROR_TYPE = "error"; //$NON-NLS-1$
    private final String text;
    private Exception exception;
    
    private final static class ErrorHandler extends KieResourceHandler {
    	public ErrorHandler(String text) {
    		super(null,text);
    	}

		/* (non-Javadoc)
		 * @see org.kie.eclipse.navigator.view.server.IKieResourceHandler#getChildren()
		 */
		@Override
		public List<? extends IKieResourceHandler> getChildren() throws Exception {
			return null;
		}
    }
    
    protected ErrorNode(IContainerNode<?> container, String text) {
        super(container, new ErrorHandler(text));
        this.text = text;
    }
    
    protected ErrorNode(IContainerNode<?> container, Exception exception) {
        super(container, new ErrorHandler("Error"));
        this.text = "Error";
        this.exception = exception;
    }

    public String getText() {
        return text;
    }

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContentNode#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return false;
	}
}
