/**
 * 
 */
package org.drools.eclipse.task.views;

import org.drools.task.service.BaseMinaHandler.ResponseHandler;

public abstract class AbstractBlockingResponseHandler
    implements
    ResponseHandler {
    protected volatile Boolean done = Boolean.FALSE;
    private String error;
    
    public boolean hasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isDone() {
        synchronized ( done ) {
            return done;
        }
    }

    public boolean waitTillDone(long time) {
        long totalWaitTime = 0;
        try {
            while ( true ) {
                synchronized ( done ) {
                    if ( done ) {
                        return true;
                    }
                }
                if ( totalWaitTime >= time ) {
                    break;
                }
                Thread.sleep( 250 );
                totalWaitTime += 250;
            }
        } catch ( Exception e ) {
            // swallow, as we are either true or false
        }
        return false;
    }

}