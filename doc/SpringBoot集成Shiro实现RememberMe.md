```text
1.接着《Spring-Boot-shiro用户认证》,当用户成功登录后,关闭浏览器然后再打开浏览器访问
    http://localhost:8080/index,页面会跳转到登录页,之前的登录因为浏览器的关闭已经失效;
2.Shiro为我们提供了Remember Me的功能,用户的登录状态不会因为浏览器的关闭而失效,直到Cookie过期;
```
```text
1.修改ShiroConfig: 
    [1]添加以下内容:
        public SimpleCookie rememberMeCookie() {
            // 设置cookie名称,对应login.html页面的<input type="checkbox" name="rememberMe"/>
            SimpleCookie cookie = new SimpleCookie("rememberMe");
            // 设置cookie的过期时间,单位为秒,这里为一天
            cookie.setMaxAge(86400);
            return cookie;
        }
        public CookieRememberMeManager rememberMeManager() {
            CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
            cookieRememberMeManager.setCookie(rememberMeCookie());
            // rememberMe cookie加密的密钥
            cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
            return cookieRememberMeManager;
        }
    [2]将cookie管理对象设置到SecurityManager中:
        @Bean
        public DefaultWebSecurityManager securityManager() {
            // 配置SecurityManager,并注入shiroRealm
            DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
            securityManager.setRealm(shiroRealm());
            securityManager.setRememberMeManager(rememberMeManager());
            return securityManager;
        }
    [3]修改权限配置:
        将shiroFilterChainDefinition()中的filterChainDefinitionMap.put("/**", "authc")
        换成filterChainDefinitionMap.put("/**", "user")
2.修改login.html: (添加Remember Me checkbox及请求访问传参)
     <!DOCTYPE html>
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>登录</title>
        <link rel="stylesheet" th:href="@{/css/login.css}" type="text/css">
        <script th:src="@{/js/jquery-1.11.1.min.js}"></script>
    </head>
    <body>
    <div class="login-page">
        <div class="form">
            <input type="text" placeholder="用户名" name="username" required="required"/>
            <input type="password" placeholder="密码" name="password" required="required"/>
            <p><input type="checkbox" name="rememberMe" />记住我</p>
            <button onclick="login()">登录</button>
        </div>
    </div>
    </body>
    <script th:inline="javascript">
        function login() {
            var username = $("input[name='username']").val();
            var password = $("input[name='password']").val();
            var rememberMe = $("input[name='rememberMe']").is(':checked');
            $.ajax({
                type: "post",
                url: "/login",
                data: {"username": username, "password": password, "rememberMe": rememberMe},
                dataType: "json",
                success: function (r) {
                    if (r.code == 0) {
                        location.href = '/index';
                    } else {
                        alert(r.msg);
                    }
                }
            });
        }
    </script>
    </html>
3.修改LoginController的login方法:
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo login(String username, String password, Boolean rememberMe) {
        // 密码MD5加密
        password = MD5Utils.encrypt(username, password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
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
