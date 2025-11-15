package com.hip.aiteachingvideo.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.hip.aiteachingvideo.utils.OSSUtils;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "文件处理")
@RequestMapping("/file")
public class FileController {
    @Value("${hip.url}")
    private String url;

    @Autowired
    private OSSUtils ossUtils;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 上传结果
     */
    @Operation(summary = "上传文件")
    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");

        String format = DateUtil.format(new Date(), "yyyy/MM/dd/");
        File folder = new File(url + format);
        if (!folder.isDirectory()) folder.mkdirs();

        String oldName = file.getOriginalFilename();
        String ext = "";
        int dotIndex = oldName == null ? -1 : oldName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < oldName.length() - 1) {
            ext = oldName.substring(dotIndex);
        }
        log.info("oldName: {}, ext: {}", oldName, ext);
        String newName = UUID.randomUUID() + ext;
        try {
            file.transferTo(new File(folder, newName));
            String filePath = "/" + format + newName;
            return Result.ok(filePath);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param filename 文件名
     * @return 删除结果
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{filename}")
    public Result deleteFile(@PathVariable String filename) {
        File file = new File(url + File.separator + filename);
        if (file.delete()) {
            return Result.ok("文件删除成功: " + filename);
        } else {
            return Result.error("文件删除失败: " + filename);
        }
    }


    /**
     * 阿里云OSS文件上传接口
     *
     * @param file 前端传来的文件
     * @return 包含访问地址的响应
     */
    @PostMapping("/oss")
    public Result ossUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空，请重新上传");
        }

        try {
            // 按照日期生成路径
            String datePath = DateUtil.format(new Date(), "yyyy/MM/dd/");
            String suffix = getFileExtension(file.getOriginalFilename());
            String objectName = "uploads/" + datePath + UUID.randomUUID() + suffix;

            String contentType = getContentType(suffix);

            String accessUrl = ossUtils.uploadInputStream(file.getInputStream(), objectName, contentType);

            return Result.ok(Map.of(
                    "url", accessUrl,
                    "key", objectName)
            );

        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件后缀名
     *
     * @param filename 文件名
     * @return 文件后缀名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取文件类型
     *
     * @param ext 文件后缀名
     * @return 文件类型
     */
    private String getContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".mp4" -> "video/mp4";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}