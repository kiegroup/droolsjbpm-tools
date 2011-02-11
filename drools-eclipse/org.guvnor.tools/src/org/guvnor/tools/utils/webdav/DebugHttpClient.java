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

package org.guvnor.tools.utils.webdav;

import java.io.IOException;

import org.eclipse.webdav.http.client.HttpClient;
import org.eclipse.webdav.http.client.Request;
import org.eclipse.webdav.http.client.Response;

/**
 * An intercept client that dumps debug information to System.out.
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
