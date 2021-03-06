package com.hw.transmitlayer.client.handler;

import com.hw.transmitlayer.client.*;

import org.apache.livy.client.common.AbstractJobHandle;
import org.apache.livy.client.common.HttpMessages;
import org.apache.livy.rsc.driver.StatementState;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestStatementJobHandlerImpl<T> extends AbstractJobHandle<T> {
    private final RLivyConnection connection;
    private final ScheduledExecutorService executors;
//    private final ConcurrentHashMap<Integer, RHttpClientSessionStore> storeMap;
    private final RHttpClientSessionStoreManager storeManager;
    private final Lock LOCK = new ReentrantLock();
    private final Condition done = LOCK.newCondition();
    private int jobId; // 当前任务处理器处理的jobId
    private RHttpClientSessionStore availableStore;// 当前处理statement处理的状态是否一致
    private volatile StatementState statementState; //  服务端当前返回的state信息 初始化的时候是空的
    private volatile T result;
    private Throwable error;
    private volatile boolean isDone;
    private final long initialPollInterval;
    private final long maxPollInterval;

//    private final Integer sessionid;
    public RequestStatementJobHandlerImpl(RLivyConnection connection, ScheduledExecutorService executors,RHttpClientSessionStoreManager storeManager,RHttpConf rHttpConf) {
        this.connection = connection;
        this.executors = executors;
        this.storeManager = storeManager;
//        Set<Map.Entry<Integer, ClienSessionStore>> entries = store.entrySet();
//        this.sessionid = null;
        this.isDone = false; // 初始化是没有做好的
        this.initialPollInterval = rHttpConf.getTimeAsMs(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL);
        long maxPollInterval = rHttpConf.getTimeAsMs(RHttpConf.Entry.JOB_MAX_POLL_INTERVAL);
        if(this.initialPollInterval > maxPollInterval) {
            throw new UnsupportedOperationException("初始间隔时长:"+ initialPollInterval+ "ms > 大于 最大间隔时长:" + maxPollInterval + "ms");
        }
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
            // 堵塞并休眠获取县官的hu够
            availableStore = storeManager.getAvailableStore();
            MyMessage.StatementResultWithCode result = this.connection.post(message,
                    MyMessage.StatementResultWithCode.class,
                    MyMessage.CODESTATEMENTFORMAT, availableStore.getSessionid());
            jobId = result.id;
            this.executors.schedule(new JobCodePollTaskLoop(initialPollInterval),
                    initialPollInterval, TimeUnit.MILLISECONDS );

        } catch (Exception e) {
            setResult(null, e, StatementState.Cancelled);
        } finally {
            LOCK.unlock();
        }
    }
    /**
     * 传递code片段，以开始轮询方式获取数据，间隔时长为
     * 提供给用户自定义result内容,传递一个对象，用来读取数
     * @param message
     */
    public void start(HttpMessages.ClientMessage message,MyMessage.StatementResultWithCode resultClass){
        LOCK.lock();// 加锁并休眠,为了使得数据能够正常的获取
        try {
            // 堵塞并休眠获取县官的hu够
            availableStore = storeManager.getAvailableStore();
            MyMessage.StatementResultWithCode result = this.connection.post(message,
                    resultClass.getClass(),
                    MyMessage.CODESTATEMENTFORMAT, availableStore.getSessionid());
            jobId = result.id;
            this.executors.schedule(new JobCodePollTaskLoop(initialPollInterval, resultClass),
                    initialPollInterval, TimeUnit.MILLISECONDS );

        } catch (Exception e) {
            setResult(null, e, StatementState.Cancelled);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 触发回调给listener,只能i被一个线程成功调用
     * @param result
     * @param error
     * @param newState
     */
    private void setResult(T result,Throwable error, StatementState newState){
        // 关门
        if(!isDone){
            LOCK.lock();
            try{
                if(!isDone){
                    this.isDone = true;
                    this.result = result;
                    this.error = error;
//                    if(state == State.valueOf())
                    transStatementState2JobState(newState);
                }
            }finally {
                LOCK.unlock();
            }

        }
    }

    /**
     * 把statement状态转换为job
     * @param newState
     */
    public void transStatementState2JobState(StatementState newState){
        if(newState.equals(StatementState.Available)){
            // 因为继承livy中的AbstractJobHandle接口，所以需要对statement就进行转换
//                        changeState(State.QUEUED);
            changeState(State.SUCCEEDED);
        }else if(newState.equals(StatementState.Cancelled)
                || newState.equals(StatementState.Cancelling)
                ) {
            changeState(State.FAILED);
        } else if (newState.equals(StatementState.Waiting) ||
                newState.equals(StatementState.Running)) {
            // 等待中
            changeState(State.QUEUED);
        } else {
            changeState(State.FAILED);
        }
    }


    public class JobCodePollTaskLoop implements Runnable {
        private long currentInterval;
        private MyMessage.StatementResultWithCode resultClass;

        public JobCodePollTaskLoop(long currentInterval) {
           this.currentInterval = currentInterval;
        }
        public JobCodePollTaskLoop(long currentInterval, MyMessage.StatementResultWithCode resultClass){
            this(currentInterval);
            this.resultClass = resultClass;
        }

        @Override
        public void run() {
            try{
                MyMessage.StatementResultWithCode resultWithCode = null;
                if(this.resultClass != null) {
                    resultWithCode = connection.get(this.resultClass.getClass(), MyMessage.CODESTATEMENTFORMAT_GET, availableStore.getSessionid(), jobId);
                } else{
                    resultWithCode = connection.get(MyMessage.StatementResultWithCode.class, MyMessage.CODESTATEMENTFORMAT_GET, availableStore.getSessionid(), jobId);
                }

                boolean finished = false;
                // 当片段返回的进度为1，或者当前状态为Available的时候就是结束
                if(resultWithCode.progress>=1.0 || resultWithCode.state.equals(StatementState.Available)) {
                    finished = true;
                }
                // 没有完成的情况，目前只有waiting的状态的售后
                if (!finished) {
//                    if(state!=){
//                        changeState(resultWithCode.state);
//                    }
                    transStatementState2JobState(resultWithCode.state);
                    // 下次执行每次以2的倍数于先前的间隔时间而增加，最高不超过设置的最大间隔时间
                    currentInterval =  Math.min(maxPollInterval,currentInterval * 2);
                    executors.schedule(this, currentInterval, TimeUnit.MILLISECONDS);
                } else {
                    setResult((T) resultWithCode, null, StatementState.Available);
                }
            }catch (Exception e){
                // 出现网络异常
                e.printStackTrace();
                setResult(null, e,StatementState.Cancelled);
            }
        }
    }


}
