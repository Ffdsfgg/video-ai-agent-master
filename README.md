 AI 智能教学视频系统（Video AI Agent）

一个融合人工智能与在线教育的智能视频教学平台，支持视频上传、AI 自动生成字幕、知识点标注、用户权限管理等功能，让教学视频“看得懂、搜得到、学得会”。
 初始入口为视频播放页，用户可直接观看 AI 增强后的教学内容。


https://github.com/user-attachments/assets/b6aed7e6-e1d0-4dbd-81b5-1bd77873da2b


 项目截图
🔧 系统架构
![系统架构](images/屏幕截图%202025-11-16%20002744.png)
⚙️ 技术栈
![技术架构](images/屏幕截图%202025-11-16%20002752.png)
🖥️ 界面展示

后台管理 前端展示
-------- --------
<a href="images/屏幕截图 2025-11-16 000015.png">
  <img src="images/屏幕截图 2025-11-16 000015.png" alt="系统架构图" width="400" height="600">
</a>
<a href="images/屏幕截图 2025-11-16 000024.png">
  <img src="images/屏幕截图 2025-11-16 000024.png" alt="系统架构图" width="400" height="600">
</a>
<a href="images/屏幕截图 2025-11-16 000035.png">
  <img src="images/屏幕截图 2025-11-16 000035.png" alt="系统架构图" width="400" height="600">
</a>
<a href="images/屏幕截图 2025-11-16 000043.png">
  <img src="images/屏幕截图 2025-11-16 000043.png" alt="系统架构图" width="400" height="600">
</a>
<a href="images/屏幕截图 2025-11-16 000057.png">
  <img src="images/屏幕截图 2025-11-16 000057.png" alt="系统架构图" width="400" height="600">
</a>
<a href="images/屏幕截图 2025-11-16 000143.png">
  <img src="images/屏幕截图 2025-11-16 000143.png" alt="系统架构图" width="600" height="400">
</a>
<a href="images/屏幕截图 2025-11-16 000152.png">
  <img src="images/屏幕截图 2025-11-16 000152.png" alt="系统架构图" width="600" height="400">
</a>
<a href="images/屏幕截图 2025-11-16 000200.png">
  <img src="images/屏幕截图 2025-11-16 000200.png" alt="系统架构图" width="600" height="400">
</a>

🛠️ 技术栈
后端（Java / Spring Boot）
核心框架：Spring Boot 3
数据库：MySQL + MyBatis-Plus
安全认证：JWT + Spring Security
对象存储：阿里云 OSS
工具类：VideoProcessorUtils（视频处理）、JwtUtil、OSSUtils 等
前端
框架：Vue / React（请根据实际补充）
视频播放：自定义播放器 + 字幕高亮交互
UI 组件库：Element Plus / Ant Design（按需替换）
构建与部署
构建工具：Maven（./mvnw）
多环境配置：application-dev.yml、application-test.yml、application.yaml

📁 项目结构

video-ai-agent-master/
├── AI-teaching-video/ # 后端 Spring Boot 项目
│ ├── src/main/java/com/hip/aiteachingvideo/
│ └── src/main/resources/
├── web-teaching-video/ # 前端工程
├── .gitignore
└── README.md

🚀 快速启动
启动后端
bash
cd AI-teaching-video
./mvnw spring-boot:run
启动前端
bash
cd web-teaching-video
npm install
npm run dev
 请先配置数据库连接、OSS 密钥等（参考 application-dev.yml）
