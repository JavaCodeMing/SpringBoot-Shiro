package com.example.session.controller;

import com.example.session.bean.ResponseBo;
import com.example.session.bean.UserOnline;
import com.example.session.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/8 10:32
 */
@Controller
@RequestMapping("/online")
public class SessionContoller {
    @Autowired
    SessionService sessionService;

    @RequestMapping("index")
    public String online() {
        return "online";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<UserOnline> list() {
        return sessionService.list();
    }

    @RequestMapping("forceLogout")
    @ResponseBody
    public ResponseBo forceLogout(String id) {
        try {
            sessionService.forceLogout(id);
            return ResponseBo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBo.error("踢出用户失败");
        }
    }
}
