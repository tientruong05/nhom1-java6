package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.OrderDetailEntity;
import poly.edu.entity.OrderEntity;
import poly.edu.entity.UserEntity;
import poly.edu.service.OrderDetailService;
import poly.edu.service.OrderService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/users/shopping-history")
public class ShoppingHistoryController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public String shoppingHistory(Model model, HttpSession session,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    List<OrderEntity> orders = orderService.findByUser(user);
                    Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
                    Page<OrderDetailEntity> orderDetailPage = orderDetailService.findByOrders(orders, pageable);

                    model.addAttribute("orderDetails", orderDetailPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", orderDetailPage.getTotalPages());
                    model.addAttribute("totalItems", orderDetailPage.getTotalElements());
                    return "shopping_history";
                })
                .orElse("redirect:/java5/asm/login");
    }
}