package com.hw.transmitlayer.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class SessionHeartbeatMan {
    private Logger Log = LoggerFactory.getLogger(this.getClass());
    private final long connect_interval;
    protected final RLivyConnection connection;
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.info("start interval to poll session infomation");
            try {
                while (true) {
                    updateSessionState();
                    removeUnAvailableSession();
                    TimeUnit.MILLISECONDS.sleep(connect_interval);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }, "heartbeat-updateSession");

    SessionHeartbeatMan(RHttpConf rHttpConf, RLivyConnection connection) {
        connect_interval = rHttpConf.getTimeAsMs(RHttpConf.Entry.CONNECTION_AUTOCONNECT_INTERVAL);
        this.connection = connection;
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 定时更新session的状态,确保可用
     */
    protected abstract void updateSessionState();

    /**
     * 定时删除无用的session
     */
    protected  abstract  void removeUnAvailableSession();
}
