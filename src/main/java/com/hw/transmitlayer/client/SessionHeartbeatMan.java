package com.hw.transmitlayer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class SessionHeartbeatMan {
    private Logger Log = LoggerFactory.getLogger(this.getClass());
    private final long connect_interval;
    protected final RLivyConnection connection;
    protected final RHttpClient rHttpClient;

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.info("start interval to poll session infomation");
            try {
                while (true && (!Thread.currentThread().isInterrupted())) {
                    balanceSessionState();
//                    removeUnAvailableSession();
                    TimeUnit.MILLISECONDS.sleep(connect_interval);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.info("end SessionHeatbeatMan!!");
        }
    }, "heartbeat-updateSession");

    SessionHeartbeatMan(RHttpConf rHttpConf, RLivyConnection connection,RHttpClient rHttpClient) {
        connect_interval = rHttpConf.getTimeAsMs(RHttpConf.Entry.CONNECTION_AUTOCONNECT_INTERVAL);
        this.connection = connection;
        this.rHttpClient = rHttpClient;
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 定时调整平衡session的状态，
     * 比如删除/更新状态，以确保
     */
    protected abstract void balanceSessionState();

    /**
     * 释放资源
     */
    public void interrupt(){
        this.thread.interrupt();
    };
}
