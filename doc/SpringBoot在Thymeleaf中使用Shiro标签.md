```text
1.在《Spring-Boot-shiro权限控制》中,当用户访问没有权限的资源时,采取的做法是跳转到403页面,
	但在实际项目中是只显示当前用户拥有访问权限的资源链接;
2.配合Thymeleaf中的Shiro标签可以很简单的实现只显示当前用户拥有访问权限的资源链接;
3.实际上Thymeleaf官方并没有提供Shiro的标签,需要引入第三方实现,
	地址为https://github.com/theborakompanioni/thymeleaf-extras-shiro
```
```text
1.引入thymeleaf-extras-shiro依赖:
    <dependency>
        <groupId>com.github.theborakompanioni</groupId>
        <artifactId>thymeleaf-extras-shiro</artifactId>
        <version>2.0.0</version>
    </dependency>
2.在ShiroConfig中配置方言标签:
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }
3.改造index.html: 
    <!DOCTYPE html>
    <html xmlns:th="http://www.thymeleaf.org" 
    	xmlns:shiro="http://www.pollix.at/thymeleaf/shiro" >
    <head>
    	<meta charset="UTF-8">
    	<title>首页</title>
    </head>
    <body>
    	<p>你好！[[${user.userName}]]</p>
    	<p shiro:hasRole="admin">你的角色为超级管理员</p>
    	<p shiro:hasRole="test">你的角色为测试账户</p>
    	<div>
    	    <a shiro:hasPermission="user:user" th:href="@{/user/list}">获取用户信息</a>
    	    <a shiro:hasPermission="user:add" th:href="@{/user/add}">新增用户</a>
    	    <a shiro:hasPermission="user:delete" th:href="@{/user/delete}">删除用户</a>
    	</div>
    	<a th:href="@{/logout}">注销</a>
    </body>
    </html>
    (使用Shiro标签需要给html标签添加: xmlns:shiro="http://www.pollix.at/thymeleaf/shiro")
4.更多shiro标签:(http://shiro.apache.org/web.html#Web-JSP%252FGSPTagLibrary)
    [1]可以写成属性,也可写成元素标签:
        (1)属性:
            <p shiro:anyTag> Goodbye cruel World! </p>
        (2)元素标签: 
            <shiro:anyTag> <p>Hello World!</p> </shiro:anyTag>
    [2]guest标签: 用户没有身份验证时显示相应信息,即游客访问信息
        <p shiro:guest=""> 游客也访问的资源 </p>
    [3]user标签: 用户已经身份验证/记住我登录后显示相应的信息
        <p shiro:user="">用户已经通过认证\记住我 登录后可访问的资源</p>
    [4]authenticated标签: 用户已经身份验证通过,即Subject.login登录成功,且不是记住我登录的
        <a shiro:authenticated="" href="updateAccount.html">身份验证通过,且不是记住我登录可访问的资源</a>
    [5]notAuthenticated标签: 用户未进行身份验证,即没有调用Subject.login进行登录,包括"记住我"也属于未进行身份验证
        <p shiro:notAuthenticated=""> 未身份验证(包括"记住我")可访问的资源 </p>
    [6]principal标签: 显示用户身份信息,默认调用Subjec.getPrincipal()获取,即Primary Principal
        <p>Hello, <span shiro:principal=""></span>, how are you today?</p>
        <p>Hello, <shiro:principal/>, how are you today?</p>
    [7]hasRole标签: 如果当前Subject有指定的角色,则显示body体内的内容
        <a shiro:hasRole="administrator" href="admin.html">角色Administer可访问的资源</a>
    [8]hasAnyRoles标签: 如果Subject有任意一个指定的角色,则显示body体里的内容
        <p shiro:hasAllRoles="developer, project manager">角色developer或project manager可访问的资源</p>
    [9]hasAllRoles标签: 如果当前 Subjec有指定所有角色,则显示body体内的内容
        <p shiro:hasAllRoles="developer, project manager">有角色developer和project manager才可访问的资源</p>
    [10]lacksRole标签: 如果当前 Subjec没有指定角色,则显示body体内的内容
        <p shiro:lacksRole="administrator">不是角色administrator时可访问的资源</p>
    [11]hasPermission标签: 如果当前Subject有指定权限,则显示body体内容
        <a shiro:hasPermission="user:create" href="createUser.html">有指定权限才可访问的资源</a>
    [12]lacksPermission标签: 如果当前Subject没有指定权限,则显示body体内容
        <p shiro:lacksPermission="user:delete">没有指定权限时可访问的资源</p>
    [13]hasAllPermissions标签: 如果当前Subject有指定的所有权限,则显示body体内容
        <p shiro:hasAllPermissions="user:create, user:delete">同时拥有新增和删除权限才可访问的资源</p>
    [14]hasAnyPermissions标签:如果Subject有任意一个指定的权限,则显示body体里的内容
        <p shiro:hasAnyPermissions="user:create, user:delete">拥有新增或删除权限时可访问的资源</p>
```
