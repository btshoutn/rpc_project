package com.xiaot.rpc.server;

import com.xiaot.rpc.common.RpcRequest;
import com.xiaot.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by xiaotian on 2018/1/15.
 * 处理具体的业务调用
 * 通过构造时传入的“业务接口及实现”handlerMap，来调用客户端所请求的业务方法
 * 并将业务方法返回值封装成response对象写入下一个handler（即编码handler——RpcEncoder）
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static  final  Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);
    //业务实现类 接口名 和接口实现对象
    private Map<String,Object> handleMap;

    public RpcHandler(Map<String,Object> handleMap){
        this.handleMap  = handleMap;
    }

    /**
     * 接收消息，处理消息，返回结果
     */
    @Override
    public void channelRead0(final ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        //封装返回对象
        RpcResponse response  = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());

        try {
            Object result = handle(rpcRequest);
            response.setResult(result);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            response.setError(throwable);
        }
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest rpcRequest) throws Throwable {

        //类名称
        String className = rpcRequest.getClassName();
        //业务对象
        Object serviceBean = handleMap.get(className);
        //方法名称
        String methodName = rpcRequest.getMethodName();
        //方法参数
        Object[] parameters = rpcRequest.getParameters();
        //参数类型
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        //反射生成对象
        Class<?> cls = Class.forName(className);
        Method method = cls.getMethod(methodName, parameterTypes);
        //调用方法返回数据
        return  method.invoke(serviceBean,parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
