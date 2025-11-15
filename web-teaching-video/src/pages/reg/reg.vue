<template>
  <view class="container">
    <!-- 标题 -->
    <view class="title">欢迎注册</view>
    <!-- 表单区域 -->
    <up-form :model="form" :rules="rules" ref="formRef">
      <view class="form">
        <!-- 用户名 -->
        <up-form-item prop="username" :borderBottom="false">
          <u-input v-model="form.username" placeholder="用户名" clearable class="input" shape="circle" />
        </up-form-item>
        <!-- 密码 -->
        <up-form-item prop="password" :borderBottom="false">
          <u-input v-model="form.password" placeholder="密码" password clearable class="input" shape="circle" />
        </up-form-item>
        <!-- 邮箱 -->
        <up-form-item prop="email" :borderBottom="false">
          <u-input v-model="form.email" placeholder="请输入邮箱（选填）" clearable class="input" shape="circle" />
        </up-form-item>

        <!-- 注册按钮 -->
        <u-button type="primary" text="注册" shape="circle" class="register-btn" :customStyle="{ fontWeight: 'bold' }"
          @click="handleSubmit" />
        <!-- 底部链接 -->
        <view class="bottom-link">
          <text>已有账号？</text>
          <text class="login-text" @click="gotoLogin">登录</text>
        </view>
      </view>
    </up-form>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { register } from "@/api/user.js";

const formRef = ref();

const form = reactive({
  username: '',
  password: '',
  email: '',
  reg: 1
});

// 验证规则（只保留 username、password、email）
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 12, message: '长度在3到12个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 18, message: '长度在6到18个字符', trigger: 'blur' }
  ],
  email: [
    {
      type: 'email',
      message: '邮箱格式不正确',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          callback(new Error(rule.message));
        } else {
          callback();
        }
      }
    }
  ]
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    console.log('表单数据:', form);

    const res = await register(form);
    console.log('注册结果:', res);
    if (res.code === 200) {
      uni.$u.toast('注册成功');
      uni.navigateTo({ url: '/pages/login/login' });
    } else {
      uni.$u.toast(res.msg || '注册失败');
    }
  } catch (error) {
    uni.$u.toast("请检查输入内容");
  }
};

// 跳转登录页
const gotoLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' });
};
</script>


<style lang="scss" scoped>
.container {
  padding: 60rpx 40rpx;
  background-color: #ffffff;
}

.title {
  font-size: 52rpx;
  font-weight: bold;
  text-align: center;
  margin: 80rpx 0 60rpx;
  color: #333;
}

.form {
  .input {
    border-bottom: #c5c5c5 2px solid;
    border-radius: 0;
    padding: 28rpx 32rpx;
  }

  .row-flex {
    display: flex;
    gap: 20rpx;

    .code-input {
      flex: 1;
      border-bottom: #c5c5c5 2px solid;
      border-radius: 0;
      padding: 28rpx 32rpx;
    }

  }

  .register-btn {
    height: 96rpx;
    font-size: 34rpx;
  }

  .bottom-link {
    margin-top: 50rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;

    .login-text {
      color: #2979ff;
      margin-left: 10rpx;
    }
  }
}
</style>