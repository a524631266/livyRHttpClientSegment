package com.hw.transmitlayer.service.client;

import com.hw.transmitlayer.service.client.model.JsonOutput;
import org.apache.livy.client.common.HttpMessages;

import java.util.List;
import java.util.Map;

/**
 * 该类用来传递post中的实体(Entity)信息作用的
 *
 */
public class MyMessage extends HttpMessages {
    // SESSIONINIT 为创建session时候的其实坐标
    public final static String SESSIONINIT = "sessions";
    // 重新建立连接地址 'http://192.168.40.179:8998/sessions/7/connect
    public final static String SESSIONRECONNECT = SESSIONINIT + "/%d/connect";

    // 获取当前session信息 'http://192.168.40.179:8998/sessions/7
    public final static String SESSIONINITFORMAT = SESSIONINIT + "/%s";
    // 提交code片段的地址 'http://192.168.40.179:8998/sessions/7/statements'
    public final static String CODESTATEMENTFORMAT = SESSIONINIT + "/%s/" + "statements";

    // GET请求获取id的地址 'http://192.168.40.179:8998/sessions/7/statements/0'
    public final  static String CODESTATEMENTFORMAT_GET = SESSIONINIT + "/%s/" + "statements" + "/%s";
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
            this.conf = conf;
            this.kind = kind;
        }

        public static class ResponseCreateClientWithType extends SessionInfo {
            public ResponseCreateClientWithType(int id, String name, String appId, String owner, String proxyUser, String state, String kind, Map<String, String> appInfo, List<String> log) {
                super(id, name, appId, owner, proxyUser, state, kind, appInfo, log);
            }
        }
    }

    /**
     * http://192.168.40.179:8998/sessions/0/statements post 返回的结果
     * http://192.168.40.179:8998/sessions/0/statements/0  get返回的结果
     */
    public static class ResultWithCode implements ClientMessage {
        public final int id; // job id -1 为空id
        public final String code; // 当前请求的内容
        public final JsonOutput output;
        public final float progress;
        public final String state;

        public ResultWithCode(int id, String code, JsonOutput output, float progress, String state) {
            this.id = id;
            this.code = code;
            this.output = output;
            this.progress = progress;
            this.state = state;
        }
    }
}
