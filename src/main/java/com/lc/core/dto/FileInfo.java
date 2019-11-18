package com.lc.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息
 * @author l5990
 */
@Data
public class FileInfo implements Serializable {
    
    // 原文件名
    private String file_name;

    // 文件后缀名
    private String file_type;

    // 文件大小
    private Long file_size;

    // 文件uuid
    private String uuid;

    // 文件存储年月yyyyMM
    private String separate_uuid;

    // 文件MD5值
    private String md5;


}
