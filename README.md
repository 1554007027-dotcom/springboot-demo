# Demo - Spring Boot 用户管理系统（后端）

基于 Spring Boot 3.2 的用户管理系统后端，提供 RESTful API、JWT 认证、权限控制、缓存、限流等完整功能。

## 技术栈

- **Spring Boot 3.2** - 应用框架
- **Java 21** - 语言版本
- **MySQL 8.0** - 关系数据库
- **Spring Data JPA** - ORM 框架
- **Redis** - 缓存
- **Spring Security + JWT** - 认证授权
- **Bucket4j / AOP** - 接口限流
- **springdoc-openapi** - 接口文档
- **Lombok** - 简化代码
- **JUnit 5 + Mockito + MockMvc** - 单元测试
- **Docker + Docker Compose** - 容器化部署

## 功能特性

- 用户 CRUD（创建、查询、更新、删除）
- 分页查询（支持排序、分页参数）cd E:\Javacode\demo
- 参数校验（用户名、邮箱格式）
- 全局异常处理（统一 JSON 响应）
- 统一响应格式（`Result<T>`）
- JWT 双 Token 认证（access + refresh）
- RBAC 权限控制（USER / ADMIN）
- Redis 缓存用户查询接口
- 登录接口限流（防暴力破解）
- AOP 日志（自动记录每个请求）
- Swagger UI 接口文档
- 40+ 单元测试（Repository / Service / Controller）
- Docker 多阶段构建
- Docker Compose 一键启动

## 快速开始

### 1. 启动依赖服务

```bash
docker compose up -d mysql redis