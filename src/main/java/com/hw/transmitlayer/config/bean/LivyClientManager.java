package com.hw.transmitlayer.config.bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 用来管理LivyClient
 * 功能有一个list
 */
@Component
public class LivyClientManager implements DisposableBean {
//    private static

    @Override
    public void destroy() throws Exception {

    }
}
