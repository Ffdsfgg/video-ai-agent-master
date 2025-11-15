package com.hip.aiteachingvideo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hip.aiteachingvideo.model.po.User;
import com.hip.aiteachingvideo.service.UserService;
import com.hip.aiteachingvideo.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 87502
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-07-05 19:33:07
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

}




