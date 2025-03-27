package poly.edu.Controller;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dao.UserDAO;
import poly.edu.entity.UserEntity;

@Controller
@RequestMapping("/java5/asm/")
public class AccountController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    model.addAttribute("user", user);
                    return "profile";
                })
                .orElse("redirect:/java5/asm/login");
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("username") String username,
                                @RequestParam("fullname") String fullname,
                                @RequestParam("phonenumber") String phoneNumber,
                                @RequestParam String address,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    if (!fullname.matches("^[^0-9]+$") || fullname.trim().isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Họ và tên không hợp lệ!");
                        return "redirect:/java5/asm/profile";
                    }
                    if (!phoneNumber.matches("^\\d{10}$")) {
                        redirectAttributes.addFlashAttribute("error", "Số điện thoại phải có đúng 10 chữ số!");
                        return "redirect:/java5/asm/profile";
                    }

                    user.setFullName(fullname);
                    user.setPhone(phoneNumber);
                    user.setAddress(address);
                    userDAO.update(user);
                    session.setAttribute("user", user);
                    redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
                    return "redirect:/java5/asm/profile";
                })
                .orElse("redirect:/java5/asm/login");
    }
}