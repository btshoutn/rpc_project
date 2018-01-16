package com.xiaot.rpc.sample.app;

import com.xiaot.rpc.client.RpcProxy;
import com.xiaot.rpc.sample.client.HelloService;
import com.xiaot.rpc.sample.client.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by xiaotian on 2018/1/15.
 * RPC测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {

    @Autowired
    RpcProxy rpcProxy;
    @Test
    public  void  hello(){
        //创建代理对象
        HelloService service = rpcProxy.create(HelloService.class);
        //调用方法
        String res = service.hello("jim");
        System.out.println("服务器返回结果：");
        System.out.println(res);
    }

    @Test
    public  void  hello2(){
        //创建代理对象
        HelloService service = rpcProxy.create(HelloService.class);
        //调用方法
        String res = service.hello(new Person("xiaot","aaaa"));
        System.out.println("服务器返回结果：");
        System.out.println(res);
    }
}
