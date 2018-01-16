package com.xiaot.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xiaotian on 2018/1/15.
 *
 */
@Target({ElementType.TYPE})//注解应用到接口上
@Retention(RetentionPolicy.RUNTIME)//JVM在运行期也保留注释，可以通过反射机制获取注解信息
@Component
public @interface RpcService {
    Class<?> value();
}
