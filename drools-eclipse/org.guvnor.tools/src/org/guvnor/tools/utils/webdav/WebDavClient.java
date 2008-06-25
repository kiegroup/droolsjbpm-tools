package org.guvnor.tools.utils.webdav;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.webdav.IContext;
import org.eclipse.webdav.ILocator;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.client.RemoteDAVClient;
import org.eclipse.webdav.client.WebDAVFactory;
import org.eclipse.webdav.http.client.HttpClient;

/**
 * WebDav wrapper client.
 * @author jgraham
 *
 */
public class WebDavClient {
	private RemoteDAVClient client;
	
	/**
	 * Ctor for this wrapper WebDav client.
	 * @param serverUrl The WebDav repository location (server)
	 */
	public WebDavClient(URL serverUrl) {
		WebDavAuthenticator authen =  new WebDavAuthenticator(serverUrl);
		HttpClient hClient = new HttpClient();
		hClient.setAuthenticator(authen);
		client = new RemoteDAVClient(new WebDAVFactory(), hClient);
	}
	
	/**
	 * Provides access to the underlying RemoteDAVClient.
	 * @return The client associated with the current repository connection.
	 */
	public RemoteDAVClient getClient() {
		return client;
	}
	
	/**
	 * Convenience method for creating a request IContext.
	 * @return An instance of IContext
	 */
	public IContext createContext() {
		return WebDAVFactory.contextFactory.newContext();
	}
	
	/**
	 * Lists a directory (collection) in WebDav.
	 * @param path The directory (collection) to list
	 * @return An association of directory content names and their properties
	 * @throws Exception Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public Map<String, ResourceProperties> listDirectory(String path) throws Exception {
		IContext context = createContext();
		context.put("Depth", "1");
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(path);
		IResponse response = client.propfind(locator, context, null);
		if (response.getStatusCode() != IResponse.SC_MULTI_STATUS 
		   && response.getStatusCode() != IResponse.SC_MULTI_STATUS) {
			throw new Exception("WebDav error: " + response.getStatusCode());
		}
		return StreamProcessingUtils.parseListing(path, response.getInputStream());
	}
	
	/**
	 * Get the contents of a resource from a WebDav repository.
	 * @param resource The address of the resource
	 * @return The contents of the resource
	 * @throws Exception Various WebDav errors can occur (See IResponse for details)
	 */
	public String getResourceContents(String resource) throws Exception {
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
		IResponse response = client.get(locator, createContext());
		if (response.getStatusCode() != IResponse.SC_OK) {
			throw new Exception("WebDav error: " + response.getStatusCode());
		}
		return StreamProcessingUtils.getStreamContents(response.getInputStream());
	}
	
	public void putResource(String location, String name, InputStream is) throws Exception {
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(location + "/" + name);
		IResponse response = client.post(locator, createContext(), is);
		if (response.getStatusCode() != IResponse.SC_OK) {
			throw new Exception("WebDav error: " + response.getStatusCode());
		}
	}
}
