package com.hip.aiteachingvideo.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @TableName videos
 */
@TableName(value = "public.videos")
@Data
public class Videos implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件位置
     */
    private String url;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 介绍
     */
    private String description;

    /**
     * 封面图片
     */
    private String imgUrl;
    /**
     * 视频时长
     */
    private String duration;

    /**
     * 视频类型
     */
    private String videoType;

    /**
     * 课件位置
     */
    private String coursewareUrl;
    /**
     * 是否公开
     */
    private Integer isPublic;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

    /**
     * 软删除
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}