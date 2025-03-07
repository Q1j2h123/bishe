
CREATE TABLE IF NOT EXISTS `question_bank` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(128) NOT NULL COMMENT '题库名称',
    `description` varchar(512) DEFAULT NULL COMMENT '题库描述',
    `difficulty` varchar(32) NOT NULL COMMENT '难度：EASY(简单)、MEDIUM(中等)、HARD(困难)',
    `tags` varchar(512) DEFAULT NULL COMMENT '标签，多个标签用逗号分隔',
    `permission` varchar(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '权限：PUBLIC(公开)、PRIVATE(私有)、SHARED(共享)',
    `isHot` tinyint NOT NULL DEFAULT '0' COMMENT '是否热门',
    `isRecommended` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐',
    `userId` bigint NOT NULL COMMENT '创建者 id',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库';
-- 创建题库题目关联表
CREATE TABLE IF NOT EXISTS `question_bank_problem` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `questionBankId` bigint NOT NULL COMMENT '题库 id',
    `problemId` bigint NOT NULL COMMENT '题目 id',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_bank_problem` (`questionBankId`,`problemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库题目关联';

-- 创建问题表
CREATE TABLE IF NOT EXISTS `problem` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(512) NOT NULL COMMENT '题目标题',
    `content` text NOT NULL COMMENT '题目内容',
    `type` varchar(128) NOT NULL COMMENT '题目类型：CHOICE(选择题)、JUDGE(判断题)、PROGRAM(编程题)',
    `jobType` varchar(32) NOT NULL COMMENT '岗位类型',
    `tags` varchar(512) DEFAULT NULL COMMENT '标签',
    `difficulty` varchar(32) NOT NULL COMMENT '难度：EASY、MEDIUM、HARD',
    `acceptRate` varchar(8) NOT NULL DEFAULT '0%' COMMENT '通过率',
    `submissionCount` int NOT NULL DEFAULT '0' COMMENT '提交次数',
    `userId` bigint NOT NULL COMMENT '创建者 id',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete` tinyint NOT NULL DEFAULT '0',
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_type` (`type`),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目基础表';
-- 创建选择题表
CREATE TABLE IF NOT EXISTS `choice_problem` (
    `id` bigint NOT NULL COMMENT '关联problem表的id',
    `options` text NOT NULL COMMENT '选项，JSON格式：[{"key": "A", "content": "选项内容"}]',
    `answer` varchar(32) NOT NULL COMMENT '正确答案，单选为A/B/C/D，多选为逗号分隔',
    `analysis` text DEFAULT NULL COMMENT '解析',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选择题表';

-- 创建判断题表
CREATE TABLE IF NOT EXISTS `judge_problem` (
    `id` bigint NOT NULL COMMENT '关联problem表的id',
    `answer` boolean NOT NULL COMMENT '正确答案：true(正确)/false(错误)',
    `analysis` text DEFAULT NULL COMMENT '解析',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='判断题表';

CREATE TABLE IF NOT EXISTS `programming_problem` (
                                                     `id` bigint NOT NULL COMMENT '关联problem表的id',
                                                     `functionName` varchar(64) NOT NULL COMMENT '函数名称',
                                                     `paramTypes` varchar(512) NOT NULL COMMENT '参数类型，JSON格式，如：["int[]", "int"]',
                                                     `returnType` varchar(64) NOT NULL COMMENT '返回值类型',
                                                     `testCases` json NOT NULL COMMENT '测试用例，包含示例和隐藏用例，JSON格式，如：[{"input": "[2,7,11,15]\\n9", "output": "[0,1]", "isExample": true}]',
                                                     `templates` json NOT NULL COMMENT '各语言代码模板，JSON格式，如：{"java": "public class...", "python": "def..."}',
                                                     `timeLimit` int NOT NULL DEFAULT 1000 COMMENT '时间限制(ms)',
                                                     `memoryLimit` int NOT NULL DEFAULT 256 COMMENT '内存限制(MB)',
                                                     `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                     `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                     `isDelete` tinyint NOT NULL DEFAULT '0',
                                                     PRIMARY KEY (`id`),
                                                     FOREIGN KEY (`id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编程题表';
-- 提交记录基础表
CREATE TABLE IF NOT EXISTS `submission` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `userId` bigint NOT NULL COMMENT '用户id',
    `problemId` bigint NOT NULL COMMENT '题目id',
    `type` varchar(32) NOT NULL COMMENT '提交类型：PROGRAMMING/CHOICE/JUDGE',
    `submissionTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_problem` (`userId`, `problemId`),
    KEY `idx_submission_time` (`submissionTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提交记录基础表';

-- 编程题提交记录扩展表
CREATE TABLE IF NOT EXISTS `programming_submission` (
    `submissionId` bigint NOT NULL COMMENT '关联submission表的id',
    `language` varchar(32) NOT NULL COMMENT '编程语言',
    `code` text NOT NULL COMMENT '代码',
     `status` varchar(32) NOT NULL COMMENT '状态：ACCEPTED(通过)/WRONG_ANSWER(答案错误)/TIME_LIMIT_EXCEEDED(超时)/MEMORY_LIMIT_EXCEEDED(内存超限)/RUNTIME_ERROR(运行错误)/COMPILE_ERROR(编译错误)',
    `executeTime` int DEFAULT NULL COMMENT '执行时间(ms)',
    `memoryUsage` int DEFAULT NULL COMMENT '内存使用(MB)',
    `testcaseResults` text DEFAULT NULL COMMENT '测试用例执行结果，JSON格式',
    PRIMARY KEY (`submissionId`),
    FOREIGN KEY (`submissionId`) REFERENCES `submission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编程题提交详情';

-- 选择判断题提交记录扩展表
CREATE TABLE IF NOT EXISTS `choice_judge_submission` (
    `submissionId` bigint NOT NULL COMMENT '关联submission表的id',
    `answer` varchar(32) NOT NULL COMMENT '用户答案',
    `isCorrect` boolean NOT NULL COMMENT '是否正确',
    PRIMARY KEY (`submissionId`),
    FOREIGN KEY (`submissionId`) REFERENCES `submission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选择判断题提交详情';


ALTER TABLE `programming_problem`
ADD COLUMN `standardSolution` json COMMENT '各语言的标准答案，JSON格式，如：{"java": "public class Solution {...}", "python": "class Solution:..."}';
ALTER TABLE `problem`
ADD COLUMN `status` json COMMENT '状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决）';