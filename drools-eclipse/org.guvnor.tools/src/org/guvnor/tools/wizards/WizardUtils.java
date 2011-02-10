/*
 * Copyright 2010 JBoss Inc
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

package org.guvnor.tools.wizards;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.utils.webdav.WebDavSessionAuthenticator;

/**
 * Utilities for the Guvnor wizards.
 * @author jgraham
 */
public class WizardUtils {
    public static void createGuvnorRepository(GuvWizardModel model) throws Exception {
        Activator.getLocationManager().addRepository(new GuvnorRepository(model.getRepLocation()));
        URL serverUrl = new URL(model.getRepLocation());
        Map<String, String> info = new HashMap<String, String>();
        info.put("username", model.getUsername()); //$NON-NLS-1$
        info.put("password", model.getPassword()); //$NON-NLS-1$
        if (model.shouldSaveAuthInfo()) {
            Platform.addAuthorizationInfo(serverUrl, "", "basic", info);     //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            IWebDavClient client = WebDavClientFactory.createClient(serverUrl);
            WebDavServerCache.cacheWebDavClient(serverUrl.toString(), client);
            WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
            authen.addAuthenticationInfo(serverUrl, "", "basic", info); //$NON-NLS-1$ //$NON-NLS-2$
            client.setSessionAuthenticator(authen);
        }
    }
}
