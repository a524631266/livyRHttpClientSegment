## 背景
目前在圈子sparkR圈子内，并没有可用的以java后端作为提交sparkR代码提交的方案，以快速迭代开发应用，支持以springboot后端进行开发
## 使用指南
可以详见test部分种的 TestRHttpClient.java部分
方便进行提交代码的测试


## how to use
1. 配置maven
```xml
<dependency>
    <groupId>com.hw</groupId>
    <artifactId>transmitlayer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
2. 自定以配置文件，以properties方式注入客户端
 配置详情请看   
 src\main\java\com\hw\transmitlayer\client\RHttpConf.java
```java
String livyPort = "http://192.168.1.10:8080";
uri = new URI(livyPort);
// 创建properties 自动配置相关文件
properties = new Properties();
// 创建一个sparkr属性
properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_KIND.key(), "sparkr");
properties.setProperty(RHttpConf.Entry.CONNECTION_SESSION_MAX_SIZE.key(), "1");
// 生成客户端
RHttpClient rHttpClient = new RHttpClient(uri, new RHttpConf(properties));
```
3. use your r code segment
```java
 String rcode = code....
```
4. addListener and write your own business logic
```java
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
            public void onJobSucceeded(JobHandle<StatementResultWithCode> jobHandle,
                    StatementResultWithCode statementResultWithCode) {
                System.out.println(statementResultWithCode.id);
                System.out.println(statementResultWithCode.progress);
                System.out.println(statementResultWithCode.state);
                // try {
                // Output output = new Output(new FileOutputStream("data/person.bin"));
                // Kryo kryo = new Kryo();
                // kryo.register(StatementResultWithCode.class);
                // kryo.writeObject(output,statementResultWithCode);
                // } catch (FileNotFoundException e) {
                // e.printStackTrace();
                // }
                if (statementResultWithCode.output.data == null) {
                    throw new RuntimeException("返回数据错误");
                }
                String result = statementResultWithCode.output.data.get("text/plain");
                try {
                    ResultClass[] resultClasses = new ObjectMapper().readValue(result, ResultClass[].class);
                    System.out.println(resultClasses);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(statementResultWithCode.state);
            }
        });
```
