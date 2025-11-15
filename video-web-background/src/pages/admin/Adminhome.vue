<template>
  <div class="common-layout">
    <el-container>
      <!-- 顶部 Header -->
      <el-header class="app-header">
        <div class="brand">
          <!-- 图标 -->
          <el-icon :size="28" color="#fff">
            <Cpu />
          </el-icon>
          <h2>AI 智能解析系统</h2>
        </div>
        <div class="user-action">
          <el-button type="danger" text>
            <RouterLink to="/login" class="logout-link">退出</RouterLink>
          </el-button>
        </div>
      </el-header>

      <el-container class="main-wrapper">
        <!-- 侧边栏 Aside -->
        <el-aside width="220" class="app-aside">
          <el-menu router :default-openeds="['3']" background-color="transparent" text-color="#e5e5e5"
            active-text-color="#fff" :unique-opened="true">
            <!-- 平台概述 -->
            <el-menu-item index="/admin">
              <el-icon size="18">
                <Histogram />
              </el-icon>
              <template #title>平台概述</template>
            </el-menu-item>

            <!-- 用户管理 -->
            <el-sub-menu index="1">
              <template #title>
                <el-icon size="18">
                  <User />
                </el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/admin/alluser">用户列表</el-menu-item>
            </el-sub-menu>

            <!-- 视频管理 -->
            <el-sub-menu index="2">
              <template #title>
                <el-icon size="18">
                  <VideoCamera />
                </el-icon>
                <span>视频管理</span>
              </template>
              <el-menu-item index="/admin/video">视频信息</el-menu-item>
              <el-menu-item index="/admin/videoInfo">AI 分析信息</el-menu-item>
              <el-menu-item index="/admin/subtitles">字幕信息</el-menu-item>
            </el-sub-menu>

            <!-- 配置管理 -->
            <el-sub-menu index="3">
              <template #title>
                <el-icon size="18">
                  <Setting />
                </el-icon>
                <span>数据信息</span>
              </template>
              <el-menu-item index="/admin/repository">本地知识库</el-menu-item>
              <el-menu-item index="/admin/thread">线程监控</el-menu-item>
              <el-menu-item index="/admin/myinfo">我的详情</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-container>
          <el-breadcrumb class="breadcrumb" separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta?.title || '未知页面' }}</el-breadcrumb-item>
          </el-breadcrumb>

          <el-main class="app-main">
            <router-view />
          </el-main>

          <el-footer class="app-footer" height="40">
            ©2025 AI 视频解析系统
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style lang="scss">
:root {
  --header-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --aside-bg: rgba(43, 40, 58, 0.85);
  --main-bg: #f4f6fc;
  --text-color: #333;
  --active-color: #fff;
  --border-color: rgba(255, 255, 255, 0.12);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/*  顶部 Header */
.app-header {
  background: var(--header-bg);
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .brand {
    display: flex;
    align-items: center;
    gap: 10px;

    h2 {
      font-size: 19px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
  }

  .user-action {
    display: flex;
    gap: 8px;

    .logout-link {
      color: inherit;
      text-decoration: none;
    }
  }
}

/* 主布局 */
.main-wrapper {
  height: calc(100vh - 60px);
  display: flex;
  background: var(--main-bg);
}

/* 侧边栏 Aside */
.app-aside {
  background: var(--aside-bg);
  border-right: 1px solid var(--border-color);
  padding: 12px 0;
  backdrop-filter: blur(10px);

  .el-menu {
    border-right: none;
    background: transparent;

    .el-menu-item,
    .el-sub-menu__title {
      font-size: 14px;
      height: 44px;
      line-height: 44px;
      margin: 4px 12px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.12);
      }

      &.is-active {
        background: rgba(255, 255, 255, 0.22);
        font-weight: 600;
      }
    }
  }
}

/* 面包屑 */
.breadcrumb {
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e9e9e9;
  font-size: 14px;
}

/* 主内容区 */
.app-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* Footer */
.app-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #e9e9e9;
}
</style>