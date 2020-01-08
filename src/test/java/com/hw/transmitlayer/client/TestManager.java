package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.RHttpClientSessionStore;
import com.hw.transmitlayer.service.client.RHttpClientSessionStoreManager;
import com.hw.transmitlayer.service.client.RHttpConf;
import com.hw.transmitlayer.service.client.RLivyConnection;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TestManager {
    @Test
    public void randomest(){
//        Random random = new Random(100);
        Random random = new Random();
//        int dd = // 0-1 的整数
        for (int i = 0; i < 100; i++) {

            int i1 = random.nextInt(5);
            System.out.println(i1);
        }
    }
    /*
    * 测试watchDog是否正常工作
     */
    @Test
    public void testManageWatchDog() {
//        ne
        RHttpConf entries = new TestConfig().testPrepareConfig();
        RLivyConnection connection = null;
        RHttpClientSessionStoreManager manager = new RHttpClientSessionStoreManager(null, entries,connection, true);
        // 消费者
        IntStream.rangeClosed(0, 1).forEach(i->{
            new Thread(
                    ()->{
                        for (int j = 0; j < 100; j++) {
//                            try {
//                                TimeUnit.SECONDS.sleep(3);
                            RHttpClientSessionStore availableStore = manager.getAvailableStore();
                            System.out.println("readid:" + availableStore.getSessionid() + "   state:" + availableStore.getState());
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            System.out.println("123123121");
                        }
                    }
            ).start();
        });
        // 生产者
        IntStream.rangeClosed(0, 10).forEach(i->{
            new Thread(
                    ()->{

                        RHttpClientSessionStore storelist = null;
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            storelist = new RHttpClientSessionStore(i,new URI("http://192.168.40.178:8080"));

                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        manager.register(storelist);
                    }
            ).start();
        });

        // 模拟更新内部状态

        try {
            TimeUnit.SECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
