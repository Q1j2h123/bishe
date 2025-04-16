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

## 系统功能模块

系统功能模块清晰划分为用户模块和管理员模块两大部分，涵盖了在线判题系统的完整功能体系。

### 用户模块
1. **账户管理**
   - 注册功能
   - 登录功能
   - 个人信息维护

2. **题目学习**
   - 题目浏览
   - 题目搜索
   - 题目详情查看
   - 解答题目
   - 查看题目答案

3. **代码提交**
   - 代码提交
   - 在线运行
   - 查看结果

4. **学习管理**
   - 提交历史
   - 错题本

### 管理员模块
1. **用户管理**
   - 用户列表
   - 权限分配

2. **题目管理**
   - 批量导入
   - 题目添加
   - 题目编辑
   - 题目删除

3. **用户数据中心**
   - 查看最近活动记录
   - 查看数据统计

## 系统用例图

系统用例图清晰展示了在线判题系统的功能结构和用户交互模式。该系统主要分为四大功能模块：用户角色模块、题目管理模块、个人中心模块和用户管理模块。

在用户角色模块中，系统支持普通用户和管理员两种角色，普通用户可进行注册登录、完善个人信息等基础操作。题目模块是系统的核心，用户可以浏览题目列表，系统通过扩展关系(extends)支持多种题型，包括编程题、选择题和判断题，用户可查看题目详情并进行解答。个人中心模块通过包含关系(includes)整合了查看个人学习统计、管理收藏题目和查看提交历史等功能。对于管理员用户，系统提供了额外的题目管理功能，包括添加题目、编辑题目、删除题目等操作，同时支持测试数据的添加与管理。

用例图通过明确的边界(boundary)划分了各功能模块的范围，并使用扩展(extends)和包含(includes)关系清晰地表达了用例之间的逻辑联系，为系统设计和开发提供了全面的功能视图。

## 主要功能特点

### 题目功能
- 支持编程题、判断题和选择题
- 题目难度分级（简单、中等、困难）
- 题目分类和标签系统
- 多语言代码模板

### 判题功能
- 本地代码执行和结果验证
- AI辅助代码评估（集成DeepSeek API）
- 详细的执行结果和错误分析
- 支持多测试用例判定

### 技术栈

#### 后端技术
- Spring Boot 2.x
- JWT（用户认证）
- MyBatis（数据库访问）
- EasyExcel（Excel处理）
- 集成AI API（DeepSeek、OpenAI等）

#### 前端技术
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