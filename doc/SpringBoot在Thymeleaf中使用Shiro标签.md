```text
1.�ڡ�Spring-Boot-shiroȨ�޿��ơ���,���û�����û��Ȩ�޵���Դʱ,��ȡ����������ת��403ҳ��,
	����ʵ����Ŀ����ֻ��ʾ��ǰ�û�ӵ�з���Ȩ�޵���Դ����;
2.���Thymeleaf�е�Shiro��ǩ���Ժܼ򵥵�ʵ��ֻ��ʾ��ǰ�û�ӵ�з���Ȩ�޵���Դ����;
3.ʵ����Thymeleaf�ٷ���û���ṩShiro�ı�ǩ,��Ҫ���������ʵ��,
	��ַΪhttps://github.com/theborakompanioni/thymeleaf-extras-shiro
```
```text
1.����thymeleaf-extras-shiro����:
    <dependency>
        <groupId>com.github.theborakompanioni</groupId>
        <artifactId>thymeleaf-extras-shiro</artifactId>
        <version>2.0.0</version>
    </dependency>
2.��ShiroConfig�����÷��Ա�ǩ:
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }
3.����index.html: 
	<!DOCTYPE html>
	<html xmlns:th="http://www.thymeleaf.org" 
		xmlns:shiro="http://www.pollix.at/thymeleaf/shiro" >
	<head>
		<meta charset="UTF-8">
		<title>��ҳ</title>
	</head>
	<body>
		<p>��ã�[[${user.userName}]]</p>
		<p shiro:hasRole="admin">��Ľ�ɫΪ��������Ա</p>
		<p shiro:hasRole="test">��Ľ�ɫΪ�����˻�</p>
		<div>
			<a shiro:hasPermission="user:user" th:href="@{/user/list}">��ȡ�û���Ϣ</a>
			<a shiro:hasPermission="user:add" th:href="@{/user/add}">�����û�</a>
			<a shiro:hasPermission="user:delete" th:href="@{/user/delete}">ɾ���û�</a>
		</div>
		<a th:href="@{/logout}">ע��</a>
	</body>
	</html>
	(ʹ��Shiro��ǩ��Ҫ��html��ǩ���: xmlns:shiro="http://www.pollix.at/thymeleaf/shiro")
4.����shiro��ǩ:(http://shiro.apache.org/web.html#Web-JSP%252FGSPTagLibrary)
    [1]����д������,Ҳ��д��Ԫ�ر�ǩ:
        (1)����:
            <p shiro:anyTag> Goodbye cruel World! </p>
        (2)Ԫ�ر�ǩ: 
            <shiro:anyTag> <p>Hello World!</p> </shiro:anyTag>
    [2]guest��ǩ: �û�û�������֤ʱ��ʾ��Ӧ��Ϣ,���οͷ�����Ϣ
        <p shiro:guest=""> �ο�Ҳ���ʵ���Դ </p>
    [3]user��ǩ: �û��Ѿ������֤/��ס�ҵ�¼����ʾ��Ӧ����Ϣ
        <p shiro:user="">�û��Ѿ�ͨ����֤\��ס�� ��¼��ɷ��ʵ���Դ</p>
    [4]authenticated��ǩ: �û��Ѿ������֤ͨ��,��Subject.login��¼�ɹ�,�Ҳ��Ǽ�ס�ҵ�¼��
        <a shiro:authenticated="" href="updateAccount.html">�����֤ͨ��,�Ҳ��Ǽ�ס�ҵ�¼�ɷ��ʵ���Դ</a>
    [5]notAuthenticated��ǩ: �û�δ���������֤,��û�е���Subject.login���е�¼,����"��ס��"Ҳ����δ���������֤
        <p shiro:notAuthenticated=""> δ�����֤(����"��ס��")�ɷ��ʵ���Դ </p>
    [6]principal��ǩ: ��ʾ�û������Ϣ,Ĭ�ϵ���Subjec.getPrincipal()��ȡ,��Primary Principal
        <p>Hello, <span shiro:principal=""></span>, how are you today?</p>
        <p>Hello, <shiro:principal/>, how are you today?</p>
    [7]hasRole��ǩ: �����ǰSubject��ָ���Ľ�ɫ,����ʾbody���ڵ�����
        <a shiro:hasRole="administrator" href="admin.html">��ɫAdminister�ɷ��ʵ���Դ</a>
    [8]hasAnyRoles��ǩ: ���Subject������һ��ָ���Ľ�ɫ,����ʾbody���������
        <p shiro:hasAllRoles="developer, project manager">��ɫdeveloper��project manager�ɷ��ʵ���Դ</p>
    [9]hasAllRoles��ǩ: �����ǰ Subjec��ָ�����н�ɫ,����ʾbody���ڵ�����
        <p shiro:hasAllRoles="developer, project manager">�н�ɫdeveloper��project manager�ſɷ��ʵ���Դ</p>
    [10]lacksRole��ǩ: �����ǰ Subjecû��ָ����ɫ,����ʾbody���ڵ�����
        <p shiro:lacksRole="administrator">���ǽ�ɫadministratorʱ�ɷ��ʵ���Դ</p>
    [11]hasPermission��ǩ: �����ǰSubject��ָ��Ȩ��,����ʾbody������
        <a shiro:hasPermission="user:create" href="createUser.html">��ָ��Ȩ�޲ſɷ��ʵ���Դ</a>
    [12]lacksPermission��ǩ: �����ǰSubjectû��ָ��Ȩ��,����ʾbody������
        <p shiro:lacksPermission="user:delete">û��ָ��Ȩ��ʱ�ɷ��ʵ���Դ</p>
    [13]hasAllPermissions��ǩ: �����ǰSubject��ָ��������Ȩ��,����ʾbody������
        <p shiro:hasAllPermissions="user:create, user:delete">ͬʱӵ��������ɾ��Ȩ�޲ſɷ��ʵ���Դ</p>
    [14]hasAnyPermissions��ǩ:���Subject������һ��ָ����Ȩ��,����ʾbody���������
        <p shiro:hasAnyPermissions="user:create, user:delete">ӵ��������ɾ��Ȩ��ʱ�ɷ��ʵ���Դ</p>
```