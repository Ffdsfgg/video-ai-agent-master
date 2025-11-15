<template>
  <view class="container">
    <!-- 标题 -->
    <view class="title">视频解析</view>
    <!-- 表单区域 -->
    <u-form :model="form" :rules="rules" ref="loginForm" class="form">
      <!-- 账号输入 -->
      <u-form-item prop="username">
        <u-input v-model="form.username" placeholder="请输入账号" clearable class="input" />
      </u-form-item>
      <!-- 密码输入 -->
      <u-form-item prop="password">
        <u-input v-model="form.password" placeholder="请输入密码" password clearable class="input" />
      </u-form-item>
      <!-- 验证码行 -->
      <u-form-item prop="code">
        <view class="row-between">
          <u-input v-model="form.code" placeholder="请输入验证码" class="code-input" />
          <up-image :show-loading="true" :src="codeImg.imageData" width="90px" height="40px" @click="loadCodeImg" />
        </view>
      </u-form-item>

      <!-- 登录按钮 -->
      <u-button type="primary" text="登录" shape="circle" class="login-btn" @click="login" />
    </u-form>
    <!-- 底部链接 -->
    <view class="footer">
      <text class="link-text" @click="gotoRegister">注册新账号</text>
    </view>

    <u-toast ref="toast"></u-toast>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { userLogin, getCodeImg, getCodeImgValidate } from '../../api/user';
// 表单数据
const form = reactive({
  username: '',
  password: '',
  code: ''
});


// 表单引用
const loginForm = ref();

// 验证码图片数据
const codeImg = ref('');
const toast = ref()

// 校验规则
const rules = {
  username: [
    {
      required: true,
      message: '账号不能为空',
      trigger: ['blur', 'change']
    },
    {
      minLength: 3,
      message: '账号长度不能小于3位',
      trigger: ['blur', 'change']
    }
  ],
  password: [
    {
      required: true,
      message: '密码不能为空',
      trigger: ['blur', 'change']
    },
    {
      minLength: 6,
      message: '密码长度不能小于6位',
      trigger: ['blur', 'change']
    }
  ],
  code: [
    {
      required: true,
      message: '验证码不能为空',
      trigger: ['blur', 'change']
    },
    {
      length: 4,
      message: '验证码必须为4位',
      trigger: ['blur', 'change']
    }
  ]
};

// 加载验证码
const loadCodeImg = async () => {
  try {
    const { data } = await getCodeImg();
    console.log('验证码数据:', data);
    codeImg.value = data;
    form.code = '';
    loginForm.value.clearValidate('code');
  } catch (e) {
    uni.showToast({ title: '验证码获取失败', icon: 'none' });
  }
};

// 登录事件
const login = async () => {
  try {

    await loginForm.value.validate();

    const res = await getCodeImgValidate(codeImg.value.token, form.code)
    console.log('验证码校验结果:', res);

    //校验没过
    if (res.code !== 200) {
      console.log('验证码校验失败:', res);
      toast.value.show({
        type: 'error',
        message: res.message,
      })
      loadCodeImg();
      return;
    }

    // 登录接口
    const data = await userLogin(form);
    console.log('登录结果:', data);
    if (data.code !== 200) {
      toast.value.show({
        type: 'error',
        message: data.message,
      })
      loadCodeImg();
      return;
    }
    //存token
    // console.log('token:', data.data);
    uni.setStorageSync('token', data.data);
    console.log('token111:', uni.getStorageSync('token'));
    uni.showToast({ title: '登录成功' });
    // 跳转到首页
    uni.switchTab({ url: '/pages/index/index' });

  } catch (error) {
    console.error('登录失败:', error[0]);
    toast.value.show({
      type: 'error',
      message: error[0].message || '登录失败',
    })
    console.error('校验失败:', error);
    loadCodeImg();
  }
};

// 初始化加载验证码
loadCodeImg();

// 页面跳转方法
const gotoRegister = () => {
  uni.navigateTo({ url: '/pages/reg/reg' });
};

</script>

<style lang="scss" scoped>
.container {
  padding: 40rpx;
  background-color: #fff;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  text-align: center;
  margin: 60rpx 0;
}

.form {
  .input {
    margin-bottom: 30rpx;
    border-bottom: #c5c5c5 2px solid;
    border-radius: 0;
    padding: 20rpx;
  }

  .row-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .code-input {
      flex: 1;
      margin-right: 20rpx;
      margin-bottom: 30rpx;
      border-bottom: #c5c5c5 2px solid;
      border-radius: 0;
      padding: 20rpx;
    }
  }

  .row-end {
    margin-bottom: 50rpx;
    display: flex;
    justify-content: flex-end;
  }
}

.link-text {
  color: #2979ff;
  font-size: 28rpx;
}

.login-btn {
  margin-top: 40rpx;
}

.footer {
  margin: 60rpx 0;
  display: flex;
  justify-content: space-around;
}

.third-login {
  .divider {
    color: #999;
    text-align: center;
    position: relative;
    margin: 40rpx 0;
  }

  .icons {
    padding: 0 200rpx;
  }
}
</style>