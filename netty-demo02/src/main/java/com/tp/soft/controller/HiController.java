package com.tp.soft.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author taop
 * @date 2019/7/26 16:32
 **/
@Controller
public class HiController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(){
        return "demo02";
    }
}
