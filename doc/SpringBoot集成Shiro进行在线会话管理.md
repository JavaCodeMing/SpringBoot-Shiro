```text
1.在Shiro中可以通过org.apache.shiro.session.mgt.eis.SessionDAO对象的getActiveSessions()方法方便的获取到当前所有有效的Session对象;
2.通过这些Session对象,可以实现一些比较有趣的功能,比如查看当前系统的在线人数,查看这些在线用户的一些基本信息,强制让某个用户下线等;
3.现有在集成缓存的基础上添加shiro标签支持,再添加在线会话管理,分别以Redis和Ehcache作为缓存实现一遍;
```

```text
1.页面添加Shiro标签支持:
    [1]添加扩展依赖:
        <dependency>
            <groupId>com.github.theborakompanioni</groupId>
            <artifactId>thymeleaf-extras-shiro</artifactId>
            <version>2.0.0</version>
        </dependency>
    [2]配置类中添加方言:
        @Bean
        public ShiroDialect shiroDialect() {
            return new ShiroDialect();
        }
    [3]页面上使用shiro标签来对资源进行访问限制;(参考上节中标签的使用)
2.自定义Session监听器:(用于统计在线人数)
    public class ShiroSessionListener implements SessionListener{
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
    (ShiroSessionListener为org.apache.shiro.session.SessionListener接口的手动实现)
3.修改配置类ShiroConfig:
    [1]配置SessionDao:
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
    [2]配置SessionManager:(SessionDao通过org.apache.shiro.session.mgt.SessionManager进行管理)
        @Bean
        public SessionManager sessionManager() {
            DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
            Collection<SessionListener> listeners = new ArrayList<SessionListener>();
            listeners.add(new ShiroSessionListener());
            sessionManager.setSessionListeners(listeners);
            sessionManager.setSessionDAO(sessionDAO());
            return sessionManager;
        }
    [3]将定义好的SessionManager添加到SecurityManager中:
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
4.创建UserOnline实体类:(用于描述每个在线用户的基本信息)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserOnline implements Serializable {
        // session id
        private String id;
        // 用户id
        private String userId;
        // 用户名称
        private String username;
        // 用户主机地址
        private String host;
        // 用户登录时系统IP
        private String systemHost;
        // 状态
        private String status;
        // session创建时间
        private Date startTimestamp;
        // session最后访问时间
        private Date lastAccessTime;
        // 超时时间
        private Long timeout;
    }
5.创建Service接口及实现:
    [1]接口:
        public interface SessionService {
            // 查看所有在线用户
            List<UserOnline> list();
            // 根据SessionId踢出用户
            boolean forceLogout(String sessionId);
        }
    [2]实现:
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
                    // 判断此session是否还在登录状态
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
                        userOnline.setStatus("离线");
                    } else {
                        userOnline.setStatus("在线");
                    }
                    userOnline.setTimeout(timeout);
                    list.add(userOnline);
                }
                return list;
            }
            // 使用Redis作缓存(视情况二选一)
            @Override
            public boolean forceLogout(String sessionId) {
                Session session = sessionDAO.readSession(sessionId);
                sessionDAO.delete(session);
                return true;
            }
            // 使用Ehcache作缓存(视情况二选一)
            @Override
            public boolean forceLogout(String sessionId) {
                Session session = sessionDAO.readSession(sessionId);
                //当用户被踢后(SessionTime置为0),该Session并不会立刻从ActiveSessions中剔除
                //可通过其timeout信息来判断该用户在线与否
                session.setTimeout(0);
                return true;
            }
        }
6.创建SessionContoller:(用于处理Session的相关操作)
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
                return ResponseBo.error("踢出用户失败");
            }
        }
    }
7.创建及修改页面:
    [1]创建online.html页面: (用于展示所有在线用户的信息)
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>在线用户管理</title>
            <script th:src="@{/js/jquery-1.11.1.min.js}"></script>
            <script th:src="@{/js/dateFormat.js}"></script>
        </head>
        <body>
        <h3>在线用户数：<span id="onlineCount"></span></h3>
        <table>
            <tr>
                <th>序号</th>
                <th>用户名称</th>
                <th>登录时间</th>
                <th>最后访问时间</th>
                <th>主机</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
        </table>
        <a th:href="@{/index}">返回</a>
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
                        +"<td><a href='#' onclick='offline(\""+r[i].id+"\",\""+r[i].status+"\")'>下线</a></td>"
                        +"</tr>";
                }
                $("table").append(html);
            }, "json");
            function offline(id, status) {
                if (status === "离线") {
                    alert("该用户已是离线状态！！");
                    return;
                }
                $.get("/online/forceLogout", {"id": id}, function (r) {
                    if (r.code === 0) {
                        alert('该用户已强制下线！');
                        location.href = '/online/index';
                    } else {
                        alert(r.msg);
                    }
                }, "json");
            }
        </script>
        </html>
        (注意拷贝日期格式化的js文件(dateFormat.js)到项目对应目录下)
    [2]修改index.html页面: (添加在线用于信息页的跳转)
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org"  xmlns:shiro="http://www.pollix.at/thymeleaf/shiro">
        <head>
            <meta charset="UTF-8">
            <title>首页</title>
        </head>
        <body>
        <p>你好！[[${user.userName}]]</p>
        <!--<h3>权限测试链接</h3>-->
        <p shiro:hasRole="admin">你的角色为超级管理员</p>
        <p shiro:hasRole="test">你的角色为测试账户</p>
        <div>
            <shiro:hasPermission name="user:user"><a th:href="@{/user/list}">获取用户信息</a></shiro:hasPermission>
            <shiro:hasPermission name="user:add"><a th:href="@{/user/add}">新增用户</a></shiro:hasPermission>
            <shiro:hasPermission name="user:delete"><a th:href="@{/user/delete}">删除用户</a></shiro:hasPermission>
        </div>
        <a shiro:hasRole="admin" th:href="@{/online/index}">在线用户管理</a>
        <a th:href="@{/logout}">注销</a>
        </body>
        </html>
8.问题:
    [1]使用Redis作缓存,在跳转到在线人数信息页时,后端抛出异常:
        (1)原因:
            在shiro-redis中默认导入的jedis与shiro-redis中使用的版本不一致,导致ScanResult.getStringCursor()方法找不到而报错;
        (2)解决: 排除shiro-redis中的jedis改用旧版本的jedis
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
