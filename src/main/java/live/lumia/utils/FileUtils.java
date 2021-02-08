package live.lumia.utils;


import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author l5990
 */
@Log4j2
public class FileUtils {
    /**
     * 获取文件后缀
     *
     * @param fileName 文件名
     * @return
     */
    public static String getSuffix(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return null;
    }

    /**
     * 删除前缀
     *
     * @param src    原文件名
     * @param prefix 前缀
     * @return
     */
    public static String removePrefix(String src, String prefix) {
        if (src != null && src.startsWith(prefix)) {
            return src.substring(prefix.length());
        }
        return src;
    }

    /**
     * 文件转String
     *
     * @param file 文件
     * @return
     */
    public static String readString(File file) {
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            FileChannel channel = inputStream.getChannel();
            while (channel.read(buffer) != -1) {
                String line = new String(buffer.array(), StandardCharsets.UTF_8);
                sb.append(line);
                buffer.rewind();
            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }

    /**
     * 将字符串写入文件
     *
     * @param file   文件
     * @param string 字符串
     */
    public static void writeString(File file, String string) {
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(string.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * 解压文件
     *
     * @param zipFilePath 文件路径
     * @throws IOException
     */
    public static void unzip(String zipFilePath) throws IOException {
        String targetPath = zipFilePath.substring(0, zipFilePath.lastIndexOf("."));
        unzip(zipFilePath, targetPath);
    }

    public static void unzip(String zipFilePath, String targetPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<?> entryEnum = zipFile.entries();
            while (entryEnum.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entryEnum.nextElement();
                if (!zipEntry.isDirectory()) {
                    File targetFile = new File(targetPath + File.separator + zipEntry.getName());
                    if (!targetFile.getParentFile().exists()) {
                        boolean mkdirs = targetFile.getParentFile().mkdirs();
                        log.info(mkdirs);
                    }
                    try (FileOutputStream outputStream = new FileOutputStream(targetFile);
                         BufferedOutputStream os = new BufferedOutputStream(outputStream);
                         InputStream is = zipFile.getInputStream(zipEntry)) {
                        byte[] buffer = new byte[4096];
                        int readLen;
                        int maxLength = 4096;
                        while ((readLen = is.read(buffer, 0, maxLength)) > 0) {
                            os.write(buffer, 0, readLen);
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
        try (FileInputStream ins = new FileInputStream(file)) {
            return getMd5(ins);
        }
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
        try (FileInputStream in = new FileInputStream(file1); FileOutputStream out = new FileOutputStream(file2)) {
            FileChannel channel = in.getChannel();
            FileChannel channel2 = out.getChannel();
            channel.transferTo(0, channel.size(), channel2);
        }
    }

    public static void inputStreamToFile(File f, InputStream input) throws IOException {
        try (FileOutputStream os = new FileOutputStream(f)) {
            int index;
            byte[] bytes = new byte[1024];
            while ((index = input.read(bytes)) != -1) {
                os.write(bytes, 0, index);
                os.flush();
            }
        }
        input.close();
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
            log.error("网络图片下载失败，url： {}", urlString, e);
            return null;
        }
    }

    public static List<Object[]> paresCsvFile(File file, String charsetName) {
        if (file == null) {
            log.error("文件不存在");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        List<Object[]> result = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charsetName);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line.split(","));
            }
        } catch (IOException e) {
            log.error("文件读取出错");
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        return result;
    }

    /**
     * bytes 转文件
     *
     * @param bytes
     * @param file
     */
    public static void bytes2File(byte[] bytes, File file) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileChannel channel = randomAccessFile.getChannel();
            channel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            log.error("转文件出错", e);
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
    }

    /**
     * 功能:压缩多个文件成一个zip文件
     *
     * @param srcFile：源文件列表
     * @param zipFile：压缩后的文件
     */
    public static void zipFiles(List<File> srcFile, File zipFile) {
        byte[] buf = new byte[1024];
        try (FileOutputStream outputStream = new FileOutputStream(zipFile); ZipOutputStream out = new ZipOutputStream(outputStream)) {
            //ZipOutputStream类：完成文件或文件夹的压缩
            for (File file : srcFile) {
                FileInputStream in = new FileInputStream(file);
                out.putNextEntry(new ZipEntry(file.getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
            }
        } catch (Exception e) {
            log.error("压缩文件失败", e);
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
    }
}
