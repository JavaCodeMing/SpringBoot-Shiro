```text
1.在Spring Boot中集成Shiro进行用户的认证过程主要可以归纳为以下三点:
    [1]定义一个ShiroConfig,然后配置SecurityManager Bean,SecurityManager为Shiro的安全管理器,管理着所有Subject;
    [2]在ShiroConfig中配置ShiroFilterFactoryBean,其为Shiro过滤器工厂类,依赖于SecurityManager;
    [3]自定义Realm实现,Realm包含doGetAuthorizationInfo()和doGetAuthenticationInfo()方法;
```
```text
1.创建Spring Boot项目,并引入以下依赖:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.1.1</version>
    </dependency>
    <!-- thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <!-- shiro-spring -->
    <dependency>
        <groupId>org.apache.shiro</groupId>
        <artifactId>shiro-spring-boot-web-starter</artifactId>
        <version>1.4.2</version>
    </dependency>
    <!--mysql数据库驱动-->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <!--Druid数据源-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.1.20</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
2.创建用户实体类和响应实体类:
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class User implements Serializable {
        private Integer id;
        private String userName;
        private String password;
        private Date createTime;
        private String status;
    }
    public class ResponseBo extends HashMap<String, Object> {
        public ResponseBo() {
            put("code", 0);
            put("msg", "操作成功");
        }
        public static ResponseBo error() { return error(1, "操作失败"); }
        public static ResponseBo error(String msg) { return error(500, msg); }
        public static ResponseBo error(int code, String msg) {
            ResponseBo responseBo = new ResponseBo();
            responseBo.put("code", code);
            responseBo.put("msg", msg);
            return responseBo;
        }
        public static ResponseBo ok(String msg) {
            ResponseBo responseBo = new ResponseBo();
            responseBo.put("msg", msg);
            return responseBo;
        }
        public static ResponseBo ok(Map<String, Object> map) {
            ResponseBo responseBo = new ResponseBo();
            responseBo.putAll(map);
            return responseBo;
        }
        public static ResponseBo ok() { return new ResponseBo(); }
        @Override
        public ResponseBo put(String key, Object value) {
            super.put(key, value);
            return this;
        }
    }
3.编写密码加密工具类:
    public class MD5Utils {
        private static final String SALT = "java-developer";
        private static final String ALGORITH_NAME = "md5";
        private static final int HASH_ITERATIONS = 2;
        public static String encrypt(String pswd){
            return new SimpleHash(ALGORITH_NAME,pswd, ByteSource.Util.bytes(SALT),HASH_ITERATIONS).toHex();
        }
        public static String encrypt(String username, String pswd){
            return new SimpleHash(ALGORITH_NAME,pswd,ByteSource.Util.bytes(username + SALT),HASH_ITERATIONS).toHex();
        }
        public static void main(String[] args) {
            System.out.println(MD5Utils.encrypt("conan", "654321"));
        }
    }
4.创建数据库表结构及插入测试数据:
    CREATE TABLE T_USERR (
       ID INT(10) NOT NULL AUTO_INCREMENT,
       USERNAME VARCHAR(20) NOT NULL COMMENT '用户名',
       PASSWD VARCHAR(128) NOT NULL COMMENT '密码',
       CREATE_TIME TIMESTAMP NULL COMMENT '创建时间',
       STATUS CHAR(1) NOT NULL COMMENT '是否有效 1:有效 0:锁定',
      PRIMARY KEY (ID) USING BTREE
    )DEFAULT CHARSET=utf8;
    INSERT INTO T_USERR VALUES ('2','test','638d77f4baea419ffdcbf4ae66066a9e','2020-02-05 14:20:20','0');
    INSERT INTO T_USERR VALUES ('1','conan','b1321142a4ff9f8a4166439ef51cb854','2020-02-05 10:50:20','1');    
5.编写持久层接口及实现:
    [1]接口:
        @Mapper
        @Repository
        public interface UserMapper {
            User findByUserName(String userName);
        }
    [2]实现:
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
        <mapper namespace="com.example.authentication.mapper.UserMapper">
            <resultMap id="User" type="com.example.authentication.bean.User">
                <id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
                <id column="username" property="userName" javaType="java.lang.String" jdbcType="VARCHAR"/>
                <id column="passwd" property="password" javaType="java.lang.String" jdbcType="VARCHAR"/>
                <id column="create_time" property="createTime" javaType="java.util.Date" jdbcType="DATE"/>
                <id column="status" property="status" javaType="java.lang.String" jdbcType="VARCHAR"/>
            </resultMap>
            <select id="findByUserName" resultMap="User">
                select * from t_user where username = #{userName}
            </select>
        </mapper>
6.编写yml文件: (database,mybatis,shiro)
    spring:
      datasource:
        druid:
          #数据库访问配置，使用Druid数据源
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/shiro?serverTimezone=GMT%2B8
          username: root
          password: root
          # 连接池配置
          initial-size: 5
          min-idle: 5
          max-active: 20
          # 连接等待超时时间
          max-wait: 30000
          # 配置检测可以关闭的空闲连接间隔时间(检测空闲连接的周期)
          time-between-eviction-runs-millis: 60000
          # 配置连接在池中的最小生存时间
          min-evictable-idle-time-millis: 300000
          validation-query: select '1' from dual
          test-while-idle: true
          test-on-borrow: false
          test-on-return: false
          # 打开PSCache，并且指定每个连接上PSCache的大小
          pool-prepared-statements: true
          max-open-prepared-statements: 20
          max-pool-prepared-statement-per-connection-size: 20
          # 配置监控统计拦截的filters, 去掉后监控界面sql无法统计, 'wall'用于防火墙
          filters: stat,wall
          # Spring监控AOP切入点，如x.y.z.service.*,配置多个英文逗号分隔
          aop-patterns: com.springboot.service.*
          # WebStatFilter配置
          web-stat-filter:
            enabled: true
            # 添加过滤规则
            url-pattern: /*
            # 忽略过滤的格式
            exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
          # StatViewServlet配置
          stat-view-servlet:
            enabled: true
            # 访问路径为/druid时，跳转到StatViewServlet
            url-pattern: /druid/*
            # 是否能够重置数据
            reset-enable: false
            # 需要账号密码才能访问控制台
            login-username: admin
            login-password: admin
            # IP白名单
            # allow: 127.0.0.1
            #　IP黑名单（共同存在时，deny优先于allow）
            # deny: 192.168.1.218
    
          # 配置StatFilter
          filter:
            stat:
              log-slow-sql: true
    mybatis:
      # type-aliases扫描路径
      # type-aliases-package:
      # com xml实现扫描路径
      mapper-locations: classpath:/mapper/*.xml
      configuration:
        # sql执行日志
        log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    shiro:
      web:
        # 开启 Shiro
        enabled: true
      sessionManager:
        # 允许将 sessionId 放入 cookie
        sessionIdCookieEnabled: true
        # 允许将 sessionId 放入 URL 地址栏
        sessionIdUrlRewritingEnabled: true
      loginUrl: /login
      successUrl: /index
      unauthorizedUrl: /403
7.编写自定义Realm实现:
    public class ShiroRealm extends AuthorizingRealm {
        @Autowired
        private UserMapper userMapper;
        /**
        * 获取用户角色和权限
        */
        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
            return null;
        }
        /**
        * 登录认证
        */
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) 
            throws AuthenticationException {
            // 获取用户输入的用户名和密码
            String userName = String.valueOf(authenticationToken.getPrincipal());
            String password = new String((char[]) authenticationToken.getCredentials());
            System.out.println("用户" + userName + "认证-----ShiroRealm.doGetAuthenticationInfo");
            // 通过用户名到数据库查询用户信息
            User user = userMapper.findByUserName(userName);
            if (user == null) {
                throw new UnknownAccountException("用户名或密码错误！");
            } else if (!password.equals(user.getPassword())) {
                throw new IncorrectCredentialsException("用户名或密码错误！");
            } else if ("0".equals(user.getStatus())) {
                throw new LockedAccountException("账号已被锁定,请联系管理员！");
            }
            return new SimpleAuthenticationInfo(user, password, getName());
        }
    }
8.编写配置类:
    @Configuration
    public class ShiroConfig {
        @Bean
        public ShiroFilterChainDefinition shiroFilterChainDefinition() {
            DefaultShiroFilterChainDefinition shiroFilterChainDefinition 
                = new DefaultShiroFilterChainDefinition();
            LinkedHashMap<String,String> filterChainDefinitionMap = new LinkedHashMap<>();
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
            filterChainDefinitionMap.put("/**", "authc");
            shiroFilterChainDefinition.addPathDefinitions(filterChainDefinitionMap);
            return shiroFilterChainDefinition;
        }
        @Bean
        public DefaultWebSecurityManager securityManager() {
            // 配置SecurityManager,并注入shiroRealm
            DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
            securityManager.setRealm(shiroRealm());
            return securityManager;
        }
        @Bean
        public ShiroRealm shiroRealm() {
            return new ShiroRealm();
        }
    }
9.编写控制器类:
    @Controller
    public class LoginController {
        @GetMapping("/login")
        public String login() {return "login"; }
        @RequestMapping("/")
        public String redirectIndex() { return "redirect:/index"; }
        @PostMapping("/login")
        @ResponseBody
        public ResponseBo login(String username, String password) {
            // 密码MD5加密
            password = MD5Utils.encrypt(username, password);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            // 获取Subject对象
            Subject subject = SecurityUtils.getSubject();
            try {
                subject.login(token);
                return ResponseBo.ok();
            } catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e) {
                return ResponseBo.error(e.getMessage());
            } catch (AuthenticationException e) {
                return ResponseBo.error("认证失败！");
            }
        }
        @RequestMapping("index")
        public String index(Model model) {
            // 登录成后,即可通过Subject获取登录的用户信息
            User user = (User) SecurityUtils.getSubject().getPrincipal();
            model.addAttribute("user", user);
            return "index";
        }
    }
10.拷贝css文件,js文件,index.html,login.html到相应目录下;
11.测试:
    [1]所有的访问路径都会重定向登录页面;
    [2]在登录页面,输入错误的用户名或密码,会有弹窗提示;
    [3]当使用test用户登录时,由于该条数据的状态为0,所以提示账户锁定;
    [4]登录成功后,点击注销,重新回到登录页面;
```

```text
1.Shiro已实现的过滤器:
[1]anon: org.apache.shiro.web.filter.authc.AnonymousFilter
    匿名拦截器,即不需要登录即可访问;一般用于静态资源过滤;示例/static/**=anon
[2]authc: org.apache.shiro.web.filter.authc.FormAuthenticationFilter
    基于表单的拦截器;如/**=authc,如果没有登录会跳到相应的登录页面登录;
[3]authcBasic: org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
    Basic HTTP身份验证拦截器;
[4]logout: org.apache.shiro.web.filter.authc.LogoutFilter
    退出拦截器,主要属性: redirectUrl:退出成功后重定向的地址(/),示例/logout=logout;
[5]noSessionCreation: org.apache.shiro.web.filter.session.NoSessionCreationFilter
    不创建会话拦截器,调用subject.getSession(false)不会有什么问题,
    但是如果subject.getSession(true)将抛出DisabledSessionException异常;
[6]perms: org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter
    权限授权拦截器,验证用户是否拥有所有权限;属性和roles一样;示例/user/**=perms["user:create"]
[7]port: org.apache.shiro.web.filter.authz.PortFilter
    端口拦截器,主要属性port(80):可以通过的端口;示例/test= port[80],
    如果用户访问该页面是非80,将自动将请求端口改为80并重定向到该80端口,其他路径/参数等都一样;
[8]rest: org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter
    rest风格拦截器,自动根据请求方法构建权限字符串;示例/users=rest[user],
    会自动拼出user:read,user:create,user:update,user:delete权限字符串进行权限匹配(所有都得匹配,isPermittedAll)
[9]roles: org.apache.shiro.web.filter.authz.RolesAuthorizationFilter
    角色授权拦截器,验证用户是否拥有所有角色;示例/admin/**=roles[admin]
[10]ssl: org.apache.shiro.web.filter.authz.SslFilter
    SSL拦截器,只有请求协议是https才能通过;否则自动跳转会https端口443;其他和port拦截器一样;
[11]user: org.apache.shiro.web.filter.authc.UserFilter
    用户拦截器,用户已经身份验证/记住我登录的都可;示例/**=user
```
