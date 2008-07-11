package org.guvnor.tools.utils.webdav;

import java.io.InputStream;
import java.util.Map;

import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.client.RemoteDAVClient;
import org.eclipse.webdav.http.client.IAuthenticator;

/**
 * Client methods for interacting with Guvnor through WebDav.
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
	 * Lists a directory (collection) in Guvnor through WebDav.
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
	 * Get the contents of a resource from Guvnor through WebDav.
	 * @param resource The address of the resource
	 * @return The contents of the resource
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public String getResourceContents(String resource) throws Exception;
	
	/**
	 * Get the contents for a specific version of a resource from a Guvnor repository.
	 * @param resource The address of the resource
	 * @param version The version number of the resource
	 * @return The contents of the resource
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public String getResourceVersionContents(String resource, String version) throws Exception;
	
	/**
	 * Get the <code>InputStream</code> of a resource from Guvnor through WebDav.
	 * @param resource The address of the resource
	 * @return The <code>IResponse</code> object, which the client <b>must</b> close
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public IResponse getResourceInputStream(String resource) throws Exception;
	
	/**
	 * Get the <code>InputStream</code> for a specific version of a resource from Guvnor through WebDav.
	 * @param resource The address of the resource
	 * @param version The version number of the resource
	 * @return The <code>IResponse</code> object, which the client <b>must</b> close
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public IResponse getResourceVersionInputStream(String resource, String version) throws Exception;
	
	/**
	 * Same as createResource(resource, is, true)
	 */
	public boolean createResource(String resource, InputStream is) throws Exception;

	/**
	 * Creates a file in Guvnor through WebDav.
	 * @param resource The path and name of the resource
	 * @param is A stream to the file contents
	 * @param overwrite Whether to overwrite the file if it already exists
	 * @return false if the file exists and overwrite = false
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public boolean createResource(String resource, InputStream is, boolean overwrite) throws Exception;
	
	/**
	 * Write a file to Guvnor through WebDav.
	 * @param resource The path and name of the resource
	 * @param is A stream to the file contents
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public void putResource(String resource, InputStream is) throws Exception;
	
	/**
	 * Gets all the version information for a resource
	 * @param resource The file to get version information about
	 * @return The <code>IResponse</code> object, which the client <b>must</b> close
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public IResponse getResourceVersions(String resource) throws Exception;
}
