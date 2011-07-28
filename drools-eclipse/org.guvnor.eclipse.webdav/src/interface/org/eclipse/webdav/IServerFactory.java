package org.eclipse.webdav;

/**
 * Factory for constructing WebDAV servers.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see IServer
 */
public interface IServerFactory {

    /**
     * Returns a new server for the WebDAV server with
     * the given URL.
     */
    public IServer newServer(String serverURL);
}
