package com.tzunicom.soft.service;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/7/6 9:24
 * @describe
 **/
public interface ChatService {
    public void register(JSONObject jsonObject, ChannelHandlerContext ctx);

    public void singleSend(JSONObject param, ChannelHandlerContext ctx);

    public void remove(ChannelHandlerContext ctx);
}
