package com.hw.transmitlayer.service.client;

import org.apache.livy.client.common.ClientConf;

import java.util.Map;
import java.util.Properties;

public class RHttpConf extends ClientConf<RHttpConf> {
    private static final String HTTP_CONF_PREFIX = "livy.client.http.";
    enum Entry implements ConfEntry{
        CONNECTION_TIMEOUT("connection.timeout", "10s"), // 连接超时
        CONNECTION_MAX_TOTAL("connection.max.total", 50), // 默认目前最大支持50个
        CONNECTION_IDLE_TIMEOUT("connection.idle.timeout", "10m"), // 最多闲置时间10分钟
        SOCKET_TIMEOUT("connection.socket.timeout", "5m"), // socket超时时间为5分钟

        CLIENT_EXECUTOR_NUMS("client.executer.num", 10), // 每个client内部持有可执行的线程数量
        JOB_INITIAL_POLL_INTERVAL("job.initial-poll-interval", "100ms"),
        JOB_MAX_POLL_INTERVAL("job.max-poll-interval", "5s");

        private final String key;
        private final Object dflt;

        Entry(String key, Object dflt) {
            this.key = key;
            this.dflt = dflt;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public Object dflt() {
            return dflt;
        }
    }

    public RHttpConf(Properties config) {
        super(config);

    }


    @Override
    protected Map<String, DeprecatedConf> getConfigsWithAlternatives() {
        return null;
    }

    @Override
    protected Map<String, DeprecatedConf> getDeprecatedConfigs() {
        return null;
    }


}
