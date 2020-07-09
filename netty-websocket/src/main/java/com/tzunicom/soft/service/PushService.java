package com.tzunicom.soft.service;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/6/18 15:40
 * @describe
 **/
public interface PushService {
    /**
     * 推送给指定用户
     * @param userId
     * @param msg
     */
    void pushMsgToOne(String userId,String fromUserId,String msg);

    /**
     * 推送给所有用户
     * @param msg
     */
    void pushMsgToAll(String msg);
}
