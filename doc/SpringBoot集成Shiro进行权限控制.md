```text
1.�ڡ�Spring-Boot-shiro�û���֤����,ͨ���̳��Զ���Realmʵ������doGetAuthenticationInfo()����������û���֤����;
    ������ͨ��doGetAuthorizationInfo()�������Shiro��Ȩ�޿��ƹ���;
2.��ȨҲ��Ϊ���ʿ���,�ǹ�����Դ���ʵĹ���;�����ݲ�ͬ�û���Ȩ���ж����Ƿ��з�����Ӧ��Դ��Ȩ��;
    ��Shiro��,Ȩ�޿������������ĵ�Ԫ��: Ȩ��,��ɫ���û�;
```

```text
1.��ģ�����
    [1]ʹ��RBAC(Role-Based Access Control,���ڽ�ɫ�ķ��ʿ���)ģ������û�,��ɫ��Ȩ�޼�Ĺ�ϵ;
        (1)һ���û�ӵ�����ɽ�ɫ,ÿһ����ɫӵ������Ȩ��;����,�͹���ɡ��û�-��ɫ-Ȩ�ޡ�����Ȩģ��;
        (2)������ģ����,�û����ɫ֮��,��ɫ��Ȩ��֮��,һ�����Ƕ�Զ�Ĺ�ϵ;
        (3)��ϵͼ:
            T_USER -- T_USER_ROLE -- T_ROLE -- T_ROLE_PERMISSION -- T_PERMISSION
    [2]���ݿ��ṹ����������: (init.sql)
        CREATE TABLE T_PERMISSION (
        ID INT(10) NOT NULL COMMENT '����',
        URL VARCHAR(256) NULL COMMENT 'url��ַ',
        NAME VARCHAR(64) NULL COMMENT 'url����',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_PERMISSION VALUES ('1', '/user', 'user:user');
        INSERT INTO T_PERMISSION VALUES ('2', '/user/add', 'user:add');
        INSERT INTO T_PERMISSION VALUES ('3', '/user/delete', 'user:delete');
        CREATE TABLE T_ROLE (
        ID INT(10) NOT NULL COMMENT '����',
        NAME VARCHAR(32) NULL COMMENT '��ɫ����',
        MEMO VARCHAR(32) NULL COMMENT '��ɫ����',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_ROLE VALUES ('1', 'admin', '��������Ա');
        INSERT INTO T_ROLE VALUES ('2', 'test', '�����˻�');
        CREATE TABLE T_ROLE_PERMISSION (
        RID INT(10) NULL COMMENT '��ɫid',
        PID INT(10) NULL COMMENT 'Ȩ��id'
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '2');
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '3');
        INSERT INTO T_ROLE_PERMISSION VALUES ('2', '1');
        INSERT INTO T_ROLE_PERMISSION VALUES ('1', '1');
        CREATE TABLE T_USER (
        ID INT(10) NOT NULL COMMENT '����',
        USERNAME VARCHAR(20) NOT NULL COMMENT '�û���',
        PASSWD VARCHAR(128) NOT NULL COMMENT '����',
        CREATE_TIME TIMESTAMP NULL COMMENT '����ʱ��',
        STATUS CHAR(1) NOT NULL COMMENT '�Ƿ���Ч 1:��Ч  0:����',
        PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER VALUES ('2','test','638d77f4baea419ffdcbf4ae66066a9e','2020-02-05 14:20:20','1');
        INSERT INTO T_USER VALUES ('1','conan','b1321142a4ff9f8a4166439ef51cb854','2020-02-05 10:50:20','1');
        CREATE TABLE T_USER_ROLE (
        USER_ID INT(10) NULL COMMENT '�û�id',
        ROLE_ID INT(10) NULL COMMENT '��ɫid'
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER_ROLE VALUES ('1', '1');
        INSERT INTO T_USER_ROLE VALUES ('2', '2');
        (1)���������ű�: 
            �û���T_USER,��ɫ��T_ROLE,�û���ɫ������T_USER_ROLE,Ȩ�ޱ�T_PERMISSION��Ȩ�޽�ɫ������T_ROLE_PERMISSION
       (2)�û���ɫ��Ȩ��:
            �û�conan��ɫΪadmin,�û�tester��ɫΪtest;
            admin��ɫӵ���û�������Ȩ��(user:user,user:add,user:delete),��test��ɫֻӵ���û��Ĳ鿴Ȩ��(user:user)
       (3)�û����붼��123456,����Shiro�ṩ��MD5����;
2.��������ʵ����,��Ӧ�û���ɫ��Role���û�Ȩ�ޱ�Permission:
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
3.��������Mapper�ӿڼ�ʵ��,�ֱ��û���ѯ�û������н�ɫ���û�������Ȩ��:
    [1]Mapper�ӿ�:
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
    [2]ʵ��:
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
4.�����Զ���Realm:
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private UserPermissionMapper userPermissionMapper;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        String userName = user.getUserName();
        System.out.println("�û�" + userName + "��ȡȨ��-----ShiroRealm.doGetAuthorizationInfo");
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        // ��ȡ�û���ɫ��
        List<Role> roleList = userRoleMapper.findByUserName(userName);
        Set<String> roleSet = new HashSet<>();
        for (Role role : roleList) {
            roleSet.add(role.getName());
        }
        simpleAuthorizationInfo.setRoles(roleSet);
        // ��ȡ�û�Ȩ�޼�
        List<Permission> permissionList = userPermissionMapper.findByUserName(userName);
        Set<String> permissionSet = new HashSet<>();
        for (Permission permission : permissionList) {
            permissionSet.add(permission.getName());
        }
        simpleAuthorizationInfo.setStringPermissions(permissionSet);
        return simpleAuthorizationInfo;
    }
    [1]ͨ������userRoleMapper.findByUserName(userName)��userPermissionMapper.findByUserName(userName)
        ��ȡ�˵�ǰ��¼�û��Ľ�ɫ��Ȩ�޼�,Ȼ�󱣴浽SimpleAuthorizationInfo������;
    [2]��SimpleAuthorizationInfo���󷵻ظ�Shiro,����Shiro�оʹ洢�˵�ǰ�û��Ľ�ɫ��Ȩ����Ϣ��;
5.�޸�������ShiroConfig: (�����������)
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor 
            = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }
6.����UserController,���޸�LoginController:
    [1]Shiro�ṩ��һЩ��Ȩ����ص�ע��:
        (1)@RequiresAuthentication:
            ��ʾ��ǰSubject�Ѿ�ͨ��login�����������֤;��Subject.isAuthenticated()����true;
        (2)@RequiresUser:
            ��ʾ��ǰSubject�Ѿ������֤����ͨ����ס�ҵ�¼��;
        (3)@RequiresGuest:
            ��ʾ��ǰSubjectû�������֤��ͨ����ס�ҵ�¼��,�����ο����;
        (4)@RequiresRoles(value={"admin", "user"}, logical= Logical.AND):
            ��ʾ��ǰSubject��Ҫ��ɫadmin��user;
        (5)@RequiresPermissions (value={"user:a", "user:b"}, logical= Logical.OR):
            ��ʾ��ǰSubject��ҪȨ��user:a��user:b;
    [2]��дUserController,���ڴ���User��ķ�������,��ʹ��ShiroȨ��ע�����Ȩ��:
        @Controller
        @RequestMapping("/user")
        public class UserController {
            @RequiresPermissions("user:user")
            @RequestMapping("/list")
            public String userList(Model model){
                model.addAttribute("value", "��ȡ�û���Ϣ");
                return "user";
            }
            @RequiresPermissions("user:add")
            @RequestMapping("/add")
            public String userAdd(Model model){
                model.addAttribute("value", "�����û�");
                return "user";
            }
            @RequiresPermissions("user:delete")
            @RequestMapping("/delete")
            public String userDelete(Model model){
                model.addAttribute("value", "ɾ���û�");
                return "user";
            }
        }
    [3]��LoginController�����һ��/403��ת:
        @GetMapping("/403")
        public String forbid() {
            return "403";
        }
7.ǰ��ҳ��ĸ��������:
    [1]��index.html���и���,��������û�����������:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>��ҳ</title>
        </head>
        <body>
            <p>��ã�[[${user.userName}]]</p>
            <h3>Ȩ�޲�������</h3>
            <div>
                <a th:href="@{/user/list}">��ȡ�û���Ϣ</a>
                <a th:href="@{/user/add}">�����û�</a>
                <a th:href="@{/user/delete}">ɾ���û�</a>
            </div>
            <a th:href="@{/logout}">ע��</a>
        </body>
        </html>
    [2]����user.html,���û����û��Ĳ�������ӦȨ��ʱ,��ת��user.html:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>[[${value}]]</title>
        </head>
        <body>
            <p>[[${value}]]</p>
            <a th:href="@{/index}">����</a>
        </body>
        </html>
    [3]����403.html:
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="UTF-8">
            <title>����Ȩ��</title>
        </head>
        <body>
            <p>��û��Ȩ�޷��ʸ���Դ����</p>
            <a th:href="@{/index}">����</a>
        </body>
8.�쳣���⴦��:
    [1]����:
        ��ʹ��test�û����ʸ���ɾ������ʱ,δ��ת���Զ������ҳ��,������ת��springboot�Ĵ���ҳ��;
    [2]����: 
        ��yml�ļ��������˵�Ч����shiro.unauthorizedUrl=/403,����֤������ֻ��filterChain������;
        ������filterChain��������filterChainDefinitionMap.put("/user/update","perms[user:update]");,
        ����û�û��user:updateȨ��,��ô�������/user/update��ʱ��,ҳ��ᱻ�ض���/403;
    [3]���: ����һ��ȫ���쳣������������
        @ControllerAdvice
        @Order(value = Ordered.HIGHEST_PRECEDENCE)
        public class GlobalExceptionHandler {
            @ExceptionHandler(value = AuthorizationException.class)
            public String handleAuthorizationException() {
                return "403";
            }
        }
```
