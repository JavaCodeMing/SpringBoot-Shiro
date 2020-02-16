package com.example.logintypes.service;

import com.example.logintypes.bean.UserOnline;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/7 16:37
 */
public interface SessionService {
    List<UserOnline> list();
    boolean forceLogout(String sessionId);
}
