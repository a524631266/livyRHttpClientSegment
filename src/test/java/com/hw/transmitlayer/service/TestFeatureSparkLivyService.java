package com.hw.transmitlayer.service;

import com.hw.Application;
import com.hw.transmitlayer.TransmitlayerApplication;
import com.hw.transmitlayer.entity.EntityItemMessageDTO;
import com.hw.transmitlayer.service.impl.FeatureSparkLivyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TransmitlayerApplication.class})// 指定启动类
public class TestFeatureSparkLivyService {
    @Resource
    FeatureSparkLivyService featureSparkLivyService;
    @Test
    public void test01(){
        EntityItemMessageDTO entityItemMessageDTO = new EntityItemMessageDTO(null, null, null);
        featureSparkLivyService.postJob(entityItemMessageDTO);
    }
}
