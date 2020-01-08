package com.hw.transmitlayer.service.client;

import org.apache.livy.sessions.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理session store用的，同时
 * 同步管理里面的store状态，以同步使用
 */
public class RHttpClientSessionStoreManager extends SessionHeartbeatMan{
    private static Logger LOG = LoggerFactory.getLogger(RHttpClientSessionStoreManager.class);
//    private final ConcurrentHashMap<Integer, RHttpClientSessionStore> storeMap;
    private final Vector<RHttpClientSessionStore> storeList = new Vector<>();
    // 目前读写次数暂时还不太确定
    private final Lock LOCK = new ReentrantLock(true);
    private final Condition done = LOCK.newCondition();
    private final AtomicInteger writeCount  = new AtomicInteger(0); // 写入store的次数 测试用

    private final AtomicInteger readCount = new AtomicInteger(0) ; // 测试用

    private final int sessionCoreSize ;
    private final int sessionMaxSize ;
    private boolean test = false;


    public RHttpClientSessionStoreManager( RHttpClientSessionStore storeList,RHttpConf rHttpConf, RLivyConnection connection) {
        this(storeList, rHttpConf, connection,false);
    }
    public RHttpClientSessionStoreManager( RHttpClientSessionStore storeList,RHttpConf rHttpConf, RLivyConnection connection, Boolean test) {
        super(rHttpConf, connection);
        this.test = test;
        this.sessionCoreSize = rHttpConf.getInt(RHttpConf.Entry.CONNECTION_SESSION_CORE_SIZE);
        this.sessionMaxSize = rHttpConf.getInt(RHttpConf.Entry.CONNECTION_SESSION_MAX_SIZE);
        register(storeList);
    }

    public void register(RHttpClientSessionStore storeList){
        LOCK.lock();
        try {
            if (storeList != null) {
                writeCount.getAndIncrement();
                this.storeList.add(storeList);
                // 触发并唤醒一个读的线程
                done.signalAll();
            }
        }finally {
            LOCK.unlock();
        }
    }

    public void remove(RHttpClientSessionStore store) {
        LOCK.lock();
        try {
        }finally {
            LOCK.unlock();
        }
    }

    /**
     * 获取一个可用的session 用来提供client使用
     * @return
     */
    public RHttpClientSessionStore getAvailableStore() {
        RHttpClientSessionStore store = null;
        readCount.getAndIncrement();
        LOCK.lock();
        try {
            while (true) {
                LOG.info("当前的sessionstore状态:" + Thread.currentThread().getName());
                for (RHttpClientSessionStore sessionStore : storeList) {

                    if (sessionStore.getState() == MyMessage.SessionState.IDLE.getKey()) {
                        store = sessionStore;
                        break;
                    }
                }
                if(store!=null){
                    break;
                }
                LOG.info("循环获取读取的任务列表");
                // 当前默认最多1秒中，重新唤醒
                done.await(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
            return  store;
        }
    }

//    public int storeSize(){
//
//        return storeList.size();
//    }


    private void logCount() {
        int writeCountL = writeCount.get();
        int readCountL = readCount.get();
        LOG.info("当前 读次数"+ readCountL +": ===== 写次数" + writeCountL );
    }

    /**
     * 周期性更新storeList
     */
    @Override
    public void updateSessionState() {
        logCount();
        LOCK.lock();
        try {
            LOG.info("开始执行后台任务：" + test);
            for (RHttpClientSessionStore store : storeList) {
                if(test){
                    // 当前测试使用
                    callMockRemoteAndUpdateState(store);
                } else {
                    // 远程连接
                    callRemoteAndUpdateState(store);
                }
            }
            // 获取当前
        } finally {
            LOCK.unlock();
        }

    }

    @Override
    protected void removeUnAvailableSession() {
        LOCK.lock();
        try{
            LOG.info("开始清理session：" + test);
            int size = storeList.size();
            ArrayList errorStore = new ArrayList();
            for (int i = 0; i < size; i++) {
                RHttpClientSessionStore store = storeList.get(i);
                if(store.getState() == MyMessage.SessionState.ERROR.getKey()
                        ||
                        store.getState() == MyMessage.SessionState.DEAD.getKey()
                        ||
                        store.getState() == MyMessage.SessionState.KILL.getKey()
                        ||
                        store.getState() == MyMessage.SessionState.SHUTTINGDOWN.getKey()
                        ){ // 当远程机子状态有问题 的时候就直接删除
                    errorStore.add(store);
                }
            }
            errorStore.forEach(store -> {
                storeList.remove(store);
            });

            // 保证池子数量
//            valitileAvailable

        }finally {
            LOCK.unlock();
        }
    }

    /**
     * 模拟使用
     * @param store
     */
    private void callMockRemoteAndUpdateState(RHttpClientSessionStore store){
        Random random = new Random();
        // 0-5 的整数
        int i = random.nextInt(5);
        String idleState = MyMessage.SessionState.IDLE.getKey();
        String noteStartedState = MyMessage.SessionState.NOTSTARTED.getKey();
        String busy = MyMessage.SessionState.BUSY.getKey();
        String dead = MyMessage.SessionState.DEAD.getKey();
        String error = MyMessage.SessionState.ERROR.getKey();

        List<String> stateList = new ArrayList<String>(){{
            add(idleState);
            add(noteStartedState);
            add(busy);
            add(dead);
            add(error);
        }};

        MyMessage.SessionSateResultMessage message
                = new MyMessage.SessionSateResultMessage(
                        store.getSessionid(), stateList.get(i));
        String state = message.getState();
        System.out.println(store.getSessionid() + " from state" + store.getState() +": update to state:" + state);
        store.setState(state);
    }
    private void callRemoteAndUpdateState(RHttpClientSessionStore store){
//        this.connection.get()
        try {
            MyMessage.SessionSateResultMessage sessionSateResultMessage = this.connection.get(MyMessage.SessionSateResultMessage.class, MyMessage.SESSION_STATE_URI, store.getSessionid());

        } catch (Exception e){

        }
    }

    /**
     *
     */
    private void valitileAvailable(){
//        Executors.newFixedThreadPool()
    }

}
