package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String username) {
        sendEmail(to, "Chào mừng bạn đến với TimeLux Watch!",
                "Xin chào " + username + ",\n\n" +
                        "Chúc mừng bạn đã đăng ký thành công tài khoản tại Luxe Watch!\n" +
                        "Chúng tôi rất vui mừng được chào đón bạn tham gia cộng đồng của chúng tôi.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Luxe Watch");
    }

    public void sendEmail(String to, String subject, String emailContent) {
        Optional.of(new SimpleMailMessage())
                .ifPresent(message -> {
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(emailContent);
                    try {
                        mailSender.send(message);
                        System.out.println("📧 Email đã gửi thành công tới: " + to);
                    } catch (Exception e) {
                        System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
                    }
                });
    }
}