# 在线判题系统 (Online Judge System)

这是一个基于Spring Boot + Vue3的在线判题系统，支持多种题型的判题，包括选择题、判断题和编程题。系统提供完整的用户、题目、提交管理功能，适用于编程教学和面试训练。

## 技术栈

### 后端
- Spring Boot 2.7
- MyBatis-Plus 3.5
- MySQL 8.0
- JWT认证
- 自定义拦截器认证机制
- Knife4j (API文档)
- JdbcTemplate

### 前端
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia (状态管理)
- Element Plus (UI组件库)
- Axios (HTTP客户端)
- ECharts (图表可视化)

## 功能特性

- 用户系统
  - 用户注册与登录
  - JWT令牌认证
  - 用户角色权限管理（普通用户/管理员）
  - 个人信息管理与头像上传

- 题目系统
  - 支持多种题型（选择题、判断题、编程题）
  - 多级难度分类（简单/中等/困难）
  - 岗位类型分类（前端/后端/算法/数据库等）
  - 题目标签管理

- 提交与判题
  - 实时在线判题
  - 支持多种编程语言
  - 详细的提交记录与结果分析
  - 用户解题状态跟踪

- 管理控制台
  - 系统数据统计仪表盘
  - 通过率、活跃用户等关键指标
  - 题目类型与难度分布统计
  - 最近活动记录

- 错题管理
  - 用户错题自动收集
  - 错题重做功能

## 系统架构

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   前端应用   │ <--> │  后端API服务 │ <--> │ MySQL数据库  │
└─────────────┘      └─────────────┘      └─────────────┘
      Vue3              Spring Boot

     包含组件:           包含模块:
   - 登录/注册         - 用户管理
   - 题目列表/详情     - 题目管理
   - 提交管理          - 提交处理
   - 个人中心          - 判题系统
   - 管理控制台        - 统计分析
```

## 项目结构

```
oj-project/
├── backend/                # 后端项目
│   ├── src/
│   │   ├── main/java/com/oj/
│   │   │   ├── annotation/      # 自定义注解
│   │   │   ├── aop/            # 面向切面编程
│   │   │   ├── common/         # 通用类
│   │   │   ├── config/         # 配置类
│   │   │   ├── constant/       # 常量定义
│   │   │   ├── controller/     # 控制器
│   │   │   ├── exception/      # 异常处理
│   │   │   ├── interceptor/    # 拦截器
│   │   │   ├── listener/       # 事件监听器
│   │   │   ├── mapper/         # MyBatis映射器
│   │   │   ├── model/          # 数据模型
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── request/    # 请求DTO
│   │   │   │   └── vo/         # 视图对象
│   │   │   ├── service/        # 服务层
│   │   │   │   └── impl/       # 服务实现
│   │   │   └── utils/          # 工具类
│   │   └── resources/         # 资源文件
│   └── pom.xml               # Maven配置
└── fronted/                  # 前端项目
    ├── src/
    │   ├── api/              # API请求
    │   ├── assets/           # 静态资源
    │   ├── components/       # 公共组件
    │   ├── hooks/            # 自定义钩子
    │   ├── router/           # 路由配置
    │   ├── stores/           # Pinia状态
    │   ├── types/            # TypeScript类型
    │   ├── utils/            # 工具函数
    │   └── views/            # 页面视图
    ├── public/               # 公共资源
    ├── index.html            # HTML模板
    ├── package.json          # 项目配置
    └── vite.config.ts        # Vite配置
```

## 数据模型

主要实体关系:
- User (用户): 管理用户账号和权限
- Problem (题目): 存储所有题目的基本信息
- Submission (提交): 记录用户的提交信息和结果
- UserProblemStatus (用户题目状态): 跟踪用户解题状态

## 快速开始

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 配置数据库：
   - 创建MySQL数据库
   - 修改`application.properties`中的数据库配置

3. 启动项目：
```bash
mvn spring-boot:run
```

### 前端启动

1. 进入前端目录：
```bash
cd fronted
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

## 开发说明

- 后端API文档访问地址：`http://localhost:8080/doc.html`
- 前端开发服务器地址：`http://localhost:5173`

## 贡献指南

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 开发日志

### 2025-03-16 至 2025-03-17
1. **后端功能优化**
   - 修复了已删除题目在管理控制面板中无法正确显示的问题
   - 优化SQL查询逻辑，解决了查询条件冲突（isDelete=0 AND isDelete=1）
   - 使用JdbcTemplate绕过MyBatis-Plus的逻辑删除过滤机制
   - 完善了Problem实体类的TableLogic注解配置

2. **通过率计算逻辑优化**
   - 修改DashboardServiceImpl中的通过率计算方法
   - 使用user_problem_status表中的SOLVED状态来计算通过率
   - 使取得的统计数据更加准确反映用户的解题情况

3. **前端修复**
   - 修复前端Dashboard组件显示通过率的问题
   - 修复前端TypeScript类型错误
   - 优化登录认证逻辑，添加更详细的错误处理
   - 改进Vite跨域代理配置，解决认证请求问题
