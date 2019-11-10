package com.lc.core.config;

import com.lc.core.pojo.FileInfo;

import java.io.File;
import java.util.Set;

/**
 * 文件上传配置
 */
public interface UploadConfig {

    // 文件上传根目录
    String basePath();

    // 上传成功后执行方法
    void uploadSuccess(FileInfo info);

    // 上传至OSS的图片最大高度
    int max_height();

    // 上传至OSS的图片最大宽度
    int max_width();

    // 图片缩略图尺寸
    Set<String> photoSize();

    // 允许上传的文件类型
    Set<String> fileTypes();

    Boolean beforeDeleteFile();

    FileInfo beforeUpload(File file);

    /**
     * 是否以文件MD5值代替UUID
     *
     * @return
     */
    Boolean md5Path();
}
