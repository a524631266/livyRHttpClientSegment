package com.hw.transmitlayer.service.client;

import com.hw.transmitlayer.service.client.model.JsonOutput;
import org.apache.livy.client.common.HttpMessages;
import org.apache.livy.rsc.driver.StatementState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类用来传递post中的实体(Entity)信息作用的
 *
 */
public class MyMessage extends HttpMessages {
    // SESSIONINIT 为创建session时候的其实坐标
    public final static String SESSIONINIT = "/sessions";
    // 重新建立连接地址 'http://192.168.40.179:8998/sessions/7/connect
    public final static String SESSIONRECONNECT = SESSIONINIT + "/%d/connect";
    // 重新建立连接地址 'http://192.168.40.179:8998/sessions/7/connect
    public final static String SESSION_STATE_URI = SESSIONINIT + "/%d/state";

    // 获取当前session信息 'http://192.168.40.179:8998/sessions/7
    public final static String SESSIONINITFORMAT = SESSIONINIT + "/%s";
    // 提交code片段的地址 'http://192.168.40.179:8998/sessions/7/statements'
    public final static String CODESTATEMENTFORMAT = SESSIONINIT + "/%d/" + "statements";

    // GET请求获取id的地址 'http://192.168.40.179:8998/sessions/7/statements/0'
    public final  static String CODESTATEMENTFORMAT_GET = SESSIONINIT + "/%s/" + "statements" + "/%s";

    public enum SessionState{
        NOTSTARTED("not_started"),STARTING("starting"),RECOVERING("recovering"),
        IDLE("idle"),RUNNING("running"),BUSY("busy"),SHUTTINGDOWN("shuttingdown")
        ,KILL("kill"),ERROR("error"),DEAD("dead");
        private final String key;

        SessionState(String key) {
            this.key = key;
        }
        public String getKey() {
            return key;
        }
    }

    /**
     * 创建远程session时需要提供的object
     *
     */
    public static class CreateClientWithTypeEntity implements ClientMessage{
        public final Map<String, String> conf; // 创建应用的初始化配置
        public final String kind; // 创建应用时候的引擎类型 rspark / pyspark/spark

        public CreateClientWithTypeEntity(Map<String, String> conf) {
            this(conf, "spark"); // 默认为spark
        }

        public CreateClientWithTypeEntity(Map<String, String> conf, String kind) {
            if(conf == null){// 这个很重要，不然会出现空指针
                this.conf = new HashMap<>();
            }else {
                this.conf = conf;
            }
            this.kind = kind;
        }

        public static class ResponseCreateClientWithType extends SessionInfo {
            public ResponseCreateClientWithType(int id, String name, String appId, String owner, String proxyUser, String state, String kind, Map<String, String> appInfo, List<String> log) {
                super(id, name, appId, owner, proxyUser, state, kind, appInfo, log);
            }
        }
    }


    public static class StatementCodeSendMessage implements ClientMessage {
        public final String code;

        public StatementCodeSendMessage(String code) {
            this.code = code;
        }
    }

    /**
     * http://192.168.40.179:8998/sessions/0/statements post 返回的结果
     * http://192.168.40.179:8998/sessions/0/statements/0  get返回的结果
     */
    public static class StatementResultWithCode implements ClientMessage {
        public final int id; // StateMentid -1 为空id
        public final String code; // 当前请求的内容
        public final JsonOutput output;
        public final float progress;
//        public final String state;
        public final StatementState state;
        public StatementResultWithCode(){
            this(-1,"",new JsonOutput(){},0.0f,StatementState.Waiting);
        }
        public StatementResultWithCode(int id, String code, JsonOutput output, float progress, StatementState state) {
            this.id = id;
            this.code = code;
            this.output = output;
            this.progress = progress;

            this.state = state;
        }

        @Override
        public String toString() {
            return "StatementResultWithCode{" +
                    "id=" + id +
                    ", code='" + code + '\'' +
                    ", output=" + output +
                    ", progress=" + progress +
                    ", state=" + state +
                    '}';
        }
    }
    /**
     * state_url = "http://192.168.40.179:8998/sessions/1/state"
     */
    public static class SessionSateResultMessage implements ClientMessage {
        private final int id;
        private final String state;
        private final String msg;

        public SessionSateResultMessage(){
            this(-1,null,null);
        }
        public SessionSateResultMessage(int id, String state, String msg) {
            this.id = id;
            this.state = state;
            this.msg = msg;
        }

        public int getId() {
            return id;
        }

        public String getState() {
            return state;
        }
    }

    /**
     * state_url = "http://192.168.40.179:8998/sessions/1/state"
     */
    public static class SessionInfoMessages implements ClientMessage {
        private final int from; // 开始编号
        private final SessionInfo[] sessions; // 目前所存在的sessions信息
        private final int total; // 总数量

        public SessionInfoMessages() {
            this(0,new SessionInfo[]{},0);
        }

        public SessionInfoMessages(int from, SessionInfo[] sessions, int total) {
            this.from = from;
            this.sessions = sessions;
            this.total = total;
        }

        public int getFrom() {
            return from;
        }

        public SessionInfo[] getSessions() {
            return sessions;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return "SessionInfoMessages{" +
                    "from=" + from +
                    ", sessions=" + Arrays.toString(sessions) +
                    ", total=" + total +
                    '}';
        }
    }
}
