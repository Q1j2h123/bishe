# OJ在线判题系统

这是一个功能完整的在线编程学习和判题系统，支持多种编程题目类型和自动评判功能。

## 项目概述

该系统是一个面向编程学习者的在线判题平台，支持编程题、判断题和选择题三种题型。系统集成了AI辅助评判功能，支持Java、Python、C++和C四种编程语言，为用户提供代码编写、提交、自动评判和结果分析的完整学习体验。

## 系统架构

系统采用前后端分离架构：

- **后端**：基于Spring Boot构建的RESTful API服务
- **前端**：使用Vue.js + TypeScript开发的单页面应用
- **判题系统**：支持本地判题和AI辅助判题两种模式
- **数据库**：MySQL关系型数据库存储题目、用户、提交记录等信息

## 主要功能

### 用户功能
- 用户注册、登录和个人信息管理
- 题目浏览、筛选和搜索
- 代码在线编辑和提交
- 查看提交历史和判题结果
- 草稿代码保存（未实现）
- 个人学习统计和进度跟踪

### 题目功能
- 支持编程题、判断题和选择题
- 题目难度分级（简单、中等、困难）
- 题目分类和标签系统
- 多语言代码模板

### 判题功能
- 本地代码执行和结果验证
- AI辅助代码评估（集成DeepSeek API,qwen-turbo API）
- 详细的执行结果和错误分析
- 支持多测试用例判定(有bug)

### 管理功能
- 题目导入（支持Excel和CSV格式）
- 用户管理和权限控制
- 系统监控和统计数据
- 错误题目报告和处理

## 技术栈

### 后端技术
- Spring Boot 2.x
- JWT（用户认证）
- MyBatis（数据库访问）
- EasyExcel（Excel处理）
- 集成AI API（DeepSeek、OpenAI,qwen-turbo）

### 前端技术
- Vue.js 3
- TypeScript
- Pinia（状态管理）
- Vue Router（路由管理）
- 响应式UI设计

## 部署指南

### 环境要求
- JDK 11+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 后端部署

1. **克隆代码仓库**
   ```bash
   git clone [仓库地址]
   cd oj-project
   ```

2. **配置数据库**
   - 创建MySQL数据库
   ```sql
   CREATE DATABASE oj_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   - 修改配置文件 `backend/src/main/resources/application.properties`
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/oj_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   spring.datasource.username=你的数据库用户名
   spring.datasource.password=你的数据库密码
   ```

3. **配置AI API**
   - 在配置文件中设置API密钥 `backend/src/main/resources/application.properties`
   ```properties
   # AI配置
   ai.provider=deepseek # 或其他提供商
   ai.api-key=你的API密钥
   ai.model=deepseek-chat # 或其他模型
   ai.api-url=https://api.deepseek.com/v1 # 或其他API地址
   ```

4. **构建并运行后端**
   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/oj-project-backend.jar
   ```

### 前端部署

1. **安装依赖**
   ```bash
   cd fronted  # 注意项目中使用了fronted而非frontend
   npm install
   ```

2. **配置API地址**
   - 修改环境配置文件 `.env` 或 `.env.production`
   ```
   VITE_API_BASE_URL=http://localhost:8080/api # 或你的API地址
   ```

3. **构建前端**
   ```bash
   npm run build
   ```
   
4. **部署静态文件**
   - 将`dist`目录下的文件部署到Web服务器（Nginx/Apache等）
   
### 使用Docker部署（可选）

1. **构建后端Docker镜像**
   ```bash
   cd backend
   docker build -t oj-project-backend .
   ```

2. **构建前端Docker镜像**
   ```bash
   cd fronted
   docker build -t oj-project-frontend .
   ```

3. **使用docker-compose运行**
   ```bash
   docker-compose up -d
   ```

## 系统使用

### 导入题目
1. 登录管理员账号
2. 进入题目管理页面
3. 选择CSV或Excel导入功能
4. 上传符合格式的题目文件

### 代码提交与评判
1. 从题目列表选择要练习的题目
2. 在线编辑代码或使用提供的模板
3. 选择编程语言并提交代码
4. 系统将自动评判并返回结果

## 项目维护和贡献

欢迎提交Issue和Pull Request来改进项目。在提交代码前，请确保：

1. 遵循项目的代码规范
2. 为新功能编写完整的测试
3. 更新相关文档
4. 在PR中详细描述变更内容

