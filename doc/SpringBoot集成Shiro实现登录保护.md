```text
1.登录保护就是在同一时间一个账号只允许一个用户在线,多人登录同一账号,最终只有一个人在线,其他人将会被挤下线;
2.在《Spring Boot Shiro在线会话管理》中以Redis作为缓存的项目的基础上,实现登录保护功能;
```

```text
1.自定义访问控制过滤器: 
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
        protected boolean isAccessAllowed(ServletRequest servletRequest, 
            ServletResponse servletResponse, Object o) throws Exception {
            return false;
        }
        @Override
        protected boolean onAccessDenied(ServletRequest servletRequest, 
            ServletResponse servletResponse) throws Exception {
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
            if(deque == null){ deque = new ArrayDeque<>(); }
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
                    Session kickoutSession = 
                        sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
                    if(kickoutSession != null){
                        //设置会话的kickout属性表示踢出了
                        kickoutSession.setAttribute("kickout",true);
                    }
                }catch (Exception e){
                    //ignore exception
                }
            }
            //如果被踢出了,直接退出,重定向到踢出后的地址
            if(session.getAttribute("kickout") != null && (Boolean)session.getAttribute("kickout")){
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
2.修改配置类ShiroConfig:
    [1]配置强制下线过滤器:
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
    [2]配置ShiroFilterFactoryBean: (替换ShiroFilterChainDefinition)
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
        (1)设置强制下线过滤器到ShiroFilterFactoryBean中;
            Map<String, Filter> filters = new LinkedHashMap<>();
            filters.put("kickout", kickoutSessionControlFilter());
            shiroFilterFactoryBean.setFilters(filters);
        (2)给所有请求添加登录保护:
            filterChainDefinitionMap.put("/**", "user,kickout");
3.修改SessionService:
    [1]list方法: (修改判断session是否还在登录状态的逻辑)
        if (sessionKey == null || session.getAttribute("kickout") != null) {
             continue;
        }
    [2]forceLogout方法: (在session中添加退出属性标识)
        if (session != null) {
            sessionDAO.delete(session);
            //设置会话的kickout属性表示踢出了
            session.setAttribute("kickout", true);
        }
```
