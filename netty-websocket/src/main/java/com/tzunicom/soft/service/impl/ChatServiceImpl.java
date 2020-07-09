package com.tzunicom.soft.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tzunicom.soft.config.NettyConfig;
import com.tzunicom.soft.enums.E_MsgType;
import com.tzunicom.soft.model.ResponseJson;
import com.tzunicom.soft.service.ChatService;
import com.tzunicom.soft.util.BaseUtils;
import com.tzunicom.soft.util.web.ApiResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/7/6 9:25
 * @describe
 **/
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Override
    public void register(JSONObject jsonObject, ChannelHandlerContext ctx) {
        String userId = jsonObject.getString("userId");
        NettyConfig.getUserChannelMap().put(userId, ctx.channel());
        String responseJson = new ResponseJson().success()
                .setData("type", E_MsgType.REGISTER.name())
                .toString();
        BaseUtils.sendMessage(ctx.channel(), responseJson);
        log.info(MessageFormat.format("userId为 {0} 的用户登记到在线用户表，当前在线人数为：{1}"
                , userId, NettyConfig.getUserChannelMap().size()));
    }

    @Override
    public void singleSend(JSONObject param, ChannelHandlerContext ctx) {
        String userId = param.getString("toUserId");
        String content = param.getString("content");
        String fromUserId = param.getString("fromUserId");
        Channel channel = NettyConfig.getUserChannelMap().get(userId);
        if(channel == null){
            //对方未在线
            return;
        }

        String responseJson = new ResponseJson().success()
                .setData("type", E_MsgType.SINGLE_SENDING.name())
                .setData("content", content)
                .setData("fromUserId", fromUserId)
                .toString();
        BaseUtils.sendMessage(channel, responseJson);

    }

    @Override
    public void remove(ChannelHandlerContext ctx) {
        Iterator<Map.Entry<String, Channel>> iterator = NettyConfig.getUserChannelMap().entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Channel> entry = iterator.next();
            if (entry.getValue() == ctx.channel()) {
                log.info("正在移除握手实例...");
                NettyConfig.getUserChannelMap().remove(ctx.channel().id().asLongText());
                log.info(MessageFormat.format("已移除握手实例，当前握手实例总数为：{0}"
                        , NettyConfig.getUserChannelMap().size()));
                iterator.remove();
                log.info(MessageFormat.format("userId为 {0} 的用户已退出聊天，当前在线人数为：{1}"
                        , entry.getKey(), NettyConfig.getUserChannelMap().size()));
                break;
            }
        }
    }
}
