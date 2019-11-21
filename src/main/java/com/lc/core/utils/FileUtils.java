package com.lc.core.utils;


import com.lc.core.error.BaseException;
import com.lc.core.enums.BaseErrorEnums;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author l5990
 */
@Log4j2
public class FileUtils {

    public static String getSuffix(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return null;
    }

    public static String removePrefix(String src, String prefix) {
        if (src != null && src.startsWith(prefix)) {
            return src.substring(prefix.length());
        }
        return src;
    }

    public static String readString(File file) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            for (int len; (len = fis.read(buffer)) > 0; ) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeString(File file, String string) {
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(string.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void unzip(String zipFilePath) throws IOException {
        String targetPath = zipFilePath.substring(0, zipFilePath.lastIndexOf("."));
        unzip(zipFilePath, targetPath);
    }

    public static void unzip(String zipFilePath, String targetPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<?> entryEnum = zipFile.entries();
            if (null != entryEnum) {
                while (entryEnum.hasMoreElements()) {
                    OutputStream os = null;
                    InputStream is = null;
                    try {
                        ZipEntry zipEntry = (ZipEntry) entryEnum.nextElement();
                        if (!zipEntry.isDirectory()) {
                            File targetFile = new File(targetPath + File.separator + zipEntry.getName());
                            if (!targetFile.getParentFile().exists()) {
                                boolean mkdirs = targetFile.getParentFile().mkdirs();
                                log.info(mkdirs);
                            }
                            os = new BufferedOutputStream(new FileOutputStream(targetFile));
                            is = zipFile.getInputStream(zipEntry);
                            byte[] buffer = new byte[4096];
                            int readLen;
                            int maxLength = 4096;
                            while ((readLen = is.read(buffer, 0, maxLength)) > 0) {
                                os.write(buffer, 0, readLen);
                            }
                        }
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                }
            }
        }
    }

    public static String extensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static String getMd5(String filePath) throws IOException {
        File file = new File(filePath);
        return getMd5(file);
    }

    public static String getMd5(File file) throws IOException {
        FileInputStream ins = new FileInputStream(file);
        String md5 = getMd5(ins);
        ins.close();
        return md5;
    }

    public static String getMd5(FileInputStream inputStream) throws IOException {
        return DigestUtils.md5Hex(inputStream);
    }

    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp;
        assert tempList != null;
        for (String s : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + s);
            } else {
                temp = new File(path + File.separator + s);
            }
            if (temp.isFile()) {
                boolean delete = temp.delete();
                if (!delete) {
                    log.error("文件删除失败");
                }
            }
            if (temp.isDirectory()) {
                // 先删除文件夹里面的文件
                delAllFile(path + File.separator + s);
                // 再删除空文件夹
                delFolder(path + File.separator + s);
            }
        }
    }

    public static void delFolder(String folderPath) {
        try {
            // 删除完里面所有内容
            delAllFile(folderPath);
            File myFilePath = new File(folderPath);
            // 删除空文件夹
            boolean delete = myFilePath.delete();
            if (!delete) {
                log.error("文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void file2File(File file1, File file2) throws IOException {
        FileInputStream in = new FileInputStream(file1);
        FileOutputStream out = new FileOutputStream(file2);
        FileChannel channel = in.getChannel();
        FileChannel channel2 = out.getChannel();
        channel.transferTo(0, channel.size(), channel2);
        in.close();
        out.close();

    }

    public static void inputStreamToFile(File f, InputStream input) throws IOException {
        FileOutputStream os = new FileOutputStream(f);
        int index;
        byte[] bytes = new byte[1024];
        while ((index = input.read(bytes)) != -1) {
            os.write(bytes, 0, index);
            os.flush();
        }
        input.close();
        os.close();
    }

    public static InputStream downloadFileFromUrl(String urlString) {
        try {
            // 构造URL
            URL url = new URL(urlString);
            // 打开连接
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            // 输入流
            return con.getInputStream();
        } catch (Exception e) {
            log.error("网络图片下载失败，url： " + urlString);
            log.error(e);
            return null;
        }
    }

    public static List<Object[]> paresCsvFile(File file, String charsetName) {
        if (file == null) {
            log.error("文件不存在");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        BufferedReader br = null;
        try {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (br == null) {
                throw new RuntimeException("文件不存在");
            }
        } catch (FileNotFoundException e) {
            log.error("文件不存在");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        String line;
        List<Object[]> result = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                result.add(line.split(","));
            }
        } catch (IOException e) {
            log.error("文件读取出错");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        return result;
    }
}
