package com.hw.transmitlayer.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.livy.client.common.HttpMessages;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 抽象出一个Connetion对象，这里用来维护与后端Interact进行交互的连接池
 * 灵感来自netty group and pipeline以及connecton\
 * 目前 connection操作分两种，一种是一次连接，另一种是多次轮询方式连接方式获取结果
 */
public class RLivyConnection {
//    private
//    static final String INITIL_URL = "/sessions";
    private static final String Application_JSON = "application/json";
    private final CloseableHttpClient client;
    private final URI server ;
    private final ObjectMapper mapper;

    public RLivyConnection(URI uri, RHttpConf rHttpConf) {
//        int port = uri.getPort() > 0 ? uri.getPort() : 8998;
        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setMaxConnTotal(
                        rHttpConf.getInt(RHttpConf.Entry.CONNECTION_MAX_TOTAL)
                )// 最大可连接数定位50 个
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .setUserAgent("livy-client-http");
        this.client = clientBuilder.build();
        this.server = uri;
        this.mapper = new ObjectMapper();
    }

    public <V> V post(Object body,  Class<V> returnCLass, String uri, Object... uriParams) throws IOException, URISyntaxException {
        HttpPost httpPost = new HttpPost();
        if(body !=null) {
            byte[] bodyBytes = mapper.writeValueAsBytes(body);
            httpPost.setEntity(new ByteArrayEntity(bodyBytes));
        }
        return this.sendJsonRequest(httpPost, returnCLass, uri, uriParams);
    }

    private <V> V sendJsonRequest(HttpRequestBase req, Class<V> returnCLass, String uri, Object... uriParams) throws IOException, URISyntaxException {
        req.setHeader(HttpHeaders.ACCEPT, Application_JSON);
        req.setHeader(HttpHeaders.CONTENT_TYPE, Application_JSON);
        req.setHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");
        return this.sendRequest(req, returnCLass, uri, uriParams);
    }

    private <V> V sendRequest(HttpRequestBase req, Class<V> returnCLass, String uri, Object... uriParams) throws URISyntaxException, IOException {
        // 请求的是只关注最新当前最新的内容就可以了
        req.setURI(new URI(server.getScheme(),null,
                server.getHost(),
                server.getPort(),
                String.format(uri, uriParams),null,null));
        if(req instanceof HttpPost || req instanceof HttpGet || req instanceof HttpDelete) {
            req.addHeader("X-Requested-By", "livy");
        }

        try(CloseableHttpResponse result = client.execute(req)){
            int status = (result.getStatusLine().getStatusCode() / 100) * 100;
            HttpEntity entity = result.getEntity();
            if(status == HttpStatus.SC_OK) {
                if(!Void.class.equals(returnCLass)){
                    return mapper.readValue(entity.getContent(), returnCLass);
                }else {
                    return null;
                }
            }else {
                String error = EntityUtils.toString(entity);
                throw new IOException(String.format("%s : %s", result.getStatusLine().getReasonPhrase(), error ));
            }

        }
    }

    public <V> V get(Class<V> returnClass, String uri,Object... uriParams) throws IOException, URISyntaxException {
            return sendJsonRequest(null, returnClass, uri, uriParams);
    }
}
