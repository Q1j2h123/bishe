# Online Judge System

## 项目结构
```
oj-project/
  ├── frontend/     # Vue 3 + TypeScript 前端代码
  └── backend/      # Spring Boot 后端代码
```

## 开发环境要求
- Node.js 16+
- JDK 8+
- Maven 3.6+

## 前端开发
```bash
cd frontend
npm install
npm run dev
```

## 后端开发
```bash
cd backend
mvn spring-boot:run
```

## API 文档
- 接口文档：http://localhost:8080/doc.html (Knife4j)

## 开发规范
1. 代码提交前请先格式化
2. 遵循 ESLint 和 Prettier 规范
3. 遵循 RESTful API 设计规范
4. 保持良好的代码注释
