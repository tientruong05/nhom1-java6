package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dao.UserDAO;
import poly.edu.entity.UserEntity;

import jakarta.servlet.http.HttpSession;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/java5/asm")
public class ChangePassController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/change_pw")
    public String changePasswordPage(Model model) {
        model.addAttribute("currentPassword", "");
        model.addAttribute("newPassword", "");
        model.addAttribute("confirmPassword", "");
        return "change_pw";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    model.addAttribute("currentPassword", currentPassword);
                    model.addAttribute("newPassword", newPassword);
                    model.addAttribute("confirmPassword", confirmPassword);

                    // Kiểm tra mật khẩu hiện tại
                    String encodedCurrentPassword = Base64.getEncoder().encodeToString(currentPassword.getBytes());
                    if (!user.getPassword().equals(encodedCurrentPassword)) {
                        model.addAttribute("currentPasswordError", "Mật khẩu hiện tại không đúng!");
                        return "change_pw";
                    }

                    // Kiểm tra mật khẩu mới và xác nhận có khớp không
                    if (!newPassword.equals(confirmPassword)) {
                        model.addAttribute("confirmPasswordError", "Mật khẩu mới và xác nhận không khớp!");
                        return "change_pw";
                    }

                    // Kiểm tra độ dài mật khẩu mới
                    if (newPassword.length() < 6) {
                        model.addAttribute("newPasswordError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                        return "change_pw";
                    }

                    // Kiểm tra mật khẩu mới có trùng với mật khẩu cũ không
                    String encodedNewPassword = Base64.getEncoder().encodeToString(newPassword.getBytes());
                    if (encodedNewPassword.equals(user.getPassword())) {
                        model.addAttribute("newPasswordError", "Mật khẩu mới không được trùng với mật khẩu cũ!");
                        return "change_pw";
                    }

                    // Kiểm tra mật khẩu mới có chứa 6 dấu cách liên tiếp không
                    if (containsSixSpaces(newPassword)) {
                        model.addAttribute("newPasswordError", "Mật khẩu mới không được chứa 6 dấu cách liên tiếp!");
                        return "change_pw";
                    }

                    // Cập nhật mật khẩu mới
                    user.setPassword(encodedNewPassword);
                    userDAO.update(user);
                    session.setAttribute("user", user);

                    model.addAttribute("currentPassword", "");
                    model.addAttribute("newPassword", "");
                    model.addAttribute("confirmPassword", "");
                    model.addAttribute("message", "Đổi mật khẩu thành công!");
                    return "redirect:/java5/asm/logout";
                })
                .orElse("redirect:/java5/asm/login");
    }

    // Phương thức kiểm tra 6 dấu cách liên tiếp
    private boolean containsSixSpaces(String password) {
        return Pattern.compile("\\s{6,}").matcher(password).find();
    }
}