package org.guvnor.tools.utils.webdav;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.webdav.http.client.IAuthenticator;

public class WebDavAuthenticator implements IAuthenticator {
	
	private class ServerAuthenInfo {
		private String serverUrl;
		private HashMap<String, Map<String, String>> authInfo;
		
		public ServerAuthenInfo(String serverUrl) {
			this.serverUrl = serverUrl;
			authInfo = new HashMap<String, Map<String, String>>();
		}
		public Map<String, String> getAuthenicationInfo(String scheme) {
			return authInfo.get(scheme);
		}
		public void addAuthenicationInfo(String scheme, Map<String, String> info) {
			authInfo.put(scheme, info);
		}
		public String getServerURL() {
			return serverUrl;
		}
	}
	
	private HashMap<String, ServerAuthenInfo> serverAuthMaps;

	public WebDavAuthenticator() {
		serverAuthMaps = new HashMap<String, ServerAuthenInfo>();
	}
	
	public WebDavAuthenticator(URL serverUrl, 
							  String username, 
							  String password, 
							  String scheme) {
		this();
		HashMap<String, String> info = new HashMap<String, String>();
		info.put("username", username);
		info.put("password", password);
		addAuthenticationInfo(serverUrl, null, scheme, info);
	}
	
	@SuppressWarnings("unchecked")
	public void addAuthenticationInfo(URL serverUrl, 
			                         String realm,
			                         String scheme, 
			                         Map info) {
		ServerAuthenInfo authInfo = serverAuthMaps.get(serverUrl);
		if (authInfo == null) {
			authInfo = new ServerAuthenInfo(serverUrl.getHost());
			serverAuthMaps.put(serverUrl.getHost().toLowerCase(), authInfo);
		}
		authInfo.addAuthenicationInfo(scheme.toLowerCase(), info);
	}

	public void addProtectionSpace(URL resourceUrl, 
			                      String realm) {
		// Not using the notion of "realm," so do nothing
	}

	@SuppressWarnings("unchecked")
	public Map getAuthenticationInfo(URL serverUrl, 
			                        String realm, 
			                        String scheme) {
		ServerAuthenInfo authInfo = serverAuthMaps.get(serverUrl.getHost().toLowerCase());
		if (authInfo == null) {
			return null;
		}
		return authInfo.getAuthenicationInfo(scheme.toLowerCase());
	}

	public String getProtectionSpace(URL resourceUrl) {
		// We don't have the notion of "realm," but the client
		// requires a non-null return value.
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public Map requestAuthenticationInfo(URL resourceUrl, 
			                            String realm,
			                            String scheme) {
		// We do not distinguish between resource- and server-based
		// authentication: all authentication is done on a per-server
		// basis. Therefore, delegate to a server authentication check.
		return getAuthenticationInfo(resourceUrl, realm, scheme);
	}
}
