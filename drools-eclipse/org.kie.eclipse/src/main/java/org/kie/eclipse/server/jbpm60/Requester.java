/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.eclipse.server.jbpm60;

import java.io.IOException;

import org.kie.eclipse.server.KieServiceDelegate;

import static org.kie.eclipse.server.IKieServiceDelegate.JOB_STATUS_SUCCESS;

public class Requester {

    private final KieServiceDelegate kieService;

    public Requester(final KieServiceDelegate kieService) {
        this.kieService = kieService;
    }

    public void run(final String title, final Action action) throws IOException, JobFailedException {

        String jobId = null;
        try {
            jobId = action.execute();
            final String status = kieService.getJobStatus(jobId, title);

            if (status == null) {
                throw new IOException(title + " has timed out");
            }
            if (!status.startsWith(JOB_STATUS_SUCCESS)) {
                throw new IOException(title + " has failed with status " + status);
            }
        } catch (final InterruptedException e) {
            throw new JobFailedException(jobId, e);
        }
    }

    public interface Action {

        String execute() throws IOException;
    }

    public interface FailAction {
        void execute(final JobFailedException e) throws IOException;

    }
}
