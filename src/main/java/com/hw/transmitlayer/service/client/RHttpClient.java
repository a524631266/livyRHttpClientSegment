package com.hw.transmitlayer.service.client;

import org.apache.livy.*;
import org.apache.livy.client.common.HttpMessages;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;

public class RHttpClient implements LivyClient, RHttpHandlerInterface {
    private final RLivyConnection conn;
    private final RHttpConf rHttpConf;

    /**
     * 工厂类创建实例对象，并且初始化连接对象
     * 内部之维护一个connection
     *
     * @param uri
     * @param rHttpConf
     */
    public RHttpClient(URI uri, RHttpConf rHttpConf) {
//        LivyClientBuilder
        this.rHttpConf = rHttpConf;

        this.conn = new RLivyConnection(uri, rHttpConf);
    }

//    @Override
//    public <T> JobHandle<T> submit(Job<T> job) {
//        return null;
//    }

    @Override
    public <T> JobHandle<T> submit(Job<T> job) {
        return null;
    }

    @Override
    public <T> Future<T> run(Job<T> job) {
        return null;
    }

    @Override
    public void stop(boolean b) {

    }

    @Override
    public Future<?> uploadJar(File file) {
        return null;
    }

    @Override
    public Future<?> addJar(URI uri) {
        return null;
    }

    @Override
    public Future<?> uploadFile(File file) {
        return null;
    }

    @Override
    public Future<?> addFile(URI uri) {
        return null;
    }

    /**
     * 其他地方不用传递，这个是最重要的提交片段的地方
     *
     * @param code
     * @return
     */
    @Override
    public Future submitcode(String code) {
//        submit(code);
        conn.post(code, HttpMessages.SessionInfo.class,"/%2/%2", "");
        return null;
    }
}
