/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.webdav.http.client;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.client.WebDAVFactory;
import org.eclipse.webdav.internal.authentication.AuthorizationAuthority;
import org.eclipse.webdav.internal.kernel.Context;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * <p>An HTTP 1.0 or 1.1 client.  Single instances of this client enable
 * users to talk with multiple origin servers.  Moreover, connections
 * to origin servers are maintained for reuse.
 *
 * <p>Conveniences are provided for managing proxy servers, handling
 * authentication, and setting up request headers.
 *
 * <p>Here is some sample code:
 *
 * <code>
 * HttpClient client = new HttpClient();
 * try {
 * 	Request request = null;
 * 	Response response = null;
 * 	try {
 * 		URL resourceUrl = new URL("http://hostname/index.html");
 * 		request = new Request("GET", resourceUrl, (Context) null);
 * 		response = client.invoke(request);
 * 		System.out.print(response);
 * 		InputStream is = response.getInputStream();
 * 		int c;
 * 		while ((c = is.read()) != -1) {
 * 			System.out.print((char) c);
 * 		}
 * 	} catch (IOException e) {
 * 		e.printStackTrace();
 * 	} finally {
 * 		if (request != null) {
 * 			try {
 * 				request.close();
 * 			} catch (IOException e) {
 * 				e.printStackTrace();
 * 			}
 * 		}
 * 		if (response != null) {
 * 			try {
 * 				response.close();
 * 			} catch (IOException e) {
 * 				e.printStackTrace();
 * 			}
 * 		}
 * 	}
 * } finally {
 * 	client.close();
 * }
 * </code>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class HttpClient implements IStatusCodes {
	/**
	 * The HTTP version of this client.
	 */
	private double httpVersion = 1.1;

	/**
	 * Indicates whether this client is closed.
	 */
	private boolean closed = true;

	/**
	 * The connections recycler is responsible for closing and discarding
	 * unused connections that have become stale.
	 */
	private ConnectionsRecycler connectionsRecycler = new ConnectionsRecycler("Connections Recycler"); //$NON-NLS-1$

	/**
	 * The authorization authority authorizes requests.
	 */
	private AuthorizationAuthority authority = null;

	/**
	 * A <code>Hashtable</code> mapping origin server <code>URL</code>s
	 * to <code>Context</code>s. The contexts in this table are preferred
	 * over the default context.
	 */
	private Hashtable contexts = new Hashtable(5);

	/**
	 * The default context is used when one hasn't been set for a
	 * particular origin server.
	 */
	private IContext defaultContext = null;

	/**
	 * The <code>URL</code> of the default proxy server. The default is
	 * not used if the particular origin server matches a proxy server
	 * exception, or if there is already a proxy server associated with
	 * the origin server.
	 */
	private URL defaultProxyServerUrl = null;

	/**
	 * A <code>Hashtable</code> mapping origin server <code>URL</code>s
	 * to proxy server <code>URL</code>s. If an origin server
	 * <code>URL</code> has an entry in this table, the proxy server
	 * with the mapped <code>URL</code> is used.
	 */
	private Hashtable proxyServerUrls = new Hashtable(5);

	/**
	 * A set of proxy server exception patterns used to bypass the
	 * default proxy server.  Any origin server whos host and port
	 * match the pattern do not use the default proxy server.
	 */
	private Hashtable proxyServerExceptions = new Hashtable(5);

	/**
	 * The maximum number of times a request is retried after an
	 * <code>IOException</code> occurs.
	 */
	private int maxRetries = 1;

	/**
	 * The maximum number of <code>URL</code> location redirects.
	 */
	private int maxRedirects = 4;

	/**
	 * The socket timeout (in milliseconds).
	 */
	private int socketTimeout = 0;

	/**
	 * The factory used to create <code>Socket</code>s.
	 */
	private ISocketFactory socketFactory = null;

	/**
	 * The factory used to create <code>Context</code>s.
	 */
	private WebDAVFactory webDAVFactory = new WebDAVFactory();

	/**
	 * The <code>ConnectionsRecycler</code> manages a collection of
	 * persistent <code>HttpConnection</code>s. Connections that remain
	 * unused for a given period of time are closed and discarded.
	 */
	public class ConnectionsRecycler extends Thread {
		/**
		 * The time (in milliseconds) that a connection remains unused
		 * before it is closed and discared.
		 */
		private long connectionTimeout = 10000;

		/**
		 * A <code>Hashtable</code> who's keys are origin server
		 * <code>URL</code>s that map to vectors of connections that are
		 * currently unused.
		 */
		private Hashtable unusedConnections = new Hashtable(5);

		/**
		 * A <code>Hashtable</code> who's keys are origin server
		 * <code>URL</code>s that map to vectors of connections that are
		 * currently in use.
		 */
		private Hashtable usedConnections = new Hashtable(5);

		/**
		 * Creates a new <code>ConnectionsRecycler</code> with the given
		 * name.
		 *
		 * @param name the name of the new connections recycler
		 */
		public ConnectionsRecycler(String name) {
			super(name);
			this.setDaemon(true);
		}

		/**
		 * Closes this connections recycler. All of its connections are
		 * closed and discarded.
		 */
		public synchronized void close() {
			interrupt();
			closeConnections(unusedConnections);
			closeConnections(usedConnections);
		}

		private synchronized void closeConnections(Hashtable connections) {
			Enumeration originServerUrls = connections.keys();
			while (originServerUrls.hasMoreElements()) {
				URL originServerUrl = (URL) originServerUrls.nextElement();
				Vector connectionsVector = (Vector) connections.get(originServerUrl);
				Enumeration connectionsEnum = connectionsVector.elements();
				while (connectionsEnum.hasMoreElements()) {
					HttpConnection connection = (HttpConnection) connectionsEnum.nextElement();
					try {
						connection.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
			connections.clear();
		}

		/**
		 * Returns an unused connection that is connected to the origin
		 * server at the given <code>URL</code>. The connection is marked
		 * as in use before it is returned.
		 *
		 * @param originServerUrl the <code>URL</code> of an origin
		 * server
		 * @return an unused connection that is connected to the origin
		 * server at the given <code>URL</code>
		 * @see #putConnection(HttpConnection)
		 */
		public synchronized HttpConnection getConnection(URL originServerUrl) {
			HttpConnection connection = null;

			Vector unusedConnections = (Vector) this.unusedConnections.get(originServerUrl);
			if (unusedConnections == null || unusedConnections.isEmpty()) {
				connection = new HttpConnection(originServerUrl);
			} else {
				connection = (HttpConnection) unusedConnections.lastElement();
				unusedConnections.removeElementAt(unusedConnections.size() - 1);
			}

			Vector usedConnections = (Vector) this.usedConnections.get(originServerUrl);
			if (usedConnections == null) {
				usedConnections = new Vector(5);
				this.usedConnections.put(originServerUrl, usedConnections);
			}

			usedConnections.addElement(connection);
			connection.setTimestamp(new Date());

			return connection;
		}

		public long getConnectionTimeout() {
			return connectionTimeout;
		}

		/**
		 * Marks the given connection that is in use as unused.
		 *
		 * @param connection a connection that is in use
		 * @see #getConnection(URL)
		 */
		public synchronized void putConnection(HttpConnection connection) {
			URL resourceUrl = connection.getResourceUrl();

			URL originServerUrl = null;
			try {
				originServerUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(), resourceUrl.getPort(), "/"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// ignore?
			}

			Vector usedConnections = (Vector) this.usedConnections.get(originServerUrl);
			usedConnections.remove(connection);

			Vector unusedConnections = (Vector) this.unusedConnections.get(originServerUrl);
			if (unusedConnections == null) {
				unusedConnections = new Vector(5);
				this.unusedConnections.put(originServerUrl, unusedConnections);
			}

			unusedConnections.addElement(connection);
			connection.setTimestamp(new Date());
		}

		private synchronized void recycle() {
			Vector staleOriginServerUrls = new Vector(3);
			Enumeration originServerUrls = unusedConnections.keys();
			while (originServerUrls.hasMoreElements()) {
				URL originServerUrl = (URL) originServerUrls.nextElement();
				Vector connectionsVector = (Vector) unusedConnections.get(originServerUrl);

				if (connectionsVector.isEmpty()) {
					staleOriginServerUrls.add(originServerUrl);
					break;
				}

				Vector staleConnections = new Vector(5);
				Enumeration connectionsEnum = connectionsVector.elements();
				while (connectionsEnum.hasMoreElements()) {
					HttpConnection connection = (HttpConnection) connectionsEnum.nextElement();

					long currentTime = System.currentTimeMillis();
					long connectionTime = connection.getTimestamp().getTime();

					if (currentTime - connectionTime >= connectionTimeout) {
						staleConnections.addElement(connection);
					}
				} // end-while

				connectionsEnum = staleConnections.elements();
				while (connectionsEnum.hasMoreElements()) {
					HttpConnection connection = (HttpConnection) connectionsEnum.nextElement();
					connectionsVector.remove(connection);

					try {
						connection.close();
					} catch (IOException e) {
						// ignore?
					}
				} // end-while
			} // end-while

			Enumeration staleOriginServerUrlsEnum = staleOriginServerUrls.elements();
			while (staleOriginServerUrlsEnum.hasMoreElements()) {
				unusedConnections.remove(staleOriginServerUrlsEnum.nextElement());
			}
		}

		public void run() {
			while (!interrupted()) {
				try {
					Thread.sleep(connectionTimeout);
					recycle();
				} catch (InterruptedException e) {
					interrupt();
				}
			}
		}

		public void setConnectionTimeout(long connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
		}
	}

	/**
	 * This type of input stream is passed back to the user in message
	 * responses.  It serves to keep the stream's connection alive by
	 * returning it to this client's unused connections when the stream
	 * is closed.
	 *
	 * @see Response
	 */
	class PersistentInputStream extends FilterInputStream {
		/**
		 * This input stream's connection.
		 */
		private HttpConnection connection;

		/**
		 * Creates a new persistent input stream on the given connection.
		 *
		 * @param connection this input stream's connection
		 */
		PersistentInputStream(HttpConnection connection) throws IOException {
			super(null);

			try {
				in = connection.getInputStream();
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
			this.connection = connection;
		}

		/**
		 * @see InputStream#available()
		 */
		public int available() throws IOException {
			try {
				return super.available();
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}

		/**
		 * @see InputStream#close()
		 */
		public void close() throws IOException {
			try {
				super.close();
			} catch (IOException e) {
				closeConnection();
				throw e;
			} finally {
				connectionsRecycler.putConnection(connection);
			}
		}

		private void closeConnection() {
			try {
				connection.close();
			} catch (IOException e) {
				// ignore?
			}
		}

		/**
		 * @see InputStream#read()
		 */
		public int read() throws IOException {
			try {
				return super.read();
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}

		/**
		 * @see InputStream#read(byte[])
		 */
		public int read(byte b[]) throws IOException {
			try {
				return super.read(b);
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}

		/**
		 * @see InputStream#read(byte[], int, int)
		 */
		public int read(byte b[], int off, int len) throws IOException {
			try {
				return super.read(b, off, len);
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}

		/**
		 * @see InputStream#reset()
		 */
		public synchronized void reset() throws IOException {
			try {
				super.reset();
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}

		/**
		 * @see InputStream#skip(long)
		 */
		public long skip(long n) throws IOException {
			try {
				return super.skip(n);
			} catch (IOException e) {
				closeConnection();
				throw e;
			}
		}
	}

	/**
	 * Creates a new <code>HttpClient</code>.
	 */
	public HttpClient() {
		open();
	}

	/**
	 * Adds the given proxy server exception pattern to this client.
	 * Origin servers whose hostname match the pattern do not communicate
	 * through the defualt proxy server.  The pattern must contain zero or
	 * one stars (*).  A star must appear at either the beginning or the
	 * end of the pattern. A star matches zero or more characters. The
	 * following are valid patterns:
	 * <ul>
	 * <li>www.company.com:80</li>
	 * <li>*.company.com</li>
	 * <li>www.company.*</li>
	 * </ul>
	 *
	 * @param pattern a proxy server exception pattern, for example:
	 * "*.company.com".
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerExceptions()
	 * @see #getProxyServerUrl(URL)
	 * @see #removeProxyServerException(String)
	 * @see #setDefaultProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public void addProxyServerException(String pattern) {
		Assert.isNotNull(pattern);
		proxyServerExceptions.put(pattern, pattern);
	}

	/**
	 * Closes this client.
	 */
	public void close() {
		if (!closed) {
			connectionsRecycler.close();
			closed = true;
		}
	}

	public long getConnectionTimeout() {
		return connectionsRecycler.getConnectionTimeout();
	}

	/**
	 * Returns the context for the origin server at the given
	 * <code>URL</code>.
	 *
	 * @param originServerUrl the <code>URL</code> of an origin server
	 * @return the context for the origin server at the given
	 * <code>URL</code>
	 * @see #getDefaultContext()
	 * @see #setDefaultContext(Context)
	 * @see #setContext(URL, Context)
	 */
	public IContext getContext(URL originServerUrl) {
		Assert.isNotNull(originServerUrl);
		return (IContext) contexts.get(originServerUrl);
	}

	/**
	 * Returns the default context. The default context is used for servers
	 * that do not have a context set. Initially the default context is
	 * <code>null</code>.
	 *
	 * @return the default context, or <code>null</code>
	 * @see #getContext(URL)
	 * @see #setContext(URL, Context)
	 * @see #setDefaultContext(Context)
	 */
	public IContext getDefaultContext() {
		return defaultContext;
	}

	/**
	 * Returns the <code>URL</code> of the default proxy server which is
	 * used for all servers that do not have their proxy server set and
	 * do not match a proxy server exception. If the default proxy server
	 * <code>URL</code> is <code>null</code>, no default proxy server is
	 * used. Initially the default proxy server <code>URL</code> is
	 * <code>null</code>.
	 *
	 * @return the <code>URL</code> of the default proxy server, or
	 * <code>null</code>
	 * @see #addProxyServerException(String)
	 * @see #getProxyServerExceptions()
	 * @see #getProxyServerUrl(URL)
	 * @see #removeProxyServerException(String)
	 * @see #setDefaultProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public URL getDefaultProxyServerUrl() {
		return defaultProxyServerUrl;
	}

	/**
	 * Returns the version of HTTP this client uses for communication with
	 * servers. HTTP/1.1 is used by default.
	 *
	 * @return the version of HTTP this client uses for communication with
	 * servers
	 * @see #setHttpVersion(double)
	 */
	public double getHttpVersion() {
		return httpVersion;
	}

	/**
	 * Returns the maximum number of <code>URL</code> location redirects. The
	 * maximum is 4 by default.
	 *
	 * @return the maximum number of <code>URL</code> location redirects
	 */
	public int getMaxRedirects() {
		return maxRedirects;
	}

	/**
	 * Returns the maximum number of times a request is retried after an
	 * <code>IOException</code> occurs. The maximum is 1 by default.
	 *
	 * @return the maximum number of retries
	 * @see #setMaxRetries(int)
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * Returns an <code>Enumeration</code> over the origin server
	 * <code>URL</code>s known to this client. The known origin server
	 * <code>URL</code>s are gleaned from this client's mapped contexts and
	 * mapped proxy server <code>URL</code>s.
	 *
	 * @return an <code>Enumeration</code> over the origin server
	 * <code>URL</code>s known to this client
	 * @see #getContext(URL)
	 * @see #setContext(URL, Context)
	 * @see #getProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public Enumeration getOriginServerUrls() {
		final Enumeration enum1 = contexts.keys();
		final Enumeration enum2 = proxyServerUrls.keys();

		Enumeration e = new Enumeration() {
			public boolean hasMoreElements() {
				return enum1.hasMoreElements() || enum2.hasMoreElements();
			}

			public Object nextElement() {
				if (enum1.hasMoreElements())
					return enum1.nextElement();
				return enum2.nextElement();
			}
		};
		return e;
	}

	/**
	 * Returns an <code>Enumeration</code> over this client's proxy server
	 * exception patterns.
	 *
	 * @return an <code>Enumeration</code> over this client's proxy server
	 * exception patterns
	 * @see #addProxyServerException(String)
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerUrl(URL)
	 * @see #removeProxyServerException(String)
	 * @see #setDefaultProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public Enumeration getProxyServerExceptions() {
		return proxyServerExceptions.keys();
	}

	/**
	 * Returns the <code>URL</code> of the proxy server that the origin
	 * server at the given <code>URL</code> uses, or <code>null</code> if
	 * no proxy server is used.
	 *
	 * @param originServerUrl the <code>URL<code> of an origin server
	 * @return the <code>URL</code> of a proxy server, or <code>null</code>
	 * @see #addProxyServerException(String)
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerExceptions()
	 * @see #removeProxyServerException(String)
	 * @see #setDefaultProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public URL getProxyServerUrl(URL originServerUrl) {
		Assert.isNotNull(originServerUrl);
		return (URL) proxyServerUrls.get(originServerUrl);
	}

	/**
	 * Returns the socket read timeout (in milliseconds) for this client. A
	 * value of zero indicates that a socket read operation will block
	 * indefinitely waiting for data. The value is zero by default.
	 *
	 * @return the socket read timeout (in milliseconds) for this client
	 * @see #setSoTimeout(int)
	 */
	public int getSoTimeout() {
		return socketTimeout;
	}

	/**
	 * Sends the given request to the server and returns the server's
	 * response.
	 *
	 * @param request the request to send to the server
	 * @return the server's response
	 * @throws IOException if an I/O error occurs. Reasons include:
	 * <ul>
	 * <li>The client is closed.
	 * <li>The client could not connect to the server
	 * <li>An I/O error occurs while communicating with the server
	 * <ul>
	 */
	public Response invoke(Request request) throws IOException {
		Assert.isNotNull(request);

		try {
			open();

			URL resourceUrl = request.getResourceUrl();
			URL originServerUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(), resourceUrl.getPort(), "/"); //$NON-NLS-1$

			URL proxyServerUrl = getProxyServerUrl(originServerUrl);
			if (proxyServerUrl == null && !matchesProxyServerException(originServerUrl)) {
				proxyServerUrl = getDefaultProxyServerUrl();
			}

			IContext context = webDAVFactory.newContext(request.getContext());

			IContext defaultContext = getContext(originServerUrl);
			if (defaultContext == null) {
				defaultContext = getDefaultContext();
			}

			if (defaultContext != null) {
				Enumeration e = defaultContext.keys();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					context.put(key, defaultContext.get(key));
				}
			}

			if (authority != null) {
				authority.authorize(request, null, context, proxyServerUrl, true);
				authority.authorize(request, null, context, proxyServerUrl, false);
			}

			return invoke1(request, context, proxyServerUrl, originServerUrl, 0, 0);
		} finally {
			request.close();
		}
	}

	private Response invoke1(Request request, IContext context, URL proxyServerUrl, URL originServerUrl, int retries, int redirects) throws IOException {
		Response response = null;

		try {
			response = invoke2(request, context, proxyServerUrl, originServerUrl, false);
			int sc = response.getStatusCode();

			if (sc == HTTP_UNAUTHORIZED || sc == HTTP_PROXY_AUTHENTICATION_REQUIRED) {
				if (authority == null || !authority.authorize(request, response, context, proxyServerUrl, sc == IStatusCodes.HTTP_PROXY_AUTHENTICATION_REQUIRED)) {
					return response;
				}
				response.close();
				return invoke1(request, context, proxyServerUrl, originServerUrl, 0, 0);

			} else if (sc >= 300 && sc < 400) {
				if (redirects >= maxRedirects) {
					return response;
				}

				String location = response.getContext().getLocation();
				if (location == null) {
					return response;
				}

				URL url = new URL(location);

				if (sc == HTTP_USE_PROXY) {
					proxyServerUrl = url;
				} else {
					originServerUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/"); //$NON-NLS-1$
					request.setResourceUrl(url);
				}

				response.close();

				return invoke1(request, context, proxyServerUrl, originServerUrl, 0, redirects + 1);
			}

			if (authority != null) {
				authority.confirm(request, response, proxyServerUrl);
			}

			return response;
		} catch (IOException e) {
			if (response != null) {
				response.close();
			}

			if (retries < maxRetries) {
				return invoke1(request, context, proxyServerUrl, originServerUrl, retries + 1, 0);
			}

			throw e;
		}
	}

	private Response invoke2(Request request, IContext context, URL proxyServerUrl, URL originServerUrl, boolean expect100Continue) throws IOException {
		HttpConnection connection = null;

		try {
			// get the connection
			connection = connectionsRecycler.getConnection(originServerUrl);

			// set the http version
			connection.setHttpVersion(httpVersion);

			// set the method, resource url, and proxy server url
			connection.setRequestMethod(request.getMethod());
			connection.setResourceUrl(request.getResourceUrl());
			connection.setProxyServerUrl(proxyServerUrl);

			// set the request header
			connection.clearRequestHeader();
			Enumeration e = context.keys();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				connection.setRequestHeaderField(key, context.get(key));
			}

			// set the content length
			long contentLength = request.getContentLength();
			if (contentLength >= 0 && context.getContentLength() == -1) {
				connection.setRequestHeaderField("Content-Length", "" + contentLength); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// set the chunked encoding
			String method = request.getMethod();
			boolean sendChunked = contentLength == -1 && httpVersion > 0 && !(method.equals("PROPPATCH") || method.equals("PROPFIND")); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setSendChunked(sendChunked);

			// make the connection persistent
			connection.setPersistent(httpVersion > 0);

			// set the socket timeout
			connection.setSoTimeout(socketTimeout);

			// set the socket factory
			connection.setSocketFactory(socketFactory);

			// send the request header
			IContext responseHeader = null;
			if (expect100Continue && (contentLength > 0 && httpVersion > 0 || sendChunked)) {
				if (!"100-continue".equalsIgnoreCase(context.get("Expect"))) { //$NON-NLS-1$ //$NON-NLS-2$
					connection.setRequestHeaderField("Expect", "100-continue"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				responseHeader = readResponseHeader(connection);
				if (connection.getStatusCode() != IStatusCodes.HTTP_CONTINUE) {
					return new Response(connection.getStatusCode(), connection.getStatusMessage(), responseHeader, new PersistentInputStream(connection));
				}
			}

			// send the request body
			if (contentLength != 0) {
				OutputStream os = connection.getOutputStream();
				request.write(os);
				os.close();
			}

			// get the response header
			responseHeader = readResponseHeader(connection);

			// return the response
			return new Response(connection.getStatusCode(), connection.getStatusMessage(), responseHeader, new PersistentInputStream(connection));
		} catch (IOException e1) {
			try {
				connection.close();
			} catch (IOException e2) {
				// ignore?
			}
			connectionsRecycler.putConnection(connection);

			throw e1;
		} // end-catch
	}

	private boolean matchesProxyServerException(URL originServerUrl) {
		String host = originServerUrl.getHost();
		int port = originServerUrl.getPort();
		String originServerUrlString = host + (port == -1 ? "" : ":" + port); //$NON-NLS-1$ //$NON-NLS-2$

		boolean found = false;
		Enumeration keys = proxyServerExceptions.keys();

		while (!found && keys.hasMoreElements()) {
			String httpProxyException = (String) keys.nextElement();
			int len = httpProxyException.length();
			found = originServerUrlString.equals(httpProxyException);
			if (!found) {
				if (httpProxyException.startsWith("*")) { //$NON-NLS-1$
					found = originServerUrlString.endsWith(httpProxyException.substring(1, len));
				} else if (httpProxyException.endsWith("*")) { //$NON-NLS-1$
					found = originServerUrlString.startsWith(httpProxyException.substring(0, len - 1));
				}
			}
		}

		return found;
	}

	private void open() {
		if (closed) {
			connectionsRecycler.start();
			closed = false;
		}
	}

	private IContext readResponseHeader(HttpConnection httpConnection) throws IOException {
		IContext responseHeader = webDAVFactory.newContext();

		int position = 0;
		String fieldName = null;
		while ((fieldName = httpConnection.getResponseHeaderFieldName(position)) != null) {
			// TBD : Should combine multiple headers
			if (responseHeader.get(fieldName.toLowerCase()) == null)
				responseHeader.put(fieldName.toLowerCase(), httpConnection.getResponseHeaderFieldValue(position));
			++position;
		}

		return responseHeader;
	}

	/**
	 * Removes the given proxy server exception pattern from this client.
	 *
	 * @param pattern a proxy server exception pattern
	 * @see #addProxyServerException(String)
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerExceptions()
	 * @see #getProxyServerUrl(URL)
	 * @see #setDefaultProxyServerUrl(URL)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public void removeProxyServerException(String pattern) {
		Assert.isNotNull(pattern);
		proxyServerExceptions.remove(pattern);
	}

	/**
	 * Sets the authenticator. Authenticators store authentication
	 * information and are required to access protected resources. If the
	 * authenticator is <code>null</code> protected resources cannot be
	 * accessed. The authenticator is <code>null</code> by default.
	 *
	 * @param authenticator the authenticator, or <code>null</code>
	 */
	public void setAuthenticator(IAuthenticator authenticator) {
		authority = authenticator == null ? null : new AuthorizationAuthority(authenticator);
	}

	public void setConnectionTimeout(long connectionTimeout) {
		connectionsRecycler.setConnectionTimeout(connectionTimeout);
	}

	/**
	 * Set the context for the origin server at the given <code>URL</code>.
	 * If the given context is <code>null</code>, the context for the
	 * specified origin server is removed.
	 *
	 * @param originServerUrl the <code>URL</code> of an origin server
	 * @param context the context for the specified origin server
	 * @see #getContext(URL)
	 * @see #getDefaultContext()
	 * @see #setDefaultContext(Context)
	 */
	public void setContext(URL originServerUrl, IContext context) {
		Assert.isNotNull(originServerUrl);
		if (context == null)
			contexts.remove(originServerUrl);
		else
			contexts.put(originServerUrl, context);
	}

	/**
	 * Sets the default context which is used by all servers that do not
	 * already have a context set.
	 *
	 * @param context the default context
	 * @see #getContext(URL)
	 * @see #getDefaultContext()
	 * @see #setContext(URL, Context)
	 */
	public void setDefaultContext(IContext context) {
		defaultContext = context;
	}

	/**
	 * Sets the <code>URL</code> of the default proxy server that is used by
	 * all servers that do not have their proxy server set and do not match
	 * a proxy server exception pattern. If the given proxy server
	 * <code>URL</code> is <code>null</code>, no default proxy server is
	 * used.
	 *
	 * @param proxyServerUrl the <code>URL</code> of the default proxy server
	 * @see #addProxyServerException(String)
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerExceptions()
	 * @see #getProxyServerUrl(URL)
	 * @see #removeProxyServerException(String)
	 * @see #setProxyServerUrl(URL, URL)
	 */
	public void setDefaultProxyServerUrl(URL proxyServerUrl) {
		defaultProxyServerUrl = proxyServerUrl;
	}

	/**
	 * Sets the version of HTTP this client uses for communication with
	 * servers. HTTP/1.1 is used by default.
	 *
	 * @param httpVersion the version of HTTP this client uses for
	 * communication with servers
	 * @see #getHttpVersion()
	 */
	public void setHttpVersion(double httpVersion) {
		Assert.isTrue(httpVersion == 1.0 || httpVersion == 1.1);
		this.httpVersion = httpVersion;
	}

	/**
	 * Sets the maximum number of <code>URL</code> location redirects. The
	 * maximum is 4 by default.
	 *
	 * @param maxRedirects the maximum number of <code>URL</code> redirects
	 */
	public void setMaxRedirects(int maxRedirects) {
		Assert.isTrue(maxRedirects >= 0);
		this.maxRedirects = maxRedirects;
	}

	/**
	 * Sets the maximum number of times a request is retried after an
	 * <code>IOException</code> occurs. The maximum is 1 by default.
	 *
	 * @param maxRetries the maximum number of times a request is retried
	 * after an <code>IOException</code> occurs
	 * @see #getMaxRetries()
	 */
	public void setMaxRetries(int maxRetries) {
		Assert.isTrue(maxRetries >= 0);
		this.maxRetries = maxRetries;
	}

	/**
	 * Sets the <code>URL</code> of the proxy server that this client uses
	 * to communicate with the origin server at the given <code>URL</code>.
	 * If the proxy server <code>URL</code> is <code>null</code>, the
	 * default proxy server is used if the specified origin server does not
	 * match a proxy server exception pattern.
	 *
	 * @param originServerUrl the <code>URL</code> of on origin server
	 * @param proxyServerUrl the <code>URL</code> of a proxy server, or
	 * <code>null</code>
	 * @see #addProxyServerException(String)
	 * @see #getDefaultProxyServerUrl()
	 * @see #getProxyServerExceptions()
	 * @see #getProxyServerUrl(URL)
	 * @see #removeProxyServerException(String)
	 * @see #setDefaultProxyServerUrl(URL)
	 */
	public void setProxyServerUrl(URL originServerUrl, URL proxyServerUrl) {
		Assert.isNotNull(originServerUrl);
		if (proxyServerUrl == null)
			proxyServerUrls.remove(originServerUrl);
		else
			proxyServerUrls.put(originServerUrl, proxyServerUrl);
	}

	/**
	 * Sets the factory that this client uses to create sockets.
	 *
	 * @param socketFactory the factory that this client uses to create
	 * sockets
	 */
	public void setSocketFactory(ISocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * Sets the socket read timeout (in milliseconds) for this client. A
	 * value of zero indicates that a socket read operation will block
	 * indefinitely waiting for data. The value is zero by default.
	 *
	 * @param timeout the socket read timeout
	 * @see #getSoTimeout()
	 */
	public void setSoTimeout(int timeout) {
		socketTimeout = timeout;
	}
}
