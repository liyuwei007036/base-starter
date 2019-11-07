package com.lc.common.utils;

import lombok.extern.log4j.Log4j2;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author l5990
 */
@Log4j2
public class EMailUtil {

    private static Set<String> RULE_FILE = new HashSet<>();
    private static Set<String> RULE_RE = new HashSet<>();

    public interface Mail {
        void parseMail(List<InputStream> inputStreams);
    }

    /**
     * 接收邮件
     */
    public static void receive(String account, String passwd, Set<String> ruleFile, Set<String> ruleRe, Mail mail) throws Exception {
        if (RULE_FILE != null) {
            EMailUtil.RULE_FILE.addAll(ruleFile);
        }
        if (ruleRe != null) {
            EMailUtil.RULE_RE.addAll(ruleRe);
        }

        /*
         * 因为现在使用的是163邮箱 而163的 pop地址是pop3.163.com 端口是110
         * 比如使用好未来企业邮箱 就需要换成 好未来邮箱的 pop服务器地址 pop.263.net  和   端口 110
         */
        // 端口号
        String port = "993";
        // 服务器地址
        String servicePath = "imap.exmail.qq.com";
        final String sSLFACTORY = "javax.net.ssl.SSLSocketFactory";

        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", servicePath);
        props.setProperty("mail.imap.port", port);
        props.setProperty("mail.imap.socketFactory.class", sSLFACTORY);
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.port", "993");
        props.setProperty("mail.imap.auth", "true");

        // 创建Session实例对象
        Session session = Session.getDefaultInstance(props, null);
        URLName url = new URLName("imaps", servicePath, 993, null, account, passwd);
        Store store = session.getStore(url);
        store.connect();

        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        //打开收件箱
        folder.open(Folder.READ_ONLY);
        // 如果未读数量大于0
        if (folder.getUnreadMessageCount() > 0) {
            Message[] messages = folder.getMessages();
            parseMessage(messages, mail);
        }

        //释放资源
        folder.close(true);
        store.close();
    }

    /**
     * 解析邮件
     *
     * @param messages 要解析的邮件列表
     */
    public static void parseMessage(Message[] messages, Mail mail) throws MessagingException, IOException {
        if (messages == null || messages.length < 1) {
            throw new MessagingException("未找到要解析的邮件!");
        }


        // 解析所有邮件
        for (int i = messages.length - 1, count = 0; i >= count; i--) {
            MimeMessage msg = (MimeMessage) messages[i];

            Boolean seen = isSeen(msg);
            if (seen) {
                continue;
            }

            String s = getSubject(msg);
            if (!RULE_RE.contains(s) && !RULE_RE.contains("*")) {
                continue;
            }

            boolean isContainerAttachment = isContainAttachment(msg);
            if (isContainerAttachment) {
                //保存附件
                List<InputStream> inputStreams = saveAttachment(msg);
                mail.parseMail(inputStreams);
            }
            StringBuffer content = new StringBuffer(30);
            getMailTextContent(msg, content);
            msg.setFlag(Flags.Flag.SEEN, true);
        }
    }


    /**
     * 邮件删除
     *
     * @param messages 要解析的邮件列表
     */
    public static void deleteMessage(Message... messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1) {
            throw new MessagingException("未找到要解析的邮件!");
        }
        // 解析所有邮件
        /**
         *   邮件删除
         */
        for (Message message : messages) {
            /**
             *   邮件删除
             */
            String subject = message.getSubject();
            // set the DELETE flag to true
            message.setFlag(Flags.Flag.DELETED, true);
           log.error("Marked DELETE for message: " + subject);
        }
    }

    /**
     * 获得邮件主题
     *
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
        if (msg == null || msg.getSubject() == null) {
            return "";
        }
        return MimeUtility.decodeText(msg.getSubject());
    }

    /**
     * 获得邮件发件人
     *
     * @param msg 邮件内容
     * @return 姓名 <Email地址>
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();
        if (froms.length < 1) {
            throw new MessagingException("没有发件人!");
        }
        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }
        from = person + "<" + address.getAddress() + ">";

        return from;
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * <p>Message.RecipientType.TO  收件人</p>
     * <p>Message.RecipientType.CC  抄送</p>
     * <p>Message.RecipientType.BCC 密送</p>
     *
     * @param msg  邮件内容
     * @param type 收件人类型
     * @return 收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
     * @throws MessagingException
     */
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuilder receiveAddress = new StringBuilder();
        Address[] addresss;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }

        if (addresss == null || addresss.length < 1) {
            throw new MessagingException("没有收件人!");
        }
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }

        //删除最后一个逗号
        receiveAddress.deleteCharAt(receiveAddress.length() - 1);
        return receiveAddress.toString();
    }

    /**
     * 获得邮件发送时间
     *
     * @param msg 邮件内容
     * @return yyyy年mm月dd日 星期X HH:mm
     * @throws MessagingException
     */
    public static String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";

        if (pattern == null || "".equals(pattern))
            pattern = "yyyy年MM月dd日 E HH:mm ";

        return new SimpleDateFormat(pattern).format(receivedDate);
    }

    /**
     * 判断邮件中是否包含附件
     *
     * @return 邮件中存在附件返回true，不存在返回false
     * @throws MessagingException
     * @throws IOException
     */
    public static boolean isContainAttachment(Part part) throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("application")) {
                        flag = true;
                    }
                    if (contentType.contains("name")) {
                        flag = true;
                    }
                }
                if (flag) break;
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part) part.getContent());
        }
        return flag;
    }

    /**
     * 判断邮件是否已读
     *
     * @param msg 邮件内容
     * @return 如果邮件已读返回true, 否则返回false
     * @throws MessagingException
     */
    public static boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }

    /**
     * 判断邮件是否需要阅读回执
     *
     * @param msg 邮件内容
     * @return 需要回执返回true, 否则返回false
     * @throws MessagingException
     */
    public static boolean isReplySign(MimeMessage msg) throws MessagingException {
        boolean replySign = false;
        String[] headers = msg.getHeader("Disposition-Notification-To");
        if (headers != null) {
            replySign = true;
        }
        return replySign;
    }

    /**
     * 获得邮件的优先级
     *
     * @param msg 邮件内容
     * @return 1(High):紧急  3:普通(Normal)  5:低(Low)
     * @throws MessagingException
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.contains("1") || headerPriority.contains("High")) {
                priority = "紧急";
            } else if (headerPriority.contains("5") || headerPriority.contains("Low")) {
                priority = "低";
            } else {
                priority = "普通";
            }
        }
        return priority;
    }

    /**
     * 获得邮件文本内容
     *
     * @param part    邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException
     * @throws IOException
     */
    public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    /**
     * 获取附件
     *
     * @param part 邮件中多个组合体中的其中一个组合体
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<InputStream> saveAttachment(Part part) throws UnsupportedEncodingException, MessagingException, FileNotFoundException, IOException {
        List<InputStream> res = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();    //复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    String file_name = bodyPart.getFileName();
                    file_name = MimeUtility.decodeText(file_name);
                    String suffix = FileUtils.getSuffix(file_name);
                    if (!RULE_FILE.contains(suffix) && !RULE_RE.contains("*")) {
                        log.info("文件格式不匹配：" + file_name);
                        continue;
                    }
                    res.add(bodyPart.getInputStream());
                } else if (bodyPart.isMimeType("multipart/*")) {
                    res.add(bodyPart.getInputStream());
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
                        res.add(bodyPart.getInputStream());
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            res.addAll(saveAttachment((Part) part.getContent()));
        }
        return res;
    }

    /**
     * 文本解码
     *
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本
     * @return 解码后的文本
     * @throws UnsupportedEncodingException
     */
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }


    public static void sendMail(String from, String passwd, String to, String msg, String subject) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");// 连接协议
        properties.put("mail.smtp.host", "smtp.exmail.qq.com");// 主机名
        properties.put("mail.smtp.port", 465);// 端口号
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");// 设置是否使用ssl安全连接 ---一般都使用
        properties.put("mail.debug", "true");// 设置是否显示debug信息 true 会在控制台显示相关信息
        // 得到回话对象
        Session session = Session.getInstance(properties);
        // 获取邮件对象
        Message message = new MimeMessage(session);
        // 设置发件人邮箱地址
        message.setFrom(new InternetAddress(from));
        // 设置收件人邮箱地址
        message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(to), new InternetAddress("liyuwei@tc5u.cn")});
        //message.setRecipient(Message.RecipientType.TO, new InternetAddress("xxx@qq.com"));//一个收件人
        // 设置邮件标题
        message.setSubject(subject);
        // 设置邮件内容
        message.setText(msg);
        // 得到邮差对象
        Transport transport = session.getTransport();
        // 连接自己的邮箱账户
        transport.connect(from, passwd);// 密码为QQ邮箱开通的stmp服务后得到的客户端授权码
        // 发送邮件
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

}


