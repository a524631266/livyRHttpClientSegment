package com.hw.transmitlayer.service.client;

import java.net.URI;

/**
 *
 */
public class RHttpClientSessionStore {
    private final String state;
    private final int sessionid;
    private final URI uri_no_path;
    public RHttpClientSessionStore(int sessionid,URI uri_no_path) {
        this(sessionid,MyMessage.SessionState.NOTSTARTED.getKey(),uri_no_path);
    }

    public RHttpClientSessionStore(int sessionid, String state, URI uri_no_path) {
        this.sessionid = sessionid;
        this.state = state;
        this.uri_no_path = uri_no_path;
    }

    public String getState() {
        return state;
    }

    public int getSessionid() {
        return sessionid;
    }
    //    public enum State {
//        INIT,RUNNGING,SHUTDOWN,SECCUCESS;
//    }
}
