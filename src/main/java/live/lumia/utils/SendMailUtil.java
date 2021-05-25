package live.lumia.utils;

import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 邮件发送service
 *
 * @author lc
 */
@Slf4j
public class SendMailUtil {


    public static JavaMailSender getMailSender() {
        return SpringUtil.getBean(JavaMailSender.class);
    }

    /**
     * 发送邮件
     *
     * @param to
     * @param subject
     * @param content
     */
    public static void sendSimpleMail(String from, String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(from);
        getMailSender().send(message);
    }

    /**
     * 发送html邮件
     *
     * @param to
     * @param subject
     * @param content
     */
    public static void sendHtmlMail(String from, String to, String subject, String content) throws MessagingException {
        MimeMessage message = getMailSender().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        getMailSender().send(message);
    }

    /**
     * 发送带附件的邮件
     *
     * @param to
     * @param subject
     * @param content
     * @param files
     */
    public static void sendAttachmentMail(String from, String to, String subject, String content, List<File> files) throws MessagingException {
        MimeMessage message = getMailSender().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        files = files.stream().filter(x -> x.exists() && x.isFile()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(files)) {
            throw new BaseException(BaseErrorEnums.FILE_NOT_EXISTS);
        }
        for (File file : files) {
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            helper.addAttachment(Objects.requireNonNull(fileSystemResource.getFilename()), fileSystemResource);
        }
        getMailSender().send(message);
    }

    /***
     * 发送带图片的邮件
     * @param to
     * @param subject
     * @param content
     * @param rscPath
     * @param rscId
     */
    public static void sendInLiResourceMail(String from, String to, String subject, String content, File rscPath, String rscId) throws MessagingException {
        log.info("发送静态邮件开始：{},{},{},{},{}", to, subject, content, rscPath, rscId);
        MimeMessage message = getMailSender().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        FileSystemResource res = new FileSystemResource(rscPath);
        helper.addInline(rscId, res);
        getMailSender().send(message);
        log.info("发送邮件成功");
    }
}
