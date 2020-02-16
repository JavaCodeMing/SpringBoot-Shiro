-- ----------------------------
-- Table structure for T_PERMISSION
-- ----------------------------
CREATE TABLE T_PERMISSION (
   ID INT(10) NOT NULL COMMENT '主键',
   URL VARCHAR(256) NULL COMMENT 'url地址',
   NAME VARCHAR(64) NULL COMMENT 'url描述',
   PRIMARY KEY (ID) USING BTREE
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_PERMISSION
-- ----------------------------
INSERT INTO T_PERMISSION VALUES ('1', '/user', 'user:user');
INSERT INTO T_PERMISSION VALUES ('2', '/user/add', 'user:add');
INSERT INTO T_PERMISSION VALUES ('3', '/user/delete', 'user:delete');

-- ----------------------------
-- Table structure for T_ROLE
-- ----------------------------
CREATE TABLE T_ROLE (
   ID INT(10) NOT NULL COMMENT '主键',
   NAME VARCHAR(32) NULL COMMENT '角色名称',
   MEMO VARCHAR(32) NULL COMMENT '角色描述',
   PRIMARY KEY (ID) USING BTREE
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_ROLE
-- ----------------------------
INSERT INTO T_ROLE VALUES ('1', 'admin', '超级管理员');
INSERT INTO T_ROLE VALUES ('2', 'test', '测试账户');

-- ----------------------------
-- Table structure for T_ROLE_PERMISSION
-- ----------------------------
CREATE TABLE T_ROLE_PERMISSION (
   RID INT(10) NULL COMMENT '角色id',
   PID INT(10) NULL COMMENT '权限id'
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_ROLE_PERMISSION
-- ----------------------------
INSERT INTO T_ROLE_PERMISSION VALUES ('1', '2');
INSERT INTO T_ROLE_PERMISSION VALUES ('1', '3');
INSERT INTO T_ROLE_PERMISSION VALUES ('2', '1');
INSERT INTO T_ROLE_PERMISSION VALUES ('1', '1');

-- ----------------------------
-- Table structure for T_USER
-- ----------------------------
CREATE TABLE T_USER (
   ID INT(10) NOT NULL COMMENT '主键',
   USERNAME VARCHAR(20) NOT NULL COMMENT '用户名',
   PASSWD VARCHAR(128) NOT NULL COMMENT '密码',
   PHONE VARCHAR(20) NULL COMMENT '手机号',
   CREATE_TIME TIMESTAMP NULL COMMENT '创建时间',
   STATUS CHAR(1) NOT NULL COMMENT '是否有效 1:有效  0:锁定',
   PRIMARY KEY (ID) USING BTREE
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_USER
-- ----------------------------
INSERT INTO T_USER VALUES ('2', 'test', '94b642a2b096f2559933bc5731722c46', '17622228081','2020-02-05 14:20:20', '1');
INSERT INTO T_USER VALUES ('1', 'conan', '94b642a2b096f2559933bc5731722c46', '17611118080','2020-02-05 10:50:20', '1');

-- ----------------------------
-- Table structure for T_USER_ROLE
-- ----------------------------
CREATE TABLE T_USER_ROLE (
   USER_ID INT(10) NULL COMMENT '用户id',
   ROLE_ID INT(10) NULL COMMENT '角色id'
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_USER_ROLE
-- ----------------------------
INSERT INTO T_USER_ROLE VALUES ('1', '1');
INSERT INTO T_USER_ROLE VALUES ('2', '2');
