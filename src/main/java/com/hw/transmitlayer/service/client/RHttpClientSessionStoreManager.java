package com.hw.transmitlayer.service.client;

import org.apache.livy.sessions.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
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


    private boolean test = false;


    public RHttpClientSessionStoreManager( RHttpClientSessionStore storeList,RHttpConf rHttpConf, RLivyConnection connection) {
        this(storeList, rHttpConf, connection,false);
    }
    public RHttpClientSessionStoreManager( RHttpClientSessionStore storeList,RHttpConf rHttpConf, RLivyConnection connection, Boolean test) {
        super(rHttpConf, connection);
        this.test = test;
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
                done.await(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
            return  store;
        }
    }

    public int storeSize(){
        return storeList.size();
    }


    private void logCount() {
        int writecount = writeCount.get();
        int readcount = readCount.get();
        LOG.info("当前 读次数"+ readcount +": ===== 写次数" + writecount );
    }


    @Override
    public void updateSessionState() {
        logCount();
        LOCK.lock();
        try {
            LOG.info("开始执行后台任务：" + test);
            for (RHttpClientSessionStore store : storeList) {
                if(test){
                    callMockRemoteAndUpdateState(store);
                } else {
                    callRemoteAndUpdateState(store);
                }
            }
            // 获取当前
        } finally {
            LOCK.unlock();
        }

    }

    private void callMockRemoteAndUpdateState(RHttpClientSessionStore store){
        Random random = new Random();
        int i = random.nextInt(5); // 0-1 的整数
        String idlestate = MyMessage.SessionState.IDLE.getKey();
        String notestartedstate = MyMessage.SessionState.NOTSTARTED.getKey();
        String busy = MyMessage.SessionState.BUSY.getKey();
        String dead = MyMessage.SessionState.DEAD.getKey();
        String error = MyMessage.SessionState.ERROR.getKey();

        List<String> stateList = new ArrayList<String>(){{
            add(idlestate);
            add(notestartedstate);
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

    }


    private void update(RHttpClientSessionStore sessionStore){

    }

}
