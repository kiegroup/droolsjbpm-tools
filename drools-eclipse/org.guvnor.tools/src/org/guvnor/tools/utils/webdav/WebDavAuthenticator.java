/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.tools.utils.webdav;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.webdav.http.client.IAuthenticator;

/**
 * A WebDav authenticator based on the Eclipse platform key-ring file.
 */
public class WebDavAuthenticator implements IAuthenticator {

    private URL repLocation;

    public WebDavAuthenticator() {
    }

    public WebDavAuthenticator(URL serverUrl) {
        this();
        repLocation = serverUrl;
    }

    @SuppressWarnings("unchecked")
    public void addAuthenticationInfo(URL serverUrl,
                                     String realm,
                                     String scheme,
                                     Map info) {
        // Not storing any local authentication information: using the platform's key ring
    }

    public void addProtectionSpace(URL resourceUrl,
                                  String realm) {
        // Not using the notion of "realm," so do nothing
    }

    @SuppressWarnings("unchecked")
    public Map getAuthenticationInfo(URL serverUrl,
                                    String realm,
                                    String scheme) {
        return Platform.getAuthorizationInfo(repLocation, "", scheme); //$NON-NLS-1$
    }

    public String getProtectionSpace(URL resourceUrl) {
        // We don't have the notion of "realm," but the client
        // requires a non-null return value.
        return ""; //$NON-NLS-1$
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
