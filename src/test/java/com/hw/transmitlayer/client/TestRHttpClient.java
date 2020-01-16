package com.hw.transmitlayer.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.hw.transmitlayer.service.client.MyMessage;
import com.hw.transmitlayer.service.client.MyMessage.StatementResultWithCode;
import com.hw.transmitlayer.service.client.RHttpClient;
import com.hw.transmitlayer.service.client.RHttpConf;
import com.hw.transmitlayer.service.client.model.JsonOutput;
import org.apache.livy.JobHandle;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestRHttpClient {
    private URI uri;
    private Properties properties;

    @Before
    public void init() throws URISyntaxException {
//        uri = new URI("http://192.168.40.179:8998");
        uri = new URI("http://192.168.10.63:8998");
        properties = new Properties();
        // 创建一个sparkr属性
        properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_KIND.key(),"sparkr");
    }
    @Test
    public void startTest() throws IOException, URISyntaxException, InterruptedException {
        RHttpClient rHttpClient = new RHttpClient(uri, new RHttpConf(properties));
        StringBuffer code = new StringBuffer();
        code.append("library(featen)")
                .append("\nlibrary(jsonlite)")
                .append("\nan_start = \"2019/11/14\"")
                .append("\nhistorystatrtime =  as.numeric(as.POSIXct(an_start, format=\"%Y/%m/%d\")) - 30 * 24 * 60* 60")
                .append("\nhistoryendtime =  as.numeric(as.POSIXct(an_start, format=\"%Y/%m/%d\")) + 7 * 24 * 60* 60")
                .append("\nentityid = \"839055\"")
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
                .append("\ncreateOrReplaceTempView(result_2_5, \"result_2_5\")")
                .append("\nreturn(toJSON(collect(toJSON(sql(\"select time,result_2_5 from result_2_5\")))))");
//                .append("\nreturn(collect(toJSON(result_2_5)))");
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
                try {
                    Output output = new Output(new FileOutputStream("data/person.bin"));
                    Kryo kryo = new Kryo();
                    kryo.register(StatementResultWithCode.class);
                    kryo.writeObject(output,statementResultWithCode);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println(statementResultWithCode.state);
            }
        });
        TimeUnit.SECONDS.sleep(200000);
    }


    @Test
    public void startWithClientResultClassTest() throws IOException, URISyntaxException, InterruptedException {
        RHttpClient rHttpClient = new RHttpClient(uri, new RHttpConf(properties));
        StringBuffer code = new StringBuffer();
        code.append("library(featen)")
                .append("\nan_start = \"2019/11/14\"")
                .append("\nhistorystatrtime =  as.numeric(as.POSIXct(an_start, format=\"%Y/%m/%d\")) - 30 * 24 * 60* 60")
                .append("\nhistoryendtime =  as.numeric(as.POSIXct(an_start, format=\"%Y/%m/%d\")) + 7 * 24 * 60* 60")
                .append("\nentityid = \"839055\"")
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
                .append("\ncreateOrReplaceTempView(result_2_5, \"result_2_5\")")
                .append("\nreturn(collect(toJSON(sql(\"select time,result_2_5 from result_2_5\"))))");
        JobHandle submitcode = rHttpClient.submitcode(code.toString(),ClientResultClass.class);
        submitcode.addListener(new JobHandle.Listener<ClientResultClass>() {
            @Override
            public void onJobQueued(JobHandle<ClientResultClass> jobHandle) {

            }

            @Override
            public void onJobStarted(JobHandle<ClientResultClass> jobHandle) {

            }

            @Override
            public void onJobCancelled(JobHandle<ClientResultClass> jobHandle) {

            }

            @Override
            public void onJobFailed(JobHandle<ClientResultClass> jobHandle, Throwable throwable) {

            }

            @Override
            public void onJobSucceeded(JobHandle<ClientResultClass> jobHandle, ClientResultClass clientResultClass) {
                System.out.println(clientResultClass.id);
                System.out.println(clientResultClass.progress);
                System.out.println(clientResultClass.state);
                System.out.println(clientResultClass.state);
            }
        });

        TimeUnit.SECONDS.sleep(200000);
    }
    static class ResultClass {
        public final long time;
        public final boolean result_2_5;

        ResultClass(long time, boolean result_2_5) {
            this.time = time;
            this.result_2_5 = result_2_5;
        }
    }
    public static class ClientResultClass extends MyMessage.StatementResultWithCode{
        public final Map<String, ResultClass[]> output;
        public ClientResultClass() {
            this(null);
        }
        public ClientResultClass(Map<String,ResultClass[]> output) {
            this.output = output;
        }
    }

    @Test
    public void startTest2() throws IOException, URISyntaxException, InterruptedException {
        RHttpClient rHttpClient = new RHttpClient(uri, new RHttpConf(properties));
        StringBuffer code = new StringBuffer();

        code.append("library(featen)").append("\nfamilies <- c(\"gaussian\", \"poisson\")")
                .append("\ntrain <- function(family) {")
                .append("\n  model <- glm(Sepal.Length ~ Sepal.Width + Species, iris, family = family)")
                .append("\n  summary(model)")
                .append("\n}")
                .append("\nmodel.summaries <- spark.lapply(families, train)")
                .append("\nprint(model.summaries)");
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