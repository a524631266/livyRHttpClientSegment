package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.*;

import com.hw.transmitlayer.service.client.model.JsonOutput;
import org.apache.livy.JobHandle;
import org.apache.livy.client.common.AbstractJobHandle;
import org.apache.livy.client.common.HttpMessages;
import org.apache.livy.rsc.driver.StatementState;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.livy.client.common.HttpMessages.*;

public class TestConnection {

    private RHttpConf rHttpConf;
    private URI uri;
    private RLivyConnection connection;
    @Before
    public void init() throws URISyntaxException {
        rHttpConf = new TestConfig().testPrepareConfig();
        uri = new URI("http://192.168.40.179:8998");
        connection = new RLivyConnection(uri, rHttpConf);
    }

    /**
     * 测试创建一个远程客户端
     * 并返回一个sessioninfo
     */
    @Test
    public void TestCreateRemoteClient() throws URISyntaxException {

        MyMessage.CreateClientWithTypeEntity createClientWithTypeEntity = new MyMessage.CreateClientWithTypeEntity(null,
                rHttpConf.get(RHttpConf.Entry.CONNECTION_SESSION_KIND.key()));
        try {
            SessionInfo result = connection.post(createClientWithTypeEntity, SessionInfo.class, MyMessage.SESSIONINIT);
            System.out.println(result);
            // state = 1
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试状态结果
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testGetState() throws IOException, URISyntaxException {
        SessionInfo resulst = new SessionInfo(3,null,null,null,null,"starting","sparkr",null,null);
        MyMessage.SessionSateResultMessage sessionSateResultMessage = connection.get(MyMessage.SessionSateResultMessage.class, MyMessage.SESSION_STATE_URI, resulst.id);
        System.out.println(sessionSateResultMessage.getState());
    }

    /**
     * 测试状态结果
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSessionInfo() throws IOException, URISyntaxException {
        MyMessage.SessionInfoMessages infoMessages = connection.get(MyMessage.SessionInfoMessages.class, MyMessage.SESSIONINIT);
        System.out.println(infoMessages);

    }


    /**
     * 异步提交代码片段
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testAsynchronizedSubmitCodeInfo() throws IOException, URISyntaxException {
//        StringBuffer code = new StringBuffer();
//        code.append();
//        HttpMessages.ClientMessage postObject = new MyMessage.ResultWithCode(-1, code.toString(), null, 0.0f, null);
//        RequestJobHandlerImpl<MyMessage.ResultWithCode> handler = new RequestJobHandlerImpl<>(conn, executors, storeManager,rHttpConf);
//        handler.start(postObject);
//        System.out.println(infoMessages);
//        JobHandle.State send = AbstractJobHandle.State.valueOf("SEND");
//        System.out.println(send);
        StringBuffer code = new StringBuffer();
        code.append("library(eaten)")
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
        MyMessage.StatementCodeSendMessage statementCodeSendMessage = new MyMessage.StatementCodeSendMessage(code.toString());
        // 提交code路径
        MyMessage.StatementResultWithCode result = connection.post(statementCodeSendMessage, MyMessage.StatementResultWithCode.class, MyMessage.CODESTATEMENTFORMAT, 1);
        System.out.println(result);
    }
    /**
     * 获取提交的结果
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testReturenCodeInfo() throws IOException, URISyntaxException {

        // 获取上次的结果
        // SESSIONID JOBID
        MyMessage.StatementResultWithCode result = connection.get( MyMessage.StatementResultWithCode.class, MyMessage.CODESTATEMENTFORMAT_GET, 1,4);
        System.out.println(result);
        int statementid = result.id; // 当前statement的id
        float progress = result.progress; // 当前进度 1.0w
        StatementState state = result.state;
        JsonOutput result2 = result.output;// 当为空的时候无结果
        String status = result2.status; //  成功的话为'ok'
        System.out.println(status);
    }

    @Test
    public void testEqualEnum(){
        StatementState available = StatementState.Available;
        System.out.println(available.equals(StatementState.Waiting));
    }
}
