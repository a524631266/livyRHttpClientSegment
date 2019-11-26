package com.hw.transmitlayer.config;

import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class SparkLivyConfig {
    @Value("${com.hw.transmitlayer.featuresparkurl}")
    private String url;
    @Bean("featurnlivy")
    public LivyClient livyClient() {
        LivyClient client = null;
        try {
            client = new LivyClientBuilder()
                    .setURI(new URI("http://" + url))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return  client;
    }
}
