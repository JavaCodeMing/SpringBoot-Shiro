package com.example.protection.controller;

import com.example.protection.bean.ResponseBo;
import com.example.protection.bean.UserOnline;
import com.example.protection.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/7 17:21
 */
@Controller
@RequestMapping("/online")
public class SessionContoller {
    @Autowired
    SessionService sessionService;

    @RequestMapping("index")
    public String online(){
        return "online";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<UserOnline> list(){
        return sessionService.list();
    }

    @RequestMapping("forceLogout")
    @ResponseBody
    public ResponseBo forceLogout(String id){
        try {
            sessionService.forceLogout(id);
            return ResponseBo.ok();
        } catch (Exception e){
            e.printStackTrace();
            return ResponseBo.error("踢出用户失败");
        }
    }
}
