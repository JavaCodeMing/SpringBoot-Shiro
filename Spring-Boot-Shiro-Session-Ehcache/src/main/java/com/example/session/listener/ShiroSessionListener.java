package com.example.session.listener;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dengzhiming
 * @date 2020/2/8 10:25
 */
public class ShiroSessionListener implements SessionListener {
    //维护一个原子类型的Integer对象,用于统计在线Session的数量
    private final AtomicInteger sessionCount = new AtomicInteger(0);

    @Override
    public void onStart(Session session) {
        sessionCount.incrementAndGet();
    }

    @Override
    public void onStop(Session session) {
        sessionCount.decrementAndGet();
    }

    @Override
    public void onExpiration(Session session) {
        sessionCount.decrementAndGet();
    }
}
