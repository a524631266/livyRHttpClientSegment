package com.hw.transmitlayer.client;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Vector;
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

    @Test
    public void testVector(){
        Vector<String> vector= new Vector<String>(){
            {
                add("123");
                add("234");
                add("234");
            }
        };
        vector.remove("234");
        System.out.println(vector.size());

    }
    /*
    * 测试watchDog是否正常工作
     */
    @Test
    public void testManageWatchDog() {
//        ne
        RHttpConf entries = new TestConfig().testPrepareConfig();
        RLivyConnection connection = null;
        RHttpClientSessionStoreManager manager = new RHttpClientSessionStoreManager(null, entries,connection, null,true);
        // 消费者
        IntStream.rangeClosed(0, 1).forEach(i->{
            new Thread(
                    ()->{
                        for (int j = 0; j < 10; j++) {
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
