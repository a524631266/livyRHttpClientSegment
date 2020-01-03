package com.hw.transmitlayer.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.livy.client.common.HttpMessages;

import java.net.URI;

/**
 * 抽象出一个Connetion对象，这里用来维护与后端Interact进行交互的连接池
 * 灵感来自netty group and pipeline以及connecton
 */
public class RLivyConnection {
//    private
    static final String INITIL_URL = "/sessions";
    private static final String Application_JSON = "application/json";
    private final CloseableHttpClient client;
    private final URI server ;
    private final ObjectMapper mapper;

    public RLivyConnection(URI uri, RHttpConf rHttpConf) {
        int port = uri.getPort() > 0 ? uri.getPort() : 8998;
        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setMaxConnTotal(rHttpConf.getInt(RHttpConf.Entry.CONNECTION_MAX_TOTAL))// 最大可连接数定位50 个
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .setUserAgent("livy-client-http");
        this.client = clientBuilder.build();
        this.server = uri;
        this.mapper = new ObjectMapper();
    }

    public void post(String code, Class info, String s, String... s1) {

    }
}
