package com.tzunicom.soft.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/7/6 9:35
 * @describe
 **/
public class BaseUtils {

    public static void sendMessage(Channel channel, String message) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

}
