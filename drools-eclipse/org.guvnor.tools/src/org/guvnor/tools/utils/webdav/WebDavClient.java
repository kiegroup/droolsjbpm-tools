package org.guvnor.tools.utils.webdav;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.webdav.IContext;
import org.eclipse.webdav.ILocator;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.client.RemoteDAVClient;
import org.eclipse.webdav.client.WebDAVFactory;
import org.eclipse.webdav.http.client.HttpClient;
import org.eclipse.webdav.http.client.IAuthenticator;

/**
 * WebDav wrapper client.
 * @author jgraham
 *
 */
public class WebDavClient implements IWebDavClient {
	
	private RemoteDAVClient client;
	
	private WebDavAuthenticator platformAuthenticator;
	private HttpClient httpClient;
	
	private IResponse response;
	
	/**
	 * Ctor for this wrapper WebDav client.
	 * @param serverUrl The WebDav repository location (server)
	 */
	/** package */ WebDavClient(URL serverUrl) {
		platformAuthenticator =  new WebDavAuthenticator(serverUrl);
		httpClient = new HttpClient();
		httpClient.setAuthenticator(platformAuthenticator);
		client = new RemoteDAVClient(new WebDAVFactory(), httpClient);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#setSessionAuthenticator(org.eclipse.webdav.http.client.IAuthenticator)
	 */
	public void setSessionAuthenticator(IAuthenticator sessionAuthen) {
		if (sessionAuthen != null) {
			httpClient.setAuthenticator(sessionAuthen);
		} else {
			httpClient.setAuthenticator(platformAuthenticator);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getClient()
	 */
	public RemoteDAVClient getClient() {
		return client;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#createContext()
	 */
	public IContext createContext() {
		IContext context = WebDAVFactory.contextFactory.newContext();
		// Need to make sure the USER-AGENT header is present for Guvnor
		context.put("USER-AGENT", "guvnor");
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#listDirectory(java.lang.String)
	 */
	public Map<String, ResourceProperties> listDirectory(String path) throws Exception {
		IContext context = createContext();
		context.put("Depth", "1");
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(path);
		response = client.propfind(locator, context, null);
		if (response.getStatusCode() != IResponse.SC_MULTI_STATUS) {
			throw new WebDavException("WebDav error: " + response.getStatusCode(), 
								     response.getStatusCode());
		}
		Map<String, ResourceProperties> res = StreamProcessingUtils.
												parseListing(path, response.getInputStream());
		addGuvnorResourceProperties(res, path);
		return res;
	}
	
	private void addGuvnorResourceProperties(Map<String, ResourceProperties> props, 
			                                String path) throws Exception {
		try {
			String apiVer = changeToAPICall(path);
			Properties guvProps = new Properties();
			guvProps.load(getResourceInputStream(apiVer));
			for (Iterator<String> it = props.keySet().iterator(); it.hasNext();) {
				String oneKey = it.next();
				String val = guvProps.getProperty(oneKey);
				if (val != null) {
					ResourceProperties resProps = props.get(oneKey);
					StringTokenizer tokens = new StringTokenizer(val, ",");
					String dateStamp = tokens.nextToken();
					String revision = tokens.nextToken();
					resProps.setLastModifiedDate(dateStamp);
					resProps.setRevision(revision);
				}
			}
		} catch (Exception e) {
//TODO: Getting some server internal errors here. Why?
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#queryProperties(java.lang.String)
	 */
	public ResourceProperties queryProperties(String resource) throws Exception {
		IContext context = createContext();
		context.put("Depth", "1");
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
		response = client.propfind(locator, context, null);
		if (response.getStatusCode() != IResponse.SC_MULTI_STATUS
		   && response.getStatusCode() != IResponse.SC_OK) {
			throw new WebDavException("WebDav error: " + response.getStatusCode(), 
								     response.getStatusCode());
		}
		Map<String, ResourceProperties> props = 
			StreamProcessingUtils.parseListing("", response.getInputStream());
		if (props.keySet().size() != 1) {
			throw new Exception(props.keySet().size() + " entries found for " + resource);
		}
		String filename = props.keySet().iterator().next();
		ResourceProperties res = props.get(filename);
		addGuvnorResourceProperties(res, filename, resource);
		return res;
	}
	
	private void addGuvnorResourceProperties(ResourceProperties props, 
			                                String filename, String resource) throws Exception {
		if (props == null) {
			return;
		}
		try {
			String path = resource.substring(0, resource.lastIndexOf('/'));
			String apiVer = changeToAPICall(path);
			Properties guvProps = new Properties();
			guvProps.load(getResourceInputStream(apiVer));
			String val = guvProps.getProperty(filename);
			if (val != null) {
				StringTokenizer tokens = new StringTokenizer(val, ",");
				String dateStamp = tokens.nextToken();
				String revision = tokens.nextToken();
				props.setLastModifiedDate(dateStamp);
				props.setRevision(revision);
			}
		} catch (Exception e) {
//TODO: Getting some server internal errors here. Why?
			System.out.println(e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getResourceContents(java.lang.String)
	 */
	public String getResourceContents(String resource) throws Exception {
		return StreamProcessingUtils.getStreamContents(getResourceInputStream(resource));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getResourceVersionContents(java.lang.String, java.lang.String)
	 */
	public String getResourceVersionContents(String resource, String version) throws Exception {
		String apiVer = changeToAPICall(resource) + "?version=" + version;
		return getResourceContents(apiVer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getInputStream(java.lang.String)
	 */
	public InputStream getResourceInputStream(String resource) throws Exception {
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
		IResponse response = client.get(locator, createContext());
		if (response.getStatusCode() != IResponse.SC_OK) {
			throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									 response.getStatusCode());
		}
		return response.getInputStream();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getResourceVersionInputStream(java.lang.String, java.lang.String)
	 */
	public InputStream getResourceVersionInputStream(String resource, String version) throws Exception {
		String apiVer = changeToAPICall(resource) + "?version=" + version;
		return getResourceInputStream(apiVer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#createResource(java.lang.String, java.io.InputStream)
	 */
	public boolean createResource(String resource, InputStream is) throws Exception {
		return createResource(resource, is, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#createResource(java.lang.String, java.io.InputStream, boolean)
	 */
	public boolean createResource(String resource, InputStream is, boolean overwrite) throws Exception {
		boolean res = true;
		if (!overwrite) {
			try {
				if (queryProperties(resource) != null) {
					res = false;
				}
			} catch (Exception e) {
				if (response.getStatusCode() != IResponse.SC_NOT_FOUND) {
					closeResponse();
					throw e;
				}
			}
			closeResponse();
		}
		if (res) {
			ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
			IResponse response = client.put(locator, createContext(), is);
			if (response.getStatusCode() != IResponse.SC_OK
		       && response.getStatusCode() != IResponse.SC_CREATED) {
				throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									     response.getStatusCode());
			}
		}
		return res;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#putResource(java.lang.String, java.io.InputStream)
	 */
	public void putResource(String resource, InputStream is) throws Exception {
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
		IResponse response = client.put(locator, createContext(), is);
		if (response.getStatusCode() != IResponse.SC_OK
		   && response.getStatusCode() != IResponse.SC_NO_CONTENT) {
			throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									 response.getStatusCode());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getResourceVersions(java.lang.String)
	 */
	public InputStream getResourceVersions(String resource) throws Exception {
		String apiVer = changeToAPICall(resource) + "?version=all";
		return getResourceInputStream(apiVer);
	}
	
	private String changeToAPICall(String path) {
		return path.replaceFirst("/webdav/", "/api/");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#closeResponse()
	 */
	public void closeResponse() throws Exception {
		if (response != null) {
			response.close();
			response = null;
		}
	}
}