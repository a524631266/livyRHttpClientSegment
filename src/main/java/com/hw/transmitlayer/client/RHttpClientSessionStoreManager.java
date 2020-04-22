package com.hw.transmitlayer.client;

import org.apache.livy.client.common.HttpMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 管理session store用的，同时
 * 同步管理里面的store状态，以同步使用
 */
public class RHttpClientSessionStoreManager extends SessionHeartbeatMan{
    private static Logger LOG = LoggerFactory.getLogger(RHttpClientSessionStoreManager.class);

    private volatile  Map<Integer,RHttpClientSessionStore> storeMap= new HashMap<Integer,RHttpClientSessionStore>();
    // 目前读写次数暂时还不太确定
    private final Lock LOCK = new ReentrantLock(true);
    private final Condition done = LOCK.newCondition();
    // 写入store的次数 测试用
    private volatile AtomicInteger writeCount = new AtomicInteger(0);
    // 测试用
    private volatile AtomicInteger readCount = new AtomicInteger(0) ;
    // 最少session数量
    private final int sessionCoreSize ;
    // 最大session数量
    private final int sessionMaxSize ;
    private boolean test = false;

    public Lock getLOCK() {
        return LOCK;
    }
    // 初始化的时候可以设置一个空的store
    public RHttpClientSessionStoreManager(RHttpConf rHttpConf, RLivyConnection connection, RHttpClient rHttpClient) {
        this(null, rHttpConf, connection,rHttpClient,false);
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
//                done.signalAll();
                done.signal();
            }
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
                // 每次获取数据的时候更新，这里会不会太耗时
                balanceSessionState();
                // 获取一个可用store，如果没有则等待
                store = this.getOneStore();

                if(store != null){
                    break;
                } else {

                    // 当执行线程的时候后台session不够用，那么就创建一个session
                    if(storeMap.size() < sessionMaxSize){
                        this.rHttpClient.createOneRemoteClientAndRegister();
                    }
                    LOG.info("无空用节点循环获取读取的任务列表");
                }
                // 当前默认最多1秒中，重新唤醒
                done.await(1, TimeUnit.SECONDS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
            return  store;
        }
    }

    /**
     *  当返回为空的时候为没有可用的store，因此需要重新布置
     * @return
     */
    private RHttpClientSessionStore getOneStore() {
        RHttpClientSessionStore store = null;

        Iterator<Map.Entry<Integer, RHttpClientSessionStore>> entryIterator = storeMap.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<Integer, RHttpClientSessionStore> storeEntry = entryIterator.next();
            RHttpClientSessionStore localStore = storeEntry.getValue();
            if (localStore.getState().equals(MyMessage.SessionState.idle)) {
                store = localStore;
                break;
            }
        }
        return store;
    }


//    public int storeSize(){
//
//        return storeList.size();
//    }


    private void logCount() {
        if(writeCount == null || readCount == null) {
            LOG.info("当前 write read Count还未初始化");
        }else {
            int writeCountL = writeCount.get();
            int readCountL = readCount.get();
            LOG.info("当前 读次数" + readCountL + ": ===== 写次数" + writeCountL);
        }
    }

    /**
     * 周期性更新storeList,
     * 1. 不存在的话就删除
     * 2. 存在的话，就直接跟新任务
     */
    @Override
    public void balanceSessionState() {
        logCount();
        LOCK.lock();
        try {
            LOG.info("开始执行后台任务：" + test);
            // 这个部分可以单独成立一个 用来测试的remote更新状态以及实际的远程状态，可以方便
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
            // 平衡操作
            // 当用户远程请求的数据为空的时候，并且用户的当前store数量不足时候，可以增加扩容
            // 可以支持并发操作
            int noDeadSize = storeMap.values().stream()
                    .filter((i) -> i.getState() != MyMessage.SessionState.dead)
                    .collect(Collectors.toList())
                    .size();
            if(noDeadSize < sessionCoreSize) {
                rHttpClient.createOneRemoteClientAndRegister();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
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
        if(sessionsInfo == null) {
            LOG.warn("has no remote livy server ");
            return;
        }
        Map stateMap = new HashMap(sessionsInfo.length){};
        for (HttpMessages.SessionInfo sessionInfo : sessionsInfo) {
            String state = sessionInfo.state;
            MyMessage.SessionState remoteSessionState = MyMessage.SessionState.valueOf(state);
            int sessionId = sessionInfo.id;
            stateMap.put(sessionId,1);
            RHttpClientSessionStore localSession = this.storeMap.getOrDefault(sessionId,null);

            // 新增状态
            if(localSession == null ){
                //添加新增状态 注册 到自身的
                this.register(
                        new RHttpClientSessionStore(sessionId,remoteSessionState,rHttpClient.getUri_no_path()));
            }else if(!localSession.getState().equals(remoteSessionState)){
                // 更新状态
                localSession.setState(remoteSessionState);
            }
        }
        // 删除状态
        Iterator<Integer> iterator = this.storeMap.keySet().iterator();
        while (iterator.hasNext()){
            Integer sessionId = iterator.next();
            if(stateMap.getOrDefault(sessionId,null) == null){
//                this.storeMap.remove(sessionId);
                // x修复ConcurrentModificationException
                iterator.remove();
            }
        }
    }

    /**
     * 获取可用session信息
     *
     */
    private HttpMessages.SessionInfo[] getSessionsInfo(){
//        Executors.newFixedThreadPool()
        // 网络异常或者其他异常
        HttpMessages.SessionInfo[] sessions = null;
        try {
            MyMessage.SessionInfoMessages sessionInfo = this.rHttpClient.getAvailableSessionInfo();
            sessions = sessionInfo.getSessions();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            return sessions;
        }
    }

}
