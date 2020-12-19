package live.lumia.service;

import live.lumia.dto.FileInfo;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件上传配置
 *
 * @author l5990
 */
public interface BaseUploadService {

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
     * 上传图片最大高度
     *
     * @return
     */
    default int maxHeight() {
        return 0;
    }

    /**
     * 上传图片最大宽度
     *
     * @return
     */
    default int maxWidth() {
        return 0;
    }

    /**
     * 图片缩略图尺寸
     *
     * @return
     */
    default Set<String> photoSize() {
        return null;
    }

    /**
     * 允许上传的文件类型
     *
     * @return
     */
    default Set<String> fileTypes() {
        HashSet<String> objects = new HashSet<>();
        objects.add("*");
        return objects;
    }

    /**
     * 删除前操作
     *
     * @return
     */
    default boolean beforeDeleteFile() {
        return false;
    }

    /**
     * 上传前操作
     *
     * @param file
     * @return
     */
    default FileInfo beforeUpload(File file) {
        return null;
    }

    /**
     * 是否以文件MD5值代替UUID
     *
     * @return
     */
    default boolean md5Path() {
        return false;
    }
}
