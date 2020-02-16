package com.example.logintypes.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @author dengzhiming
 * @date 2020/2/11 21:56
 */
public class UserToken extends UsernamePasswordToken {
    private LoginType loginType;

    public UserToken(String username, String password, boolean rememberMe, LoginType loginType) {
        super(username, password, rememberMe);
        this.loginType = loginType;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }
}
