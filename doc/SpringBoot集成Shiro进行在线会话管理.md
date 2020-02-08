```text
1.��Shiro�п���ͨ��org.apache.shiro.session.mgt.eis.SessionDAO�����getActiveSessions()��������Ļ�ȡ����ǰ������Ч��Session����;
2.ͨ����ЩSession����,����ʵ��һЩ�Ƚ���Ȥ�Ĺ���,����鿴��ǰϵͳ����������,�鿴��Щ�����û���һЩ������Ϣ,ǿ����ĳ���û����ߵ�;
3.�����ڼ��ɻ���Ļ��������shiro��ǩ֧��,��������߻Ự����,�ֱ���Redis��Ehcache��Ϊ����ʵ��һ��;
```

```text
1.ҳ�����Shiro��ǩ֧��:
    [1]�����չ����:
        <dependency>
            <groupId>com.github.theborakompanioni</groupId>
            <artifactId>thymeleaf-extras-shiro</artifactId>
            <version>2.0.0</version>
        </dependency>
    [2]����������ӷ���:
        @Bean
        public ShiroDialect shiroDialect() {
            return new ShiroDialect();
        }
    [3]ҳ����ʹ��shiro��ǩ������Դ���з�������;(�ο��Ͻ��б�ǩ��ʹ��)
2.�Զ���Session������:(����ͳ����������)
    public class ShiroSessionListener implements SessionListener{
        //ά��һ��ԭ�����͵�Integer����,����ͳ������Session������
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
    (ShiroSessionListenerΪorg.apache.shiro.session.SessionListener�ӿڵ��ֶ�ʵ��)
3.�޸�������ShiroConfig:
    [1]����SessionDao:
        (1)Ehcache:
            @Bean
            public SessionDAO sessionDAO() {
                MemorySessionDAO sessionDAO = new MemorySessionDAO();
                return sessionDAO;
            }
        (2)Redis:
            @Bean
            public RedisSessionDAO sessionDAO() {
                RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
                redisSessionDAO.setRedisManager(redisManager());
                return redisSessionDAO;
            }
    [2]����SessionManager:(SessionDaoͨ��org.apache.shiro.session.mgt.SessionManager���й���)
        @Bean
        public SessionManager sessionManager() {
            DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
            Collection<SessionListener> listeners = new ArrayList<SessionListener>();
            listeners.add(new ShiroSessionListener());
            sessionManager.setSessionListeners(listeners);
            sessionManager.setSessionDAO(sessionDAO());
            return sessionManager;
        }
    [3]������õ�SessionManager��ӵ�SecurityManager��:
        @Bean
        public DefaultWebSecurityManager securityManager() {
            // ����SecurityManager,��ע��shiroRealm
            DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
            securityManager.setRealm(shiroRealm());
            securityManager.setRememberMeManager(rememberMeManager());
            securityManager.setCacheManager(cacheManager());
            securityManager.setSessionManager(sessionManager());
            return securityManager;
        }    
4.����UserOnlineʵ����:(��������ÿ�������û��Ļ�����Ϣ)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserOnline implements Serializable {
        // session id
        private String id;
        // �û�id
        private String userId;
        // �û�����
        private String username;
        // �û�������ַ
        private String host;
        // �û���¼ʱϵͳIP
        private String systemHost;
        // ״̬
        private String status;
        // session����ʱ��
        private Date startTimestamp;
        // session������ʱ��
        private Date lastAccessTime;
        // ��ʱʱ��
        private Long timeout;
    }
5.����Service�ӿڼ�ʵ��:
    [1]�ӿ�:
        public interface SessionService {
            // �鿴���������û�
            List<UserOnline> list();
            // ����SessionId�߳��û�
            boolean forceLogout(String sessionId);
        }
    [2]ʵ��:
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
                    Object sessionKey = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                    // �жϴ�session�Ƿ��ڵ�¼״̬
                    if (sessionKey == null) { continue; }
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
                        userOnline.setStatus("����");
                    } else {
                        userOnline.setStatus("����");
                    }
                    userOnline.setTimeout(timeout);
                    list.add(userOnline);
                }
                return list;
            }
            // ʹ��Redis������(�������ѡһ)
            @Override
            public boolean forceLogout(String sessionId) {
                Session session = sessionDAO.readSession(sessionId);
                sessionDAO.delete(session);
                return true;
            }
            // ʹ��Ehcache������(�������ѡһ)
            @Override
            public boolean forceLogout(String sessionId) {
                Session session = sessionDAO.readSession(sessionId);
                //���û����ߺ�(SessionTime��Ϊ0),��Session���������̴�ActiveSessions���޳�
                //��ͨ����timeout��Ϣ���жϸ��û��������
                session.setTimeout(0);
                return true;
            }
        }
6.����SessionContoller:(���ڴ���Session����ز���)
    @Controller
    @RequestMapping("/online")
    public class SessionContoller {
        @Autowired
        SessionService sessionService;
        @RequestMapping("index")
        public String online(){ return "online"; }
        @RequestMapping("list")
        @ResponseBody
        public List<UserOnline> list(){
            return sessionService.list();
        }
        @RequestMapping("forceLogout")
        @ResponseBody
        public ResponseBo forceLogout(String id){
            try {
                sessionService.forceLogout(id);
                return ResponseBo.ok();
            } catch (Exception e){
                e.printStackTrace();
                return ResponseBo.error("�߳��û�ʧ��");
            }
        }
    }
7.�������޸�ҳ��:
    [1]����online.htmlҳ��: (����չʾ���������û�����Ϣ)
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>�����û�����</title>
            <script th:src="@{/js/jquery-1.11.1.min.js}"></script>
            <script th:src="@{/js/dateFormat.js}"></script>
        </head>
        <body>
        <h3>�����û�����<span id="onlineCount"></span></h3>
        <table>
            <tr>
                <th>���</th>
                <th>�û�����</th>
                <th>��¼ʱ��</th>
                <th>������ʱ��</th>
                <th>����</th>
                <th>״̬</th>
                <th>����</th>
            </tr>
        </table>
        <a th:href="@{/index}">����</a>
        </body>
        <script th:inline="javascript">
            $.get("/online/list", {}, function (r) {
                var length = r.length;
                $("#onlineCount").text(length);
                var html = "";
                for (var i = 0; i < length; i++) {
                    html += "<tr>"
                        +"<td>" + (i + 1) + "</td>"
                        +"<td>" + r[i].username + "</td>"
                        +"<td>" + new Date(r[i].startTimestamp).Format("yyyy-MM-dd hh:mm:ss") + "</td>"
                        +"<td>" + new Date(r[i].lastAccessTime).Format("yyyy-MM-dd hh:mm:ss") + "</td>"
                        +"<td>" + r[i].host + "</td>"
                        +"<td>" + r[i].status + "</td>"
                        +"<td><a href='#' onclick='offline(\""+r[i].id+"\",\""+r[i].status+"\")'>����</a></td>"
                        +"</tr>";
                }
                $("table").append(html);
            }, "json");
            function offline(id, status) {
                if (status === "����") {
                    alert("���û���������״̬����");
                    return;
                }
                $.get("/online/forceLogout", {"id": id}, function (r) {
                    if (r.code === 0) {
                        alert('���û���ǿ�����ߣ�');
                        location.href = '/online/index';
                    } else {
                        alert(r.msg);
                    }
                }, "json");
            }
        </script>
        </html>
        (ע�⿽�����ڸ�ʽ����js�ļ�(dateFormat.js)����Ŀ��ӦĿ¼��)
    [2]�޸�index.htmlҳ��: (�������������Ϣҳ����ת)
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org"  xmlns:shiro="http://www.pollix.at/thymeleaf/shiro">
        <head>
            <meta charset="UTF-8">
            <title>��ҳ</title>
        </head>
        <body>
        <p>��ã�[[${user.userName}]]</p>
        <!--<h3>Ȩ�޲�������</h3>-->
        <p shiro:hasRole="admin">��Ľ�ɫΪ��������Ա</p>
        <p shiro:hasRole="test">��Ľ�ɫΪ�����˻�</p>
        <div>
            <shiro:hasPermission name="user:user"><a th:href="@{/user/list}">��ȡ�û���Ϣ</a></shiro:hasPermission>
            <shiro:hasPermission name="user:add"><a th:href="@{/user/add}">�����û�</a></shiro:hasPermission>
            <shiro:hasPermission name="user:delete"><a th:href="@{/user/delete}">ɾ���û�</a></shiro:hasPermission>
        </div>
        <a shiro:hasRole="admin" th:href="@{/online/index}">�����û�����</a>
        <a th:href="@{/logout}">ע��</a>
        </body>
        </html>
8.����:
    [1]ʹ��Redis������,����ת������������Ϣҳʱ,����׳��쳣:
        (1)ԭ��:
            ��shiro-redis��Ĭ�ϵ����jedis��shiro-redis��ʹ�õİ汾��һ��,����ScanResult.getStringCursor()�����Ҳ���������;
        (2)���: �ų�shiro-redis�е�jedis���þɰ汾��jedis
            <dependency>
                <groupId>org.crazycake</groupId>
                <artifactId>shiro-redis</artifactId>
                <version>3.2.3</version>
                <exclusions>
                    <exclusion>
                        <groupId>redis.clients</groupId>
                        <artifactId>jedis</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>
            <dependency>
                <groupId>redis.clients</groupId>
                <artifactId>jedis</artifactId>
                <version>2.9.3</version>
            </dependency>  
```
