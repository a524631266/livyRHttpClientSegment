package com.hw.transmitlayer.service.client;

import org.apache.livy.sessions.SessionState;
/**
 *
 */
public class ClienSessionStore {
    private final String state;

    public ClienSessionStore() {
        this(SessionState.NotStarted.toString());
    }
    public ClienSessionStore(String state) {
        this.state = state;
    }

//    public enum State {
//        INIT,RUNNGING,SHUTDOWN,SECCUCESS;
//    }
}
