/**
 * 
 */
package org.drools.eclipse.task.views;

import org.drools.task.service.TaskClientHandler.TaskOperationResponseHandler;
import org.drools.task.service.responsehandlers.AbstractBlockingResponseHandler;

public class BlockingTaskOperationResponseHandler extends AbstractBlockingResponseHandler implements TaskOperationResponseHandler {
      public void setIsDone(boolean done) {
          setDone(done);
      }
}