package org.guvnor.tools.utils.webdav;

import java.net.URL;

/**
 * Encapsulating the creation of IWebDavClient so it can be swapped out for other
 * WebDav libraries if necessary.
 * @author jgraham
 */
public class WebDavClientFactory {
	public static IWebDavClient createClient(URL serverUrl) {
		return new WebDavClient(serverUrl);
	}
}
