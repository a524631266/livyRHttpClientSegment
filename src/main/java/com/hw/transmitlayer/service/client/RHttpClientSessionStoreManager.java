package com.hw.transmitlayer.service.client;

import org.apache.livy.client.common.HttpMessages;
import org.apache.livy.rsc.driver.StatementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    private final Map<Integer,RHttpClientSessionStore> storeMap= new HashMap<Integer,RHttpClientSessionStore>();
    // 目前读写次数暂时还不太确定
    private final Lock LOCK = new ReentrantLock(true);
    private final Condition done = LOCK.newCondition();
    private final AtomicInteger writeCount  = new AtomicInteger(0); // 写入store的次数 测试用

    private final AtomicInteger readCount = new AtomicInteger(0) ; // 测试用

    private final int sessionCoreSize ; // 最少session数量
    private final int sessionMaxSize ; // 最大session数量
    private boolean test = false;

    public Lock getLOCK() {
        return LOCK;
    }

    public RHttpClientSessionStoreManager(RHttpClientSessionStore store, RHttpConf rHttpConf, RLivyConnection connection, RHttpClient rHttpClient) {
        this(store, rHttpConf, connection,rHttpClient,false);
    }


    public RHttpClientSessionStoreManager( RHttpClientSessionStore store,RHttpConf rHttpConf, RLivyConnection connection,RHttpClient rHttpClient, Boolean test) {
        super(rHttpConf, connection,rHttpClient);
        this.test = test;
        this.sessionCoreSize = rHttpConf.getInt(RHttpConf.Entry.CONNECTION_SESSION_CORE_SIZE);
        this.sessionMaxSize = rHttpConf.getInt(RHttpConf.Entry.CONNECTION_SESSION_MAX_SIZE);
        register(store);
    }

    public void register(RHttpClientSessionStore store){
        LOCK.lock();
        try {
            if (store != null) {
                writeCount.getAndIncrement();
                this.storeMap.put(store.getSessionid(),store);
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
//                HttpMessages.SessionInfo[] sessionsInfo = getSessionsInfo();
////                updateSessionStoreList(sessionsInfo);
                // 每次获取数据的时候更新
                updateSessionState();

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
     * 周期性更新storeList,
     * 1. 不存在的话就删除
     * 2. 存在的话，就直接跟新任务
     */
    @Override
    public void updateSessionState() {
        logCount();
        LOCK.lock();
        try {
            LOG.info("开始执行后台任务：" + test);
            if(test){
                // 当前测试使用
                Collection<RHttpClientSessionStore> values = storeMap.values();
                for (RHttpClientSessionStore store : values) {
                    callMockRemoteAndUpdateState(store);
                }
            } else {
                // 远程连接 并更新状态,并删除无用状态
                callRemoteAndUpdateStateAndDeleteState();
            }
        } finally {
            LOCK.unlock();
        }

    }

    @Override
    protected void removeUnAvailableSession() {
        LOCK.lock();
//        try{
//            LOG.info("开始清理session：" + test);
//            int size = storeMap.size();
//            ArrayList errorStore = new ArrayList();
//            for (int i = 0; i < size; i++) {
//                RHttpClientSessionStore store = storeList.get(i);
//                if(store.getState() == MyMessage.SessionState.error
//                        ||
//                        store.getState() == MyMessage.SessionState.dead
//                        ||
//                        store.getState() == MyMessage.SessionState.kill
//                        ||
//                        store.getState() == MyMessage.SessionState.shuttingdown
//                        ){ // 当远程机子状态有问题 的时候就直接删除
//                    errorStore.add(store);
//                }
//            }
//
//            // 保证池子数量
////            valitileAvailable
//
//        }finally {
//            LOCK.unlock();
//        }
    }

    /**
     * 模拟使用
     * @param store
     */
    private void callMockRemoteAndUpdateState(RHttpClientSessionStore store){
        Random random = new Random();
        // 0-5 的整数
        int i = random.nextInt(5);
        String idleState = MyMessage.SessionState.idle.toString();
        String noteStartedState = MyMessage.SessionState.not_started.toString();
        String busy = MyMessage.SessionState.busy.toString();
        String dead = MyMessage.SessionState.dead.toString();
        String error = MyMessage.SessionState.error.toString();

        List<MyMessage.SessionState> stateList = new ArrayList<MyMessage.SessionState>(){{
//            add(idleState);
            add(MyMessage.SessionState.idle);
//            add(noteStartedState);
            add(MyMessage.SessionState.not_started);
//            add(busy);
            add(MyMessage.SessionState.busy);
//            add(dead);
            add(MyMessage.SessionState.dead);
//            add(error);
            add(MyMessage.SessionState.error);
        }};

        MyMessage.SessionSateResultMessage message
                = new MyMessage.SessionSateResultMessage(
                        store.getSessionid(), stateList.get(i), "");
        MyMessage.SessionState state = message.getState();
        System.out.println(store.getSessionid() + " from state" + store.getState() +": update to state:" + state);
        store.setState(state);
    }
    private void callRemoteAndUpdateStateAndDeleteState(){
//        this.connection.get()
        HttpMessages.SessionInfo[] sessionsInfo = getSessionsInfo();
        LOCK.lock();
        try {
            // 更新状态
            Map statemap = new HashMap(){};
            for (HttpMessages.SessionInfo sessionInfo : sessionsInfo) {
                String state = sessionInfo.state;
                MyMessage.SessionState remoteSessionState = MyMessage.SessionState.valueOf(state);
                int sessionid = sessionInfo.id;
                statemap.put(sessionid,null);
                RHttpClientSessionStore localSession = this.storeMap.getOrDefault(sessionid, null);
                if(localSession!=null && !localSession.getState().equals(remoteSessionState)){
                    localSession.setState(remoteSessionState);
                }
            }
            // 删除状态
            Set<Integer> clientSessionIds = this.storeMap.keySet();

            for (Integer sessionId : clientSessionIds) {
                if(statemap.getOrDefault(sessionId,null) == null){
                    this.storeMap.remove(sessionId);
                }
            }

        }finally {
            LOCK.unlock();
        }
    }

    /**
     * 获取可用sessionS
     *
     */
    private HttpMessages.SessionInfo[] getSessionsInfo(){
//        Executors.newFixedThreadPool()
        // 网络异常或者其他异常
        HttpMessages.SessionInfo[] sessions = null;
        try {
            MyMessage.SessionInfoMessages sessionInfo = this.rHttpClient.getAvaliableSessionInfo();
            sessions = sessionInfo.getSessions();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            return sessions;
        }
    }

}
