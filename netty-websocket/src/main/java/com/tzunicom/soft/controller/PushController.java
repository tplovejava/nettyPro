package com.tzunicom.soft.controller;

import com.tzunicom.soft.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/6/18 15:40
 * @describe
 **/
@RestController
@RequestMapping("/push")
public class PushController {
    @Autowired
    private PushService pushService;


    /**
     * 推送给所有用户
     * @param msg
     */
    @PostMapping("/pushAll")
    public void pushToAll(@RequestParam("msg") String msg){
        pushService.pushMsgToAll(msg);
    }
    /**
     * 推送给指定用户
     * @param userId
     * @param msg
     */
    @PostMapping("/pushOne")
    public void pushMsgToOneo(@RequestParam("userId") String userId,@RequestParam("fromUserId") String fromUserId,@RequestParam("msg") String msg){
        pushService.pushMsgToOne(userId,fromUserId,msg);
    }
}
