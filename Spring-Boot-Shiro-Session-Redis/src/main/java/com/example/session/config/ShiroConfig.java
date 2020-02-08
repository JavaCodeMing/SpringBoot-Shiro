package com.example.session.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.example.session.listener.ShiroSessionListener;
import com.example.session.shiro.ShiroRealm;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author dengzhiming
 * @date 2020/2/5 10:45
 */
@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition shiroFilterChainDefinition = new DefaultShiroFilterChainDefinition();
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 定义filterChain,静态资源不拦截
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        // druid数据源监控页面不拦截
        filterChainDefinitionMap.put("/druid/**", "anon");
        // 配置退出过滤器,其中具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/", "anon");
        // 除上以外所有url都必须认证通过才可以访问,未通过认证自动访问LoginUrl
        //filterChainDefinitionMap.put("/**", "authc");
        //用户认证通过或者配置了Remember Me记住用户登录状态后可访问
        filterChainDefinitionMap.put("/**", "user");
        shiroFilterChainDefinition.addPathDefinitions(filterChainDefinitionMap);
        return shiroFilterChainDefinition;
    }

    @Bean
    public DefaultWebSecurityManager securityManager() {
        // 配置SecurityManager,并注入shiroRealm
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroRealm());
        securityManager.setRememberMeManager(rememberMeManager());
        securityManager.setCacheManager(cacheManager());
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    public ShiroRealm shiroRealm() {
        // 配置Realm,需自己实现
        return new ShiroRealm();
    }

    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        // rememberMe cookie加密的密钥
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    public SimpleCookie rememberMeCookie() {
        // 设置cookie名称,对应login.html页面的<input type="checkbox" name="rememberMe"/>
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        // 设置cookie的过期时间,单位为秒,这里为一天
        cookie.setMaxAge(86400);
        return cookie;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

    public RedisCacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(new RedisManager());
        return redisCacheManager;
    }

    @Bean
    public ShiroDialect shiroDialect() {
        //配置方言标签
        return new ShiroDialect();
    }

    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        Collection<SessionListener> listeners = new ArrayList<>();
        listeners.add(new ShiroSessionListener());
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionDAO(sessionDAO());
        return sessionManager;
    }

    @Bean
    public RedisSessionDAO sessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(new RedisManager());
        return redisSessionDAO;
    }

}
