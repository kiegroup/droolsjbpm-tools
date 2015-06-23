/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.eclipse.webdav.client;

import java.net.MalformedURLException;
import org.eclipse.webdav.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * <code>Dav Client</code>s implement the <code>IServer</code> interface
 * and represent a client's connection to a server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public abstract class DAVClient implements IServer {

    protected WebDAVFactory davFactory = null;

    /**
     * Creates a new DAV client from a clone of the given DAV client.
     *
     * @param davClient the DAV client to clone
     */
    public DAVClient(DAVClient davClient) {
        davFactory = davClient.davFactory;
    }

    /**
     * Creates a new DAV client.
     *
     * @param davFactory
     */
    public DAVClient(WebDAVFactory davFactory) {
        Assert.isNotNull(davFactory);
        this.davFactory = davFactory;
    }

    /**
     * @see Object#clone()
     */
    protected abstract Object clone();

    public WebDAVFactory getDAVFactory() {
        return davFactory;
    }

    /**
     * Returns a new context that is based on the given context.
     *
     * @param userContext
     * @param locator
     *
     * @return a new context that is based on the given context
     */
    protected IContext newContext(IContext userContext, ILocator locator) throws MalformedURLException {
        Assert.isNotNull(userContext);
        Assert.isNotNull(locator);
        IContext context = davFactory.newContext(userContext);
        if (locator.getLabel() != null)
            context.setLabel(locator.getLabel());
        return context;
    }

    /**
     * Shut down the client for future use, and release any system resources associated with the
     * instance.  Callers should not expect the client to succeed with further API calls once the
     * client has been closed.
     */
    public void close() {
        // Default is to do nothing.
    }
}
