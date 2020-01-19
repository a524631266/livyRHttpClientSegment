//package com.hw.transmitlayer.service;
//
//import com.hw.Application;
//import com.hw.transmitlayer.TransmitlayerApplication;
//import com.hw.transmitlayer.entity.EntityItemMessageDTO;
//import com.hw.transmitlayer.job.PiJob;
//import com.hw.transmitlayer.service.impl.FeatureSparkLivyService;
//import org.apache.livy.JobHandle;
//import org.apache.livy.LivyClient;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.annotation.Resource;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {TransmitlayerApplication.class})// 指定启动类
//public class TestFeatureSparkLivyService {
//    @Resource
//    FeatureSparkLivyService featureSparkLivyService;
//
//    @Resource(name = "featurnlivy")
//    private LivyClient livyClient;
//    @Test
//    public void test01(){
////        EntityItemMessageDTO entityItemMessageDTO = new EntityItemMessageDTO(null, null, null);
////        featureSparkLivyService.postJob(entityItemMessageDTO);
//        Thread thread = new Thread(() -> {
//            livyClient.submit(new PiJob(10)).addListener(new JobHandle.Listener<Double>() {
//                @Override
//                public void onJobQueued(JobHandle<Double> jobHandle) {
//                    System.out.println("onJobQueued");
//                }
//
//                @Override
//                public void onJobStarted(JobHandle<Double> jobHandle) {
//                    System.out.println("onJobStarted");
//                }
//
//                @Override
//                public void onJobCancelled(JobHandle<Double> jobHandle) {
//                    System.out.println("onJobCancelled");
//                }
//
//                @Override
//                public void onJobFailed(JobHandle<Double> jobHandle, Throwable throwable) {
//                    System.out.println("onJobFailed");
//                    System.out.println(throwable.getMessage());
//                    livyClient.stop(true);
//                }
//
//                @Override
//                public void onJobSucceeded(JobHandle<Double> jobHandle, Double aDouble) {
//                    System.out.println("onJobSucceeded");
//                }
//            });
//        });
//        thread.setDaemon(true);
//        thread.start();
//
//
//    }
//}
