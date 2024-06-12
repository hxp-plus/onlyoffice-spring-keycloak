# Spring 后端对接 OnlyOffice+Keycloak 示例

## 项目简介

本项目为 Spring 后端对接 OnlyOffice 和 Keycloak 示例，所涉及的版本信息如下：

### SpringBoot 后端应用服务器

由 [OnlyOffice 官方 Spring 示例](https://github.com/ONLYOFFICE/document-server-integration/tree/master/web/documentserver-example/java-spring)
改进而来，改进如下：

1. 重新编译 document-server 开源代码，解除了 OnlyOffice 只能 20 个文档同时被编辑的限制
2. 将 Spring 后端对接 Keycloak
3. 对 Spring 后端编译成 jar 后无法运行的问题进行修复
4. 将 Spring 后端默认文件上传最大大小修改为原先的 10 倍
5. 将 document-server 修改为使用外置 rabbitmq 、 redis 、 postgresql 、 nginx
6. 将 document-server 修改为使用 supervisor 进行启动
7. 配置使用 JWT 保证安全

其使用的组件如下：

- OpenJDK 11.0
- SpringBoot 2.7.18
- Maven 3.9.7

### Keycloak 单点认证服务器

Keycloak 单点认证服务器用于实现整个系统的访问控制和鉴权，其涉及的组件如下：

- Keycloak 24.0.2
- MySQL 8.0.37

### OnlyOffice 文档服务器

OnlyOffice 文档服务器用于给本项目提供在线文档编辑服务，其涉及的组件如下：

- Nginx 1.27.0
- OnlyOffice 8.0.1
- PostgreSQL 13.15
- RabbitMQ 3.13.3
- Redis 7.4

## 开发环境搭建

### 为 OnlyOffice 生成一个 secret

使用如下命令生成一个长度为 32 个字符的随机 secret ：

```
tr -dc A-Za-z0-9 </dev/urandom | head -c 32; echo
```

找到 `dev-environment/onlyoffice-docs/production-linux.json` 的如下几行的进行替换：

```
"secret": {
   "inbox": {
      "string": "yoursecret"
   },
   "outbox": {
      "string": "yoursecret"
   },
   "session": {
      "string": "yoursecret"
   }
}
```

### 修改容器启动环境变量

需要把环境 `compose.yaml` 中 `keycloak-app` 的环境变量 `KC_HOSTNAME` 替换成 docker 机器的 IP 地址或者域名。

```
services:
  keycloak-app:
    environment:
      KC_HOSTNAME: gentoo.hxp.lan
```

### 启动容器

使用 docker 搭建 Spring 后端运行需要的两个服务：

```
cd dev-environment
docker compose up
```

初次启动 Keycloak 连接不上为已知问题，按`Ctrl-C`然后再次启动即可。`onlyoffice-docs` 镜像的构建需要 60GB 磁盘空间和 4 小时编译时间，推荐直接把编译好生成的镜像导入来跳过此步骤。

### 初始化 Keycloak 配置

需要在 Keycloak 里创建相应的 Realm 和 client ，需要操作的步骤有：

1. 新建名称为 test 的 Realm
2. 新建类型为 OpenID Connect ，ID 为 test-client 的 client
3. test-client 的 Client authentication 为 OFF
4. test-client 的 Valid redirect URIs 为 \*
5. test-client 的 Web origins 为 \*
6. 在名称为 test 的 Realm 里新建用户 testuser

### 初始化 PostgreSQL 数据库

初次启动需要进行数据铺底：

```
docker exec onlyoffice-postgresql /bin/sh -c 'PG_PASSWORD=onlyoffice psql -h 127.0.0.1 -U onlyoffice -d onlyoffice < /root/createdb.sql'
```

### 修改 application.properties 配置

Spring 项目配置文件需要客户化以下几行：

```
# OnlyOffice 文档服务器访问地址
files.docservice.url.site=http://gentoo.hxp.lan:8081/
# OnlyOffice 文档服务器的 secret
files.docservice.secret=45JW21YqkaXS9xJSUHC4WcVo0FJohjkW
# Keycloak 单点认证服务器的 client id
spring.security.oauth2.client.registration.keycloak.client-id=test-client
# Keycloak 单点认证服务器的 Realm 地址
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://gentoo.hxp.lan:8080/realms/test
# Keycloak 单点认证服务器的 Realm 地址
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://gentoo.hxp.lan:8080/realms/test
```

之后启动 Spring 后端进行测试。

## 生产环境部署

### 单独挂载逻辑卷

创建单独的逻辑卷 `lv_onlyoffice` ，将其格式化并挂载到 `/onlyoffice` 目录。

### 使用 maven 编译 jar 包

使用 maven 编译本项目：

```
mvn clean
mvn package
```

编译后会在 `target` 目录下生成 jar 包，将其放在 `/onlyoffice` 目录下。

### 编写配置文件

仿照开发环境，新建 `application.properties` 配置，将其放到 `/onlyoffice/config` 目录下。

### 运行 jar 包

在确定 Java 版本为 11 后，运行：

```
cd /onlyoffice
java -jar /onlyoffice/onlyoffice-server-v20240611.jar --spring.config.location=file:///onlyoffice/config/application.properties
```

## 更新记录

### v20240611 内测版

- 解决了文档大小超过 1MB 则编辑器页面下载失败的 BUG。
- 升级 springboot 到 2 的最终版本 2.7.18 。
- 优化容器镜像：使用 supervisor 启动 onlyoffice-doc 服务，防止容器内进程崩溃后无法自动重启。
- 优化容器镜像：将 onlyoffice-doc 镜像改为使用外部 PostgreSQL 、 RabbitMQ 和 Redis 。
- 升级所有 maven 依赖到最新版本。
- 解决主页删除文档没有二次确认，导致容易误删的问题。

### v20240604 内测版

- 修复了用户没有登录过的 Cookie 时，直接打开/editor 页面报 400 的 BUG。
- 将 application.properties 修改为不打包到 jar 中，在启动时手动指定。
- 修改主页布局。
- 修复上传文档后立刻编辑报错的 BUG。
- 修复主页下载按钮下载的文档为初次上传版本的 BUG。
- 修复 title 的 typo 。

### v20240531 内测版

- 项目初始化。
- 解决了默认上传大小限制过小问题。
- 为 Keycloak 认证增加支持。
- 修复原项目编译成 jar 包无法正常运行的问题。
- 完成界面汉化。
