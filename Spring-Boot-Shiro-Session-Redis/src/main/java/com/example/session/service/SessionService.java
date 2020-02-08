package com.example.session.service;

import com.example.session.bean.UserOnline;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/7 16:37
 */
public interface SessionService {
    List<UserOnline> list();
    boolean forceLogout(String sessionId);
}
