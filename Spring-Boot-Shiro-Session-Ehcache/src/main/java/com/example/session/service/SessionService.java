package com.example.session.service;

import com.example.session.bean.UserOnline;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/8 10:28
 */
public interface SessionService {
    // 查看所有在线用户
    List<UserOnline> list();
    // 根据SessionId踢出用户
    boolean forceLogout(String sessionId);
}
