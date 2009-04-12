package org.drools.eclipse.task.views;

import java.util.List;

import org.drools.task.query.TaskSummary;
import org.drools.task.service.TaskClientHandler.TaskSummaryResponseHandler;
import org.drools.task.service.responsehandlers.AbstractBlockingResponseHandler;

public class BlockingTaskSummaryResponseHandler extends AbstractBlockingResponseHandler implements TaskSummaryResponseHandler {
	private static final int DEFAULT_WAIT_TIME = 10000;

    private volatile List<TaskSummary> results;

    public synchronized void execute(List<TaskSummary> results) {
        this.results = results;
        setDone(true);
	}

	public synchronized List<TaskSummary> getResults() {
         // note that this method doesn't need to be synced because if waitTillDone returns true,
        // it means attachmentId is available
        boolean done = waitTillDone(DEFAULT_WAIT_TIME);

        if ( !done ) {
            throw new TimeoutException();
        }
        
        return results;
	}
}

