package org.guvnor.tools.utils.webdav;

import java.util.HashMap;

/**
 * Simple cache for WebDav connections.
 * @author jgraham
 *
 */
public class WebDavServerCache {
	
	private static HashMap<String, WebDavClient> cache;
	
	public static WebDavClient getWebDavClient(String serverUrl) {
		if (cache == null) {
			return null;
		}
		return cache.get(serverUrl);
	}
	
	public static void cacheWebDavClient(String serverUrl, WebDavClient client) {
		if (cache == null) {
			cache = new HashMap<String, WebDavClient>();
		}
		cache.put(serverUrl, client);
	}
}
