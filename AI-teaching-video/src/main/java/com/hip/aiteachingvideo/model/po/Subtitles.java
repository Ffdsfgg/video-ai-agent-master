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
 * 
 * @TableName subtitles
 */
@TableName(value ="public.subtitles")
@Builder
@Data
public class Subtitles implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 视频表id
     */
    private Integer videoId;

    /**
     * 字幕位置
     */
    private String subtitleUrl;

    /**
     * 字幕类型
     */
    private String subtitleType;

    /**
     * 语言类型
     */
    private String languagType;

    /**
     * 称号
     */
    private String designation;
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