package com.hw.transmitlayer.config;

import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Destroyed;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class SparkLivyConfig implements DisposableBean {

    @Value("${com.hw.transmitlayer.featuresparkurl}")
    private String url;

    private LivyClient client = null;

    @Bean(value = "featurnlivy")
    public LivyClient livyClient() {
        try {
            client = new LivyClientBuilder()
                    .setConf("kind","sparkr")
                    .setURI(new URI("http://" + url))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return  client;
    }

    @Override
    public void destroy() throws Exception {
        client.stop(true);
        System.out.println("close 222");
    }
}
