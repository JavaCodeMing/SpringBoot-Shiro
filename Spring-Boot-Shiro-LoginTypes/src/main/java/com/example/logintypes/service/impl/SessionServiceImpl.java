package com.example.logintypes.service.impl;

import com.example.logintypes.bean.User;
import com.example.logintypes.bean.UserOnline;
import com.example.logintypes.service.SessionService;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/7 16:37
 */
@Service
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionDAO sessionDAO;

    @Override
    public List<UserOnline> list() {
        List<UserOnline> list = new ArrayList<>();
        Collection<Session> activeSessions = sessionDAO.getActiveSessions();
        for (Session session : activeSessions) {
            UserOnline userOnline = new UserOnline();
            User user;
            SimplePrincipalCollection principalCollection;
            // 判断此session是否还在登录状态
            Object sessionKey = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
            if (sessionKey == null || session.getAttribute("kickout") != null) {
                continue;
            }
            //如果此session正在登录,将此session的数据放入principalCollection集合中,从而可获取登录用户对象数据
            principalCollection = (SimplePrincipalCollection) sessionKey;
            user = (User) principalCollection.getPrimaryPrincipal();
            userOnline.setUsername(user.getUserName());
            userOnline.setUserId(user.getId().toString());
            userOnline.setId(String.valueOf(session.getId()));
            userOnline.setHost(session.getHost());
            userOnline.setStartTimestamp(session.getStartTimestamp());
            userOnline.setLastAccessTime(session.getLastAccessTime());
            long timeout = session.getTimeout();
            if (timeout == 0) {
                userOnline.setStatus("离线");
            } else {
                userOnline.setStatus("在线");
            }
            userOnline.setTimeout(timeout);
            list.add(userOnline);
        }
        return list;
    }

    @Override
    public boolean forceLogout(String sessionId) {
        Session session = sessionDAO.readSession(sessionId);
        if (session != null) {
            sessionDAO.delete(session);
            //设置会话的kickout属性表示踢出了
            session.setAttribute("kickout", true);
        }
        return true;
    }
}
