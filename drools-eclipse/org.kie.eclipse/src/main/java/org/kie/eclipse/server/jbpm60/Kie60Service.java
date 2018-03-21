/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013, 2014 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 ******************************************************************************/

package org.kie.eclipse.server.jbpm60;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.KieOrganizationHandler;
import org.kie.eclipse.server.KieProjectHandler;
import org.kie.eclipse.server.KieRepositoryHandler;
import org.kie.eclipse.server.KieServiceDelegate;

public class Kie60Service extends KieServiceDelegate {

    private final Requester requester;

    public Kie60Service() {
        requester = new Requester(this);
    }

    @Override
    public List<IKieOrganizationHandler> getOrganizations(final IKieServerHandler server) throws IOException {
        final List<IKieOrganizationHandler> ret = new ArrayList<IKieOrganizationHandler>();

        for (final JsonValue spaceJson : httpGetSpaces()) {
            ret.add(newKieOrganizationHandler(server, spaceJson.asObject()));
        }

        return ret;
    }

    @Override
    public List<IKieRepositoryHandler> getRepositories(final IKieServerHandler server) throws IOException {
        final List<IKieRepositoryHandler> ret = new ArrayList<IKieRepositoryHandler>();

        for (final JsonValue spaceJson : httpGetSpaces()) {
            final IKieOrganizationHandler organization = newKieOrganizationHandler(server, spaceJson.asObject());
            for (final JsonValue projectJson : organization.getProperties().get("projects").asArray()) {
                ret.add(newKieRepositoryHandler(organization, projectJson.asObject()));
            }
        }

        return ret;
    }

    @Override
    public List<IKieRepositoryHandler> getRepositories(final IKieOrganizationHandler organization) throws IOException {
        final List<IKieRepositoryHandler> ret = new ArrayList<IKieRepositoryHandler>();

        for (final JsonValue projectJson : httpGetProjects(organization)) {
            ret.add(newKieRepositoryHandler(organization, projectJson.asObject()));
        }

        return ret;
    }

    @Override
    public List<IKieProjectHandler> getProjects(final IKieRepositoryHandler repository) {
        final IKieProjectHandler project = new KieProjectHandler(repository, repository.getName());
        project.setProperties(repository.getProperties());
        return new ArrayList<IKieProjectHandler>(Collections.singletonList(project));
    }

    private IKieOrganizationHandler newKieOrganizationHandler(final IKieServerHandler server, final JsonObject spaceJson) {
        final KieOrganizationHandler organization = new KieOrganizationHandler(server, spaceJson.get("name").asString());
        organization.setProperties(spaceJson);
        return organization;
    }

    private IKieRepositoryHandler newKieRepositoryHandler(final IKieOrganizationHandler organization, final JsonObject projectJson) {
        final KieRepositoryHandler repository = new KieRepositoryHandler(organization, projectJson.get("name").asString());
        repository.setProperties(projectJson);
        return repository;
    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    @Override
    public void createOrganization(final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to create organization '" + organization.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("organizationalunits", organization.getProperties());
            }
        });
    }

    @Override
    public void createRepository(final IKieRepositoryHandler repository) throws IOException {
        runJob("Request to create repository '" + repository.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories", repository.getProperties());
            }
        });
    }

    public void addRepository(final IKieRepositoryHandler repository, final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to add repository '" + repository.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("organizationalunits/" + organization.getName() + "/repositories/" + repository.getName(), null);
            }
        });
    }

    @Override
    public void createProject(final IKieProjectHandler project) throws IOException {
        runJob("Request to create project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories/" + project.getParent().getName() + "/projects/", project.getProperties());
            }
        });
    }

    @Override
    public void deleteOrganization(final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to delete organization '" + organization.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpDelete("organizationalunits/" + organization.getName());
            }
        });
    }

    @Override
    public void deleteRepository(final IKieRepositoryHandler repository, final boolean removeOnly) throws IOException {
        runJob("Request to delete repository '" + repository.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                if (removeOnly) {
                    final String organization = repository.getParent().getName(); // only remove the repo from its organizational unit
                    return httpDelete("organizationalunits/" + organization + "/repositories/" + repository.getName());
                } else {
                    return httpDelete("repositories/" + repository.getName()); // completely obliterate the repository
                }
            }
        });
    }

    @Override
    public void deleteProject(final IKieProjectHandler project) throws IOException {
        runJob("Request to delete project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpDelete("repositories/" + project.getParent().getName() + "/projects/" + project.getName());
            }
        });
    }

    @Override
    public void updateOrganization(final String oldName, final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to update Organization '" + organization.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                final JsonObject properties = new JsonObject(organization.getProperties());
                properties.remove("repositories"); // remove illegal properties
                return httpPost("organizationalunits/" + oldName, properties);
            }
        });
    }

    public void mavenCompile(final IKieProjectHandler project, final JsonObject params) throws IOException {
        runJob("Request to compile Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories/" + project.getParent().getName() + "/projects/maven/compile/", params);
            }
        });
    }

    public void mavenInstall(final IKieProjectHandler project, final JsonObject params) throws IOException {
        runJob("Request to install Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories/" + project.getParent().getName() + "/projects/maven/install/", params);
            }
        });
    }

    public void mavenTest(final IKieProjectHandler project, final JsonObject params) throws IOException {
        runJob("Request to test Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories/" + project.getParent().getName() + "/projects/maven/test/", params);
            }
        });
    }

    public void mavenDeploy(final IKieProjectHandler project, final JsonObject params) throws IOException {
        runJob("Request to deploy Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("repositories/" + project.getParent().getName() + "/projects/maven/deploy/", params);
            }
        });
    }

    private void runJob(final String title, final Requester.Action action) throws IOException {
        try {
            requester.run(title, action);
        } catch (final JobFailedException e) {
            deleteJob(e.getJobId());
        }
    }

    @Override
    public void updateProject(String oldName, IKieProjectHandler project) throws IOException {
        //Not implemented
    }

    @Override
    public void updateRepository(String oldName, IKieRepositoryHandler repository) throws IOException {
        //Not implemented
    }
}
