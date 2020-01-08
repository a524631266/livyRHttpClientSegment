package com.hw.transmitlayer.client;

import com.hw.transmitlayer.service.client.MyMessage;
import com.hw.transmitlayer.service.client.RHttpConf;
import com.hw.transmitlayer.service.client.RLivyConnection;
import org.apache.livy.client.common.HttpMessages;
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
    @Test
    public void testGetState() throws IOException, URISyntaxException {
        SessionInfo resulst = new SessionInfo(1,null,null,null,null,"starting","sparkr",null,null);
        MyMessage.SessionSateResultMessage sessionSateResultMessage = connection.get(MyMessage.SessionSateResultMessage.class, MyMessage.SESSION_STATE_URI, resulst.id);
        System.out.println(sessionSateResultMessage);
    }
}
