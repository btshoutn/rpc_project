package com.xiaot.rpc.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xiaotian on 2018/1/15.
 * RPC服务启动类
 */
public class RpcBootstrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
