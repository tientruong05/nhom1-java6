package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dao.UserDAO;
import poly.edu.entity.UserEntity;
import poly.edu.service.MailService;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/account")
public class ForgotPassController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MailService mailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_pass";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        return userDAO.findByUsernameAndEmail(username, email)
                .map(user -> {
                    String newPassword = generateValidPassword();
                    user.setPassword(newPassword); // Không mã hóa bằng Base64
                    userDAO.update(user);

                    String emailContent = "Mật khẩu mới của bạn là: " + newPassword;
                    // mailService.mailSender(user.getEmail(), "Khôi phục mật khẩu", emailContent);

                    redirectAttributes.addFlashAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn.");
                    return "redirect:/login";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản với thông tin này.");
                    return "redirect:/forgot-password";
                });
    }

    private String generateValidPassword() {
        return Optional.of(generateRandomPassword())
                .filter(password -> !containsSixSpaces(password))
                .orElseGet(this::generateValidPassword); // Đệ quy nếu không hợp lệ
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        return random.ints(8, 0, chars.length())
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private boolean containsSixSpaces(String password) {
        return Pattern.compile("\\s{6,}").matcher(password).find();
    }
}