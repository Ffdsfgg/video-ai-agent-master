package com.hip.aiteachingvideo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="public.user")
@Data
@Builder
public class User implements Serializable {
    /**
     * 主id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;


    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String description;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 当前状态(0-正常 1-异常 2-注销)
     */
    private Integer status;
    /**
     * 角色(0-老师 1-学生)
     */
    private Integer role;

    /**
     * 创建IP
     */
    private String createIp;

    /**
     * 创建时间 
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}