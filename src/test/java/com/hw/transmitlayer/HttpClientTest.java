package com.hw.transmitlayer;

import com.hw.transmitlayer.client.RHttpClient;
import com.hw.transmitlayer.client.RHttpConf;
import org.apache.livy.JobHandle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class HttpClientTest {
    public static void main(String[] args) throws URISyntaxException {
        Properties properties = new Properties();
        ;
        properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_KIND.key(), "sparkr");
        RHttpConf conf = new RHttpConf(properties);
        RHttpClient rHttpClient = new RHttpClient(new URI("http://192.168.40.179:8998"),conf);
        try {
            JobHandle asdf = rHttpClient.submitcode("asdf");
            asdf.addListener(new JobHandle.Listener() {
                @Override
                public void onJobQueued(JobHandle jobHandle) {

                }

                @Override
                public void onJobStarted(JobHandle jobHandle) {

                }

                @Override
                public void onJobCancelled(JobHandle jobHandle) {

                }

                @Override
                public void onJobFailed(JobHandle jobHandle, Throwable throwable) {

                }

                @Override
                public void onJobSucceeded(JobHandle jobHandle, Object o) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
