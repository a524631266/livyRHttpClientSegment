package com.hw.transmitlayer.service.client;

import com.hw.transmitlayer.service.client.handler.RequestJobHandlerImpl;
import com.hw.transmitlayer.service.client.model.JsonOutput;
import org.apache.livy.*;
import org.apache.livy.client.common.HttpMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RHttpClient implements LivyClient, RHttpHandlerInterface {
    private static Logger LOG = LoggerFactory.getLogger(RHttpClient.class);
    private final RLivyConnection conn;
    private final RHttpConf rHttpConf;
    /**
     * 一个执行器，用来执行任务，每个执行者
     */
    private final ScheduledExecutorService executors;
    /**
     * 用来维护一个session，对存活状态的session 发起请求
      */
    private final ConcurrentHashMap<Integer, ClienSessionStore> store = new ConcurrentHashMap<>();

    /**
     * 工厂类创建实例对象，并且初始化连接对象
     * 内部之维护一个connection
     *
     * @param uri
     * @param rHttpConf
     */
    public RHttpClient(URI uri, RHttpConf rHttpConf) {
//        LivyClientBuilder
        // 用户可以通过sessions
        Matcher m = Pattern.compile(MyMessage.SESSIONINIT + "/([0-9]+)").matcher(uri.getPath());
        this.rHttpConf = rHttpConf;

        this.conn = new RLivyConnection(uri, rHttpConf);

        try {
            if(m.matches()){
                int id = Integer.valueOf(m.group(1));
                HttpMessages.SessionInfo sessionInfo = this.conn.post(null, HttpMessages.SessionInfo.class, MyMessage.SESSIONRECONNECT, m.group(1));
                String state = sessionInfo.state;
                ClienSessionStore sessionStore = new ClienSessionStore(state);
                this.store.put(id, sessionStore);
            } else {
                Map<String,String> conf = null;
                // 获取的是 sparkr/spark/pyspark
                String kind = rHttpConf.get("kind");
                MyMessage.CreateClientWithTypeEntity createClientWithTypeEntity = new MyMessage.CreateClientWithTypeEntity(conf, kind);
                HttpMessages.SessionInfo result = this.conn.post(createClientWithTypeEntity, HttpMessages.SessionInfo.class, uri.toString());
                int id = result.id;
                ClienSessionStore sessionStore = new ClienSessionStore();
                this.store.put(id, sessionStore);
            }
        } catch (IOException e) {
            propagateErr(e);
        } catch (URISyntaxException e) {
            propagateErr(e);
        }
//        this.executors = Executors.newFixedThreadPool(rHttpConf.getInt(RHttpConf.Entry.CLIENT_EXECUTOR_NUMS));
        int corePoolSize = rHttpConf.getInt(RHttpConf.Entry.CLIENT_EXECUTOR_NUMS); // 10 个线程
        int maximumPoolSize = corePoolSize * 2; // 20 个等待
        long keepAliveTime = 0;
//        this.executors = new ThreadPoolExecutor(
//                corePoolSize,
//                maximumPoolSize,
//                keepAliveTime ,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>()){
//        };
        this.executors = Executors.newScheduledThreadPool(corePoolSize,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        String name = "HttpClient" + UUID.randomUUID();
                        Thread thread = new Thread(r, name);
                        LOG.info("创建一个新线程:" + name+",提供给runnable对象");
                        thread.setDaemon(true);
                        return thread;
                    }
                });

//        Executors.newSingleThreadExecutor()
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
     * @param code 代码为
     */
    @Override
    public Future submitcode(String code) throws IOException, URISyntaxException {
        MyMessage.ResultWithCode postObject = new MyMessage.ResultWithCode(-1, code, null, 0.0f, null);
        RequestJobHandlerImpl<MyMessage.ResultWithCode> handler = new RequestJobHandlerImpl<>(conn, executors, store);
        handler.start(code, postObject);
        return handler;
    }
    private RuntimeException propagateErr(Exception err){
        if(err instanceof RuntimeException){
            throw (RuntimeException) err;
        }else {
            throw new RuntimeException(err);
        }
    }

    // only for testing
    public ConcurrentHashMap<Integer, ClienSessionStore> getStore() {
        return store;
    }
}
