classDiagram
direction BT
class node0 {
   varchar(32) answer  /* 用户答案 */
   bigint submissionId  /* 关联submission表的id */
}
class node7 {
   text options  /* 选项，JSON格式：[{"key": "A", "content": "选项内容"}] */
   varchar(32) answer  /* 正确答案，单选为A/B/C/D，多选为逗号分隔 */
   text analysis  /* 解析 */
   bigint id  /* 关联problem表的id */
}
class node4 {
   bigint userId  /* 用户ID */
   bigint problemId  /* 题目ID */
   datetime lastErrorTime  /* 最近错误时间 */
   status  /* 状态(0-未掌握, 1-已掌握) */ tinyint
   datetime createTime  /* 创建时间 */
   datetime updateTime  /* 更新时间 */
   isDelete  /* 是否删除(0-未删, 1-已删) */ tinyint
   bigint id  /* ID */
}
class node9 {
   answer  /* 正确答案：true(正确)/false(错误) */ tinyint(1)
   text analysis  /* 解析 */
   bigint id  /* 关联problem表的id */
}
class node2 {
   varchar(512) title  /* 题目标题 */
   text content  /* 题目内容 */
   type  /* 题目类型：CHOICE(选择题)、JUDGE(判断题)、PROGRAM(编程题) */ varchar(128)
   varchar(32) jobType  /* 岗位类型 */
   varchar(512) tags  /* 标签 */
   varchar(32) difficulty  /* 难度：EASY、MEDIUM、HARD */
   bigint userId  /* 创建者 id */
   varchar(8) acceptRate  /* 通过率 */
   int submissionCount  /* 提交次数 */
   datetime createTime
   datetime updateTime
   tinyint isDelete
   varchar(255) status  /* 状态（UNSOLVED-未解决，ATTEMPTED-尝试过，SOLVED-已解决） */
   bigint id  /* 主键 */
}
class node8 {
   varchar(64) functionName  /* 函数名称 */
   varchar(512) paramTypes  /* 参数类型，JSON格式，如：["int[]", "int"] */
   varchar(64) returnType  /* 返回值类型 */
   json testCases  /* 测试用例，包含示例和隐藏用例，JSON格式，如：[{"input": "[2,7,11,15]\n9", "output"... */
   json templates  /* 各语言代码模板，JSON格式，如：{"java": "public class...", "python": "def..."} */
   timeLimit  /* 时间限制(ms) */ int
   memoryLimit  /* 内存限制(MB) */ int
   datetime createTime
   datetime updateTime
   tinyint isDelete
   json standardSolution  /* 各语言的标准答案，JSON格式，如：{"java": "public class Solution {...}", "py... */
   bigint id  /* 关联problem表的id */
}
class node3 {
   varchar(32) language  /* 编程语言 */
   text code  /* 代码 */
   executeTime  /* 执行时间(ms) */ int
   memoryUsage  /* 内存使用(MB) */ int
   text testcaseResults  /* 测试用例执行结果，JSON格式 */
   text errorMessage  /* 详细错误信息，包含编译错误或运行时错误的具体内容 */
   bigint submissionId  /* 关联submission表的id */
}
class node6 {
   bigint userId  /* 用户id */
   bigint problemId  /* 题目id */
   varchar(32) type  /* 提交类型：PROGRAMMING/CHOICE/JUDGE */
   datetime submissionTime  /* 提交时间 */
   datetime createTime  /* 创建时间 */
   datetime updateTime  /* 更新时间 */
   tinyint isDelete  /* 是否删除 */
   status  /* 提交状态：PENDING(待评测)、JUDGING(评测中)、ACCEPTED(通过)、WRONG_ANSWER(答案错误... */ varchar(32)
   bigint id  /* 主键 */
}
class node5 {
   varchar(256) userAccount  /* 账号 */
   varchar(512) userPassword  /* 密码 */
   varchar(256) unionId  /* 微信开放平台id */
   varchar(256) mpOpenId  /* 公众号openId */
   varchar(256) userName  /* 用户昵称 */
   varchar(1024) userAvatar  /* 用户头像 */
   varchar(512) userProfile  /* 用户简介 */
   varchar(256) userRole  /* 用户角色：user/admin/ban */
   datetime createTime  /* 创建时间 */
   datetime updateTime  /* 更新时间 */
   tinyint isDelete  /* 是否删除 */
   bigint id  /* id */
}
class user_problem_status {
   bigint userId
   bigint problemId
   varchar(32) status  /* UNSOLVED/ATTEMPTED/SOLVED */
   datetime lastSubmitTime
   datetime createTime
   datetime updateTime
   bigint id
}

node0  -->  node6 : submissionId:id
node7  -->  node2 : id
node9  -->  node2 : id
node8  -->  node2 : id
node3  -->  node6 : submissionId:id
