package com.hw.transmitlayer.service.client;

import org.apache.livy.client.common.ClientConf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RHttpConf extends ClientConf<RHttpConf> {
    private static final String HTTP_CONF_PREFIX = "livy.client.http.";
    public enum Entry implements ConfEntry{
        CONNECTION_TIMEOUT("connection.timeout", "10s"), // 连接超时
        CONNECTION_MAX_TOTAL("connection.max.total", 50), // 默认目前最大支持50个
        CONNECTION_IDLE_TIMEOUT("connection.idle.timeout", "10m"), // 最多闲置时间10分钟
        SOCKET_TIMEOUT("connection.socket.timeout", "5m"), // socket超时时间为5分钟

        CLIENT_EXECUTOR_NUMS("client.executer.num", 10), // 每个client内部持有可执行的线程数量
        JOB_INITIAL_POLL_INTERVAL("job.initial-poll-interval", "100ms"), // 默认为每100ms获取一次
        JOB_MAX_POLL_INTERVAL("job.max-poll-interval", "5s"), // 每次轮询，以2倍的速度增长
        CONNECTION_SESSION_KIND("connection.session.kind", "shared"),
        CONNECTION_AUTOCONNECT_INTERVAL("connection.autoconnect.interval", "1m"),
        CONNECTION_SESSION_CORE_SIZE("connection.session.core.size", 1), // 该客户端中最少要保留的session,远程的spark进程数量
        CONNECTION_SESSION_MAX_SIZE("connection.session.max.size", 10) // 该客户端中最多持有的shell数量，防止使用过度
        ;


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

    private static final Map<String, DeprecatedConf> configsWithAlternatives
            = Collections.unmodifiableMap(new HashMap<String, DeprecatedConf>(){{
                put(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL.key,DepConf.JOB_INITIAL_POLL_INTERVAL);
                put(RHttpConf.Entry.JOB_MAX_POLL_INTERVAL.key,DepConf.JOB_MAX_POLL_INTERVAL);
//                put(RHttpConf.Entry.CONNECTION_SESSION_KIND.key,DepConf.CONNECTION_SESSION_KIND);
    }});

    private static final Map<String, DeprecatedConf> deprecatedConfigs
             = Collections.unmodifiableMap(new HashMap<String,DeprecatedConf>());


    @Override
    protected Map<String, DeprecatedConf> getConfigsWithAlternatives() {
        return configsWithAlternatives;
    }

    @Override
    protected Map<String, DeprecatedConf> getDeprecatedConfigs() {
        return deprecatedConfigs;
    }

    static enum DepConf implements DeprecatedConf{
        JOB_INITIAL_POLL_INTERVAL("job.initial_poll_interval", "0.6"), // 默认为每100ms获取一次
        JOB_MAX_POLL_INTERVAL("job.max_poll_interval", "0.6"), // 每次轮询，以2倍的速度增长
        CONNECTION_SESSION_KIND("connection.session.kind", "0.6");
        private final String key;
        private final String version;
        private final String deprecationMessage;

        DepConf(String key, String version) {
            this(key,version,"");
        }


        DepConf(String key, String version, String deprecationMessage) {
            this.key = key;
            this.version = version;
            this.deprecationMessage = deprecationMessage;
        }


        @Override
        public String key() {
            return key;
        }

        @Override
        public String version() {
            return version;
        }

        @Override
        public String deprecationMessage() {
            return deprecationMessage;
        }
    }

}
