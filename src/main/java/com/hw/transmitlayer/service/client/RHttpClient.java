package com.hw.transmitlayer.service.client;

import com.hw.transmitlayer.service.client.handler.RequestStatementJobHandlerImpl;

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
    private final URI uri_no_path;

    public URI getUri_no_path() {
        return uri_no_path;
    }

    /**
     * 一个执行器，用来执行任务，每个执行者
     */
    private final ScheduledExecutorService executors;
    /**
     * 用来维护一个session，对存活状态的session 发起请求
      */
//    private final ConcurrentHashMap<Integer, RHttpClientSessionStore> store = new ConcurrentHashMap<>();
    private  RHttpClientSessionStoreManager storeManager;
    /**
     * 工厂类创建实例对象，并且初始化连接对象
     * 内部之维护一个connection
     * 通过似乎创建一个manager用来管理存储所需要的url
     * @param uri
     * @param rHttpConf
     */
    public RHttpClient(URI uri, RHttpConf rHttpConf) {
//        LivyClientBuilder
        // 用户可以通过sessions
        Matcher m = Pattern.compile(MyMessage.SESSIONINIT + "/([0-9]+)").matcher(uri.getPath());
        this.rHttpConf = rHttpConf;

        this.conn = new RLivyConnection(uri, rHttpConf);
//        RHttpClientSessionStoreManager manager = null;
        URI uri_no_path = null;
        try {
            uri_no_path = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),null,null,null);

            storeManager = new RHttpClientSessionStoreManager(rHttpConf,this.conn,this);
            if(m.matches()){
                int id = Integer.valueOf(m.group(1));
                HttpMessages.SessionInfo sessionInfo = this.conn.post(null, HttpMessages.SessionInfo.class, MyMessage.SESSIONRECONNECT, m.group(1));
                String state = sessionInfo.state;
                RHttpClientSessionStore sessionStore = new RHttpClientSessionStore(id,MyMessage.SessionState.valueOf(state), uri_no_path);
                storeManager.register(sessionStore);
            } else {
                Map<String,String> conf = null;
                // 如果远程数量太多，就直接跳过
//                int currentRemoteSessionSize = this.getAvailableSessionInfo().getSessions().length;
//                if(currentRemoteSessionSize < rHttpConf.getInt(RHttpConf.Entry.CONNECTION_SESSION_CORE_SIZE)) {
//                    // 获取的是 sparkr/spark/pyspark
//                    String kind = rHttpConf.get(RHttpConf.Entry.CONNECTION_SESSION_KIND);
//                    MyMessage.CreateClientWithTypeEntity createClientWithTypeEntity = new MyMessage.CreateClientWithTypeEntity(conf, kind);
//
//                    HttpMessages.SessionInfo result = this.conn.post(createClientWithTypeEntity, HttpMessages.SessionInfo.class, MyMessage.SESSIONINIT);
//                    int id = result.id;
//                    String state = result.state;
//
//                    RHttpClientSessionStore sessionStore = new RHttpClientSessionStore(id, MyMessage.SessionState.valueOf(state), uri_no_path);
//                    storeManager.register(sessionStore);
//                }
            }
        } catch (IOException e) {
            propagateErr(e);
        } catch (URISyntaxException e) {
            propagateErr(e);
        }finally {
//            this.storeManager = manager;
            this.uri_no_path = uri_no_path;
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
    @Deprecated
    @Override
    public <T> JobHandle<T> submit(Job<T> job) {
        return null;
    }

    @Deprecated
    @Override
    public <T> Future<T> run(Job<T> job) {
        return null;
    }

    @Deprecated
    @Override
    public void stop(boolean b) {

    }

    @Deprecated
    @Override
    public Future<?> uploadJar(File file) {
        return null;
    }

    @Deprecated
    @Override
    public Future<?> addJar(URI uri) {
        return null;
    }

    @Deprecated
    @Override
    public Future<?> uploadFile(File file) {
        return null;
    }

    @Deprecated
    @Override
    public Future<?> addFile(URI uri) {
        return null;
    }

    /**
     * 其他地方不用传递，这个是最重要的提交片段的地方
     * 返回一个句柄给用户，用户可以对其进行监听使用，方便回调
     * 处理逻辑暂时不放在这里，用户可以自定义监听器处理结果
     * @param code 代码为
     */
    @Override
    public JobHandle submitcode(String code) throws IOException, URISyntaxException {
        MyMessage.StatementCodeSendMessage sendMessage = new MyMessage.StatementCodeSendMessage(code);

        RequestStatementJobHandlerImpl<MyMessage.StatementResultWithCode> handler
                = new RequestStatementJobHandlerImpl<MyMessage.StatementResultWithCode>(
                        conn, executors, storeManager,rHttpConf);
        handler.start(sendMessage);
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
    public RHttpClientSessionStoreManager getStoreManager() {
        return storeManager;
    }

    /**
     * 获取远程的session
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public  MyMessage.SessionInfoMessages getAvailableSessionInfo() throws IOException, URISyntaxException {
        MyMessage.SessionInfoMessages infoMessages = conn.get(MyMessage.SessionInfoMessages.class, MyMessage.SESSIONINIT);
        return infoMessages;
    }

    /**
     * 客户端创建一个远程连接
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public RHttpClientSessionStore createOneRemoteClientAndRegister() throws IOException, URISyntaxException {
        MyMessage.CreateClientWithTypeEntity createClientWithTypeEntity
                = new MyMessage.CreateClientWithTypeEntity(null,
                rHttpConf.get(RHttpConf.Entry.CONNECTION_SESSION_KIND.key()));

        HttpMessages.SessionInfo sessionInfo = conn.post(createClientWithTypeEntity, HttpMessages.SessionInfo.class, MyMessage.SESSIONINIT);
        String state = sessionInfo.state;
        int sessionid = sessionInfo.id;
        RHttpClientSessionStore sessionStore = new RHttpClientSessionStore(sessionid, MyMessage.SessionState.valueOf(state), uri_no_path);
        storeManager.register(sessionStore);
        return sessionStore;
    }
    /**
     * 删除一个信息
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */

}
