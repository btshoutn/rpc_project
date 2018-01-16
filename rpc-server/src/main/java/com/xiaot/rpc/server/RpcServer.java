package com.xiaot.rpc.server;

import com.xiaot.rpc.common.RpcDecoder;
import com.xiaot.rpc.common.RpcEncoder;
import com.xiaot.rpc.common.RpcRequest;
import com.xiaot.rpc.common.RpcResponse;
import com.xiaot.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ConcurrentMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaotian on 2018/1/15.
 * RPC服务器
 */
public class RpcServer implements ApplicationContextAware,InitializingBean {

    private Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    //服务器地址
    private String serverAddress;
    //服务注册
    private ServiceRegistry serviceRegistry;

    //用于存贮业务接口名和接口实现类对象
    private  Map<String,Object> handleMap = new HashMap<>();

    public RpcServer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress,ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 通过注解，获取标注了rpc服务注解的业务类的----接口及impl对象，将它放到handlerMap中
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //获取业务接口类有RpcService注解的实现类对象信息
        Map<String, Object> beansWithAnnotationMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotationMap)){
            for (Object serviceBean : beansWithAnnotationMap.values()) {
                try {
                   String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                   handleMap.put(interfaceName,serviceBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 在此启动netty服务，绑定handle流水线：
     * 1、接收请求数据进行反序列化得到request对象
     * 2、根据request中的参数，让RpcHandler从handlerMap中找到对应的业务imple，调用指定方法，获取返回结果
     * 3、将业务调用结果封装到response并序列化后发往客户端
     *
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(parentGroup,childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class)) //in 1
                                    .addLast(new RpcEncoder(RpcResponse.class))//out
                                    .addLast(new RpcHandler(handleMap)); //in 2
                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("server started on port {}", port);
            //服务注册
            if (serverAddress!=null){
                serviceRegistry.register(serverAddress);
            }
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parentGroup.shutdownGracefully();//关闭事件组
            childGroup.shutdownGracefully();
        }

    }
}
