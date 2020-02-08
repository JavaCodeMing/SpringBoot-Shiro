```text
在Shiro中加入缓存可使权限相关操作尽可能快,避免频繁访问数据库获取权限信息,因为对于一个用户来说,其权限在短时间内基本是不会变化的;
Shiro提供了Cache的抽象,其并没有直接提供相应的实现,因为这已经超出了一个安全框架的范围;
在Shiro中可以集成常用的缓存实现,这里介绍基于Redis和Ehcache缓存的实现;
```
# Shiro集成Redis
```text
1.引入Redis依赖:
    <!-- shiro-redis -->
    <dependency>
        <groupId>org.crazycake</groupId>
        <artifactId>shiro-redis</artifactId>
        <version>3.2.3</version>
    </dependency>
2.在application.yml配置文件中加入Redis配置:
    spring:  
      redis:
        # Redis数据库索引（默认为0）
        database: 0
        # Redis服务器地址
        host: 127.0.0.1
        # Redis服务器连接端口
        port: 6379
		# 数据库连接超时时间,2.0 中该参数的类型为Duration,这里在配置时需指明单位
        timeout: 30s  
        # 连接池配置,2.0中直接使用jedis或者lettuce配置连接池
        jedis:
          pool:
            #最大连接数据库连接数,设 0 为没有限制
            max-active: 8
            #最大建立连接等待时间。如果超过此时间将接到异常。设为-1表示无限制
            max-wait: -1
            #最大等待连接中的数量,设 0 为没有限制
            max-idle: 8
            #最小等待连接中的数量,设 0 为没有限制
            min-idle: 0
3.修改ShiroConfig:
    [1]添加Redis缓存管理器:
        public RedisCacheManager cacheManager() {
            RedisCacheManager redisCacheManager = new RedisCacheManager();
            redisCacheManager.setRedisManager(new RedisManager());
            return redisCacheManager;
        }
    [2]给securityManager设置Redis缓存管理器:
        @Bean
        public DefaultWebSecurityManager securityManager() {
            // 配置SecurityManager,并注入shiroRealm
            DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
            securityManager.setRealm(shiroRealm());
            securityManager.setRememberMeManager(rememberMeManager());
            securityManager.setCacheManager(cacheManager());
            return securityManager;
        }
```
# Shiro集成Ehcache
```text
1.添加Ehcache依赖:
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
2.添加Ehcache配置: (src/main/resource/config/shiro-ehcache.xml)
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
		<!-- 登录记录缓存锁定1小时 -->
		<cache 
			name="passwordRetryCache"
			maxEntriesLocalHeap="2000"
			eternal="false"
			timeToIdleSeconds="3600"
			timeToLiveSeconds="0"
			overflowToDisk="false"
			statistics="true" />
	</ehcache>
3.ShiroConfig配置Ehcache:
    [1]注入Ehcache缓存:
        @Bean
        public EhCacheManager getEhCacheManager() {
            EhCacheManager em = new EhCacheManager();
            em.setCacheManagerConfigFile("classpath:config/shiro-ehcache.xml");
            return em;
        }
    [2]将缓存管理器设置到SecurityManager:
		@Bean
		public DefaultWebSecurityManager securityManager() {
			// 配置SecurityManager,并注入shiroRealm
			DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
			securityManager.setRealm(shiroRealm());
			securityManager.setRememberMeManager(rememberMeManager());
			securityManager.setCacheManager(getEhCacheManager());
			return securityManager;
		}        
```
