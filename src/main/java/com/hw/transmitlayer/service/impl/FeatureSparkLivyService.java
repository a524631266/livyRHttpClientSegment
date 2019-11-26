package com.hw.transmitlayer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hw.transmitlayer.entity.EntityItemMessageDTO;
import com.hw.transmitlayer.job.PiJob;
import com.hw.transmitlayer.service.BaseTransmitService;
import org.apache.livy.LivyClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Service
public class FeatureSparkLivyService implements BaseTransmitService {


    @Resource(name = "featurnlivy")
    private LivyClient livyClient;
    @Override
    public JSONObject postJob(EntityItemMessageDTO efdto) {
        try {
//            efdto.getFaultType()
            Double aDouble = livyClient.submit(new PiJob(1000)).get();
            System.out.println("result aDounble:" + aDouble);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
