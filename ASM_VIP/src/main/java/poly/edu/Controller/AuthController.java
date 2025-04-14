package poly.edu.Controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.dao.UserDAO;
import poly.edu.entity.*;
import poly.edu.service.CartService;

import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/java5/asm")
public class AuthController {

    @Autowired private UserDAO userDAO;
    @Autowired private JavaMailSender mailSender;
    @Autowired private CartService cartService;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "redirectUrl", required = false) String redirectUrl, HttpSession session) {
        // Store the redirect URL in the session if provided
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            session.setAttribute("redirectUrlAfterLogin", redirectUrl);
        }
        return "login-register";
    }

    @GetMapping("/forgot-pass")
    public String forgotPasswordPage() {
        return "forgot_pass";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            Model model, HttpSession session) {
        return userDAO.findByEmail(username)
                .filter(user -> user.getPassword().equals(Base64.getEncoder().encodeToString(password.getBytes())) && user.isStatus())
                .map(user -> {
                    session.setAttribute("user", user);
                    int cartCount = cartService.getCartItemsByUserId(user.getId()).stream()
                            .mapToInt(CartEntity::getQty).sum();
                    session.setAttribute("cartCount", cartCount);

                    // Check for redirect URL first
                    String redirectUrl = (String) session.getAttribute("redirectUrlAfterLogin");
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        session.removeAttribute("redirectUrlAfterLogin"); // Remove after use
                        return "redirect:" + redirectUrl;
                    }
                    
                    // Existing logic if no redirect URL
                    if (user.getFullName().isEmpty() && user.getAddress().isEmpty() && user.getPhone().isEmpty()) {
                        return "redirect:/java5/asm/profile";
                    }

                    return Optional.ofNullable((String) session.getAttribute("securityUri"))
                            .filter(uri -> uri.startsWith("/java5/asm/cart"))
                            .map(uri -> "redirect:/java5/asm/cart")
                            .orElse("redirect:/java5/asm/index");
                })
                .orElseGet(() -> {
                    model.addAttribute("message", "Sai mật khẩu, tài khoản chưa kích hoạt hoặc email không tồn tại!");
                    return "login-register";
                });
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String fullname,
                           Model model) {
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            model.addAttribute("message", "Email không hợp lệ!");
            return "login-register";
        }
        if (userDAO.findByEmail(email).isPresent()) {
            model.addAttribute("message", "Email đã được sử dụng!");
            return "login-register";
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setFullName(fullname);
        newUser.setEmail(email);
        newUser.setPassword(Base64.getEncoder().encodeToString(password.getBytes()));
        newUser.setPhone(phone == null || phone.trim().isEmpty() ? "N/A" : phone);
        newUser.setAddress(address);
        newUser.setRole(false);
        newUser.setStatus(false);

        userDAO.save(newUser);
        sendActivationEmail(newUser);
        model.addAttribute("message", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
        return "login-register";
    }

    private void sendActivationEmail(UserEntity user) {
        String activationLink = "http://localhost:8080/java5/asm/activate?email=" + user.getEmail();
        String subject = "Chào mừng bạn đến với TimeLux Watch - Xác nhận tài khoản";
        String htmlContent =
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='padding: 20px; background-color: #ffffff; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color: #1a1a1a; text-align: center;'>Xác Nhận Tài Khoản</h2>" +
                    "<p style='color: #333333;'>Kính gửi " + user.getFullName() + ",</p>" +
                    "<p style='color: #333333;'>Chào mừng bạn đến với TimeLux Watch - Nơi thời gian được tôn vinh!</p>" +
                    "<p style='color: #333333;'>Chúng tôi vô cùng hân hạnh được đồng hành cùng bạn trên hành trình khám phá những kiệt tác đồng hồ xa xỉ.</p>" +
                    "<div style='text-align: center; margin: 30px 0;'>" +
                        "<a href='" + activationLink + "' style='background-color: #1a1a1a; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>XÁC NHẬN TÀI KHOẢN</a>" +
                    "</div>" +
                    "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #1a1a1a; font-weight: bold;'>Tại TimeLux Watch, chúng tôi cam kết mang đến cho bạn:</p>" +
                        "<ul style='color: #333333;'>" +
                            "<li>Bộ sưu tập đồng hồ cao cấp từ các thương hiệu danh tiếng</li>" +
                            "<li>Dịch vụ chăm sóc khách hàng đẳng cấp 5 sao</li>" +
                            "<li>Chế độ bảo hành và hậu mãi ưu việt</li>" +
                        "</ul>" +
                    "</div>" +
                    "<p style='color: #666666;'>Nếu bạn cần hỗ trợ, đừng ngần ngại liên hệ với chúng tôi qua hotline: <span style='color: #1a1a1a; font-weight: bold;'>093.934.8888</span></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #666666;'>" +
                    "<p style='margin: 5px 0;'><strong>TimeLux Watch</strong></p>" +
                    "<p style='margin: 5px 0;'>Luxury Timepieces & Exceptional Service</p>" +
                    "<p style='margin: 5px 0;'>Website: www.timeluxwatch.com</p>" +
                    "<p style='margin: 5px 0;'>Email: support@timeluxwatch.com</p>" +
                "</div>" +
            "</div>";

        try {
           MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam String email, Model model) {
        userDAO.findByEmail(email)
                .ifPresentOrElse(user -> {
                    user.setStatus(true);
                    userDAO.save(user);
                    model.addAttribute("message", "Tài khoản đã được kích hoạt thành công! Bạn có thể đăng nhập ngay.");
                }, () -> model.addAttribute("message", "Liên kết kích hoạt không hợp lệ!"));
        return "login-register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/java5/asm/login";
    }

    @PostMapping("/account/resetPassword")
    public String resetPassword(@RequestParam String email, Model model) {
        return userDAO.findByEmail(email)
                .map(user -> {
                    String newPassword = generateRandomPassword();
                    user.setPassword(Base64.getEncoder().encodeToString(newPassword.getBytes()));
                    userDAO.save(user);
                    sendPasswordResetEmail(user.getEmail(), newPassword);
                    model.addAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn!");
                    return "login-register";
                })
                .orElseGet(() -> {
                    model.addAttribute("message", "Email không tồn tại trong hệ thống!");
                    return "forgot_pass";
                });
    }

    private void sendPasswordResetEmail(String email, String newPassword) {
        String subject = "Mật Khẩu Mới Của Bạn - TimeLux Watch";
        String htmlContent =
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='padding: 20px; background-color: #ffffff; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                    "<h2 style='color: #1a1a1a; text-align: center;'>Mật Khẩu Mới Của Bạn</h2>" +
                    "<p style='color: #333333;'>Chào bạn,</p>" +
                    "<p style='color: #333333;'>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu từ bạn. Dưới đây là mật khẩu mới của bạn:</p>" +
                    "<div style='text-align: center; margin: 20px 0;'>" +
                        "<p style='font-size: 1.2rem; font-weight: bold; color: #d4af37;'>" + newPassword + "</p>" +
                    "</div>" +
                    "<p style='color: #333333;'>Vui lòng đăng nhập bằng mật khẩu này và đổi mật khẩu mới để bảo mật tài khoản của bạn.</p>" +
                    "<div style='text-align: center; margin: 30px 0;'>" +
                        "<a href='http://localhost:8080/java5/asm/login' style='background-color: #1a1a1a; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>ĐĂNG NHẬP NGAY</a>" +
                    "</div>" +
                    "<p style='color: #666666;'>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng liên hệ với chúng tôi ngay qua hotline: <span style='color: #1a1a1a; font-weight: bold;'>093.934.8888</span></p>" +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #666666;'>" +
                    "<p style='margin: 5px 0;'><strong>TimeLux Watch</strong></p>" +
                    "<p style='margin: 5px 0;'>Luxury Timepieces & Exceptional Service</p>" +
                    "<p style='margin: 5px 0;'>Website: www.timeluxwatch.com</p>" +
                    "<p style='margin: 5px 0;'>Email: support@timeluxwatch.com</p>" +
                "</div>" +
            "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomPassword() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}