package com.xiaot.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by xiaotian on 2018/1/15.
 * RPC编码器
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public  RpcEncoder(Class<?> cls){
        this.genericClass = cls;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {

        if (genericClass.isInstance(obj)){//判断是否初始化
            byte[] data = SerializationUtil.serialize(obj);//序列化
            byteBuf.writeInt(data.length);//写出数据的长度
            byteBuf.writeBytes(data);//写出的数据
        }
    }
}
