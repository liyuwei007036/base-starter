package com.lc.core.service;

import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.io.File;


/**
 * 邮件发送service
 *
 * @author lc
 */
@Slf4j
@ConditionalOnClass(MailMessage.class)
@Component
public class MailService {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送邮件
     *
     * @param to
     * @param subject
     * @param content
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(from);
        mailSender.send(message);
    }

    /**
     * 发送html邮件
     *
     * @param to
     * @param subject
     * @param content
     */
    public void sendHTMLMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("【邮件发送失败】", e);
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param to
     * @param subject
     * @param content
     * @param f
     */
    public void sendAttachmentMail(String to, String subject, String content, File f) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(f);
            String fileName = file.getFilename();
            if (StringUtils.isEmpty(fileName)) {
                throw new BaseException(BaseErrorEnums.FILE_NOT_EXISTS);
            }
            helper.addAttachment(fileName, file);
            //发送多个附件的操纵
            helper.addAttachment(fileName + "_", file);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("【邮件发送失败】", e);
        }
    }

    /***
     * 发送带图片的邮件
     * @param to
     * @param subject
     * @param content
     * @param rscPath
     * @param rscId
     */
    public void sendInLiResourceMail(String to, String subject, String content, File rscPath, String rscId) {
        log.info("发送静态邮件开始：{},{},{},{},{}", to, subject, content, rscPath, rscId);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            FileSystemResource res = new FileSystemResource(rscPath);
            helper.addInline(rscId, res);
            mailSender.send(message);
            log.info("发送邮件成功");
        } catch (Exception e) {
            log.error("发送邮件失败：", e);
            e.printStackTrace();
        }
    }
}
