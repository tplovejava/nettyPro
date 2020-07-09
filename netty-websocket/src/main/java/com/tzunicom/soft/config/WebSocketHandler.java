package com.tzunicom.soft.config;

import com.alibaba.fastjson.JSONObject;
import com.tzunicom.soft.enums.E_MsgType;
import com.tzunicom.soft.model.ResponseJson;
import com.tzunicom.soft.service.ChatService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/6/18 15:28
 * @describe
 **/
@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private ChatService chatService;

    /**
     * 一旦连接，第一个被执行
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded 被调用"+ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
//        NettyConfig.getUserChannelMap().put(ctx.channel().id().asLongText(), ctx.channel());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 删除通道
        chatService.remove(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}",cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        ctx.close();
    }


    /**
     * 描述：读取完连接的消息后，对消息进行处理。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        handlerWebSocketFrame(channelHandlerContext, webSocketFrame);

    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) {
        // 关闭请求
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker =
                    NettyConfig.webSocketHandshakerMap.get(ctx.channel().id().asLongText());
            if (handshaker == null) {
                sendErrorMessage(ctx, "不存在的客户端连接！");
            } else {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
            }
            return;
        }

        // ping请求
        if (webSocketFrame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }

        // 只支持文本格式，不支持二进制消息
        if (!(webSocketFrame instanceof TextWebSocketFrame)) {
            sendErrorMessage(ctx, "仅支持文本(Text)格式，不支持二进制消息");
        }

        String request = ((TextWebSocketFrame)webSocketFrame).text();
        log.info("服务端收到新信息：" + request);

        JSONObject param = null;
        try {
            param = JSONObject.parseObject(request);
        } catch (Exception e) {
            sendErrorMessage(ctx, "JSON字符串转换出错！");
            e.printStackTrace();
        }

        if(param == null){
            sendErrorMessage(ctx, "参数为空！");
            return;
        }

        String type = (String) param.get("type");
        System.out.println(type);
        if(E_MsgType.REGISTER.name().equals(type)){
            chatService.register(param, ctx);
        }else if(E_MsgType.SINGLE_SENDING.name().equals(type)){
            chatService.singleSend(param, ctx);
        }else if(E_MsgType.GROUP_SENDING.name().equals(type)){

        }else if(E_MsgType.FILE_MSG_SINGLE_SENDING.name().equals(type)){

        }else if(E_MsgType.FILE_MSG_GROUP_SENDING.name().equals(type)){

        }
    }

    private void sendErrorMessage(ChannelHandlerContext ctx, String errorMsg) {
        String responseJson = new ResponseJson()
                .error(errorMsg)
                .toString();
        ctx.channel().writeAndFlush(new TextWebSocketFrame(responseJson));
    }
}

