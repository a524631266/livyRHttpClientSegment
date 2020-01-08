package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.RHttpClient;
import com.hw.transmitlayer.service.client.RHttpConf;
import org.junit.Test;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class TestConfig {
    @Test
    public void test() {
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
    @Test
    public RHttpConf testPrepareConfig(){
        Properties properties = new Properties();
        properties.setProperty(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL.key(), String.valueOf(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL.dflt()));
        properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_KIND.key(), "sparkr");
        properties.setProperty(RHttpConf.Entry.CONNECTION_AUTOCONNECT_INTERVAL.key(), "3s");
        RHttpConf entries = new RHttpConf(properties);
        System.out.println(entries.get(RHttpConf.Entry.CONNECTION_SESSION_KIND.key()));
        long timeAsMs = entries.getTimeAsMs(RHttpConf.Entry.JOB_INITIAL_POLL_INTERVAL);
        System.out.println("init poll interval:" + timeAsMs);
        long timeAsMs1 = entries.getTimeAsMs(RHttpConf.Entry.JOB_MAX_POLL_INTERVAL);
        System.out.println("max poll interval:" + timeAsMs1);
        long timeAsMs2 = entries.getTimeAsMs(RHttpConf.Entry.CONNECTION_AUTOCONNECT_INTERVAL);
        System.out.println("connect interval:" + timeAsMs2);
        return entries;
    }

}
