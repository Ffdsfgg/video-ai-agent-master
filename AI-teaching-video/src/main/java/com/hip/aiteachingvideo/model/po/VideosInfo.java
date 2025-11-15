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
 * @TableName videos_info
 */
@TableName(value = "public.videos_info")
@Builder
@Data
public class VideosInfo implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视频id
     */
    private Integer videoId;

    /**
     * json放回的全部数据
     */
    private String jsonText;

    /**
     * 总结的内容
     */
    private String sumUp;

    /**
     * 如时间段对应的内容
     */
    private String structuredOutput;

    /**
     * 提问内容
     */
    private String quizJson;
    /**
     * 大纲内容
     */
    private String teachingOutline;

    /**
     * 表格内容
     */
    private String tableText;

    /**
     * 内容摘要
     */
    private String contentSummary;



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