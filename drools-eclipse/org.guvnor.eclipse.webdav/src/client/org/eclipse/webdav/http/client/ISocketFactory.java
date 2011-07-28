package org.eclipse.webdav.http.client;

import java.io.IOException;
import java.net.Socket;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public interface ISocketFactory {
    public Socket createSocket(String protocol, String host, int port) throws IOException;
}
