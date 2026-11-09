package com.mycompany.manage_electronics.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailUtil {
    // Thông tin Gmail đọc từ biến môi trường MAIL_USER và MAIL_PASSWORD (App Password 16 ký tự của Google).
    // KHÔNG ghi mật khẩu trực tiếp vào mã nguồn.
    private static final String FROM_EMAIL = System.getenv("MAIL_USER");
    private static final String PASSWORD = System.getenv("MAIL_PASSWORD");
    
    public static boolean sendEmail(String toEmail, String subject, String body) {
        if (FROM_EMAIL == null || PASSWORD == null) {
            System.err.println("EmailUtil: chua cau hinh bien moi truong MAIL_USER / MAIL_PASSWORD");
            return false;
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.mime.charset", "UTF-8");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            message.setContent(body, "text/plain; charset=UTF-8");
            
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String generateResetToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
