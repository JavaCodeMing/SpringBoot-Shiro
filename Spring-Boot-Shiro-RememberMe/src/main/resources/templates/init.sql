-- ----------------------------
-- Table structure for T_USER
-- ----------------------------
CREATE TABLE T_USER (
   ID INT(10) NOT NULL COMMENT '主键',
   USERNAME VARCHAR(20) NOT NULL COMMENT '用户名',
   PASSWD VARCHAR(128) NOT NULL COMMENT '密码',
   CREATE_TIME TIMESTAMP NULL COMMENT '创建时间',
   STATUS CHAR(1) NOT NULL COMMENT '是否有效 1:有效  0:锁定',
   PRIMARY KEY (ID) USING BTREE
)DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_USER
-- ----------------------------
INSERT INTO T_USER VALUES ('2', 'test', '638d77f4baea419ffdcbf4ae66066a9e', '2020-02-05 14:20:20', '0');
INSERT INTO T_USER VALUES ('1', 'conan', 'b1321142a4ff9f8a4166439ef51cb854', '2020-02-05 10:50:20', '1');
