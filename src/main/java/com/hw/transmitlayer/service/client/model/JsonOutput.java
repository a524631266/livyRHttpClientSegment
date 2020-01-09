package com.hw.transmitlayer.service.client.model;

import java.util.List;
import java.util.Map;

public class JsonOutput {
    public Map<String, String> data; // {'text/plain': '[1] 4'}
    public int execution_count; // 0
    public String status;// 'OK'
    public String ename;
    public String evalue;
    public List traceback;
//    'output': {'ename': 'Error',
//            'evalue': '[1] "Error in library(eaten): there is no package '
//                              'called ‘eaten’"',
//                'execution_count': 4,
//                'status': 'error',
//                'traceback': []},
    @Override
    public String toString() {
        return "JsonOutput{" +
                "data=" + data +
                ", execution_count=" + execution_count +
                ", status='" + status + '\'' +
                '}';
    }
}
