<div align="center">
  <h1>Kubernetes AI Dashboard</h1>
  <p><b>K8s Cluster Management + AI Assistant Platform</b></p>
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.8-green">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.5-brightgreen">
  <img alt="Kubernetes" src="https://img.shields.io/badge/Kubernetes-v1.24%2B-blue">
  <img alt="License" src="https://img.shields.io/badge/License-Internal-lightgrey">
</div>

<p align="center">
  🇨🇳 <a href="./README.md">简体中文</a> | 🇺🇸 <a href="./README.en.md">English</a>
</p>

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Kubernetes AI Dashboard is a Kubernetes management platform built for **Ops teams**, integrating an AI assistant and an MCP toolchain. It uses Fabric8 Kubernetes Client for full-resource management, Spring AI for tool-calling and chat, and ships with a built-in A2A (Agent-to-Agent) protocol framework.

> [!NOTE]
> Designed for operations scenarios — unified K8s cluster resource management with AI-assisted ops.

## Features

- ☸️ **Full K8s Resource Management**: CRUD for Namespace / Pod / Deployment / Service / Ingress / PVC / ConfigMap / Secret / Event / Job / CRD
- 📊 **Cluster Monitoring**: node status and resource usage statistics
- 🔒 **Resource Ownership Tracking**: resources created via the platform are tracked per user; users can only delete their own
- 🤖 **AI Chat**: Spring AI integration with tool-calling, streaming responses, and conversational memory
- 🔌 **MCP Toolchain**: 7 built-in MCP tool services (K8s / System Info / Command Line / File / SSH / OSS / Docker Registry)
- 🤝 **A2A Protocol Framework**: Agent-to-Agent JSON-RPC 2.0 endpoint with reserved extension point
- 💾 **Object Storage**: MinIO integration, auto-creates bucket
- 🔐 **Auth**: Sa-Token, 32-char random token, Redis-backed sessions

## Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.8 |
| AI Framework | Spring AI | 1.1.4 |
| K8s Client | Fabric8 Kubernetes Client | 6.9.2 |
| ORM | MyBatis Plus | 3.5.14 |
| Auth | Sa-Token | 1.44.0 |
| Object Storage | MinIO | 8.6.0 |
| Database | MySQL | 8.0+ |
| Cache | Redis | 7.0+ |
| Frontend Framework | Vue | 3.5 |
| Frontend Build | Vite | 8 |
| UI Components | Element Plus | 2.13 |

## Project Structure

```mermaid
<!-- Experimental: if rendering fails, preview on GitHub -->
graph TD
    A[kubernetes-ai-dashboard] --> B[dashboard backend app]
    A --> C[dashboard-fe frontend]
    A --> D[common shared module]
    A --> E[ai-base AI core]
    A --> F[oss object storage]
    A --> G[user auth]
    A --> H[search full-text search]
    A --> I[message site messages]
```

```
kubernetes-ai-dashboard/
├── dashboard/              # Backend main app (cc.shiyi.dashboard)
├── dashboard-fe/           # Frontend app (Vue 3)
├── common/                 # Shared module (AOP annotations, utils)
├── ai-base/                # AI core (ChatClient pool, chat memory)
├── oss/                    # Object storage (MinIO)
├── user/                   # Auth (Sa-Token)
├── search/                 # Full-text search (Lucene)
├── message/                # Site messages
└── pom.xml
```

## Quick Start

### Prerequisites

- JDK 21+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose
- Kubernetes cluster v1.24+

### Start Dependencies

```bash
cd docker
docker-compose up -d mysql redis minio
```

### Backend

```bash
mvn clean install -DskipTests
cd dashboard
mvn spring-boot:run
```

Backend runs at http://localhost:8080

### Frontend

```bash
cd dashboard-fe
npm install
npm run dev
```

Frontend runs at http://localhost:3000

## Configuration

Config file: `dashboard/src/main/resources/application.yml`. All values support `${ENV_VAR:default}` overrides.

| Env Var | Description | Default |
|---------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | jdbc:mysql://localhost:3306/mpi_dashboard |
| `SPRING_DATA_REDIS_HOST` | Redis host | localhost |
| `MINIO_ENDPOINT` | MinIO URL | http://localhost:9000 |
| `SPRING_AI_OPENAI_BASE_URL` | AI API URL | https://api.deepseek.com |
| `SPRING_AI_OPENAI_API_KEY` | AI API Key | `<YOUR_API_KEY>` |
| `KUBERNETES_CLIENT_CONFIG_URL` | kubeconfig download URL | - |
| `KUBERNETES_CLIENT_CONFIG_PATH` | kubeconfig local path | - |
| `KUBERNETES_CLIENT_TRUST_CERTS` | Trust K8s API certs | true |
| `A2A_API_KEY` | A2A protocol API Key | `<YOUR_API_KEY>` |

> [!IMPORTANT]
> The Kubernetes client requires an explicit kubeconfig (`KUBERNETES_CLIENT_CONFIG_URL` or `KUBERNETES_CLIENT_CONFIG_PATH`), otherwise startup fails. In-cluster ServiceAccount auto-detection is NOT supported.

## API Reference

### Internal API (`/api/v1/*`, Sa-Token login required)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/ai/chat` | AI chat (streaming/non-streaming) |
| GET/POST/PUT/DELETE | `/api/v1/k8s/*` | K8s resource management |
| GET | `/api/v1/k8s/cluster/stats` | Cluster statistics |
| GET | `/api/v1/resources/ownership` | Resource ownership query |
| POST/GET | `/api/v1/user/*` | User management |

### A2A Protocol (`/a2a`, API Key auth)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/a2a/card` | Get AgentCard |
| GET | `/.well-known/agent-card.json` | Standard discovery endpoint |
| POST | `/a2a` | JSON-RPC 2.0 (message/send, tasks/get, tasks/cancel) |

<details>
<summary>MCP Tools (7)</summary>

| Tool Service | Capability |
|--------------|------------|
| KubernetesMcpService | Namespace/Pod/Deployment ops, cluster stats |
| SystemInfoMcpService | CPU/memory/disk/uptime |
| CommandLineMcpService | Command execution (dangerous-command blacklist) |
| FileMcpService | File read/write (sandboxed) |
| SshMcpService | Remote SSH commands |
| OssMcpService | MinIO upload/list |
| DockerRegistryMcpService | Docker registry query |

</details>

## Deployment

For Docker deployment see [DOCKER_README.md](DOCKER_README.md).

```bash
# Build backend image
docker build -f dashboard/Dockerfile -t <REGISTRY>/mpi-dashboard-backend:latest .

# Build frontend image
docker build -f dashboard-fe/Dockerfile -t <REGISTRY>/mpi-dashboard-frontend:latest ./dashboard-fe
```

## Contributing

- [ ] Improve cluster monitoring alerts
- [ ] Add more K8s resource types

Issues and Pull Requests are welcome.

## License

This project uses an **Internal License** and is not open-sourced.
