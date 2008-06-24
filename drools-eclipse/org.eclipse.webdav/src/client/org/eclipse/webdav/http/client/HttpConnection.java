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
import java.net.*;
import java.util.Date;
import java.util.Vector;
import org.eclipse.webdav.client.Policy;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A connection to an HTTP/1.0 or HTTP/1.1 compatable server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class HttpConnection implements IStatusCodes {
	// The HTTP version to use (1.0 or 1.1).
	private double httpVersion = 1.1;

	// The request method.
	private String method = "GET"; //$NON-NLS-1$

	// The URL of the requested resource.
	private URL resourceUrl = null;

	// The URL of the proxy server.
	private URL proxyServerUrl = null;

	// The request and response headers.
	protected Header requestHeader = new Header();
	protected Header responseHeader = new Header();
	protected Header internalHeader = new Header();

	// The status code and status message of the response.
	private int statusCode;
	private String statusMessage;

	// The socket factory, socket, and socket settings.
	private ISocketFactory socketFactory = null;
	private Socket socket = null;
	private int receiveBufferSize = 0;
	private int sendBufferSize = 0;
	private int soLinger = -1;
	private int soTimeout = 0;
	private boolean tcpNoDelay = false;

	// The actual socket streams.
	protected InputStream socketIn = null;
	protected OutputStream socketOut = null;

	// The user's socket streams.
	private InputStream is = null;
	private OutputStream os = null;

	// A boolean indicating whether the connection should be closed after
	// the current request.
	private boolean closeConnection;

	// A boolean indicating whether the client wishes the connection to
	// remain open after the current request.
	private boolean persistent = true;

	// A boolean indicating whether the client wishes the request's
	// content to be sent chunked encoded.
	private boolean sendChunked = true;

	// A boolean indicating whether this connection is currently
	// connected to a server.
	private boolean connected = false;

	// A boolean indicating whether the current request has been sent.
	private boolean sentRequest = false;

	// This connection's timestamp.
	private Date timestamp = null;

	/**
	 * A request or response header.
	 */
	public class Header {
		// This header's field names
		private Vector fieldNames = new Vector(5);

		// This header's field values
		private Vector fieldValues = new Vector(5);

		/**
		 * Adds the specified header field to this header.
		 *
		 * @param fieldName the new header field's name
		 * @param fieldValue the new header field's value
		 */
		public void addField(String fieldName, String fieldValue) {
			Assert.isNotNull(fieldName);
			Assert.isNotNull(fieldValue);
			fieldNames.addElement(fieldName);
			fieldValues.addElement(fieldValue);
		}

		/**
		 * Removes all header fields from this header.
		 */
		public void clear() {
			fieldNames.removeAllElements();
			fieldValues.removeAllElements();
		}

		/**
		 * Returns the field value of the header field at the given
		 * position, or <code>null</code> if there is no such postition.
		 *
		 * @param position the position of a header field
		 * @return the field value of the header field at the given
		 * position
		 */
		public String getFieldValue(int position) {
			if (position < 0 || position >= fieldValues.size())
				return null;
			return (String) fieldValues.elementAt(position);
		}

		/**
		 * Returns the field value of the header field with the given
		 * field name, or <code>null</code> if there is no such field
		 * name.
		 *
		 * @param fieldName the name of a header field
		 * @return the field value of the header field with the given
		 * field name
		 */
		public String getFieldValue(String fieldName) {
			Assert.isNotNull(fieldName);
			return getFieldValue(fieldNames.indexOf(fieldName));
		}

		/**
		 * Returns the field name of the header field at the given
		 * position, or <code>null</code> if there is no such position.
		 *
		 * @param position the position of a header field in this header
		 * @return the field name of the header field at the given
		 * position
		 */
		public String getFieldName(int position) {
			if (position < 0 || position >= fieldNames.size())
				return null;
			return (String) fieldNames.elementAt(position);
		}

		/**
		 * Returns the number of header fields in this header.
		 *
		 * @return the number of header fields in this header
		 */
		public int size() {
			return fieldNames.size();
		}
	}

	/**
	 * A limited input stream reads up to a given number of bytes from
	 * this connection's underlying socket input stream.
	 */
	private class LimitedInputStream extends InputStream {
		/**
		 * The number of bytes remaining in this stream.
		 */
		private int bytesRemaining;

		/**
		 * Creates a new limited input stream that reads up to
		 * <code>length</code> bytes of data from this connection's
		 * underlying socket input stream.
		 *
		 * @param length the length of this input stream
		 */
		public LimitedInputStream(int length) {
			Assert.isTrue(length >= 0);
			bytesRemaining = length;
		}

		/**
		 * @see InputStream#available()
		 */
		public int available() throws IOException {
			return Math.min(socketIn.available(), bytesRemaining);
		}

		/**
		 * @see InputStream#close()
		 */
		public void close() throws IOException {
			while (skip(4096) > 0);
		}

		/**
		 * @see InputStream#read()
		 */
		public int read() throws IOException {
			if (bytesRemaining <= 0)
				return -1;
			--bytesRemaining;
			return socketIn.read();
		}

		/**
		 * @see InputStream#read(byte[], int, int)
		 */
		public int read(byte[] buf, int offset, int length) throws IOException {
			if (bytesRemaining <= 0)
				return -1;
			int result = socketIn.read(buf, offset, Math.min(length, bytesRemaining));
			if (result > 0)
				bytesRemaining -= result;
			return result;
		}

		/**
		 * @see InputStream#skip(int)
		 */
		public long skip(int amount) throws IOException {
			if (bytesRemaining <= 0)
				return -1;
			long result = socketIn.skip(Math.min(amount, bytesRemaining));
			if (result > 0)
				bytesRemaining -= result;
			return result;
		}
	}

	/**
	 * A chunked input stream reads bytes, that have been chunked
	 * transfer encoded, from this connection's underlying socket input
	 * stream.
	 */
	private class ChunkedInputStream extends InputStream {

		/**
		 * The number of bytes remaining in this stream's current chunk.
		 */
		private int bytesRemaining = -1;

		/**
		 * A boolean indicating whether the end of this stream has been
		 * reached.
		 */
		private boolean atEnd = false;

		/**
		 * @see InputStream#available()
		 */
		public int available() throws IOException {
			return Math.min(socketIn.available(), bytesRemaining);
		}

		/**
		 * @see InputStream#close()
		 */
		public void close() throws IOException {
			while (skip(4096) > 0);
		}

		/**
		 * @see InputStream#read()
		 */
		public int read() throws IOException {
			if (!atEnd && bytesRemaining <= 0)
				readChunkSize();
			if (atEnd)
				return -1;
			--bytesRemaining;
			return socketIn.read();
		}

		/**
		 * @see InputStream#read(byte[], int, int)
		 */
		public int read(byte[] buf, int offset, int length) throws IOException {
			if (!atEnd && bytesRemaining <= 0)
				readChunkSize();
			if (atEnd)
				return -1;
			int result = socketIn.read(buf, offset, Math.min(length, bytesRemaining));
			if (result > 0)
				bytesRemaining -= result;
			return result;
		}

		/**
		 * Read the size of the next chunk of data.
		 */
		private void readChunkSize() throws IOException {
			if (bytesRemaining == 0)
				readln();
			String size = readln();
			int index = size.indexOf(";"); //$NON-NLS-1$
			if (index >= 0)
				size = size.substring(0, index);
			try {
				bytesRemaining = Integer.parseInt(size.trim(), 16);
			} catch (NumberFormatException exception) {
				throw new IOException(exception.getMessage());
			}
			if (bytesRemaining == 0) {
				atEnd = true;
				readHeader(responseHeader);
			}
		}

		/**
		 * @see InputStream#skip(int)
		 */
		public long skip(int amount) throws IOException {
			if (!atEnd && bytesRemaining <= 0)
				readChunkSize();
			if (atEnd)
				return -1;
			long result = socketIn.skip(Math.min(amount, bytesRemaining));
			if (result > 0)
				bytesRemaining -= result;
			return result;
		}
	}

	/**
	 * A limited output stream writes up to a given number of bytes to
	 * this connection's underlying socket input stream.
	 */
	private class LimitedOutputStream extends OutputStream {
		// The maximum number of bytes that can be written to this
		// stream.
		private int limit;

		// A boolean indicating whether this stream is closed.
		private boolean closed = false;

		/**
		 * Creates a new limited output stream that writes up to the
		 * given limit of bytes to this connection's underlying socket
		 * output stream.
		 *
		 * @param limit the maximum number of bytes that can be written
		 * to this stream
		 */
		public LimitedOutputStream(int limit) {
			Assert.isTrue(limit >= 0);
			this.limit = limit;
		}

		/**
		 * @see OutputStream#close()
		 */
		public void close() throws IOException {
			if (closed)
				return;
			try {
				flush();
				if (limit > 0)
					throw new IOException(Policy.bind("exception.contentLengthUnderflow")); //$NON-NLS-1$
				readServerResponse();
			} finally {
				closed = true;
			}
		}

		/**
		 * @see OutputStream#flush()
		 */
		public void flush() throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			socketOut.flush();
		}

		/**
		 * @see OutputStream#write(int)
		 */
		public void write(int data) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			if (limit == 0)
				throw new IOException(Policy.bind("exception.contentLengthExceeded")); //$NON-NLS-1$
			--limit;
			socketOut.write(data);
		}

		/**
		 * @see OutputStream#write(byte[], int, int)
		 */
		public void write(byte[] buffer, int offset, int count) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			if (count > limit)
				throw new IOException(Policy.bind("exception.contentLengthExceeded")); //$NON-NLS-1$
			limit -= count;
			socketOut.write(buffer, offset, count);
		}
	}

	/**
	 * A chunked output stream writes bytes to this connection's
	 * underlying socket output stream. The bytes are chunked transfer
	 * encoded before they are written to the stream.
	 */
	private class ChunkedOutputStream extends OutputStream {
		// The maximum size of this output stream's buffer
		private static final int MAX_BUFFER_SIZE = 1024;

		// A buffer that holds the next chunk of data to be written to
		// this output stream.
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream(MAX_BUFFER_SIZE);

		// A boolean indicating whether this output stream is closed.
		private boolean closed = false;

		/**
		 * @see OutputStream#close()
		 */
		public void close() throws IOException {
			if (closed)
				return;
			try {
				sendBuffer();
				output(socketOut, "0\r\n\r\n"); //$NON-NLS-1$
				readServerResponse();
			} finally {
				closed = true;
			}
		}

		/**
		 * @see OutputStream#flush()
		 */
		public void flush() throws IOException {
			if (closed)
				throw new IOException("closed"); //$NON-NLS-1$
			sendBuffer();
			socketOut.flush();
		}

		/**
		 * Writes the content of the buffer to this stream.
		 */
		public void sendBuffer() throws IOException {
			int chunkSize = buffer.size();
			if (chunkSize > 0) {
				output(socketOut, Integer.toHexString(chunkSize) + "\r\n"); //$NON-NLS-1$
				buffer.writeTo(socketOut);
				buffer.reset();
				output(socketOut, "\r\n"); //$NON-NLS-1$
			}
		}

		/**
		 * @see OutputStream#write(int)
		 */
		public void write(int data) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			buffer.write(data);
			if (buffer.size() >= MAX_BUFFER_SIZE)
				sendBuffer();
		}

		/**
		 * @see OutputStream#write(byte[], int, int)
		 */
		public void write(byte[] buf, int off, int len) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			int bufferSize = buffer.size();
			if (bufferSize + len < MAX_BUFFER_SIZE) {
				buffer.write(buf, off, len);
			} else {
				output(socketOut, Integer.toHexString(bufferSize + len) + "\r\n"); //$NON-NLS-1$
				buffer.writeTo(socketOut);
				buffer.reset();
				socketOut.write(buf, off, len);
				output(socketOut, "\r\n"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * A cached output stream writes bytes to a cache before sending the
	 * data to the underlying socket output stream. This is done so the
	 * content length can be determined.
	 */
	private class CachedOutputStream extends OutputStream {
		// The initial size of this output stream's cache
		private static final int INITIAL_CACHE_SIZE = 1024;

		// A cache that stores the bytes written to this output stream.
		private ByteArrayOutputStream cache = new ByteArrayOutputStream(INITIAL_CACHE_SIZE);

		// A boolean indicating whether this output stream is closed.
		private boolean closed = false;

		/**
		 * @see OutputStream#close()
		 */
		public void close() throws IOException {
			if (closed)
				return;
			try {
				String contentLength = Integer.toString(cache.size());
				internalHeader.addField("Content-Length", contentLength); //$NON-NLS-1$
				sendRequest();
				cache.writeTo(socketOut);
				readServerResponse();
			} finally {
				closed = true;
			}
		}

		/**
		 * @see OutputStream#flush()
		 */
		public void flush() throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
		}

		/**
		 * @see OutputStream#write(int)
		 */
		public void write(int data) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			cache.write(data);
		}

		/**
		 * @see OutputStream#write(byte[], int, int)
		 */
		public void write(byte[] buffer, int offset, int count) throws IOException {
			if (closed)
				throw new IOException(Policy.bind("exception.closed")); //$NON-NLS-1$
			cache.write(buffer, offset, count);
		}
	}

	/**
	 * Creates a new connection on the specified resource.
	 *
	 * @param resourceUrl the <code>URL</code> of a resource
	 */
	public HttpConnection(URL resourceUrl) {
		Assert.isNotNull(resourceUrl);
		this.resourceUrl = resourceUrl;
	}

	/**
	 * Creates a new connection on the specified resource. This connection
	 * communicates through the proxy at the given proxy server
	 * <code>URL</code>.
	 *
	 * @param proxyServerUrl the <code>URL</code> of a proxy server
	 * @param resourceUrl the <code>URL</code> of a resource
	 */
	public HttpConnection(URL proxyServerUrl, URL resourceUrl) {
		Assert.isNotNull(proxyServerUrl);
		Assert.isNotNull(resourceUrl);
		this.proxyServerUrl = proxyServerUrl;
		this.resourceUrl = resourceUrl;
	}

	/**
	 * Clear the request header.
	 */
	public void clearRequestHeader() {
		endRequest();
		requestHeader.clear();
	}

	/**
	 * Close this connection.
	 *
	 * @exception IOException if there is an I/O error closing the socket
	 */
	public void close() throws IOException {
		endRequest();
		if (connected) {
			connected = false;
			socket.close();
		}
	}

	private void connect() throws IOException {
		if (!connected) {
			String protocol;
			String host;
			int port;
			if (proxyServerUrl == null) {
				protocol = resourceUrl.getProtocol();
				host = resourceUrl.getHost();
				port = getPort(resourceUrl);
			} else {
				protocol = proxyServerUrl.getProtocol();
				host = proxyServerUrl.getHost();
				port = getPort(proxyServerUrl);
			}

			if (socketFactory == null) {
				socket = new Socket(host, port);
			} else {
				socket = socketFactory.createSocket(protocol, host, port);
			}

			if (receiveBufferSize > 0) {
				socket.setReceiveBufferSize(receiveBufferSize);
			}

			if (sendBufferSize > 0) {
				socket.setSendBufferSize(sendBufferSize);
			}

			socket.setSoLinger(soLinger >= 0, soLinger >= 0 ? soLinger : 0);
			socket.setSoTimeout(soTimeout);
			socket.setTcpNoDelay(tcpNoDelay);

			socketOut = new BufferedOutputStream(socket.getOutputStream());
			socketIn = new BufferedInputStream(socket.getInputStream());

			closeConnection = httpVersion == 1.0 || !persistent;

			connected = true;
		}
	}

	private void endRequest() {
		if (sentRequest) {
			boolean failed = false;

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					failed = true;
				}
			}

			try {
				getInputStream().close();
			} catch (IOException e) {
				failed = true;
			}

			sentRequest = false;

			if (closeConnection || failed) {
				try {
					connected = false;
					socket.close();
				} catch (IOException e) {
					// ignore or log?
				}
			}
		}

		os = null;
		is = null;

		internalHeader.clear();
	}

	/**
	 * Returns the version of HTTP this connection uses for communication
	 * with servers. HTTP/1.1 is used by default.
	 *
	 * @return the version of HTTP this connection uses for communication
	 * with servers
	 * @see #setHttpVersion(double)
	 */
	public double getHttpVersion() {
		return httpVersion;
	}

	/**
	 * Returns this connection's <code>InputStream</code>.
	 *
	 * @return this connection's <code>InputStream</code>
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 */
	public InputStream getInputStream() throws IOException {
		if (is != null)
			return is;
		sendRequest();
		String transferEncoding = responseHeader.getFieldValue("Transfer-Encoding"); //$NON-NLS-1$
		String contentLength = responseHeader.getFieldValue("Content-Length"); //$NON-NLS-1$
		if ("chunked".equalsIgnoreCase(transferEncoding)) { //$NON-NLS-1$
			is = new ChunkedInputStream();
		} else if (method.equals("HEAD") && statusCode == HTTP_OK) { //$NON-NLS-1$
			is = new LimitedInputStream(0);
		} else if (contentLength != null) {
			try {
				is = new LimitedInputStream(Integer.parseInt(contentLength));
			} catch (NumberFormatException e) {
				throw new IOException(e.getMessage());
			}
		} else if ((statusCode >= 100 && statusCode < 200) || statusCode == HTTP_NO_CONTENT || statusCode == HTTP_NOT_MODIFIED) {
			is = new LimitedInputStream(0);
		} else {
			closeConnection = true;
			is = socketIn;
		}
		return is;
	}

	/**
	 * Returns this connection's <code>OutputStream</code>.
	 *
	 * @return this connection's <code>OutputStream</code>
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 */
	public OutputStream getOutputStream() throws IOException {
		if (os != null)
			return os;
		String contentLength = requestHeader.getFieldValue("Content-Length"); //$NON-NLS-1$
		if (sendChunked && httpVersion > 1.0) {
			os = new ChunkedOutputStream();
		} else if (contentLength != null) {
			try {
				os = new LimitedOutputStream(Integer.parseInt(contentLength));
			} catch (NumberFormatException e) {
				throw new IOException(Policy.bind("exception.malformedContentLength")); //$NON-NLS-1$
			}
		} else {
			os = new CachedOutputStream();
			return os;
		}
		sendRequest();
		return os;
	}

	/**
	 * Returns a boolean indicating whether this connection should remain
	 * open after each request.
	 *
	 * @return a boolean indicating whether this connection should remain
	 * open after each request
	 * @see #setPersistent(boolean)
	 */
	public boolean getPersistent() {
		return persistent;
	}

	private int getPort(URL url) {
		String protocol = url.getProtocol();
		int port = url.getPort();

		if (port == -1) {
			if (protocol.equals("http")) //$NON-NLS-1$
				return 80;
			if (protocol.equals("https")) //$NON-NLS-1$
				return 443;
			return -1;
		}

		return port;
	}

	/**
	 * Returns the <code>URL</code> of the proxy server this connection uses
	 * to communicate with the origin server, or <code>null</code> if a proxy
	 * server is not used.
	 *
	 * @return the <code>URL</code> of the proxy server this connection uses
	 * to communicate with the origin server
	 * @see #setProxyServerUrl(URL)
	 */
	public URL getProxyServerUrl() {
		return proxyServerUrl;
	}

	/**
	 * @see Socket#getReceiveBufferSize()
	 * @see #setReceiveBufferSize(int)
	 */
	public int getReceiveBufferSize() throws IOException {
		if (connected)
			return socket.getReceiveBufferSize();
		return receiveBufferSize;
	}

	/**
	 * Returns the request header value associated with the given field name,
	 * or <code>null</code> if there is no such field name.
	 *
	 * @param fieldName the request header field name
	 * @return the request header value associated with the given field name
	 * @see #setRequestHeaderField(String, String)
	 */
	public String getRequestHeaderFieldValue(String fieldName) {
		Assert.isNotNull(fieldName);
		return requestHeader.getFieldValue(fieldName);
	}

	/**
	 * Returns the request method. "GET" is used by default.
	 *
	 * @return the request method
	 * @see #setRequestMethod(String)
	 */
	public String getRequestMethod() {
		return method;
	}

	/**
	 * Returns the <code>URL</code> of this connection's resource.
	 *
	 * @return the <code>URL</code> of this connection's resource
	 * @see #setResourceUrl(URL)
	 */
	public URL getResourceUrl() {
		return resourceUrl;
	}

	/**
	 * Returns the response header field name at the given position, or
	 * <code>null</code> if there is no field name at that position.
	 *
	 * @param position a position in the response header greater than or
	 * equal to zero
	 * @return the response header field name at the given postion
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 * @see #getResponseHeaderFieldValue(int)
	 * @see #getResponseHeaderFieldValue(String)
	 */
	public String getResponseHeaderFieldName(int position) throws IOException {
		Assert.isTrue(position >= 0);
		sendRequest();
		return responseHeader.getFieldName(position);
	}

	/**
	 * Returns the response header field value at the given position, or
	 * <code>null</code> if there is no value at that position.
	 *
	 * @param position a position in the response header greater than or
	 * equal to zero
	 * @return the response header field value at the given postion
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 * @see #getResponseHeaderFieldName(int)
	 * @see #getResponseHeaderFieldValue(String)
	 */
	public String getResponseHeaderFieldValue(int position) throws IOException {
		Assert.isTrue(position >= 0);
		sendRequest();
		return responseHeader.getFieldValue(position);
	}

	/**
	 * Returns the response header field value that is associated with the
	 * given field name, or <code>null</code> if there is no value associated
	 * with that field name.
	 *
	 * @param fieldName the name of a response header field
	 * @return the response header field value that is associated with the
	 * given field name
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 * @see #getResponseHeaderFieldValue(int)
	 * @see #getResponseHeaderFieldName(int)
	 */
	public String getResponseHeaderFieldValue(String fieldName) throws IOException {
		Assert.isNotNull(fieldName);
		sendRequest();
		return responseHeader.getFieldValue(fieldName);
	}

	/**
	 * @see Socket#getSendBufferSize()
	 * @see #setSendBufferSize(int)
	 */
	public int getSendBufferSize() throws IOException {
		if (connected)
			return socket.getSendBufferSize();
		return sendBufferSize;
	}

	/**
	 * Returns a boolean indicating whether the request's body should be sent
	 * chunked encoded.
	 *
	 * @return a boolean indicating whether the request's body should be sent
	 * chunked encoded
	 * @see #setSendChunked(boolean)
	 */
	public boolean getSendChunked() {
		return sendChunked;
	}

	/**
	 * @see Socket#getSoLinger()
	 * @see #setSoLinger(boolean, int)
	 */
	public int getSoLinger() {
		return soLinger;
	}

	/**
	 * @see Socket#getSoTimeout()
	 * @see #setSoTimeout(int)
	 */
	public int getSoTimeout() {
		return soTimeout;
	}

	/**
	 * Returns the status code of the server's response.
	 *
	 * @return the status code of the server's response
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 * @see #getStatusMessage()
	 */
	public int getStatusCode() throws IOException {
		sendRequest();
		return statusCode;
	}

	/**
	 * Returns the status message of the server's response.
	 *
	 * @return the status message of the server's response
	 * @exception IOException if an I/O error occurs while sending the
	 * request
	 * @see #getStatusCode()
	 */
	public String getStatusMessage() throws IOException {
		sendRequest();
		return statusMessage;
	}

	/**
	 * @see Socket#getTcpNoDelay()
	 * @see #setTcpNoDelay(boolean)
	 */
	public boolean getTcpNoDelay() {
		return tcpNoDelay;
	}

	/**
	 * Returns this connection's timestamp.
	 *
	 * @return this connection's timestamp
	 * @see #setTimestamp(Date)
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	protected void output(OutputStream stream, String output) throws IOException {
		stream.write(output.getBytes("UTF8")); //$NON-NLS-1$
	}

	protected void readHeader(Header header) throws IOException {
		String line = null;

		while ((line = readln()).length() > 0) {
			int index = line.indexOf(":"); //$NON-NLS-1$

			if (index < 0)
				throw new IOException(Policy.bind("exception.malformedHeaderField")); //$NON-NLS-1$
			String fieldName = line.substring(0, index);
			String fieldValue = line.substring(index + 1).trim();
			header.addField(fieldName, fieldValue);
		}

		String fieldValue = header.getFieldValue("Connection"); //$NON-NLS-1$
		if (fieldValue != null && fieldValue.equalsIgnoreCase("close")) { //$NON-NLS-1$
			closeConnection = true;
		}
	}

	protected String readln() throws IOException {
		boolean foundCR = false;
		StringBuffer result = new StringBuffer();

		while (true) {
			int c = socketIn.read();

			if (c < 0) {
				throw new IOException(Policy.bind("exception.unexpectedEndStream")); //$NON-NLS-1$
			}

			if (foundCR) {
				if (c == '\n')
					break;
				result.append('\r');
			}

			if (c == '\r') {
				foundCR = true;
			} else {
				result.append((char) c);
			}
		}
		return result.toString();
	}

	protected void readServerResponse() throws IOException {
		socketOut.flush();

		String statusLine = readln();

		if (!statusLine.startsWith("HTTP/")) { //$NON-NLS-1$
			throw new IOException(Policy.bind("exception.malformedStatusLine")); //$NON-NLS-1$
		}

		int firstSpace = statusLine.indexOf(' ', 5);
		if (firstSpace == -1) {
			throw new IOException(Policy.bind("exception.malformedStatusLine")); //$NON-NLS-1$
		}

		int secondSpace = statusLine.indexOf(' ', firstSpace + 1);
		if (secondSpace == -1) {
			throw new IOException(Policy.bind("exception.malformedStatusLine")); //$NON-NLS-1$
		}

		double protocolVersion;

		try {
			protocolVersion = Double.parseDouble(statusLine.substring(5, firstSpace));
			statusCode = Integer.parseInt(statusLine.substring(firstSpace + 1, secondSpace));
			statusMessage = statusLine.substring(secondSpace + 1);
		} catch (NumberFormatException e) {
			throw new IOException(Policy.bind("exception.malformedStatusLine")); //$NON-NLS-1$
		}

		if (protocolVersion == 1.0) {
			httpVersion = 1.0;
			closeConnection = true;
		}

		responseHeader.clear();
		readHeader(responseHeader);
	}

	protected void sendRequest() throws IOException {
		if (sentRequest)
			return;
		connect();
		if (requestHeader.getFieldValue("Host") == null) { //$NON-NLS-1$
			internalHeader.addField("Host", //$NON-NLS-1$
					resourceUrl.getHost() + (resourceUrl.getPort() == -1 ? "" : ":" + resourceUrl.getPort())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (httpVersion > 1.0 && !persistent && requestHeader.getFieldValue("Connection") == null) { //$NON-NLS-1$
			internalHeader.addField("Connection", "close"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (os != null || "100-continue".equals(requestHeader.getFieldValue("Expect"))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (requestHeader.getFieldValue("Content-Type") == null) { //$NON-NLS-1$
				internalHeader.addField("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (httpVersion > 1.0 && sendChunked) {
				internalHeader.addField("Transfer-Encoding", "chunked"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		output(socketOut, method);
		output(socketOut, " "); //$NON-NLS-1$
		output(socketOut, proxyServerUrl == null ? resourceUrl.getFile() : resourceUrl.toString());
		output(socketOut, " HTTP/1.1\r\n"); //$NON-NLS-1$

		writeHeader(internalHeader);
		writeHeader(requestHeader);
		output(socketOut, "\r\n"); //$NON-NLS-1$

		sentRequest = true;

		if (os == null) {
			readServerResponse();
		}
	}

	/**
	 * Sets the version of HTTP this connection uses for communication with
	 * servers. HTTP/1.1 is used by default.
	 *
	 * @param version the version of HTTP this connection uses for
	 * communication with servers
	 * @see #getHttpVersion()
	 */
	public void setHttpVersion(double version) {
		Assert.isTrue(version == 1.0 || version == 1.1);
		endRequest();
		httpVersion = version;
	}

	/**
	 * Sets a boolean indicating whether this connection should remain open
	 * after each request.
	 *
	 * @param close a boolean indicating whether this connection should
	 * remain open after each request
	 * @see #getPersistent()
	 */
	public void setPersistent(boolean close) {
		endRequest();
		persistent = close;
	}

	/**
	 * Sets the <code>URL</code> of the proxy server this connection uses to
	 * communicate with the origin server. If <code>null</code> is given, no
	 * proxy server is used.
	 *
	 * @param proxyServerUrl the <code>URL</code> of a proxy server
	 * @see #getProxyServerUrl()
	 */
	public void setProxyServerUrl(URL proxyServerUrl) {
		endRequest();
		if (proxyServerUrl == null && this.proxyServerUrl == null)
			return;
		boolean closeConnection = true;

		if (proxyServerUrl != null && this.proxyServerUrl != null) {
			URL oldProxyServerUrl = null;
			URL newProxyServerUrl = null;

			try {
				oldProxyServerUrl = new URL(this.proxyServerUrl.getProtocol(), this.proxyServerUrl.getHost(), this.proxyServerUrl.getPort(), "/"); //$NON-NLS-1$
				newProxyServerUrl = new URL(proxyServerUrl.getProtocol(), proxyServerUrl.getHost(), proxyServerUrl.getPort(), "/"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// ignore or log?
			}

			if (oldProxyServerUrl.equals(newProxyServerUrl)) {
				closeConnection = false;
			}
		}

		if (closeConnection) {
			try {
				close();
			} catch (IOException e) {
				// ignore or log?
			}
		}

		this.proxyServerUrl = proxyServerUrl;
	}

	/**
	 * @see Socket#setReceiveBufferSize(int)
	 * @see #getReceiveBufferSize()
	 */
	public void setReceiveBufferSize(int size) throws IOException {
		Assert.isTrue(size > 0);
		if (size != getReceiveBufferSize()) {
			receiveBufferSize = size;
			if (connected) {
				socket.setReceiveBufferSize(receiveBufferSize);
			}
		}
	}

	/**
	 * Sets the request header value associated with the given field.
	 *
	 * @param fieldName the request header field
	 * @param fieldValue the request header value
	 * @see #getRequestHeaderFieldValue(String)
	 */
	public void setRequestHeaderField(String fieldName, String fieldValue) {
		Assert.isNotNull(fieldName);
		Assert.isNotNull(fieldValue);
		endRequest();
		requestHeader.addField(fieldName, fieldValue);
	}

	/**
	 * Sets the request method. "GET" is used by default.
	 *
	 * @param method the request method
	 * @see #getRequestMethod()
	 */
	public void setRequestMethod(String method) {
		Assert.isNotNull(method);
		endRequest();
		this.method = method;
	}

	/**
	 * Sets the <code>URL</code> of this connection's resource.
	 *
	 * @param resourceUrl the <code>URL</code> of this connection's resource
	 * @see #getResourceUrl()
	 */
	public void setResourceUrl(URL resourceUrl) {
		Assert.isNotNull(resourceUrl);
		endRequest();
		URL oldOriginServerUrl = null;
		URL newOriginServerUrl = null;
		try {
			oldOriginServerUrl = new URL(this.resourceUrl.getProtocol(), this.resourceUrl.getHost(), this.resourceUrl.getPort(), "/"); //$NON-NLS-1$
			newOriginServerUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(), resourceUrl.getPort(), "/"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// ignore?
		}
		if (!oldOriginServerUrl.equals(newOriginServerUrl)) {
			try {
				close();
			} catch (IOException e) {
				// ignore?
			}
		}
		this.resourceUrl = resourceUrl;
	}

	/**
	 * @see Socket#setSendBufferSize(int)
	 * @see #getSendBufferSize()
	 */
	public void setSendBufferSize(int size) throws IOException {
		Assert.isTrue(size > 0);
		if (size != getSendBufferSize()) {
			sendBufferSize = size;
			if (connected) {
				socket.setSendBufferSize(sendBufferSize);
			}
		}
	}

	/**
	 * Sets a boolean indicating whether the request's body should be sent
	 * chunked encoded.
	 *
	 * @param chunked a boolean indicating whether the request's body should
	 * be sent chunked encoded
	 * @see #getSendChunked()
	 */
	public void setSendChunked(boolean chunked) {
		endRequest();
		sendChunked = chunked;
	}

	/**
	 * Sets the factory this connection uses to create sockets. If the given
	 * socket factory is <code>null</code> the default socket is used.
	 *
	 * @param socketFactory the factory this connection uses to create
	 * sockets
	 */
	public void setSocketFactory(ISocketFactory socketFactory) {
		endRequest();
		if (socketFactory == this.socketFactory)
			return;
		try {
			close();
		} catch (IOException e) {
			// ignore?
		}
		this.socketFactory = socketFactory;
	}

	/**
	 * @see Socket#setSoLinger(boolean, int)
	 * @see #getSoLinger()
	 */
	public void setSoLinger(boolean on, int linger) throws IOException {
		Assert.isTrue(linger >= 0);
		if (!on && soLinger != -1 || on && linger != soLinger) {
			soLinger = on ? linger : -1;
			if (connected) {
				socket.setSoLinger(on, linger);
			}
		}
	}

	/**
	 * @see Socket#setSoTimeout(int)
	 * @see #getSoTimeout()
	 */
	public void setSoTimeout(int timeout) throws IOException {
		Assert.isTrue(timeout >= 0);
		if (timeout != soTimeout) {
			soTimeout = timeout;
			if (connected) {
				socket.setSoTimeout(soTimeout);
			}
		}
	}

	/**
	 * @see Socket#setTcpNoDelay(boolean)
	 * @see #getTcpNoDelay()
	 */
	public void setTcpNoDelay(boolean on) throws IOException {
		if (on != tcpNoDelay) {
			tcpNoDelay = on;
			if (connected) {
				socket.setTcpNoDelay(tcpNoDelay);
			}
		}
	}

	/**
	 * Sets this connection's timestamp.
	 *
	 * @param date this connection's timestamp
	 * @see #getTimestamp()
	 */
	public void setTimestamp(Date date) {
		timestamp = date;
	}

	private void writeHeader(Header header) throws IOException {
		for (int i = 0; i < header.size(); ++i) {
			String fieldName = header.getFieldName(i);
			String fieldValue = header.getFieldValue(fieldName);
			output(socketOut, fieldName);
			output(socketOut, ": "); //$NON-NLS-1$
			output(socketOut, fieldValue);
			output(socketOut, "\r\n"); //$NON-NLS-1$
		}
	}
}
