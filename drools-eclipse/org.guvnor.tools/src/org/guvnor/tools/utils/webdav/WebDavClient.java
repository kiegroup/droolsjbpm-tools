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
import org.eclipse.webdav.http.client.IAuthenticator;

/**
 * WebDav wrapper client.
 * @author jgraham
 *
 */
public class WebDavClient implements IWebDavClient {
	private RemoteDAVClient client;
	
	private WebDavAuthenticator authen;
	private IAuthenticator sessionAuthen;
	private HttpClient hClient;
	private boolean usingSessionAuthen;
	
	/**
	 * Ctor for this wrapper WebDav client.
	 * @param serverUrl The WebDav repository location (server)
	 */
	/** package */ WebDavClient(URL serverUrl) {
		authen =  new WebDavAuthenticator(serverUrl);
		hClient = new HttpClient();
		hClient.setAuthenticator(authen);
		client = new RemoteDAVClient(new WebDAVFactory(), hClient);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#setSessionAuthenticator(org.eclipse.webdav.http.client.IAuthenticator)
	 */
	public void setSessionAuthenticator(IAuthenticator sessionAuthen) {
		this.sessionAuthen = sessionAuthen;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#isUsingSessionAuthenication()
	 */
	public boolean isUsingSessionAuthenication() {
		return usingSessionAuthen;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#setSessionAuthentication(boolean)
	 */
	public boolean setSessionAuthentication(boolean useSession) {
		// We can't use session authenticator, if a session authenticator isn't present
		if (useSession && sessionAuthen == null) {
			usingSessionAuthen = false;
			return false;
		} else {
			usingSessionAuthen = useSession;
			return true;
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
		return WebDAVFactory.contextFactory.newContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#listDirectory(java.lang.String)
	 */
	public Map<String, ResourceProperties> listDirectory(String path) throws Exception {
		if (isUsingSessionAuthenication()) {
			if (sessionAuthen != null) {
				hClient.setAuthenticator(sessionAuthen);
			} else {
				setSessionAuthentication(false);
			}
		}
		try {
			IContext context = createContext();
			context.put("Depth", "1");
			ILocator locator = WebDAVFactory.locatorFactory.newLocator(path);
			IResponse response = client.propfind(locator, context, null);
			if (response.getStatusCode() != IResponse.SC_MULTI_STATUS 
			   && response.getStatusCode() != IResponse.SC_MULTI_STATUS) {
				throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									     response.getStatusCode());
			}
			return StreamProcessingUtils.parseListing(path, response.getInputStream());
		} finally {
			if (isUsingSessionAuthenication()) {
				hClient.setAuthenticator(authen);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#queryProperties(java.lang.String)
	 */
	public ResourceProperties queryProperties(String resource) throws Exception {
		if (isUsingSessionAuthenication()) {
			if (sessionAuthen != null) {
				hClient.setAuthenticator(sessionAuthen);
			} else {
				setSessionAuthentication(false);
			}
		}
		try {
			IContext context = createContext();
			context.put("Depth", "1");
			ILocator locator = WebDAVFactory.locatorFactory.newLocator(resource);
			IResponse response = client.propfind(locator, context, null);
			if (response.getStatusCode() != IResponse.SC_MULTI_STATUS 
			   && response.getStatusCode() != IResponse.SC_MULTI_STATUS) {
				throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									     response.getStatusCode());
			}
			Map<String, ResourceProperties> props = 
				StreamProcessingUtils.parseListing("", response.getInputStream());
			if (props.keySet().size() != 1) {
				throw new Exception(props.keySet().size() + " entries found for " + resource);
			}
			return props.get(props.keySet().iterator().next());
		} finally {
			if (isUsingSessionAuthenication()) {
				hClient.setAuthenticator(authen);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getResourceContents(java.lang.String)
	 */
	public String getResourceContents(String resource) throws Exception {
		return StreamProcessingUtils.getStreamContents(getInputStream(resource));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String resource) throws Exception {
		if (isUsingSessionAuthenication()) {
			if (sessionAuthen != null) {
				hClient.setAuthenticator(sessionAuthen);
			} else {
				setSessionAuthentication(false);
			}
		}
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
	 * @see org.guvnor.tools.utils.webdav.IWebDavClient#putResource(java.lang.String, java.lang.String, java.io.InputStream)
	 */
	public void putResource(String location, String name, InputStream is) throws Exception {
		ILocator locator = WebDAVFactory.locatorFactory.newLocator(location + "/" + name);
		IResponse response = client.post(locator, createContext(), is);
		if (response.getStatusCode() != IResponse.SC_OK) {
			throw new WebDavException("WebDav error: " + response.getStatusCode(), 
									 response.getStatusCode());
		}
	}
}