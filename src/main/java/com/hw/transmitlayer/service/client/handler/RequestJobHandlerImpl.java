package com.hw.transmitlayer.service.client.handler;

import com.hw.transmitlayer.service.client.ClienSessionStore;
import com.hw.transmitlayer.service.client.MyMessage;
import com.hw.transmitlayer.service.client.RLivyConnection;
import org.apache.livy.client.common.AbstractJobHandle;
import org.apache.livy.sessions.SessionState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class RequestJobHandlerImpl<T> extends AbstractJobHandle<T> {
    private final RLivyConnection connection;
    private final ExecutorService executors;
    private final ConcurrentHashMap<Integer, ClienSessionStore> store;
    private final Integer sessionid;
    public RequestJobHandlerImpl(RLivyConnection connection, ExecutorService executors, ConcurrentHashMap<Integer, ClienSessionStore> store) {
        this.connection = connection;
        this.executors = executors;
        this.store = store;
        Set<Map.Entry<Integer, ClienSessionStore>> entries = store.entrySet();
        this.sessionid = null;

    }


    @Override
    protected T result() {
        return null;
    }

    @Override
    protected Throwable error() {
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    public void start(String postObject, MyMessage.ResultWithCode code) {

    }
    public static void main(String[] args) {
        ConcurrentHashMap<Integer, ClienSessionStore> store = new ConcurrentHashMap<>();
        store.put(1, new ClienSessionStore());
        store.put(2, new ClienSessionStore(SessionState.Idle.toString()));
        store.forEach((key, value)-> {
            System.out.println(";key" +key);
            System.out.println("value:" + value);
        });
    }

}
