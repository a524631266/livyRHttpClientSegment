package com.hw.transmitlayer.service;

import com.alibaba.fastjson.JSONObject;
import com.hw.transmitlayer.entity.EntityItemMessageDTO;
import org.springframework.stereotype.Service;

public interface BaseTransmitService {
    // 代表需要传递的的服务层
//    String url = null;
    JSONObject postJob(EntityItemMessageDTO entityItemMessageDTO);
}
