```text
1.��¼����������ͬһʱ��һ���˺�ֻ����һ���û�����,���˵�¼ͬһ�˺�,����ֻ��һ��������,�����˽��ᱻ������;
2.�ڡ�Spring Boot Shiro���߻Ự��������Redis��Ϊ�������Ŀ�Ļ�����,ʵ�ֵ�¼��������;
```

```text
1.�Զ�����ʿ��ƹ�����: 
    public class KickoutSessionControlFilter extends AccessControlFilter {
        //�߳��󵽵ĵ�ַ
        private String kickoutUrl;
        //�߳�֮ǰ��¼�Ļ���֮���¼���û�,Ĭ���߳�֮ǰ��¼���û�
        private boolean kickoutAfter = false;
        //ͬһ���ʺ����Ự��,Ĭ��1
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
        //����Cache��key��ǰ׺
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
                //���û�е�¼��ֱ�ӽ���֮�������
                return true;
            }
            Session session = subject.getSession();
            User user = (User)subject.getPrincipal();
            String userName = user.getUserName();
            Serializable sessionId = session.getId();
            //��ȡ����,û�оʹ���
            Deque<Serializable> deque = cache.get(userName);
            if(deque == null){ deque = new ArrayDeque<>(); }
            //���������û�д�sessionId,���û�û�б��߳�,�������
            if(!deque.contains(sessionId) && session.getAttribute("kickout") == null){
                //��sessionId�������
                deque.push(sessionId);
                //���û���sessionId���л���
                cache.put(userName,deque);
            }
            //����������sessionId���������Ự��,��ʼ����
            while (deque.size() > maxSession){
                Serializable kickoutSessionId = null;
                if(kickoutAfter){
                    //����߳�����
                    kickoutSessionId = deque.removeFirst();
                }else {
                    //�����߳�ǰ��
                    kickoutSessionId = deque.removeLast();
                }
                //�߳����ٸ����»������
                cache.put(userName, deque);
                try {
                    //��ȡ���߳���sessionId��session����
                    Session kickoutSession = 
                        sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
                    if(kickoutSession != null){
                        //���ûỰ��kickout���Ա�ʾ�߳���
                        kickoutSession.setAttribute("kickout",true);
                    }
                }catch (Exception e){
                    //ignore exception
                }
            }
            //������߳���,ֱ���˳�,�ض����߳���ĵ�ַ
            if(session.getAttribute("kickout") != null && (Boolean)session.getAttribute("kickout")){
                //�Ự���߳���
                try {
                    //�˳���¼
                    subject.logout();
                }catch (Exception e){
                    //ignore exception
                }
                saveRequest(servletRequest);
                //�ض���
                WebUtils.issueRedirect(servletRequest,servletResponse,kickoutUrl);
                return false;
            }
            return true;
        }
    }
2.�޸�������ShiroConfig:
    [1]����ǿ�����߹�����:
        public KickoutSessionControlFilter kickoutSessionControlFilter(){
            KickoutSessionControlFilter kickoutSessionControlFilter = new KickoutSessionControlFilter();
            //ʹ��cacheManager��ȡ��Ӧ��cache�������û���¼�ĻỰ;���ڱ����û����Ự֮��Ĺ�ϵ��;
            //�������ǻ�����֮ǰshiroʹ�õ�redisManager()ʵ�ֵ�cacheManager()�������
            //Ҳ����������дһ��,�������û���ʱ��֮����Զ��建������
            kickoutSessionControlFilter.setCacheManager(cacheManager());
            //���ڸ��ݻỰID,��ȡ�Ự�����߳�������;
            kickoutSessionControlFilter.setSessionManager(sessionManager());
            //�Ƿ��߳�������¼��,Ĭ����false;�����ߵ�¼���û��߳�ǰ�ߵ�¼���û�;�߳�˳��;
            kickoutSessionControlFilter.setKickoutAfter(false);
            //ͬһ���û����ĻỰ��,Ĭ��1;����2����˼��ͬһ���û��������ͬʱ�����˵�¼;
            kickoutSessionControlFilter.setMaxSession(1);
            //���߳����ض��򵽵ĵ�ַ
            kickoutSessionControlFilter.setKickoutUrl("/login");
            return kickoutSessionControlFilter;
        }
    [2]����ShiroFilterFactoryBean: (�滻ShiroFilterChainDefinition)
        @Bean
        public ShiroFilterFactoryBean shiroFilterFactoryBean() {
            ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
            // ����securityManager
            shiroFilterFactoryBean.setSecurityManager(securityManager());
            // ��¼��url
            shiroFilterFactoryBean.setLoginUrl("/login");
            // ��¼�ɹ�����ת��url
            shiroFilterFactoryBean.setSuccessUrl("/index");
            // δ��Ȩurl
            shiroFilterFactoryBean.setUnauthorizedUrl("/403");
            LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
            // ��ӹ�����
            Map<String, Filter> filters = new LinkedHashMap<>();
            filters.put("kickout", kickoutSessionControlFilter());
            shiroFilterFactoryBean.setFilters(filters);
            // ����filterChain,��̬��Դ������
            filterChainDefinitionMap.put("/css/**", "anon");
            filterChainDefinitionMap.put("/js/**", "anon");
            filterChainDefinitionMap.put("/fonts/**", "anon");
            filterChainDefinitionMap.put("/img/**", "anon");
            // druid����Դ���ҳ�治����
            filterChainDefinitionMap.put("/druid/**", "anon");
            // �����˳������������о�����˳�����Shiro�Ѿ�������ʵ����
            filterChainDefinitionMap.put("/logout", "logout");
            filterChainDefinitionMap.put("/", "anon");
            // ������������url��������֤ͨ���ſ��Է���,δͨ����֤�Զ�����LoginUrl
            //filterChainDefinitionMap.put("/**", "authc");
            //�û���֤ͨ������������Remember Me��ס�û���¼״̬��ɷ���
            filterChainDefinitionMap.put("/**", "user,kickout");
            shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
            return shiroFilterFactoryBean;
        }
        (1)����ǿ�����߹�������ShiroFilterFactoryBean��;
            Map<String, Filter> filters = new LinkedHashMap<>();
            filters.put("kickout", kickoutSessionControlFilter());
            shiroFilterFactoryBean.setFilters(filters);
        (2)������������ӵ�¼����:
            filterChainDefinitionMap.put("/**", "user,kickout");
3.�޸�SessionService:
    [1]list����: (�޸��ж�session�Ƿ��ڵ�¼״̬���߼�)
        if (sessionKey == null || session.getAttribute("kickout") != null) {
             continue;
        }
    [2]forceLogout����: (��session������˳����Ա�ʶ)
        if (session != null) {
            sessionDAO.delete(session);
            //���ûỰ��kickout���Ա�ʾ�߳���
            session.setAttribute("kickout", true);
        }
```
