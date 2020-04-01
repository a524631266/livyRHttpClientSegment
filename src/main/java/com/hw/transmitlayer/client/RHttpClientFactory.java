package com.hw.transmitlayer.client;

import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientFactory;

import java.net.URI;
import java.util.Properties;

public class RHttpClientFactory implements LivyClientFactory {

    @Override
    public LivyClient createClient(URI uri, Properties properties) {
        return new RHttpClient(uri, new RHttpConf(properties));
    }
}
