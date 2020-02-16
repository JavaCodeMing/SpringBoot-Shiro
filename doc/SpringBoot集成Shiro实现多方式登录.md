```
1.多方式验证登录: 当一个用户可通过不同的登录方式进行登录或不同用户对应不同身份进行登录时,
    需要根据登录方式或用户身份使用不同的验证逻辑;
2.多方式验证登录的简单应用场景:
    [1]系统用户表结构包含用户名,用户手机号,用户密码等信息,此时可为系统提供用户名+密码和用户手机号+验证码等登录方式;
    [2]不同的用户选择不同的身份进行登录,如学校教务系统根据学生和老师的身份,进行不同的验证;
3.在《Spring Boot Shiro登录保护》的基础上,通过用户名+密码和手机号+密码的方式简单实现多方式验证登录;
```

```text
1.修改用户表的表结构: (添加手机号的字段)
    [1]重新建表导入:
        CREATE TABLE T_USER (
            ID INT(10) NOT NULL COMMENT '主键',
            USERNAME VARCHAR(20) NOT NULL COMMENT '用户名',
            PASSWD VARCHAR(128) NOT NULL COMMENT '密码',
            PHONE VARCHAR(20) NULL COMMENT '手机号',
            CREATE_TIME TIMESTAMP NULL COMMENT '创建时间',
            STATUS CHAR(1) NOT NULL COMMENT '是否有效 1:有效  0:锁定',
            PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER VALUES ('2','test','94b642a2b096f2559933bc5731722c46','17622228081','2020-02-05 14:20:20','1');
        INSERT INTO T_USER VALUES ('1','conan','94b642a2b096f2559933bc5731722c46','17611118080','2020-02-05 10:50:20','1');
    [2]修改实体类User:
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public class User implements Serializable {
            private Integer id;
            private String userName;
            private String password;
            private String phone;
            private Date createTime;
            private String status;
        }
    [3]修改UserMapper接口和实现:
        (1)接口: (添加通过手机号查询用户信息的方法)
            @Mapper
            @Repository
            public interface UserMapper {
                User findByUserName(String userName);
                User findByPhone(String phone);
            }
        (2)实现: (更新字段并添加新方法的实现)
			<?xml version="1.0" encoding="UTF-8"?>
			<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
			<mapper namespace="com.example.logintypes.mapper.UserMapper">
				<resultMap id="User" type="com.example.logintypes.bean.User">
					<id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
					<id column="username" property="userName" javaType="java.lang.String" jdbcType="VARCHAR"/>
					<id column="passwd" property="password" javaType="java.lang.String" jdbcType="VARCHAR"/>
					<id column="phone" property="phone" javaType="java.lang.String" jdbcType="VARCHAR"/>
					<id column="create_time" property="createTime" javaType="java.util.Date" jdbcType="DATE"/>
					<id column="status" property="status" javaType="java.lang.String" jdbcType="VARCHAR"/>
				</resultMap>
				<select id="findByUserName" resultMap="User">
					select * from t_user where username = #{userName}
				</select>
				<select id="findByPhone" resultMap="User">
					select * from t_user where phone = #{phone}
				</select>
			</mapper>            
2.编写登录类型的枚举类:
    public enum LoginType {
        USERNAME, PHONE;
    }
3.创建UserToken继承UsernamePasswordToken: (用于提供登录类型判断)
	public class UserToken extends UsernamePasswordToken {
		private LoginType loginType;
		public UserToken(String username, String password, 
			boolean rememberMe, LoginType loginType) {
			super(username, password, rememberMe);
			this.loginType = loginType;
		}
		public LoginType getLoginType() {
			return loginType;
		}
		public void setLoginType(LoginType loginType) {
			this.loginType = loginType;
		}
	}    
4.修改ShiroRealm为BaseShiroRealm并将 doGetAuthenticationInfo 交由子类实现:
    [1]编写子类UserNameRealm:
		public class UserNameRealm extends BaseShiroRealm{
			@Autowired
			private UserMapper userMapper;
			@Override
			protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
				throws AuthenticationException {
				UserToken userToken = (UserToken) authenticationToken;
				if (userToken.getLoginType() == LoginType.USERNAME) {
					// 获取用户输入的用户名和密码
					String userName = String.valueOf(userToken.getPrincipal());
					String password = new String((char[]) userToken.getCredentials());
					System.out.println("用户" + userName + "认证-----UserNameRealm.doGetAuthenticationInfo");
					// 通过用户名到数据库查询用户信息
					User user = userMapper.findByUserName(userName);
					if (user == null || !password.equals(user.getPassword())) {
						throw new UnknownAccountException("用户名或密码错误！");
					} else if ("0".equals(user.getStatus())) {
						throw new LockedAccountException("账号已被锁定,请联系管理员！");
					}
					// 验证通过返回一个封装了用户信息的AuthenticationInfo实例即可
					return new SimpleAuthenticationInfo(user, password, getName());
				}else {
					return null;
				}
			}
		}
	[2]编写子类PhoneRealm:
		public class PhoneRealm extends BaseShiroRealm {
			@Autowired
			private UserMapper userMapper;
			@Override
			protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) 
				throws AuthenticationException {
				UserToken userToken = (UserToken) authenticationToken;
				if (userToken.getLoginType() == LoginType.PHONE) {
					// 获取用户输入的手机号和密码
					String phone = String.valueOf(authenticationToken.getPrincipal());
					String password = new String((char[]) authenticationToken.getCredentials());
					System.out.println("手机号" + phone + "认证-----PhoneRealm.doGetAuthenticationInfo");
					// 通过手机号到数据库查询用户信息
					User user = userMapper.findByPhone(phone);
					if (user == null || !password.equals(user.getPassword())) {
						throw new UnknownAccountException("手机号或密码错误！");
					} else if ("0".equals(user.getStatus())) {
						throw new LockedAccountException("账号已被锁定,请联系管理员！");
					}
					// 验证通过返回一个封装了用户信息的AuthenticationInfo实例即可
					return new SimpleAuthenticationInfo(user, password, getName());
				}else {
					return null;
				}
			}
		}	    
5.修改配置类ShiroConfig:
    [1]使用新的realm替换原来的realm:
        @Bean
        BaseShiroRealm userNameRealm() {
            return new UserNameRealm();
        }
        @Bean
        BaseShiroRealm phoneRealm() {
            return new PhoneRealm();
        }
    [2]用新的realm替换原来的realm注入SecurityManager:
		@Bean
		public DefaultWebSecurityManager securityManager() {
			// 配置SecurityManager,并注入shiroRealm
			DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
			List<Realm> realmList = new ArrayList<>();
			realmList.add(userNameRealm());
			realmList.add(phoneRealm());
			securityManager.setRealms(realmList);
			securityManager.setRememberMeManager(rememberMeManager());
			securityManager.setCacheManager(cacheManager());
			securityManager.setSessionManager(sessionManager());
			return securityManager;
		}
6.修改登录接口并分别测试用户名和手机号登录: 
	(由于不想改前端页面,所以使用手机号登录时传参依旧用username,但实际上是手机号)
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByUserName(String username, String password, Boolean rememberMe) {
        // 密码MD5加密
        password = MD5Utils.encrypt(username, password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.USERNAME);
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
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByPhoneNum(String username, String password, Boolean rememberMe) {
        // 密码MD5加密
        password = MD5Utils.encrypt(password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.PHONE);
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
```
