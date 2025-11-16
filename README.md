# AI 智能教学视频系统 🎓

<div align="center">

**Video AI Agent - 让教学视频"看得懂、搜得到、学得会"**

*融合人工智能与在线教育的智能视频教学平台*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0%2B-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3%2B-green?logo=vue.js)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue?logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

## 🎬 功能演示

<div align="center">

[![功能演示视频](https://img.shields.io/badge/🎥-点击观看演示视频-ff69b4)](https://github.com/user-attachments/assets/b6aed7e6-e1d0-4dbd-81b5-1bd77873da2b)

*体验AI增强的教学视频功能*

</div>

## 🏗️ 系统架构

<div align="center">
  <img src="images/屏幕截图%202025-11-16%20002744.png" alt="系统架构" width="700">
  <br>
  <em>系统整体架构设计</em>
</div>

## ⚙️ 技术架构

<div align="center">
  <img src="images/屏幕截图%202025-11-16%20002752.png" alt="技术架构" width="700">
  <br>
  <em>技术栈与组件关系</em>
</div>

## 🖥️ 界面展示

### 📱 前端展示界面

<div align="center">
  <div style="display: flex; justify-content: center; gap: 10px; flex-wrap: wrap;">
    <img src="images/屏幕截图%202025-11-16%20000015.png" alt="视频播放页" width="280" height="420">
    <img src="images/屏幕截图%202025-11-16%20000024.png" alt="视频列表" width="280" height="420">
    <img src="images/屏幕截图%202025-11-16%20000035.png" alt="搜索功能" width="280" height="420">
    <img src="images/屏幕截图%202025-11-16%20000043.png" alt="用户界面" width="280" height="420">
    <img src="images/屏幕截图%202025-11-16%20000057.png" alt="移动端适配" width="280" height="420">
  </div>
</div>

### 💻 后台管理系统

<div align="center">
  <div style="display: flex; justify-content: center; gap: 10px; flex-wrap: wrap;">
    <img src="images/屏幕截图%202025-11-16%20000143.png" alt="后台管理1" width="400" height="267">
    <img src="images/屏幕截图%202025-11-16%20000200.png" alt="后台管理3" width="400" height="267">
  </div>
</div>

## ✨ 核心功能

| 功能 | 描述 | 特色 |
|------|------|------|
| **🎥 智能视频处理** | 自动生成字幕、知识点标注 | AI驱动的内容增强 |
| **🔍 智能搜索** | 基于字幕和知识点的全文检索 | 精准定位教学内容 |
| **📚 知识点标注** | 自动识别和标记关键知识点 | 提升学习效率 |
| **👥 权限管理** | 多角色用户权限控制 | 安全的访问控制 |
| **🎯 个性化学习** | 基于用户行为的推荐 | 自适应学习路径 |

## 🛠️ 技术栈

### 💻 后端技术（Java / Spring Boot）

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **核心框架** | Spring Boot 3 | 现代化Java开发框架 |
| **数据持久化** | MySQL + MyBatis-Plus | 高效数据库操作 |
| **安全认证** | JWT + Spring Security | 安全的身份验证 |
| **文件存储** | 阿里云 OSS | 可靠的云存储服务 |
| **工具类** | VideoProcessorUtils、JwtUtil、OSSUtils | 业务工具封装 |

### 🌐 前端技术

| 部分 | 技术选型 | 说明 |
|------|----------|------|
| **框架** | Vue 3 / React | 现代化前端框架 |
| **视频播放** | 自定义播放器 + 字幕高亮 | 增强的观看体验 |
| **UI组件** | Element Plus / Ant Design | 丰富的UI组件库 |
| **状态管理** | Pinia / Redux | 可预测的状态管理 |

### 🚀 构建与部署

| 环节 | 工具 | 配置 |
|------|------|------|
| **构建工具** | Maven (`./mvnw`) | 项目构建和依赖管理 |
| **环境配置** | 多环境配置文件 | dev/test/prod环境隔离 |
| **部署方式** | 容器化部署 | 支持Docker部署 |

## 📁 项目结构

```bash
video-ai-agent-master/
├── AI-teaching-video/                 # 后端 Spring Boot 项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/hip/aiteachingvideo/
│   │       │   ├── controller/        # 控制器层
│   │       │   ├── service/           # 业务逻辑层
│   │       │   ├── mapper/            # 数据访问层
│   │       │   ├── entity/            # 实体类
│   │       │   ├── config/            # 配置类
│   │       │   └── utils/             # 工具类
│   │       └── resources/
│   │           ├── application.yaml   # 主配置文件
│   │           ├── application-dev.yml # 开发环境配置
│   │           └── application-test.yml # 测试环境配置
├── web-teaching-video/                # 前端工程
│   ├── src/
│   ├── public/
│   └── package.json
├── .gitignore
└── README.md
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **MySQL**: 8.0+
- **Node.js**: 16+
- **Maven**: 3.6+

### 后端启动

1. **克隆项目**
```bash
git clone <项目地址>
cd video-ai-agent-master
```

2. **数据库配置**
   - 创建MySQL数据库
   - 修改 `application-dev.yml` 中的数据库连接信息

3. **配置云存储**
   - 申请阿里云OSS服务
   - 配置AccessKey和Bucket信息

4. **启动服务**
```bash
cd AI-teaching-video
./mvnw spring-boot:run
```

### 前端启动

```bash
cd web-teaching-video
npm install
npm run dev
```

### 配置说明

主要配置文件：
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_teaching_video
    username: your_username
    password: your_password
  # 其他配置...
```

## 🔧 核心特性详解

### 🎯 AI视频处理流程
1. **视频上传** → 阿里云OSS存储
2. **语音识别** → 自动生成字幕
3. **内容分析** → 知识点标注
4. **索引构建** → 支持智能搜索

### 🛡️ 安全机制
- JWT令牌认证
- 基于角色的访问控制(RBAC)
- 文件上传安全校验
- SQL注入防护

## 🤝 贡献指南

我们欢迎社区贡献！请按以下步骤参与：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

如有问题或建议，请通过以下方式联系：

- **项目地址**: [GitHub Repository](https://github.com/your-username/video-ai-agent)
- **问题反馈**: [GitHub Issues](https://github.com/your-username/video-ai-agent/issues)
- **邮箱**: contact@aiteachingvideo.com

---

<div align="center">

**用AI技术重新定义在线教育，让每个学习者都能获得个性化的教学体验** 🚀

*智慧教育，从视频开始*

</div>
