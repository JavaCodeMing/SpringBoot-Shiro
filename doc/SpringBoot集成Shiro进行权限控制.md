```text
1.在《Spring-Boot-shiro用户认证》中,通过继承自定义Realm实现类中doGetAuthenticationInfo()方法完成了用户认证操作;
    接下来通过doGetAuthorizationInfo()方法完成Shiro的权限控制功能;
2.授权也称为访问控制,是管理资源访问的过程;即根据不同用户的权限判断其是否有访问相应资源的权限;
    在Shiro中,权限控制有三个核心的元素: 权限,角色和用户;
```

```text
1.库模型设计
    [1]使用RBAC(Role-Based Access Control,基于角色的访问控制)模型设计用户,角色和权限间的关系;
        (1)一个用户拥有若干角色,每一个角色拥有若干权限;这样,就构造成“用户-角色-权限”的授权模型;
        (2)在这种模型中,用户与角色之间,角色与权限之间,一般者是多对多的关系;
        (3)关系图:
            T_USER -- T_USER_ROLE -- T_ROLE -- T_ROLE_PERMISSION -- T_PERMISSION
    [2]数据库表结构及测试数据: (init.sql)
        CREATE TABLE T_PERMISSION (
        ID INT(10) NOT NULL COMMENT '主键',
        URL VARCHAR(256) NULL COMMENT 'url地址',
        NAME VARCHAR(64) NULL COMMENT 'url描述',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_PERMISSION VALUES ('1', '/user', 'user:user');
        INSERT INTO T_PERMISSION VALUES ('2', '/user/add', 'user:add');
        INSERT INTO T_PERMISSION VALUES ('3', '/user/delete', 'user:delete');
        CREATE TABLE T_ROLE (
        ID INT(10) NOT NULL COMMENT '主键',
        NAME VARCHAR(32) NULL COMMENT '角色名称',
        MEMO VARCHAR(32) NULL COMMENT '角色描述',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_ROLE VALUES ('1', 'admin', '超级管理员');
        INSERT INTO T_ROLE VALUES ('2', 'test', '测试账户');
        CREATE TABLE T_ROLE_PERMISSION (
        RID INT(10) NULL COMMENT '角色id',
        PID INT(10) NULL COMMENT '权限id'
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '2');
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '3');
        INSERT INTO T_ROLE_PERMISSION VALUES ('2', '1');
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '1');
        CREATE TABLE T_USER (
        ID INT(10) NOT NULL COMMENT '主键',
        USERNAME VARCHAR(20) NOT NULL COMMENT '用户名',
        PASSWD VARCHAR(128) NOT NULL COMMENT '密码',
        CREATE_TIME TIMESTAMP NULL COMMENT '创建时间',
        STATUS CHAR(1) NOT NULL COMMENT '是否有效 1:有效  0:锁定',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER VALUES ('2','test','638d77f4baea419ffdcbf4ae66066a9e','2020-02-05 14:20:20','1');
        INSERT INTO T_USER VALUES ('1','conan','b1321142a4ff9f8a4166439ef51cb854','2020-02-05 10:50:20','1');
        CREATE TABLE T_USER_ROLE (
        USER_ID INT(10) NULL COMMENT '用户id',
        ROLE_ID INT(10) NULL COMMENT '角色id'
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER_ROLE VALUES ('1', '1');
        INSERT INTO T_USER_ROLE VALUES ('2', '2');
        (1)创建了五张表: 
            用户表T_USER,角色表T_ROLE,用户角色关联表T_USER_ROLE,权限表T_PERMISSION和权限角色关联表T_ROLE_PERMISSION
       (2)用户角色与权限:
            用户conan角色为admin,用户tester角色为test;
            admin角色拥有用户的所有权限(user:user,user:add,user:delete),而test角色只拥有用户的查看权限(user:user)
       (3)用户密码都是123456,经过Shiro提供的MD5加密;
2.创建两个实体类,对应用户角色表Role和用户权限表Permission:
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Role implements Serializable {
        private Integer id;
        private String name;
        private String memo;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Permission implements Serializable {
        private Integer id;
        private String url;
        private String name;
    }
3.创建两个Mapper接口及实现,分别用户查询用户的所有角色和用户的所有权限:
    [1]Mapper接口:
        @Mapper
        @Repository
        public interface UserRoleMapper {
            List<Role> findByUserName(String userName);
        }
        @Mapper
        @Repository
        public interface UserPermissionMapper {
            List<Permission> findByUserName(String userName);
        }
    [2]实现:
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
        <mapper namespace="com.example.authorization.mapper.UserRoleMapper">
            <resultMap id="role" type="com.example.authorization.bean.Role">
                <id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
                <id column="name" property="name" javaType="java.lang.String" jdbcType="VARCHAR"/>
                <id column="memo" property="memo" javaType="java.lang.String" jdbcType="VARCHAR"/>
            </resultMap>
            <select id="findByUserName" resultMap="role">
                select tr.ID,tr.NAME,tr.MEMO from t_role tr
                left join t_user_role tur on tr.ID = tur.ROLE_ID
                left join t_user tu on tu.ID = tur.USER_ID
                where tu.USERNAME = #{userName}
            </select>
        </mapper>
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
        <mapper namespace="com.example.authorization.mapper.UserPermissionMapper">
            <resultMap id="permission" type="com.example.authorization.bean.Permission">
                <id property="id" column="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
                <id property="url" column="url" javaType="java.lang.String" jdbcType="VARCHAR"/>
                <id property="name" column="name" javaType="java.lang.String" jdbcType="VARCHAR"/>
            </resultMap>
            <select id="findByUserName" resultMap="permission">
                select tp.ID,tp.URL,tp.NAME from t_role tr
                left join t_user_role tur on tr.ID = tur.ROLE_ID
                left join t_user tu on tur.USER_ID = tu.ID
                left join t_role_permission trp on trp.RID = tr.ID
                left join t_permission tp on trp.PID = tp.ID
                where tu.USERNAME = #{userName}
            </select>
        </mapper>
4.改造自定义Realm:
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private UserPermissionMapper userPermissionMapper;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        String userName = user.getUserName();
        System.out.println("用户" + userName + "获取权限-----ShiroRealm.doGetAuthorizationInfo");
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // 获取用户角色集
        List<Role> roleList = userRoleMapper.findByUserName(userName);
        Set<String> roleSet = new HashSet<>();
        for (Role role : roleList) {
            roleSet.add(role.getName());
        }
        simpleAuthorizationInfo.setRoles(roleSet);
        // 获取用户权限集
        List<Permission> permissionList = userPermissionMapper.findByUserName(userName);
        Set<String> permissionSet = new HashSet<>();
        for (Permission permission : permissionList) {
            permissionSet.add(permission.getName());
        }
        simpleAuthorizationInfo.setStringPermissions(permissionSet);
        return simpleAuthorizationInfo;
    }
    [1]通过方法userRoleMapper.findByUserName(userName)和userPermissionMapper.findByUserName(userName)
        获取了当前登录用户的角色和权限集,然后保存到SimpleAuthorizationInfo对象中;
    [2]将SimpleAuthorizationInfo对象返回给Shiro,这样Shiro中就存储了当前用户的角色和权限信息了;
5.修改配置类ShiroConfig: (添加以下内容)
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor 
            = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }
6.新增UserController,并修改LoginController:
    [1]Shiro提供的一些和权限相关的注解:
        (1)@RequiresAuthentication:
            表示当前Subject已经通过login进行了身份验证;即Subject.isAuthenticated()返回true;
        (2)@RequiresUser:
            表示当前Subject已经身份验证或者通过记住我登录的;
        (3)@RequiresGuest:
            表示当前Subject没有身份验证或通过记住我登录过,即是游客身份;
        (4)@RequiresRoles(value={"admin", "user"}, logical= Logical.AND):
            表示当前Subject需要角色admin和user;
        (5)@RequiresPermissions (value={"user:a", "user:b"}, logical= Logical.OR):
            表示当前Subject需要权限user:a或user:b;
    [2]编写UserController,用于处理User类的访问请求,并使用Shiro权限注解控制权限:
        @Controller
        @RequestMapping("/user")
        public class UserController {
            @RequiresPermissions("user:user")
            @RequestMapping("/list")
            public String userList(Model model){
                model.addAttribute("value", "获取用户信息");
                return "user";
            }
            @RequiresPermissions("user:add")
            @RequestMapping("/add")
            public String userAdd(Model model){
                model.addAttribute("value", "新增用户");
                return "user";
            }
            @RequiresPermissions("user:delete")
            @RequestMapping("/delete")
            public String userDelete(Model model){
                model.addAttribute("value", "删除用户");
                return "user";
            }
        }
    [3]在LoginController中添加一个/403跳转:
        @GetMapping("/403")
        public String forbid() {
            return "403";
        }
7.前端页面的改造与添加:
    [1]对index.html进行改造,添加三个用户操作的链接:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>首页</title>
        </head>
        <body>
            <p>你好！[[${user.userName}]]</p>
            <h3>权限测试链接</h3>
            <div>
                <a th:href="@{/user/list}">获取用户信息</a>
                <a th:href="@{/user/add}">新增用户</a>
                <a th:href="@{/user/delete}">删除用户</a>
            </div>
            <a th:href="@{/logout}">注销</a>
        </body>
        </html>
    [2]新增user.html,当用户对用户的操作有相应权限时,跳转到user.html:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>[[${value}]]</title>
        </head>
        <body>
            <p>[[${value}]]</p>
            <a th:href="@{/index}">返回</a>
        </body>
        </html>
    [3]新增403.html:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>暂无权限</title>
        </head>
        <body>
            <p>您没有权限访问该资源！！</p>
            <a th:href="@{/index}">返回</a>
        </body>
8.异常问题处理:
    [1]问题:
        在使用test用户访问更新删除方法时,未跳转到自定义错误页面,而是跳转到springboot的错误页面;
    [2]分析: 
        在yml文件中配置了等效属性shiro.unauthorizedUrl=/403,经验证该设置只对filterChain起作用;
        比如在filterChain中设置了filterChainDefinitionMap.put("/user/update","perms[user:update]");,
        如果用户没有user:update权限,那么当其访问/user/update的时候,页面会被重定向到/403;
    [3]解决: 定义一个全局异常捕获类来处理
        @ControllerAdvice
        @Order(value = Ordered.HIGHEST_PRECEDENCE)
        public class GlobalExceptionHandler {
            @ExceptionHandler(value = AuthorizationException.class)
            public String handleAuthorizationException() {
                return "403";
            }
        }
```
