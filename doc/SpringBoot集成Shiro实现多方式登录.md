```
1.�෽ʽ��֤��¼: ��һ���û���ͨ����ͬ�ĵ�¼��ʽ���е�¼��ͬ�û���Ӧ��ͬ��ݽ��е�¼ʱ,
    ��Ҫ���ݵ�¼��ʽ���û����ʹ�ò�ͬ����֤�߼�;
2.�෽ʽ��֤��¼�ļ�Ӧ�ó���:
    [1]ϵͳ�û���ṹ�����û���,�û��ֻ���,�û��������Ϣ,��ʱ��Ϊϵͳ�ṩ�û���+������û��ֻ���+��֤��ȵ�¼��ʽ;
    [2]��ͬ���û�ѡ��ͬ����ݽ��е�¼,��ѧУ����ϵͳ����ѧ������ʦ�����,���в�ͬ����֤;
3.�ڡ�Spring Boot Shiro��¼�������Ļ�����,ͨ���û���+������ֻ���+����ķ�ʽ��ʵ�ֶ෽ʽ��֤��¼;
```

```text
1.�޸��û���ı�ṹ: (����ֻ��ŵ��ֶ�)
    [1]���½�����:
        CREATE TABLE T_USER (
            ID INT(10) NOT NULL COMMENT '����',
            USERNAME VARCHAR(20) NOT NULL COMMENT '�û���',
            PASSWD VARCHAR(128) NOT NULL COMMENT '����',
            PHONE VARCHAR(20) NULL COMMENT '�ֻ���',
            CREATE_TIME TIMESTAMP NULL COMMENT '����ʱ��',
            STATUS CHAR(1) NOT NULL COMMENT '�Ƿ���Ч 1:��Ч  0:����',
            PRIMARY KEY (ID) USING BTREE
        )DEFAULT CHARSET=utf8;
        INSERT INTO T_USER VALUES ('2','test','94b642a2b096f2559933bc5731722c46','17622228081','2020-02-05 14:20:20','1');
        INSERT INTO T_USER VALUES ('1','conan','94b642a2b096f2559933bc5731722c46','17611118080','2020-02-05 10:50:20','1');
    [2]�޸�ʵ����User:
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
    [3]�޸�UserMapper�ӿں�ʵ��:
        (1)�ӿ�: (���ͨ���ֻ��Ų�ѯ�û���Ϣ�ķ���)
            @Mapper
            @Repository
            public interface UserMapper {
                User findByUserName(String userName);
                User findByPhone(String phone);
            }
        (2)ʵ��: (�����ֶβ�����·�����ʵ��)
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
2.��д��¼���͵�ö����:
    public enum LoginType {
        USERNAME, PHONE;
    }
3.����UserToken�̳�UsernamePasswordToken: (�����ṩ��¼�����ж�)
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
4.�޸�ShiroRealmΪBaseShiroRealm���� doGetAuthenticationInfo ��������ʵ��:
    [1]��д����UserNameRealm:
		public class UserNameRealm extends BaseShiroRealm{
			@Autowired
			private UserMapper userMapper;
			@Override
			protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
				throws AuthenticationException {
				UserToken userToken = (UserToken) authenticationToken;
				if (userToken.getLoginType() == LoginType.USERNAME) {
					// ��ȡ�û�������û���������
					String userName = String.valueOf(userToken.getPrincipal());
					String password = new String((char[]) userToken.getCredentials());
					System.out.println("�û�" + userName + "��֤-----UserNameRealm.doGetAuthenticationInfo");
					// ͨ���û��������ݿ��ѯ�û���Ϣ
					User user = userMapper.findByUserName(userName);
					if (user == null || !password.equals(user.getPassword())) {
						throw new UnknownAccountException("�û������������");
					} else if ("0".equals(user.getStatus())) {
						throw new LockedAccountException("�˺��ѱ�����,����ϵ����Ա��");
					}
					// ��֤ͨ������һ����װ���û���Ϣ��AuthenticationInfoʵ������
					return new SimpleAuthenticationInfo(user, password, getName());
				}else {
					return null;
				}
			}
		}
	[2]��д����PhoneRealm:
		public class PhoneRealm extends BaseShiroRealm {
			@Autowired
			private UserMapper userMapper;
			@Override
			protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) 
				throws AuthenticationException {
				UserToken userToken = (UserToken) authenticationToken;
				if (userToken.getLoginType() == LoginType.PHONE) {
					// ��ȡ�û�������ֻ��ź�����
					String phone = String.valueOf(authenticationToken.getPrincipal());
					String password = new String((char[]) authenticationToken.getCredentials());
					System.out.println("�ֻ���" + phone + "��֤-----PhoneRealm.doGetAuthenticationInfo");
					// ͨ���ֻ��ŵ����ݿ��ѯ�û���Ϣ
					User user = userMapper.findByPhone(phone);
					if (user == null || !password.equals(user.getPassword())) {
						throw new UnknownAccountException("�ֻ��Ż��������");
					} else if ("0".equals(user.getStatus())) {
						throw new LockedAccountException("�˺��ѱ�����,����ϵ����Ա��");
					}
					// ��֤ͨ������һ����װ���û���Ϣ��AuthenticationInfoʵ������
					return new SimpleAuthenticationInfo(user, password, getName());
				}else {
					return null;
				}
			}
		}	    
5.�޸�������ShiroConfig:
    [1]ʹ���µ�realm�滻ԭ����realm:
        @Bean
        BaseShiroRealm userNameRealm() {
            return new UserNameRealm();
        }
        @Bean
        BaseShiroRealm phoneRealm() {
            return new PhoneRealm();
        }
    [2]���µ�realm�滻ԭ����realmע��SecurityManager:
		@Bean
		public DefaultWebSecurityManager securityManager() {
			// ����SecurityManager,��ע��shiroRealm
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
6.�޸ĵ�¼�ӿڲ��ֱ�����û������ֻ��ŵ�¼: 
	(���ڲ����ǰ��ҳ��,����ʹ���ֻ��ŵ�¼ʱ����������username,��ʵ�������ֻ���)
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByUserName(String username, String password, Boolean rememberMe) {
        // ����MD5����
        password = MD5Utils.encrypt(username, password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.USERNAME);
        // ��ȡSubject����
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return ResponseBo.ok();
        } catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e) {
            return ResponseBo.error(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseBo.error("��֤ʧ�ܣ�");
        }
    }
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo loginByPhoneNum(String username, String password, Boolean rememberMe) {
        // ����MD5����
        password = MD5Utils.encrypt(password);
        UserToken token = new UserToken(username, password, rememberMe, LoginType.PHONE);
        // ��ȡSubject����
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return ResponseBo.ok();
        } catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e) {
            return ResponseBo.error(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseBo.error("��֤ʧ�ܣ�");
        }
    }
```
