```text
��Shiro�м��뻺���ʹȨ����ز��������ܿ�,����Ƶ���������ݿ��ȡȨ����Ϣ,��Ϊ����һ���û���˵,��Ȩ���ڶ�ʱ���ڻ����ǲ���仯��;
Shiro�ṩ��Cache�ĳ���,�䲢û��ֱ���ṩ��Ӧ��ʵ��,��Ϊ���Ѿ�������һ����ȫ��ܵķ�Χ;
��Shiro�п��Լ��ɳ��õĻ���ʵ��,������ܻ���Redis��Ehcache�����ʵ��;
```
# Shiro����Redis
```text
1.����Redis����:
    <!-- shiro-redis -->
    <dependency>
        <groupId>org.crazycake</groupId>
        <artifactId>shiro-redis</artifactId>
        <version>3.2.3</version>
    </dependency>
2.��application.yml�����ļ��м���Redis����:
    spring:  
      redis:
        # Redis���ݿ�������Ĭ��Ϊ0��
        database: 0
        # Redis��������ַ
        host: 127.0.0.1
        # Redis���������Ӷ˿�
        port: 6379
		# ���ݿ����ӳ�ʱʱ��,2.0 �иò���������ΪDuration,����������ʱ��ָ����λ
        timeout: 30s  
        # ���ӳ�����,2.0��ֱ��ʹ��jedis����lettuce�������ӳ�
        jedis:
          pool:
            #����������ݿ�������,�� 0 Ϊû������
            max-active: 8
            #��������ӵȴ�ʱ�䡣���������ʱ�佫�ӵ��쳣����Ϊ-1��ʾ������
            max-wait: -1
            #���ȴ������е�����,�� 0 Ϊû������
            max-idle: 8
            #��С�ȴ������е�����,�� 0 Ϊû������
            min-idle: 0
3.�޸�ShiroConfig:
    [1]���Redis���������:
        public RedisCacheManager cacheManager() {
            RedisCacheManager redisCacheManager = new RedisCacheManager();
            redisCacheManager.setRedisManager(new RedisManager());
            return redisCacheManager;
        }
    [2]��securityManager����Redis���������:
        @Bean
        public DefaultWebSecurityManager securityManager() {
            // ����SecurityManager,��ע��shiroRealm
            DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
            securityManager.setRealm(shiroRealm());
            securityManager.setRememberMeManager(rememberMeManager());
            securityManager.setCacheManager(cacheManager());
            return securityManager;
        }
```
# Shiro����Ehcache
```text
1.���Ehcache����:
    <!-- shiro ehcache -->
    <dependency>
        <groupId>org.apache.shiro</groupId>
        <artifactId>shiro-ehcache</artifactId>
        <version>1.4.2</version>
    </dependency>
    <!-- ehchache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>net.sf.ehcache</groupId>
        <artifactId>ehcache</artifactId>
    </dependency>
2.���Ehcache����: (src/main/resource/config/shiro-ehcache.xml)
	<?xml version="1.0" encoding="UTF-8"?>
	<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd"
		updateCheck="false">
		<diskStore path="java.io.tmpdir/Tmp_EhCache" />
		<defaultCache
			maxElementsInMemory="10000"
			eternal="false"
			timeToIdleSeconds="120"
			timeToLiveSeconds="120"
			overflowToDisk="false"
			diskPersistent="false"
			diskExpiryThreadIntervalSeconds="120"/>
		<!-- ��¼��¼��������1Сʱ -->
		<cache 
			name="passwordRetryCache"
			maxEntriesLocalHeap="2000"
			eternal="false"
			timeToIdleSeconds="3600"
			timeToLiveSeconds="0"
			overflowToDisk="false"
			statistics="true" />
	</ehcache>
3.ShiroConfig����Ehcache:
    [1]ע��Ehcache����:
        @Bean
        public EhCacheManager getEhCacheManager() {
            EhCacheManager em = new EhCacheManager();
            em.setCacheManagerConfigFile("classpath:config/shiro-ehcache.xml");
            return em;
        }
    [2]��������������õ�SecurityManager:
		@Bean
		public DefaultWebSecurityManager securityManager() {
			// ����SecurityManager,��ע��shiroRealm
			DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
			securityManager.setRealm(shiroRealm());
			securityManager.setRememberMeManager(rememberMeManager());
			securityManager.setCacheManager(getEhCacheManager());
			return securityManager;
		}        
```
