package com.hw.transmitlayer.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理session store用的，同时
 * 同步管理里面的store状态，以同步使用
 */
public class RHttpClientSessionStoreManager {
    private static Logger LOG = LoggerFactory.getLogger(RHttpClientSessionStoreManager.class);
//    private final ConcurrentHashMap<Integer, RHttpClientSessionStore> storeMap;
    private final ConcurrentSkipListSet<RHttpClientSessionStore> storeList = new ConcurrentSkipListSet<>();
    private final Lock LOCK = new ReentrantLock();
    private final Condition done = LOCK.newCondition();

    public RHttpClientSessionStoreManager( RHttpClientSessionStore storeList) {
        this.storeList.add(storeList);
    }

    public RHttpClientSessionStore getAvailableStore() {
        RHttpClientSessionStore store = null;
        LOCK.lock();
        try {
            while (true) {
                for (RHttpClientSessionStore sessionStore : storeList) {
                    LOG.info("当前的sessionstore状态:" + sessionStore.getState());
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
}
