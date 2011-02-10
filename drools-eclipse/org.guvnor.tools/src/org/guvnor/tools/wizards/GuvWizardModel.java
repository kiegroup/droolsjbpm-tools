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

import java.util.List;

/**
 * Container for Guvnor connection details.
 * @author jgraham
 */
public class GuvWizardModel {
    private String repLocation;
    private String username;
    private String password;
    private boolean createNewRep;
    private boolean saveAuthInfo;

    private String targetLocation;

    private List<String> resources;
    private String version;

    public String getRepLocation() {
        return repLocation;
    }
    public void setRepLocation(String repLocation) {
        this.repLocation = repLocation;
    }
    public String getUsername() {
        return username != null?username:""; //$NON-NLS-1$
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password != null?password:""; //$NON-NLS-1$
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean shouldCreateNewRep() {
        return createNewRep;
    }
    public void setCreateNewRep(boolean createNewRep) {
        this.createNewRep = createNewRep;
    }
    public String getTargetLocation() {
        return targetLocation;
    }
    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }
    public boolean shouldSaveAuthInfo() {
        return saveAuthInfo;
    }
    public void setSaveAuthInfo(boolean saveAuthInfo) {
        this.saveAuthInfo = saveAuthInfo;
    }
    public List<String> getResources() {
        return resources;
    }
    public void setResources(List<String> resources) {
        this.resources = resources;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
