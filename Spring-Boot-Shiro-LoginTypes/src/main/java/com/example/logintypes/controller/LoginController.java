package com.example.logintypes.controller;

import com.example.logintypes.bean.ResponseBo;
import com.example.logintypes.bean.User;
import com.example.logintypes.shiro.LoginType;
import com.example.logintypes.shiro.UserToken;
import com.example.logintypes.util.MD5Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author dengzhiming
 * @date 2020/2/5 13:38
 */
@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    /*@PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByUserName(String username, String password, Boolean rememberMe) {
        // 密码MD5加密
        password = MD5Utils.encrypt(username, password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.USERNAME);
        // 获取Subject对象
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return ResponseBo.ok();
        } catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e) {
            return ResponseBo.error(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseBo.error("认证失败！");
        }
    }*/

    @PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByPhoneNum(String username, String password, Boolean rememberMe) {
        // 密码MD5加密
        password = MD5Utils.encrypt(password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.PHONE);
        // 获取Subject对象
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return ResponseBo.ok();
        } catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e) {
            return ResponseBo.error(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseBo.error("认证失败！");
        }
    }

    @RequestMapping("index")
    public String index(Model model) {
        // 登录成后,即可通过Subject获取登录的用户信息
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/403")
    public String forbid() {
        return "403";
    }
}
