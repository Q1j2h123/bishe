CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userAccount` varchar(256) NOT NULL COMMENT '账号',
  `userPassword` varchar(512) NOT NULL COMMENT '密码',
  `unionId` varchar(256) DEFAULT NULL COMMENT '微信开放平台id',
  `mpOpenId` varchar(256) DEFAULT NULL COMMENT '公众号openId',
  `userName` varchar(256) DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_userAccount` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户'; 