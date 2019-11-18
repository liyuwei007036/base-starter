package com.lc.core.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加解密工具
 *
 * @author l5990
 */
public class EncryptionUtils {

    /**
     * 生成 MD5
     *
     * @param data
     * @param md5Secret
     * @param isUpper
     * @return
     * @throws Exception
     */
    public static String md5(String data, String md5Secret, boolean isUpper) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data.getBytes(StandardCharsets.UTF_8));
        md.update(md5Secret.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String result = new String(digest, StandardCharsets.UTF_8);
        if (isUpper) {
            return result.toUpperCase();
        } else {
            return result;
        }
    }


    /**
     * 生成 HMACSHA256
     *
     * @param data 待处理数据
     * @param key  密钥
     * @return 加密结果
     * @throws Exception
     */
    public static String hMACSHA256(String data, String key) throws Exception {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        byte[] array = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }


    //----------------- AES 对称加密------

    /**
     * 获得一个 密钥长度为 256 位的 AES 密钥，
     *
     * @return 返回经 BASE64 处理之后的密钥字符串
     */
    public static String getStrKeyAes() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        // 这里可以是 128、192、256、越大越安全
        keyGen.init(256, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 将使用 Base64 加密后的字符串类型的 secretKey 转为 SecretKey
     *
     * @param strKey
     * @return SecretKey
     */
    public static SecretKey strKey2SecretKey(String strKey) {
        byte[] bytes = Base64.getDecoder().decode(strKey);
        return new SecretKeySpec(bytes, "AES");
    }

    /**
     * 加密
     *
     * @param content   待加密内容
     * @param secretKey 加密使用的 AES 密钥
     * @return 加密后的密文 byte[]
     */
    public static byte[] encryptAes(byte[] content, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

    /**
     * 解密
     *
     * @param content   待解密内容
     * @param secretKey 解密使用的 AES 密钥
     * @return 解密后的明文 byte[]
     */
    public static byte[] decryptAes(byte[] content, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }


    //------------------------- RAS 非对称加密-------------

    /**
     * 生成密钥对：密钥对中包含公钥和私钥
     *
     * @return 包含 RSA 公钥与私钥的 keyPair
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        // 获得RSA密钥对的生成器实例
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        // 说的一个安全的随机数
        SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        // 这里可以是1024、2048 初始化一个密钥对
        keyPairGenerator.initialize(2048, secureRandom);
        // 获得密钥对
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取公钥 (并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的公钥字符串
     */
    public static String getPublicKey(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获取私钥(并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的私钥字符串
     */
    public static String getPrivateKey(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将Base64编码后的公钥转换成 PublicKey 对象
     *
     * @param pubStr
     * @return PublicKey
     */
    public static PublicKey string2PublicKey(String pubStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.getDecoder().decode(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 将Base64编码后的私钥转换成 PrivateKey 对象
     *
     * @param priStr
     * @return PrivateKey
     */
    public static PrivateKey string2PrivateKey(String priStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.getDecoder().decode(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 公钥加密
     *
     * @param content   待加密的内容 byte[]
     * @param publicKey 加密所需的公钥对象 PublicKey
     * @return 加密后的字节数组 byte[]
     */
    public static byte[] publicEncryType(byte[] content, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    /**
     * 私钥解密
     *
     * @param content    待解密的内容 byte[]
     * @param privateKey 解密需要的私钥对象 PrivateKey
     * @return 解密后的字节数组 byte[]
     */
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }


}
