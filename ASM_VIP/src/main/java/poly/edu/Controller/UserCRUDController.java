package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.UserEntity;
import poly.edu.service.UserService;

import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/crud/users")
public class UserCRUDController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size) {
        Page<UserEntity> userPage = userService.getAllUsers(page, size);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "crud_users";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "crud_users";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute UserEntity user, RedirectAttributes redirectAttributes) {
        return Optional.of(user)
                .filter(u -> !userService.existsByUsername(u.getUsername()))
                .filter(u -> !userService.existsByEmail(u.getEmail()))
                .filter(u -> !userService.existsByPhone(u.getPhone()))
                .map(u -> {
                    userService.createUser(u);
                    redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
                    return "redirect:/java5/asm/crud/users";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", determineError(user));
                    return "redirect:/java5/asm/crud/users";
                });
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") int id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "crud_users";
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute UserEntity user, RedirectAttributes redirectAttributes) {
        return userService.getUserById(user.getId())
                .map(existingUser -> {
                    if (!existingUser.getUsername().equals(user.getUsername()) && userService.existsByUsername(user.getUsername())) {
                        redirectAttributes.addFlashAttribute("error", "Tên đăng nhập '" + user.getUsername() + "' đã tồn tại!");
                        return "redirect:/java5/asm/crud/users";
                    }
                    if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
                        redirectAttributes.addFlashAttribute("error", "Email '" + user.getEmail() + "' đã tồn tại!");
                        return "redirect:/java5/asm/crud/users";
                    }
                    if (!existingUser.getPhone().equals(user.getPhone()) && userService.existsByPhone(user.getPhone())) {
                        redirectAttributes.addFlashAttribute("error", "Số điện thoại '" + user.getPhone() + "' đã tồn tại!");
                        return "redirect:/java5/asm/crud/users";
                    }
                    userService.updateUser(user);
                    redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
                    return "redirect:/java5/asm/crud/users";
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + user.getId()));
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "Xóa người dùng thành công!");
        return "redirect:/java5/asm/crud/users";
    }

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("users", userService.searchUsersByName(keyword));
        return "crud_users";
    }

    @GetMapping("/role/{role}")
    public String filterByRole(@PathVariable("role") int role, Model model) {
        model.addAttribute("users", userService.getUsersByRole(role));
        return "crud_users";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public UserEntity getUserById(@PathVariable("id") int id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }

    private String determineError(UserEntity user) {
        return userService.existsByUsername(user.getUsername()) ? "Tên đăng nhập '" + user.getUsername() + "' đã tồn tại!" :
                userService.existsByEmail(user.getEmail()) ? "Email '" + user.getEmail() + "' đã tồn tại!" :
                        "Số điện thoại '" + user.getPhone() + "' đã tồn tại!";
    }
}