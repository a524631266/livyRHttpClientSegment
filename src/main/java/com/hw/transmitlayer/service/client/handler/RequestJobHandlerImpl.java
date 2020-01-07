package com.hw.transmitlayer.service.client.handler;

import com.hw.transmitlayer.service.client.*;
import org.apache.livy.client.common.AbstractJobHandle;
import org.apache.livy.client.common.HttpMessages;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestJobHandlerImpl<T> extends AbstractJobHandle<T> {
    private final RLivyConnection connection;
    private final ScheduledExecutorService executors;
//    private final ConcurrentHashMap<Integer, RHttpClientSessionStore> storeMap;
    private final RHttpClientSessionStoreManager storeManager;
    private final Lock LOCK = new ReentrantLock();
    private final Condition done = LOCK.newCondition();
    private int jobId; // 当前任务处理器处理的jobId
    private RHttpClientSessionStore availableStore;// 当前处理句柄所使用的
    private T result;
    private Throwable error;
    private volatile boolean isDone;
    private final long initialPollInterval;
    private final long maxPollInterval;

//    private final Integer sessionid;
    public RequestJobHandlerImpl(RLivyConnection connection, ScheduledExecutorService executors,RHttpClientSessionStoreManager storeManager,RHttpConf rHttpConf) {
        this.connection = connection;
        this.executors = executors;
        this.storeManager = storeManager;
//        Set<Map.Entry<Integer, ClienSessionStore>> entries = store.entrySet();
//        this.sessionid = null;
        this.isDone = false; // 初始化是没有做好的
        this.initialPollInterval = rHttpConf.getTimeAsMs(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL);
        long maxPollInterval = rHttpConf.getTimeAsMs(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL);
        this.maxPollInterval = maxPollInterval;
    }

    /**
     * 方便获取结果
     * @return
     */
    @Override
    protected T result() {
        return result;
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

    /**
     * 传递code片段，以开始轮询方式获取数据，间隔时长为
     *
     * @param message
     */
    public void start(HttpMessages.ClientMessage message){
        LOCK.lock();// 加锁并休眠,为了使得数据能够正常的获取
        try {
            // 堵塞吗
            availableStore = storeManager.getAvailableStore();
            MyMessage.ResultWithCode result = this.connection.post(message, MyMessage.ResultWithCode.class, MyMessage.CODESTATEMENTFORMAT, availableStore.getSessionid());
            jobId = result.id;
            this.executors.schedule(new JobCodePollTaskLoop(initialPollInterval), initialPollInterval, TimeUnit.MILLISECONDS );

        } catch (Exception e) {
            setResult(null, e, State.FAILED);
        } finally {
            LOCK.unlock();
        }
    }

    private void setResult(T result,Throwable error, State newState){
        if(!isDone){
            LOCK.lock();
            try{
                if(!isDone){
                    this.isDone = true;
                    this.result = result;
                    this.error = error;
                    changeState(newState); // 触发回调任务
                }
            }finally {
                LOCK.unlock();
            }

        }
    }

    public class JobCodePollTaskLoop implements Runnable {
        private final long currentInterval;

        public JobCodePollTaskLoop(long currentInterval) {
           this.currentInterval = currentInterval;
        }

        @Override
        public void run() {
            try{
                MyMessage.ResultWithCode resultWithCode = connection.get(MyMessage.ResultWithCode.class, MyMessage.CODESTATEMENTFORMAT_GET, availableStore.getSessionid(), jobId);
                boolean finished = false;
                if(resultWithCode.progress>=1.0) {
                    finished = true;
                }
                if (!finished) {
//                    if(state!=){
//                        changeState(resultWithCode.state);
//                    }
                    executors.schedule(this, currentInterval, TimeUnit.MILLISECONDS);
                } else {
                    setResult((T) resultWithCode, null, State.SUCCEEDED);
                }
            }catch (Exception e){
                setResult(null, e, State.FAILED);
            }
        }
    }


}
