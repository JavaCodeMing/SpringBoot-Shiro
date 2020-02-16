package com.example.logintypes.shiro;

import com.example.logintypes.bean.User;
import com.example.logintypes.mapper.UserMapper;
import org.apache.shiro.authc.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author dengzhiming
 * @date 2020/2/11 21:30
 */
public class PhoneRealm extends BaseShiroRealm {

    @Autowired
    private UserMapper userMapper;
    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UserToken userToken = (UserToken) authenticationToken;
        if (userToken.getLoginType() == LoginType.PHONE) {
            // 获取用户输入的手机号和密码
            String phone = String.valueOf(authenticationToken.getPrincipal());
            String password = new String((char[]) authenticationToken.getCredentials());
            System.out.println("手机号" + phone + "认证-----PhoneRealm.doGetAuthenticationInfo");
            // 通过手机号到数据库查询用户信息
            User user = userMapper.findByPhone(phone);
            if (user == null || !password.equals(user.getPassword())) {
                throw new UnknownAccountException("手机号或密码错误！");
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
