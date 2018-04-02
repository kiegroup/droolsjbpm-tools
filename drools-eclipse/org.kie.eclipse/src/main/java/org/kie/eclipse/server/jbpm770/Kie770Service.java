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

package org.kie.eclipse.server.jbpm770;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eclipsesource.json.JsonArray;
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

public class Kie770Service extends KieServiceDelegate {

    private final Requester requester;

    public Kie770Service() {
        requester = new Requester(this);
    }

    @Override
    public List<IKieOrganizationHandler> getOrganizations(final IKieServerHandler server) throws IOException {
        final List<IKieOrganizationHandler> ret = new ArrayList<IKieOrganizationHandler>();

        for (final JsonValue spaceJson : httpGetOrganizations()) {
            ret.add(newKieOrganizationHandler(server, spaceJson.asObject()));
        }

        return ret;
    }

    @Override
    public List<IKieRepositoryHandler> getRepositories(final IKieServerHandler server) throws IOException {
        final List<IKieRepositoryHandler> ret = new ArrayList<IKieRepositoryHandler>();

        for (final JsonValue spaceJson : httpGetOrganizations()) {
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

        for (final JsonValue projectJson : httpGetRepositories(organization)) {
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

    @Override
    protected JsonArray httpGetOrganizations() throws IOException {
        return JsonArray.readFrom(httpGet("spaces/"));
    }

    @Override
    protected JsonArray httpGetRepositories(final IKieOrganizationHandler organizationHandler) throws IOException {
        return JsonArray.readFrom(httpGet("spaces/" + organizationHandler.getName() + "/projects"));
    }

    @Override
    public void createOrganization(final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to create organization '" + organization.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/", organization.getProperties());
            }
        });
    }

    @Override
    public void deleteOrganization(final IKieOrganizationHandler organization) throws IOException {
        runJob("Request to delete organization '" + organization.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpDelete("spaces/" + organization.getName());
            }
        });
    }

    @Override
    public void updateOrganization(final String oldName, final IKieOrganizationHandler organization) throws IOException {
        //Updating organization properties is not supported yet.
    }

    //Repository

    @Override
    public void createRepository(final IKieRepositoryHandler repository) throws IOException {
        runJob("Request to create repository '" + repository.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/" + repository.getParent().getName() + "/projects/", repository.getProperties());
            }
        });
    }

    public void addRepository(final IKieRepositoryHandler repository, final IKieOrganizationHandler organization) throws IOException {
        //Adding a repository is not supported yet.
    }

    @Override
    public void deleteRepository(final IKieRepositoryHandler repository, final boolean removeOnly) throws IOException {
        final String spaceName = repository.getParent().getName();
        runJob("Request to delete repository '" + repository.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpDelete("spaces/" + spaceName + "/projects/" + repository.getName());
            }
        });
    }

    @Override
    public void createProject(final IKieProjectHandler project) throws IOException {
        //Creating a project is the same as creating a Repository.
    }

    @Override
    public void deleteProject(final IKieProjectHandler project) throws IOException {
        //Deleting a project is the same as deleting a Repository.
    }

    public void mavenCompile(final IKieProjectHandler project, final JsonObject params) throws IOException {
        final String spaceName = project.getParent().getParent().getName();
        runJob("Request to compile Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/" + spaceName + "/projects/" + project.getName() + "/maven/compile/", params);
            }
        });
    }

    public void mavenInstall(final IKieProjectHandler project, final JsonObject params) throws IOException {
        final String spaceName = project.getParent().getParent().getName();
        runJob("Request to install Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/" + spaceName + "/projects/" + project.getName() + "/maven/install/", params);
            }
        });
    }

    public void mavenTest(final IKieProjectHandler project, final JsonObject params) throws IOException {
        final String spaceName = project.getParent().getParent().getName();
        runJob("Request to test Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/" + spaceName + "/projects/" + project.getName() + "/maven/test/", params);
            }
        });
    }

    public void mavenDeploy(final IKieProjectHandler project, final JsonObject params) throws IOException {
        final String spaceName = project.getParent().getParent().getName();
        runJob("Request to deploy Project '" + project.getName() + "'", new Requester.Action() {
            @Override
            public String execute() throws IOException {
                return httpPost("spaces/" + spaceName + "/projects/" + project.getName() + "/maven/deploy/", params);
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
