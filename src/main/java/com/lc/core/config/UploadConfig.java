package com.lc.core.config;

import com.lc.core.dto.FileInfo;

import java.io.File;
import java.util.Set;

/**
 * 文件上传配置
 */
public interface UploadConfig {

    /**
     * 文件上传根目录
     *
     * @return
     */
    String basePath();

    /**
     * 上传成功后执行方法
     *
     * @param info
     */
    void uploadSuccess(FileInfo info);

    /**
     * 上传至OSS的图片最大高度
     *
     * @return
     */
    int maxHeight();

    /**
     * 上传至OSS的图片最大宽度
     *
     * @return
     */
    int maxWidth();

    /**
     * 图片缩略图尺寸
     *
     * @return
     */
    Set<String> photoSize();

    /**
     * 允许上传的文件类型
     *
     * @return
     */
    Set<String> fileTypes();

    /**
     * 删除前操作
     *
     * @return
     */
    boolean beforeDeleteFile();

    /**
     * 上传前操作
     *
     * @param file
     * @return
     */
    FileInfo beforeUpload(File file);

    /**
     * 是否以文件MD5值代替UUID
     *
     * @return
     */
    boolean md5Path();
}
