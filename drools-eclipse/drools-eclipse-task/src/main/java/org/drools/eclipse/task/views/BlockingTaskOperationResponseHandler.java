/**
 * 
 */
package org.drools.eclipse.task.views;

import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;
import org.drools.task.service.TaskClientHandler.TaskOperationResponseHandler;

public class BlockingTaskOperationResponseHandler extends AbstractBlockingResponseHandler implements TaskOperationResponseHandler {
      public void setIsDone(boolean done) {
          synchronized ( this.done ) {
            this.done = done;
        }
      }
}