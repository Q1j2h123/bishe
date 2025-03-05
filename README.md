# 在线判题系统 (Online Judge System)

这是一个基于Spring Boot + Vue3的在线判题系统，支持多种题型的判题，包括选择题、判断题和编程题。

## 技术栈

### 后端
- Spring Boot
- MyBatis-Plus
- MySQL
- JWT认证
- Knife4j (API文档)

### 前端
- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus

## 功能特性

- 用户管理
  - 用户注册
  - 用户登录
  - 个人信息管理

- 题目管理
  - 支持多种题型（选择题、判断题、编程题）
  - 题目导入导出
  - 题目分类管理

- 题库管理
  - 创建题库
  - 题库权限管理
  - 题目添加到题库

- 判题系统
  - 实时判题
  - 多种编程语言支持
  - 详细的判题结果展示

## 项目结构

```
oj-project/
├── backend/                # 后端项目
│   ├── src/               # 源代码
│   │   ├── main/         # 主要代码
│   │   └── test/         # 测试代码
│   └── pom.xml           # Maven配置文件
└── fronted/               # 前端项目
    ├── src/              # 源代码
    ├── public/           # 静态资源
    └── package.json      # 项目配置文件
```

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
