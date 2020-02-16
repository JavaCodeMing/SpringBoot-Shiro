package com.example.logintypes.filter;

import com.example.logintypes.bean.User;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author dengzhiming
 * @date 2020/2/8 11:08
 */
public class KickoutSessionControlFilter extends AccessControlFilter {
    //踢出后到的地址
    private String kickoutUrl;
    //踢出之前登录的还是之后登录的用户,默认踢出之前登录的用户
    private boolean kickoutAfter = false;
    //同一个帐号最大会话数,默认1
    private int maxSession = 1;
    private SessionManager sessionManager;
    private Cache<String, Deque<Serializable>> cache;

    public void setKickoutUrl(String kickoutUrl) {
        this.kickoutUrl = kickoutUrl;
    }

    public void setKickoutAfter(boolean kickoutAfter) {
        this.kickoutAfter = kickoutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    //设置Cache的key的前缀
    public void setCacheManager(CacheManager cacheManager){
        this.cache = cacheManager.getCache("shiro_redis_cache");
    }
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);
        if(!subject.isAuthenticated() && !subject.isRemembered()){
            //如果没有登录，直接进行之后的流程
            return true;
        }
        Session session = subject.getSession();
        User user = (User)subject.getPrincipal();
        String userName = user.getUserName();
        Serializable sessionId = session.getId();
        //读取缓存,没有就存入
        Deque<Serializable> deque = cache.get(userName);
        if(deque == null){
            deque = new ArrayDeque<>();
        }
        //如果队列里没有此sessionId,且用户没有被踢出,放入队列
        if(!deque.contains(sessionId) && session.getAttribute("kickout") == null){
            //将sessionId存入队列
            deque.push(sessionId);
            //将用户的sessionId队列缓存
            cache.put(userName,deque);
        }
        //如果队列里的sessionId数超出最大会话数,开始踢人
        while (deque.size() > maxSession){
            Serializable kickoutSessionId = null;
            if(kickoutAfter){
                //如果踢出后者
                kickoutSessionId = deque.removeFirst();
            }else {
                //否则踢出前者
                kickoutSessionId = deque.removeLast();
            }
            //踢出后再更新下缓存队列
            cache.put(userName, deque);
            try {
                //获取被踢出的sessionId的session对象
                Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
                if(kickoutSession != null){
                    //设置会话的kickout属性表示踢出了
                    kickoutSession.setAttribute("kickout",true);
                }
            }catch (Exception e){
                //ignore exception
            }
        }
        //如果被踢出了,直接退出,重定向到踢出后的地址
        if(session.getAttribute("kickout") != null && (Boolean) session.getAttribute("kickout")){
            //会话被踢出了
            try {
                //退出登录
                subject.logout();
            }catch (Exception e){
                //ignore exception
            }
            saveRequest(servletRequest);
            //重定向
            WebUtils.issueRedirect(servletRequest,servletResponse,kickoutUrl);
            return false;
        }
        return true;
    }
}
