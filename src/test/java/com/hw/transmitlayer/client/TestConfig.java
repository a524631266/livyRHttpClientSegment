package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.RHttpClient;
import com.hw.transmitlayer.service.client.RHttpConf;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class TestConfig {
    public static void main(String[] args) {
        Properties properties = new Properties();
        RHttpConf entries = new RHttpConf(properties);
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            System.out.println(key + "===" + value);
        }
    }
}
