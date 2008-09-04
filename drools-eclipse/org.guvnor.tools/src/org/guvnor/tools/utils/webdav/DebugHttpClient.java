package org.guvnor.tools.utils.webdav;

import java.io.IOException;

import org.eclipse.webdav.http.client.HttpClient;
import org.eclipse.webdav.http.client.Request;
import org.eclipse.webdav.http.client.Response;

/**
 * An intercept client that dumps debug information to System.out.
 * @author jgraham
 */
public class DebugHttpClient extends HttpClient {

	@Override
	public Response invoke(Request request) throws IOException {
		System.out.println(request.toString());
		Response response = super.invoke(request);
		System.out.println(response.toString());
		return response;
	}
}
