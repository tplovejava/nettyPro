package com.tzunicom.soft.service.impl;

import com.tzunicom.soft.config.NettyConfig;
import com.tzunicom.soft.enums.E_MsgType;
import com.tzunicom.soft.model.ResponseJson;
import com.tzunicom.soft.service.PushService;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/6/18 15:41
 * @describe
 **/
@Service
public class PushServiceImpl implements PushService {
    @Override
    public void pushMsgToOne(String userId, String fromUserId, String msg) {
        ConcurrentHashMap<String, Channel> userChannelMap = NettyConfig.getUserChannelMap();
        Channel channel = userChannelMap.get(userId);
        String responseJson = new ResponseJson().success()
                .setData("type", E_MsgType.SINGLE_SENDING.name())
                .setData("content", msg)
                .setData("fromUserId", fromUserId)
                .toString();

        channel.writeAndFlush(new TextWebSocketFrame(responseJson));
    }

    @Override
    public void pushMsgToAll(String msg) {
        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
    }
}
