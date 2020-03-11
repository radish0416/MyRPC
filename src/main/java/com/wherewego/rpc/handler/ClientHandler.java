package com.wherewego.rpc.handler;

import com.wherewego.rpc.codec.Serializer;
import com.wherewego.rpc.codec.SerializerFactory;
import com.wherewego.rpc.connect.pool.CallBackFactory;
import com.wherewego.rpc.transport.Request;
import com.wherewego.rpc.transport.Response;
import com.wherewego.rpc.transport.Transport;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 处理客户端连接事件
 * @Author:lbl
 * @Date:Created in 23:17 2020/3/6
 * @Modified By:
 */
public class ClientHandler  extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        LOGGER.info("客户端接收返回值");
        if(msg instanceof Transport){
            Serializer serializer = SerializerFactory.instance(((Transport) msg).getSerializeType());
            //解码请求信息
            Response request = (Response) serializer.deserialize(((Transport) msg).getBytes(), Response.class);
            //移除
            CallBackFactory.Item item = CallBackFactory.remove(ctx.channel().id(),request.getId());
            item.invoker.invoke(request);
            synchronized (item.lock){
                item.lock.notify();
            }
        }else{

        }

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("客户端异常"+cause.getMessage());
        ctx.fireExceptionCaught(cause);
        //出现异常应该都移除，并通知
        //。。。。。。。
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("客户端断开连接inactive");
        ctx.fireChannelInactive();
    }
}