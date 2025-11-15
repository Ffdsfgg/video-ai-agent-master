package com.hip.aiteachingvideo.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hip.aiteachingvideo.context.BaseContext;
import com.hip.aiteachingvideo.model.po.User;
import com.hip.aiteachingvideo.service.UserService;
import com.hip.aiteachingvideo.utils.IpUtils;
import com.hip.aiteachingvideo.utils.JwtUtil;
import com.hip.aiteachingvideo.utils.Result;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "用户管理")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${hip.jwt.admin-secret-key}")
    private String SecretKey;
    @Value("${hip.jwt.admin-ttl}")
    private long adminTtl;

    /**
     * 账号密码登录
     *
     * @param user
     * @return
     */
    @PostMapping
    @Operation(summary = "账号密码登录")
    public Result login(@RequestBody User user) {
        //密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        //登录
        User serviceOne = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword()));

        if (serviceOne == null) return Result.error("账号或密码错误"); //找不到用户
        if (serviceOne.getStatus() != 0) return Result.error("账号已被禁用"); //账号已被禁用
        //登录成功
        Map<String, Object> map = new HashMap<>();
        map.put("userId", serviceOne.getId());
        return Result.ok(JwtUtil.createJWT(SecretKey, adminTtl, map));
    }

    /**
     * 注册
     *
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "注册")
    @Transactional
    public Result register(HttpServletRequest request, @RequestBody User user) {

        //判断用户名是否存在
        User serviceOne = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (serviceOne != null) return Result.error("用户名已存在");

        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        //创建新用户配置用户基本信息
        User newUser = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .status(0)
                .avatarUrl("/2025/08/10/d9528fbe-9c96-4648-a554-de1bb8b26ac9.jpg")
                .createTime(DateUtil.date())
                .updateTime(DateUtil.date())
                .createIp(IpUtils.getIpAddress(request))
                .build();
        //保存用户信息
        boolean save = userService.save(newUser);
        //判断有没有成功保存
        return save ? Result.ok("注册成功") : Result.error("注册失败");
    }

    @Operation(summary = "角色验证")
    @GetMapping("/role/validate")
    public Result validateRole() {
        Long currentId = BaseContext.getCurrentId();
        User byId = userService.getById(currentId);
        if (byId.getRole() == 0) {
            return Result.ok(true);
        } else {
            return Result.ok(false);
        }
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @Operation(summary = "获取用户信息")
    @GetMapping("/profile")
    public Result getUserInfo() {
        return Result.ok(userService.getById(BaseContext.getCurrentId()));
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @return
     */
    @Operation(summary = "修改用户信息")
    @PutMapping("/profile")
    public Result updateUserInfo(@RequestBody User user) {
        user.setId(Math.toIntExact(BaseContext.getCurrentId()));
        return Result.ok(userService.updateById(user));
    }

    /**
     * 修改密码
     *
     * @param map
     * @return
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result updatePassword(@RequestBody Map<String, String> map) {
        String oldPassword = map.get("oldPwd");
        String newPassword = map.get("newPwd");
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)) {
            return Result.error("参数不能为空");
        }
        User user = userService.getById(BaseContext.getCurrentId());
        if (!DigestUtils.md5DigestAsHex(oldPassword.getBytes()).equals(user.getPassword())) {
            return Result.error("旧密码错误");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        return Result.ok(userService.updateById(user));
    }

    /**
     * 账号密码登录
     *
     * @param user
     * @return
     */
    @PostMapping("/admin")
    @Operation(summary = "账号密码登录")
    public Result AdminLogin(@RequestBody User user) {
        //密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        //登录
        User serviceOne = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword())
                .eq(User::getRole, 0));

        if (serviceOne == null) return Result.error("账号或密码错误"); //找不到用户
        if (serviceOne.getStatus() != 0) return Result.error("账号已被禁用"); //账号已被禁用
        //登录成功
        Map<String, Object> map = new HashMap<>();
        map.put("userId", serviceOne.getId());
        return Result.ok(JwtUtil.createJWT(SecretKey, adminTtl, map));
    }

    /**
     * 用户分页查询
     *
     * @param username
     * @param status
     * @param role
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Operation(summary = "用户分页查询")
    @GetMapping("/page")
    public Result<Page<User>> pageQuery(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 构建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(username)) queryWrapper.like(User::getUsername, username);// 模糊查询用户名
        if (status != null) queryWrapper.eq(User::getStatus, status); // 状态筛选
        if (role != null) queryWrapper.eq(User::getRole, role);// 角色筛选
        // 执行分页查询
        return Result.ok(userService.page(page, queryWrapper));
    }
}