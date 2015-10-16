/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.eclipse.webdav.internal.kernel;

public class LockToken {

    private String fToken = null;

    public LockToken(String token) {
        fToken = token;
    }

    public String getToken() {
        return fToken;
    }

    public void setToken(String token) {
        fToken = token;
    }

    public String toString() {
        return fToken;
    }
}
