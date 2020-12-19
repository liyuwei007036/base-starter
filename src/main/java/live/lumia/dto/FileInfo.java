package live.lumia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息
 *
 * @author l5990
 */
@Data
public class FileInfo implements Serializable {

    /**
     * 原文件名
     */
    private String fileName;

    /**
     * 文件后缀名
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件uuid
     */
    private String uuid;

    /**
     * 文件存储年月yyyyMM
     */
    private String separateUuid;

    /**
     * 文件MD5值
     */
    private String md5;


}
