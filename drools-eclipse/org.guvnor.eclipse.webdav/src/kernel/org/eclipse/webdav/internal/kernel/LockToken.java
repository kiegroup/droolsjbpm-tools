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
