package org.guvnor.tools.utils.webdav;

import java.io.InputStream;
import java.util.Map;

import org.eclipse.webdav.IContext;
import org.eclipse.webdav.client.RemoteDAVClient;
import org.eclipse.webdav.http.client.IAuthenticator;

/**
 * Client methods for interacting with WebDav.
 * @author jgraham
 */
public interface IWebDavClient {
	/**
	 * Tell the client to use the supplied authenticator, instead
	 * of one tied to the platform key ring. If the authenticator is
	 * null, the client defaults back to the platform key ring authenticator.
	 * @param sessionAuthen The authenticator
	 */
	public void setSessionAuthenticator(IAuthenticator sessionAuthen);
	
	/**
	 * Provides access to the underlying RemoteDAVClient.
	 * @return The client associated with the current repository connection.
	 */
	public RemoteDAVClient getClient();
	
	/**
	 * Convenience method for creating a request IContext.
	 * @return An instance of IContext
	 */
	public IContext createContext();
	
	/**
	 * Lists a directory (collection) in WebDav.
	 * @param path The directory (collection) to list
	 * @return An association of directory content names and their properties
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public Map<String, ResourceProperties> listDirectory(String path) throws Exception;
	
	/**
	 * Queries the server for properties of a given resource.
	 * @param resource The resource to get properties for
	 * @return The resource properties
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public ResourceProperties queryProperties(String resource) throws Exception;
	
	/**
	 * Get the contents of a resource from a WebDav repository.
	 * @param resource The address of the resource
	 * @return The contents of the resource
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public String getResourceContents(String resource) throws Exception;
	
	/**
	 * Get the <code>InputStream</code> of a resource from a WebDav repository.
	 * @param resource The address of the resource
	 * @return An code>InputStream</code> for the resource
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public InputStream getResourceInputStream(String resource) throws Exception;
	
	/**
	 * Same as createResource(resource, is, true)
	 */
	public boolean createResource(String resource, InputStream is) throws Exception;

	/**
	 * Creates a file in the WebDav repository
	 * @param resource The path and name of the resource
	 * @param is A stream to the file contents
	 * @param overwrite Whether to overwrite the file if it already exists
	 * @return false if the file exists and overwrite = false
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public boolean createResource(String resource, InputStream is, boolean overwrite) throws Exception;
	
	/**
	 * Write a file to the WebDav repository
	 * @param resource The path and name of the resource
	 * @param is A stream to the file contents
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public void putResource(String resource, InputStream is) throws Exception;
	
	/**
	 * Gets all the version information for a resource
	 * @param resource The file to get version information about
	 * @return A stream to the version properties
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public InputStream getResourceVersions(String resource) throws Exception;
	
	/**
	 * <b>Must</b> be called after server operation methods to ensure
	 * resource clean up.
	 */
	public void closeResponse() throws Exception;
}
