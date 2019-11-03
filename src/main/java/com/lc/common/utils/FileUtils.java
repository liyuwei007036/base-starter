package com.lc.common.utils;


import com.lc.common.error.BaseException;
import com.lc.common.error.BaseErrorEnums;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;

import javax.validation.constraints.NotNull;
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
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int len; (len = fis.read(buffer)) > 0; ) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
        } finally {
            close(fis, baos);
        }
        return null;
    }

    public static void writeString(File file, String string) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, false);
            fos.write(string.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
        } finally {
            close(null, fos);
        }
    }

    private static void close(InputStream is, OutputStream os) {
        if (is != null)
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (os != null)
            try {
                os.close();
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
                                targetFile.getParentFile().mkdirs();
                            }
                            os = new BufferedOutputStream(new FileOutputStream(targetFile));
                            is = zipFile.getInputStream(zipEntry);
                            byte[] buffer = new byte[4096];
                            int readLen = 0;
                            while ((readLen = is.read(buffer, 0, 4096)) > 0) {
                                os.write(buffer, 0, readLen);
                            }
                        }
                    } finally {
                        if (is != null)
                            is.close();
                        if (os != null)
                            os.close();
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

    public static String getMD5(String file_path) throws IOException {
        File file = new File(file_path);
        return getMD5(file);
    }

    public static String getMD5(File file) throws IOException {
        FileInputStream ins = new FileInputStream(file);
        String md5 = getMD5(ins);
        ins.close();
        return md5;
    }

    public static String getMD5(FileInputStream inputStream) throws IOException {
        return DigestUtils.md5Hex(inputStream);
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + File.separator + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + File.separator + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
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

    public static void InputStreamToFile(@NotNull File f, InputStream input) throws IOException {
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

    public static InputStream downloadFileFromURL(String urlString) {
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
        } catch (FileNotFoundException e) {
            log.error("文件不存在");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        String line;
        List<Object[]> result = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                result.add(line.split("\\,"));
            }
        } catch (IOException e) {
            log.error("文件读取出错");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        return result;
    }
}
