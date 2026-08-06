# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Java 21 / Spring Boot 3.5.8 monorepo for a Kubernetes AI dashboard platform. Provides K8s cluster resource management, AI chat (Spring AI), MCP tool integration, and an A2A (Agent-to-Agent) server framework. Uses Spring AI for LLM/Agent/MCP capabilities and Fabric8 Kubernetes Client for cluster operations.

## Architecture

### Deployable Application
One deployable Spring Boot app:
- **dashboard** (port 8080) - main class `cc.shiyi.dashboard.Main`. Maven module `dashboard` (directory `dashboard/`); the Java package remains `cc.shiyi.dashboard` (unchanged by the module rename). Aggregates `message`, `common`, `oss`, `user`, `ai-base`. Handles K8s resource management, AI chat, MCP server+client, A2A server framework.
  - Annotations on `Main`: `@SpringBootApplication`, `@EnableDiscoveryClient`, `@EnableFeignClients`, `@ComponentScan("cc.shiyi")`, `@MapperScan("cc.shiyi")` (MyBatis-Plus, `BaseMapper`-scoped), `@EnableScheduling`.
  - Legacy naming: `spring.application.name` is `ceditor` and `common`/`user` packages are `cc.shiyi.coleditor.*` (inherited from a prior codebase). Ignore the names - it's all the dashboard app.

All beans from aggregated modules load into a single Spring context (`@ComponentScan("cc.shiyi")`) - no module isolation.

### Library Modules
- **common** (`cc.shiyi.coleditor.common`) - AOP annotations + aspects: `@RedisCache`, `@EvictRedisCache`, `@DistributedLock` (Redis SETNX spin-lock), `@DistributedRateLimiter` (Lua sliding-window), `@Log` (JSON args/return logging). Also `ResponseWrapper`, `GlobleExceptionHandler` (typo is intentional/legacy), `JacksonConfig`, `RedisConfig`, `WebClientConfig`, utils (`ExcelUtil`, `JsonUtil`, `ListPageUtil`, `MD5Util`, `SshUtil`).
- **ai-base** (`cc.shiyi.base.ai`) - `ChantClientPool` (Caffeine cache, 1000 entries, 30min TTL, keyed by conversationId) wraps `ChatClient.Builder` + `MessageChatMemoryAdvisor`. JDBC-backed chat memory via `MessageWindowChatMemory` (keeps last 20 messages, MySQL dialect, table `SPRING_AI_CHAT_MEMORY`). `PromptComposer` for RAG context injection.
- **oss** (`cc.shiyi.oss`) - MinIO integration: auto-creates bucket with public-read policy on startup. `UploadService`, `MinioFileListService`.
- **user** (`cc.shiyi.coleditor.user`) - Sa-Token auth with custom 32-char random token generation. `UserService`, `StpInterfaceImpl`. Seeds `admin/admin123` on boot.
- **search** (`cc.shiyi.search`) - Apache Lucene full-text search with IK Analyzer for Chinese text. Not a dependency of dashboard (standalone library module).
- **message** (`cc.shiyi.message`) - Site broadcast messages (`site_message`, `site_message_read` tables) via `SiteMessageService`.

### Inter-Module Dependencies
```
dashboard --> message, common, oss, user, ai-base
message   --> common
search    --> common              (not aggregated into dashboard)
user, oss, ai-base --> common
```

### Frontend (dashboard-fe)
Vue 3.5 + Vite 8 + Element Plus + Pinia, with vue-router, echarts 6, marked 18, highlight.js 11. Dev server on port 3000, proxies `/api` to `http://localhost:8080`. Single-page `Dashboard.vue` aggregates K8s management views. API clients in `src/api/` (ai.js, k8s.js, message.js, oss.js, registry.js, user.js).

## Key Architectural Patterns

### Kubernetes Operations
- **`KubernetesService`** - generic K8s CRUD: namespaces, pods, deployments, services, ingresses, PVCs, configmaps, secrets, events, jobs. Also `getClusterStats()`.
- **K8s client config (`KubernetesClientConfig`)**: requires an explicit kubeconfig via `kubernetes.client.config-url` (HTTP download to temp file) or `config-path` (local file). Throws if neither is set - in-cluster ServiceAccount auto-detection is NOT implemented. `trust-certs` defaults true.
- **Resource ownership tracking**: K8s resources created through the platform are recorded in `platform_resource` (+ `ResourceOwnershipService` / `ResourceOwnershipController`) per `creator_id`. Users can only delete resources they created (`ResourceNotManagedException`).

### Internal API
All REST under `/api/v1/**` (ai, k8s, kube-* resources, user, oss, messages) requires Sa-Token login.

### A2A (Agent-to-Agent) Server Framework
Retained as an empty shell for future extension. Gated by `a2a.enabled` (default true).
- **`A2AController`** - `GET /a2a/card` + `GET /.well-known/agent-card.json` (AgentCard discovery), `POST /a2a` (JSON-RPC 2.0 endpoint).
- **`A2ARequestHandler`** - dispatches JSON-RPC methods (`message/send`, `tasks/get`, `tasks/cancel`, `tasks/list`) to `SolverProvider` beans. Method-name aliases case-insensitive (`message/send` / `sendMessage` / `SendMessage`).
- **`SolverProvider` SPI** (`cc.shiyi.dashboard.advisor.SolverProvider`) - extension point: `getSolverType()`, `getSkills()`, `processMessage()`, `getTask()`, `cancelTask()`. **No implementations are currently registered** (solvers were removed), so `AgentCard` skills is empty and `message/send` returns "No solver provider available". To add a capability: implement `SolverProvider` + register as a bean; `A2AServerConfig` auto-discovers all providers and aggregates skills into the `AgentCard` - no config edit needed.
- **Auth**: `A2AAuthFilter` validates an API key (defaults to `external.api-key`). Sa-Token excludes `/a2a/**` and `/.well-known/agent-card.json`.

### AI/MCP Dual Role
- **MCP Server**: `McpConfig` registers **7** `@Tool`-annotated services as a `ToolCallbackProvider`: `KubernetesMcpService`, `SystemInfoMcpService`, `CommandLineMcpService` (danger-command blacklist), `FileMcpService` (sandboxed to configured root), `SshMcpService`, `OssMcpService`, `DockerRegistryMcpService`. To add a tool: create a `@Tool`-method service under `service/mcp/` and add it to the `toolObjects(...)` list in `McpConfig`.
- **MCP Client**: consumes tools from external MCP servers (configured in `ai.yml`, `type: SYNC`).
- **Chat flow**: `AiChatController` -> `ChantClientPool` (per-conversation ChatClient) -> `MessageChatMemoryAdvisor` (JDBC) -> streaming/non-streaming response with tool callbacks attached.

### LLM Configuration
Chat model defaults to **DeepSeek** (`deepseek-v4-pro` via `https://api.deepseek.com`, OpenAI-compatible). Embedding: `BGE_M3` via OneAPI. All overridable via `SPRING_AI_OPENAI_*` env vars. (Milvus/vector-store wiring is not active - don't assume vector search is wired up.)

## Build & Run

```bash
mvn clean install                          # build entire project
mvn clean install -pl dashboard -am        # build dashboard with dependencies
cd dashboard && mvn spring-boot:run        # run dashboard (port 8080)
cd dashboard-fe && npm run dev             # frontend dev server (port 3000)
```

### Test
```bash
mvn test                                   # all tests
mvn test -pl dashboard                     # dashboard module tests
mvn test -pl dashboard -Dtest=SomeTest     # single class
```

### Dev Infrastructure
Docker-based services in `docker/` (mysql, redis, minio, milvus, elasticsearch, loki). Windows batch launchers or `docker-compose up -d` at repo root.

### Configuration
`dashboard/src/main/resources/application.yml` imports fragments (all `optional:`, all support `${ENV_VAR:default}`): `mysql`, `redis`, `oss`, `json`, `ai`, `sa-token`, `lucene`, `kubernetes`, `docker-registry`.

Key config files in `dashboard/src/main/resources/`:
- `application.yml` - main config + `external.api-key` + `a2a.*` block.
- `mysql.yml` - JDBC URL (`createDatabaseIfNotExist=true`), credentials.
- `ai.yml` - Spring AI MCP server/client, OpenAI-compatible model settings.
- `sa-token.yml` - token timeout (12h), `excludePath` (swagger, register, login, AI endpoints, `/a2a/**`, `/.well-known/agent-card.json`), alone-redis settings.
- `kubernetes.yml` - `kubernetes.client.config-url` / `config-path` / `trust-certs`.

Database schema is auto-initialized from `dashboard/src/main/resources/db/schema.sql` on every boot (`spring.sql.init.mode=always`). The script also seeds the `admin/admin123` user and a welcome `site_message`.

## Key Dependencies
- Java 21, Spring Boot 3.5.8, Spring Cloud 2025.0.0, Spring AI 1.1.4
- Fabric8 Kubernetes Client BOM 6.9.2
- MyBatis Plus 3.5.14, MySQL, Redis (Lettuce), Caffeine
- Sa-Token 1.44.0 (random 32-char tokens, Redis-backed sessions via `alone-redis`)
- MinIO 8.6.0, Apache Lucene (IK Analyzer), JSch (SSH MCP tool), commons-email
- OpenFeign + Resilience4j circuit breaker, Spring Batch
- Lombok, SpringDoc OpenAPI 2.8.13, commonmark (markdown rendering)

## Base Package
All Java code lives under `cc.shiyi.*`. `common` and `user` use `cc.shiyi.coleditor.*` (legacy from the ceditor origin); `ai-base` uses `cc.shiyi.base.ai.*`; the dashboard module's Java package is `cc.shiyi.dashboard.*` (unchanged from before the module was renamed `dashboard` -> `dashboard`); `search`, `message` use `cc.shiyi.<module>.*`.
