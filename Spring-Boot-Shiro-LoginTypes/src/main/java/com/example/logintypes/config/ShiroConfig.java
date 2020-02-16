package com.example.logintypes.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.example.logintypes.filter.KickoutSessionControlFilter;
import com.example.logintypes.listener.ShiroSessionListener;
import com.example.logintypes.shiro.BaseShiroRealm;
import com.example.logintypes.shiro.PhoneRealm;
import com.example.logintypes.shiro.UserNameRealm;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.*;

/**
 * @author dengzhiming
 * @date 2020/2/5 10:45
 */
@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置securityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        // 登录的url
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后跳转的url
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权url
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 添加过滤器
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("kickout", kickoutSessionControlFilter());
        shiroFilterFactoryBean.setFilters(filters);
        // 定义filterChain,静态资源不拦截
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        // druid数据源监控页面不拦截
        filterChainDefinitionMap.put("/druid/**", "anon");
        // 配置退出过滤器，其中具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/", "anon");
        // 除上以外所有url都必须认证通过才可以访问,未通过认证自动访问LoginUrl
        //filterChainDefinitionMap.put("/**", "authc");
        //用户认证通过或者配置了Remember Me记住用户登录状态后可访问
        filterChainDefinitionMap.put("/**", "user,kickout");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /*@Bean
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
    }*/

    @Bean
    public DefaultWebSecurityManager securityManager() {
        // 配置SecurityManager,并注入shiroRealm
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //securityManager.setRealm(shiroRealm());
        List<Realm> realmList = new ArrayList<>();
        realmList.add(userNameRealm());
        realmList.add(phoneRealm());
        securityManager.setRealms(realmList);
        securityManager.setRememberMeManager(rememberMeManager());
        securityManager.setCacheManager(cacheManager());
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    BaseShiroRealm userNameRealm() {
        return new UserNameRealm();
    }

    @Bean
    BaseShiroRealm phoneRealm() {
        return new PhoneRealm();
    }

    /*@Bean
    public ShiroRealm shiroRealm() {
        // 配置Realm,需自己实现
        return new ShiroRealm();
    }*/

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

    public KickoutSessionControlFilter kickoutSessionControlFilter(){
        KickoutSessionControlFilter kickoutSessionControlFilter = new KickoutSessionControlFilter();
        //使用cacheManager获取相应的cache来缓存用户登录的会话;用于保存用户—会话之间的关系的;
        //这里我们还是用之前shiro使用的redisManager()实现的cacheManager()缓存管理
        //也可以重新另写一个,重新配置缓存时间之类的自定义缓存属性
        kickoutSessionControlFilter.setCacheManager(cacheManager());
        //用于根据会话ID,获取会话进行踢出操作的;
        kickoutSessionControlFilter.setSessionManager(sessionManager());
        //是否踢出后来登录的,默认是false;即后者登录的用户踢出前者登录的用户;踢出顺序;
        kickoutSessionControlFilter.setKickoutAfter(false);
        //同一个用户最大的会话数,默认1;比如2的意思是同一个用户允许最多同时两个人登录;
        kickoutSessionControlFilter.setMaxSession(1);
        //被踢出后重定向到的地址
        kickoutSessionControlFilter.setKickoutUrl("/login");
        return kickoutSessionControlFilter;
    }

}
