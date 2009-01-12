package org.drools.eclipse.task.views;

import java.util.List;

import org.drools.task.query.TaskSummary;
import org.drools.task.service.TaskClientHandler.TaskSummaryResponseHandler;

public class BlockingTaskSummaryResponseHandler extends AbstractBlockingResponseHandler implements TaskSummaryResponseHandler {
	
	private volatile List<TaskSummary> results;

	public synchronized void execute(List<TaskSummary> results) {
        synchronized ( this.done ) {        
    		this.results = results;
            this.done = true;
            notifyAll(); 
        }
	}

	public synchronized List<TaskSummary> getResults() {
        boolean isDone;
        synchronized ( done ) {
            isDone = this.done;
        }
        if ( !isDone ) {                  
            try {
                wait( 10000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notification
            }
        }        
        synchronized ( done ) {
            isDone = this.done;
        }        
        if ( !isDone ) {
            throw new TimeoutException();
        }
        
        return results;
	}

};

