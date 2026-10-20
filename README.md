# mall-cloud

B2C 电商微服务工程：**Spring Cloud Alibaba + DDD 分层 + Maven 多模块聚合**。

- 语言/构建：JDK 21 · Maven 3.9
- 框架：Spring Boot 3.3.5 · Spring Cloud 2023.0.3 · Spring Cloud Alibaba 2023.0.3.2
- 中间件：Nacos · Gateway · OpenFeign · Sentinel · Seata · MySQL 8 · Redis · RabbitMQ · Elasticsearch 8
- 组件：MyBatis-Plus 3.5.9 · JWT(jjwt 0.12.6) · Knife4j 4.5.0 · XXL-Job 2.4.1 · Spring Boot Admin 3.3.4

---

## 1. 版本矩阵与 JDK 21 兼容性

| 组件 | 版本 | 为什么是这个版本 |
|---|---|---|
| JDK | 21 (LTS) | Boot 3.3 支持 17~22，21 是当前最低成本的最佳选择 |
| Spring Boot | 3.3.5 | 该版本已内置对 JDK 21 的完整支持（含虚拟线程开关） |
| Spring Cloud | 2023.0.3 | 与 Boot 3.3.x 官方对应 |
| Spring Cloud Alibaba | 2023.0.3.2 | 与 Cloud 2023.0.3 官方对应；三者必须成套升降 |
| MyBatis-Plus | 3.5.9 | **3.5.9 起分页插件拆到 `mybatis-plus-jsqlparser`**，本工程已显式引入 |
| Redisson | 3.35.0 | 其 starter 内置 `redisson-spring-data-33`，正好对应 Spring Data Redis 3.3 |
| Seata | 2.1.0 | 2.x 起 groupId 变为 `org.apache.seata`，注解包名为 `org.apache.seata.spring.annotation` |
| Spring Data ES | 5.3.5（Boot 管理） | 注意 `IndexCoordinates` 在 5.3 位于 `core.mapping` 包，不在 `core` |
| Knife4j | 4.5.0 | OpenAPI3 + Jakarta 版本，配套 springdoc 2.x |

> 这套组合已在 JDK 21 + Maven 3.9.16 下完成 `clean compile` 与 `package` 全量验证。
> 升级 Boot 时必须同步升级 Cloud 与 SCA，**不要只升其中一个**。

---

## 2. 模块清单与职责

### 2.1 公共模块（只产 jar，不启动）

| 模块 | 职责 | 约束 |
|---|---|---|
| `common-core` | 统一响应 `R` / `PageResult`、`BizException`、错误码、`UserContext`、`JsonUtils`、MQ 常量、**JWT 工具** | 零 Web 依赖（不能引 spring-webmvc/webflux），否则网关无法复用 |
| `common-mybatis` | MP 插件链（分页/乐观锁/防全表更新）、`BaseEntity`、字段自动填充 | 仅数据服务引入 |
| `common-redis` | 序列化策略、`RedisCache` 门面、`@DistributedLock` 分布式锁 | — |
| `common-security` | 登录态还原拦截器、`@RequiresLogin`/`@RequiresPerm`、全局异常、统一 JSON 序列化 | 依赖 spring-webmvc，**网关禁止引入** |
| `common-swagger` | Knife4j / OpenAPI3 统一配置 | — |
| `common-feign` | Token 与用户上下文透传、统一错误解码、降级基类 | — |

> JWT 放在 `common-core` 而不是 `common-security`：网关是 WebFlux 应用，
> 若 JWT 工具依赖 spring-webmvc，网关就不得不重复实现一份验签逻辑。

### 2.2 契约层 `mall-api`

每个业务服务一个子模块，只放 **Feign 接口 + 跨服务 DTO + MQ 事件**，没有实现。

这样做的原因：如果让 `mall-order` 直接依赖 `mall-product` 的 jar 去调 Feign，
商品服务的 Mapper、Entity、配置会全部进入订单服务的 classpath，形成编译期强耦合，
而且极易演变成循环依赖。

### 2.3 业务微服务（独立启动）

| 模块 | 端口 | 职责 | 存储 |
|---|---|---|---|
| `mall-gateway` | 8080 | 路由、JWT 鉴权、限流、跨域、TraceId、拒绝 `/inner/**` 外网访问 | 无 |
| `mall-user` | 8101 | 注册登录、JWT 签发、收货地址、会员等级 | MySQL `mall_user` |
| `mall-product` | 8102 | 类目/SPU/SKU、上下架、详情缓存、商品变更事件 | MySQL + Redis |
| `mall-ware` | 8103 | 锁定 / 解锁 / 实扣、库存流水 | MySQL `mall_ware` |
| `mall-order` | 8104 | 订单状态机、下单（Seata）、超时关单、支付结果消费 | MySQL `mall_order` |
| `mall-payment` | 8105 | 支付单、渠道回调、支付结果事件 | MySQL `mall_payment` |
| `mall-cart` | 8106 | 购物车增删改查、勾选、失效标记 | **纯 Redis** |
| `mall-coupon` | 8107 | 券模板、领取、核销、退回 | MySQL `mall_coupon` |
| `mall-search` | 8108 | ES 索引维护、关键词检索、筛选分页 | **纯 ES** |
| `mall-comment` | 8109 | 评价、商家回复、评分统计 | MySQL `mall_comment` |
| `mall-notice` | 8110 | 站内信/短信/邮件、模板渲染、发送流水 | MySQL `mall_notice` |

### 2.4 运维模块

| 模块 | 端口 | 职责 |
|---|---|---|
| `mall-admin` | 8200 | 后台 BFF：管理员认证与 RBAC、跨服务聚合查询 |
| `mall-job` | 8300 | XXL-Job 执行器：超时关单兜底、ES 全量重建 |
| `mall-monitor` | 8301 | Spring Boot Admin：实例监控与指标 |

---

## 3. 依赖关系

### 3.1 公共模块 DAG（严格单向，无环）

```
common-core  ←─┬─ common-mybatis
               ├─ common-redis ←─ common-security ←─ common-feign
               └─ common-swagger
```

### 3.2 服务间调用

```
client ─▶ mall-gateway ─┬─▶ mall-user / product / ware / order / payment
                        ├─▶ cart / coupon / search / comment / notice
                        └─▶ mall-admin（BFF，再 Feign 聚合各服务）

mall-order  ──Feign──▶ product / ware / cart / coupon / user / payment
mall-payment ──MQ───▶ order            （不用 Feign 回调，断开循环）
mall-product ──MQ───▶ search           （search 用 Feign 回源详情）
mall-coupon ←──MQ─── order（关单退券）
mall-ware   ←──MQ─── order（关单解锁，Feign 之外的双保险）
```

---

## 4. 快速开始

### 4.1 启动基础设施

```bash
docker compose -f docs/docker/docker-compose-env.yml up -d
```

首次启动 MySQL 会自动执行 `docs/sql/*.sql` 完成建库建表。
若手动执行，注意顺序：`00-init-database.sql` → `01`…`09`，Seata 需要 `10-seata-server.sql`。

### 4.2 编译与打包

```bash
mvn clean install -DskipTests
```

`install` 会把 `mall-common` / `mall-api` 的 jar 装进本地仓库，之后单独构建某个服务（`mvn -pl mall-service/mall-order -am package`）才不会报找不到依赖。

构建完成后，14 个可启动模块的产物都在各自 `target/*.jar`，是内嵌 Tomcat 的可执行 fat jar：

```bash
java -jar mall-service/mall-order/target/mall-order-1.0.0.jar
```

### 4.3 启动顺序（有依赖关系，别乱序）

1. Nacos → 各服务注册
2. `mall-user`、`mall-product` → 提供基础数据
3. `mall-ware`、`mall-coupon` → 下单依赖
4. `mall-cart`、`mall-search`、`mall-comment`、`mall-notice`、`mall-payment`
5. `mall-order` → 依赖前四类
6. `mall-gateway` → 最后起，避免前端打到未就绪的服务
7. 可选：`mall-admin`、`mall-job`、`mall-monitor`

### 4.4 访问

| 入口 | 地址 |
|---|---|
| 网关 | http://localhost:8080 |
| 接口文档 | http://localhost:8080/doc.html（各服务也可直连 `:端口/doc.html`） |
| Nacos 控制台 | http://localhost:8848/nacos |
| RabbitMQ 控制台 | http://localhost:15672（guest/guest） |
| Sentinel 控制台 | http://localhost:8858（sentinel/sentinel） |
| 监控中心 | http://localhost:8301 |
| 后台默认账号 | `admin` / `admin123`（登录后请立即修改） |

---

## 5. 核心链路

### 5.1 下单（唯一使用 Seata 的链路）

```
order.create()
  ├─ Feign  cart.listChecked        取勾选商品
  ├─ Feign  product.listSku         校验价格（不信任前端传值）
  ├─ Feign  user.getAddress         地址快照
  ├─ Feign  coupon.lock             锁券      ← Seata 分支事务
  ├─ Feign  ware.lock               预扣库存  ← Seata 分支事务
  ├─ 本地事务                       写 order + order_item
  └─ 提交后异步
       ├─ cart.clearChecked
       ├─ MQ 延迟消息（30min TTL）
       └─ MQ order.created
```

任何一步失败，Seata AT 通过 `undo_log` 回滚已提交的分支事务。

### 5.2 支付

```
order ──Feign──▶ payment.create          创建支付单
payment ◀──渠道回调── payment.callback     CAS 更新状态 → 发 payment.result
order ◀──MQ── payment.result              状态 0→1，Feign 调 ware.deduct 实扣
```

### 5.3 超时关单（双保险）

- 主链路：下单时投递 TTL 消息 → 30 分钟后死信到 `mall.order.exchange` → `order.closed`
- 兜底：`mall-job` 每分钟扫描 `status=0 AND expire_time < now()`

`order.closed` 被三个队列各自绑定消费：`order.close`（关单）、`ware.unlock`（解锁）、`coupon.release`（退券）。
用**独立队列**而不是共享队列，是为了让任一消费者故障不影响其它补偿。

### 5.4 商品变更同步 ES

```
product 写库 → 事务提交后（TransactionSynchronization.afterCommit）
             → MQ product.changed（只带 spuId + version）
search 消费 → Feign 回源 product.getProductDoc → 按 version 幂等 upsert
```

不下架数据留在索引里：`status != 1` 直接 delete。

---

## 6. 关键设计决策

| 决策 | 原因 |
|---|---|
| 只在「下单」用 Seata | 支付、积分、通知、索引同步全部走 MQ 最终一致，避免全网被全局事务拖慢 |
| 网关是唯一鉴权点 | 业务服务只信任网关注入的 `X-User-*` 头，减少重复验签；网关会先剥离客户端伪造的同名头 |
| 一服务一库 | 禁止跨库 JOIN，库边界即服务边界 |
| 价格不入购物车 | 每次查看实时回源，杜绝"车里 99、结算 129" |
| 记录类入参用 `record` | 不可变、无 setter、序列化开销小；注意访问器是 `xxx()` 不是 `getXxx()` |
| 状态迁移一律 CAS UPDATE | `WHERE status = 旧值`，天然抵御重复回调与并发冲突 |
| MQ 消息用 JSON 字符串 | 不依赖 `Jackson2JsonMessageConverter`，避免生产者/消费者转换器不一致的类型头问题 |
| 内部接口统一 `/inner/**` | 网关按 `/**/inner/**` 直接拒绝外部访问 |

---

## 7. 已知边界（后续可补）

1. `mall-payment` 目前是**模拟支付**（`/payment/mock/{orderNo}`），接入微信/支付宝需补验签与证书管理。
2. 后台 RBAC 是简化版（单表存 `role_code` + `perms`），生产建议拆角色/菜单/数据权限三张表。
3. 未接入链路追踪（SkyWalking / Micrometer Tracing），日志已预留 `traceId` 字段。
4. 秒杀、拼团、分销等营销玩法未实现，`mall-coupon` 已预留发券原子扣减能力。
5. 各服务配置当前走本地 `application.yml`，Nacos 配置中心的 `import-check` 已关闭，
   开启方式：`spring.config.import` 增加 `nacos:mall-user.yml` 并把 `import-check.enabled` 置为 `true`。

---

## 8. 如何新增一个业务服务

1. `mall-api` 下建 `api-xxx`：Feign 接口 + DTO（接口路径必须与 Controller 一致）
2. `mall-service` 下建 `mall-xxx`：pom 继承 `mall-service`，按需引 `common-mybatis`/`common-redis`
3. 代码分层：`controller / service / service.impl / mapper / entity / dto / vo / convert / config / mq`
4. 启动类固定三件套：`@EnableDiscoveryClient`、`@EnableFeignClients(basePackages="com.mallcloud.api")`、`@MapperScan`
5. 网关加路由 + 白名单（如需匿名访问）
6. 建库建表 + `docs/sql` 补一份 DDL
