package com.hw.transmitlayer.client;

import java.net.URI;

/**
 * state:
 */
public class RHttpClientSessionStore {
    private MyMessage.SessionState state;
    private final int sessionid;
    private final URI uri_no_path;
    public RHttpClientSessionStore(int sessionid,URI uri_no_path) {
        this(sessionid,MyMessage.SessionState.not_started,uri_no_path);
    }

    public RHttpClientSessionStore(int sessionid, MyMessage.SessionState state, URI uri_no_path) {
        this.sessionid = sessionid;
        this.state = state;
        this.uri_no_path = uri_no_path;
    }

    public  MyMessage.SessionState  getState() {
        return state;
    }
    public void setState(MyMessage.SessionState state){
        this.state = state;
    }
    public int getSessionid() {
        return sessionid;
    }
    //    public enum State {
//        INIT,RUNNGING,SHUTDOWN,SECCUCESS;
//    }

    @Override
    public String toString() {
        return "RHttpClientSessionStore{" +
                "state='" + state + '\'' +
                ", sessionid=" + sessionid +
                ", uri_no_path=" + uri_no_path +
                '}';
    }
}
