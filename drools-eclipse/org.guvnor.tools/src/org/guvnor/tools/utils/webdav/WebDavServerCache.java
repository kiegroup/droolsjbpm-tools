package org.guvnor.tools.utils.webdav;

import java.util.HashMap;

/**
 * Simple cache for WebDav connections.
 * @author jgraham
 *
 */
public class WebDavServerCache {
	
	private static HashMap<String, IWebDavClient> cache;
	
	public static IWebDavClient getWebDavClient(String serverUrl) {
		if (cache == null) {
			return null;
		}
		return cache.get(serverUrl);
	}
	
	public static void cacheWebDavClient(String serverUrl, IWebDavClient client) {
		if (cache == null) {
			cache = new HashMap<String, IWebDavClient>();
		}
		cache.put(serverUrl, client);
	}
}
