package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.MyMessage;
import com.hw.transmitlayer.service.client.MyMessage.StatementResultWithCode;
import com.hw.transmitlayer.service.client.RHttpClient;
import com.hw.transmitlayer.service.client.RHttpConf;
import org.apache.livy.JobHandle;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestRHttpClient {
    private URI uri;
    private Properties properties;

    @Before
    public void init() throws URISyntaxException {
        uri = new URI("http://192.168.40.179:8998");
        properties = new Properties();
        // 创建一个sparkr属性
        properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_KIND.key(),"sparkr");
    }
    @Test
    public void startTest() throws IOException, URISyntaxException, InterruptedException {
        RHttpClient rHttpClient = new RHttpClient(uri, new RHttpConf(properties));
        StringBuffer code = new StringBuffer();
        code.append("library(featen)")
                .append("\nhistorystatrtime =  as.numeric(as.POSIXct(\"2016/08/04\", format=\"%Y/%m/%d\")) - 30 * 24 * 60* 60")
                .append("\nhistoryendtime =  as.numeric(as.POSIXct(\"2016/08/04\", format=\"%Y/%m/%d\")) + 7 * 24 * 60* 60")
                .append("\nentityid = \"108491\"")
                .append("\nData_history = featen.getHistoryData(entityid,as.character(historystatrtime),as.character(historyendtime),spark)")
                .append("\nVData = Data_history$historydataDataFrame")
                .append("\ncache(VData)")
                .append("\nSensor = Data_history$sensorDataFrame")
                .append("\ncreateOrReplaceTempView(Sensor, \"sensor\")")
                .append("\nnames <- collect(sql(\"select collect_list(name) from sensor\"))[[1]][[1]]")
                .append("\ncache(Sensor)")
                .append("\nMData = featen.preprocess(VData,c(0:(length(names)-1)))")
                .append("\ncache(MData)")
                .append("\nnames(MData) <-  as.character(c(\"time\",names))")
                .append("\nstime_ana = historyendtime - 7 * 24 * 60* 60")
                .append("\netime_ana = historyendtime")
                .append("\nsensorname = \"IC\"")
                .append("\nresult_2_5 <- Feature_2_5(MData,Sensor,stime_ana,etime_ana)")
                .append("\ncreateOrReplaceTempView(result_2_5 , \"result_2_5\")")
                .append("\nresult_2_5_df <- as.data.frame(sql(\"select time,result_2_5 from result_2_5\"))")
                .append("\ncollect(result_2_5)");
        JobHandle submitcode = rHttpClient.submitcode(code.toString());
        submitcode.addListener(new JobHandle.Listener<StatementResultWithCode>() {
            @Override
            public void onJobQueued(JobHandle<StatementResultWithCode> jobHandle) {

            }

            @Override
            public void onJobStarted(JobHandle<StatementResultWithCode> jobHandle) {

            }

            @Override
            public void onJobCancelled(JobHandle<StatementResultWithCode> jobHandle) {

            }

            @Override
            public void onJobFailed(JobHandle<StatementResultWithCode> jobHandle, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onJobSucceeded(JobHandle<StatementResultWithCode> jobHandle, StatementResultWithCode statementResultWithCode) {
                System.out.println(statementResultWithCode.id);
                System.out.println(statementResultWithCode.progress);
                System.out.println(statementResultWithCode.state);
                System.out.println(statementResultWithCode.state);
            }
        });
        TimeUnit.SECONDS.sleep(200000);
    }

}
