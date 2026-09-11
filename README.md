# Demo - Spring Boot 用户管理 API

基于 Spring Boot 3.2 的用户管理 RESTful API，包含 CRUD、分页、参数校验、单元测试和容器化部署。

## 技术栈

- Spring Boot 3.2.0
- Java 21
- MySQL 8.0
- Spring Data JPA
- springdoc-openapi 2.3.0 (Swagger)
- JUnit 5 + Mockito
- Docker

## 功能

- 用户 CRUD（增删改查）
- 分页查询
- 参数校验
- 全局异常处理
- Swagger UI 接口文档
- AOP 日志
- 22 个单元测试

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users | 查询所有 |
| GET | /api/users/{id} | 查询单个 |
| GET | /api/users/query/paged | 分页 |
| POST | /api/users | 创建 |
| PUT | /api/users/{id} | 更新 |
| DELETE | /api/users/{id} | 删除 |

## 快速开始

### 启动 MySQL

docker run -d --name mysql-demo -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=demo mysql:8.0

### 启动应用

mvnw.cmd spring-boot:run

### 访问

- 接口：http://localhost:8080/api/users
- Swagger：http://localhost:8080/swagger-ui.html

## 项目结构

src/main/java/com/example/demo/
- DemoApplication.java
- aspect/LoggingAspect.java
- controller/UserController.java
- entity/User.java
- exception/GlobalExceptionHandler.java
- repository/UserRepository.java
- service/UserService.java