package com.example.logintypes.shiro;

import com.example.logintypes.bean.User;
import com.example.logintypes.mapper.UserMapper;
import org.apache.shiro.authc.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author dengzhiming
 * @date 2020/2/11 17:50
 */
public class UserNameRealm extends BaseShiroRealm{

    @Autowired
    private UserMapper userMapper;
    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UserToken userToken = (UserToken) authenticationToken;
        if (userToken.getLoginType() == LoginType.USERNAME) {
            // 获取用户输入的用户名和密码
            String userName = String.valueOf(userToken.getPrincipal());
            String password = new String((char[]) userToken.getCredentials());
            System.out.println("用户" + userName + "认证-----UserNameRealm.doGetAuthenticationInfo");
            // 通过用户名到数据库查询用户信息
            User user = userMapper.findByUserName(userName);
            if (user == null || !password.equals(user.getPassword())) {
                throw new UnknownAccountException("用户名或密码错误！");
            } else if ("0".equals(user.getStatus())) {
                throw new LockedAccountException("账号已被锁定,请联系管理员！");
            }
            // 验证通过返回一个封装了用户信息的AuthenticationInfo实例即可
            return new SimpleAuthenticationInfo(user, password, getName());
        }else {
            return null;
        }
    }
}
