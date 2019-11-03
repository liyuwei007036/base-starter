package com.lc.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.lc.common.commonenums.ResponseEnums;
import com.lc.common.config.UploadConfig;
import com.lc.common.error.BaseErrorEnums;
import com.lc.common.error.BaseException;
import com.lc.common.pojo.FileInfo;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传工具类
 * TODO 对于PS 过的文件 CMYK 格式的 会出现异常 需要进行转换为RGB 格式
 * CMYK 格式转换为RGB 格式
 * <p>
 * http://www.360doc.com/content/15/0507/16/9148133_468770042.shtml
 *
 * @author l5990
 */
@Log4j2
public class OperationFileUtil {

    private Boolean isDev;

    private UploadConfig config;

    private OperationFileUtil() {

    }

    public OperationFileUtil(UploadConfig config) {
        this.config = config;
        isDev = !"prod".equals(SpringUtil.getProperty("spring.profiles.active"));
    }

    /**
     * 校验config
     */
    private void checkConfig() {
        if (isDev && StringUtils.isEmpty(config.basePath())) {
            log.error("文件上传（开发环境）未配置 文件上传目录");
            throw new BaseException(BaseErrorEnums.ERROR_CONFIG);
        }

        if (!isDev && StringUtils.isEmpty(config.bucketName())) {
            log.error("文件上传（生产环境）未配置 OSSBucketName");
            throw new BaseException(BaseErrorEnums.ERROR_CONFIG);
        }
    }

    /**
     * 文件上传
     *
     * @param file
     */
    public void upload(MultipartFile file) throws IOException {
        File tempFile = null;
        try {
            String s = FileUtils.extensionName(file.getOriginalFilename());
            tempFile = File.createTempFile("tmp", "." + s);
            file.transferTo(tempFile);
            upload(tempFile);
        } finally {
            if (tempFile != null) {
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("delete file fail ");
                }
            }
        }
    }

    public void upload(File file) throws IOException {
        // 配置校验
        checkConfig();
        // 开始上传
        doUpload(file);
    }

    private void doUpload(File f) throws IOException {
        String s = FileUtils.extensionName(f.getName());
        File tempFile = File.createTempFile("tmp", "." + s);
        FileInfo info;
        try {
            info = config.beforeUpload(tempFile);
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                log.error("delete file fail ");
            }
        }
        if (info == null) {
            String file_path = config.basePath();
            String separate_uuid = new SimpleDateFormat("yyyyMM").format(new Date());
            String extensionName = FileUtils.extensionName(f.getName()).toLowerCase();
            Set<String> strings = config.fileTypes();

            if (!strings.contains("*")) {
                if (!strings.contains(extensionName)) {
                    log.error("文件上传: 格式错误");
                    throw new BaseException(BaseErrorEnums.ERROR_FILE_FORMAT);
                }
            }
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            info = new FileInfo();
            info.setSeparate_uuid(separate_uuid);
            info.setFile_name(f.getName());
            info.setFile_type(extensionName);
            info.setUuid(uuid);

            // 开发环境
            if (isDev) {
                doUploadForDisk(file_path, separate_uuid, extensionName, uuid, f, info);
            } else {
                doUploadForOSS(separate_uuid, extensionName, uuid, f, info);
            }
        }
        // 文件保存完成执行后续方法
        config.uploadSuccess(info);
    }

    private void doUploadForDisk(String filePath, String separateUuid, String extensionName, String uuid, File file, FileInfo info) throws IOException {
        String md5 = FileUtils.getMD5(new FileInputStream(file));
        if (config.md5Path()) {
            uuid = md5;
        }
        // 构建文件存放路径
        filePath += File.separator + separateUuid + File.separator + uuid.substring(0, 2) + File.separator + uuid + File.separator;

        // 构建新文件名
        String newFilename = uuid + "." + extensionName;
        File savedFile = new File(filePath, newFilename);

        // 创建文件夹
        File fileParent = savedFile.getParentFile();
        if (!fileParent.exists()) {
            boolean mkdirs = fileParent.mkdirs();
            if (!mkdirs) {
                log.error("mkdirs file");
            }
        }
        try {
            FileUtils.file2File(file, savedFile);
            // 保存文件
            info.setFile_size(savedFile.length());
            // 生成文件缩略图
            if (config.photoSize() != null && isImg(extensionName)) {
                // 如果图片大小超过2M 进行0.5倍质量压缩
                if (file.length() > 1024 * 1024 * 2) {
                    Thumbnails.of(savedFile)
                            .scale(1f)
                            .outputQuality(0.5f)
                            .toFile(savedFile);
                }
                for (String imgSize : new ArrayList<>(config.photoSize())) {
                    int w = ObjectUtil.getInteger(imgSize.split("X")[0]);
                    int h = ObjectUtil.getInteger(imgSize.split("X")[1]);
                    if (w < 1 || h < 1) {
                        continue;
                    }
                    String fileName = String.format("%s%s_%dX%d.%s", filePath, uuid, w, h, extensionName);
                    Thumbnails.of(savedFile)
                            .size(w, h)
                            .keepAspectRatio(false)
                            .toFile(fileName);
                }
            }
            info.setMd5(md5);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败", e);
            throw new BaseException(BaseErrorEnums.ERROR_SYS);
        }
    }


    /**
     * 根据文件后缀名判断是否为图片文件
     *
     * @param extensionName
     * @return
     */
    private Boolean isImg(String extensionName) {
        extensionName = extensionName.toLowerCase();
        if (extensionName.equals("jpg") || extensionName.equals("jpeg") || extensionName.equals("png")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 文件删除
     *
     * @param path_obj
     */
    public void deleteFile(JSONObject path_obj) throws Exception {
        Boolean del_file = config.beforeDeleteFile();
        if (del_file) {
            if (path_obj == null || path_obj.size() < 1) {
                log.error("文件路径为空");
                throw new BaseException(BaseErrorEnums.ERROR_ARGS);
            }
            if (path_obj.size() != 3) {
                log.error("文件路径为空");
                throw new BaseException(BaseErrorEnums.ERROR_ARGS);
            }
            String separate_uuid = path_obj.getString("separate_uuid");
            String uuid = path_obj.getString("uuid");
            String dir_u = uuid.substring(0, 2);
            String file_name = path_obj.getString("file_name");
            if (isDev) {
                String file_path = config.basePath() + File.separator + separate_uuid + File.separator + dir_u + File.separator + uuid + File.separator;
                FileUtils.delAllFile(file_path);
            } else {
                String file_path = separate_uuid + "/" + file_name;
            }
        }
    }

    /**
     * 获取文件
     *
     * @param separate_uuid
     * @param uuid
     * @param file_ext
     * @param zoom
     * @return
     */
    public JSONObject getFile(OSSClient ossClient, String separate_uuid, String uuid, String file_ext, String zoom) {
        int w = 0, h = 0;
        if (!StringUtils.isEmpty(zoom)) {
            w = ObjectUtil.getInteger(zoom.split("_")[1].split("X")[0]);
            h = ObjectUtil.getInteger(zoom.split("_")[1].split("X")[1]);
        }
        String oss_file_name = separate_uuid + File.separator + uuid + "." + file_ext;
        String disk_file_path = separate_uuid + File.separator + uuid.substring(0, 2) + File.separator + uuid + File.separator + uuid;
        Boolean is_img = isImg(file_ext);
        JSONObject object = new JSONObject();
        object.put("success", true);
        if (isDev) {
            getFileFromDisk(disk_file_path, w, h, file_ext, zoom, object);
        } else {
            getFileFromOSS(ossClient, oss_file_name, is_img, w, h, object);
        }
        return object;
    }

    /**
     * 从OSS 获取文件
     *
     * @param file_name
     * @param is_img
     * @param w
     * @param h
     * @return
     */
    private void getFileFromOSS(OSSClient ossClient, String file_name, Boolean is_img, int w, int h, JSONObject object) {
        try {
            OSSObject ossObject;
            if (is_img && w > 0 && h > 0) {
                ossObject = OSSUtils.getImage(ossClient, config.bucketName(), file_name, w, h);
            } else {
                ossObject = OSSUtils.getFile(ossClient, config.bucketName(), file_name);
            }
            if (ossObject == null) {
                log.error("无法从OSS获取文件");
                throw new BaseException(BaseErrorEnums.ERROR_SYS);
            }
            InputStream ins = ossObject.getObjectContent();
            long size = ossObject.getObjectMetadata().getContentLength();
            object.put("lastModified", ossObject.getObjectMetadata().getLastModified().getTime());
            object.put("Content-Length", size);
            object.put("inputstream", ins);
            object.put("ossClient", ossClient);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("无法取得文件", e);
            object.put("success", false);
        }
    }

    /**
     * 从本地磁盘获取文件
     *
     * @param disk_file_name
     * @param w
     * @param h
     * @param file_ext
     * @param zoom
     * @param object
     * @throws FileNotFoundException
     */
    private void getFileFromDisk(String disk_file_name, int w, int h, String file_ext, String zoom, JSONObject object) {
        String file_name = config.basePath() + File.separator + disk_file_name;
        if (w > 0 && h > 0) {
            file_name += zoom + "." + file_ext;
        } else {
            file_name += "." + file_ext;
        }
        File file = new File(file_name);
        if (!file.exists()) {
            object.put("success", false);
            log.error("从磁盘获取文件失败");
        } else {
            object.put("lastModified", file.lastModified());
            object.put("Content-Length", file.length());
            try {
                object.put("inputstream", new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                log.error("从磁盘获取文件失败");
                object.put("success", false);
            }
        }
    }

    public void renderFile(HttpServletResponse response, String separate_uuid, String uuid, String file_ext, String zoom) {
        OSSClient ossClient = null;
        if (!isDev) {
            ossClient = OSSUtils.createOSSClient();
        }
        JSONObject info = getFile(ossClient, separate_uuid, uuid, file_ext, zoom);
        Boolean success = info.getBoolean("success");
        if (success) {
            Long lastModified = info.getLong("lastModified");
            Long contentLength = info.getLong("Content-Length");
            try {
                String file_name = separate_uuid + "/" + uuid + "." + file_ext;
                // 开始设置 Http Response
                response.setHeader("Content-disposition", "filename=" + file_name);
                Calendar cd = Calendar.getInstance();
                cd.setTimeInMillis(lastModified);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                String timeStr = sdf.format(cd.getTime());
                response.setHeader("Last-Modified", timeStr);
                response.setHeader("Accept-Ranges", "bytes");
                response.setContentType(ResponseEnums.getValue(file_ext.toLowerCase()));
                response.setHeader("Content-Length", contentLength.toString());
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = (InputStream) info.get("inputstream");
                    outputStream = response.getOutputStream();
                    byte[] buffer = new byte[409600];
                    for (int len = -1; (len = inputStream.read(buffer)) != -1; ) {
                        outputStream.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("获取文件失败", e);
                    throw new BaseException(BaseErrorEnums.FILE_NOT_EXISTS);
                } finally {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (ossClient != null) {
                        ossClient.shutdown();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("无法取得图片", e);
                throw new BaseException(BaseErrorEnums.FILE_NOT_EXISTS);
            }
        } else {
            throw new BaseException(BaseErrorEnums.FILE_NOT_EXISTS);
        }
    }
}
