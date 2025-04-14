package poly.edu.Controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.DTO.CartDetailDTO;
import poly.edu.DTO.OrderDTO;
import poly.edu.DTO.OrderDetailDTO;
import poly.edu.entity.*;
import poly.edu.service.CartService;
import poly.edu.service.OrderService;
import poly.edu.service.OrderDetailService;
import poly.edu.service.ProductService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/java5/asm")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private ProductService productService;
    @Autowired private OrderService orderService;
    @Autowired private OrderDetailService orderDetailService;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    List<CartDetailDTO> cartItemDTOs = cartService.getCartDetailsByUserId(user.getId());
                    int cartCount = cartService.getCartCount(user.getId());
                    double total = cartService.calculateCartTotal(cartItemDTOs);

                    model.addAttribute("cartItems", cartItemDTOs);
                    model.addAttribute("total", total);
                    model.addAttribute("shippingFee", 30000.0);
                    session.setAttribute("cartCount", cartCount);
                    return "cart";
                })
                .orElse("redirect:/java5/asm/login");
    }

    @GetMapping("/cart/check-stock")
    @ResponseBody
    public List<CartDetailDTO> checkStock(HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> cartService.getCartDetailsByUserId(user.getId()))
                .orElse(List.of());
    }

    @PostMapping("/cart/add/{productId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> addToCart(@PathVariable("productId") int productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        String result = cartService.addItemToCart(user, productId, quantity);
        if (result.equals("success")) {
            session.setAttribute("cartCount", cartService.getCartCount(user.getId()));
            return ResponseEntity.ok("success");
        } else if (result.startsWith("error:")){
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error: Unexpected server error");
        }
    }

    @PostMapping("/cart/update")
    @ResponseBody
    @Transactional
    public String updateCart(@RequestParam("itemId") int itemId,
                             @RequestParam("quantity") int quantity) {
        return cartService.updateCartItemQuantity(itemId, quantity);
    }

    @PostMapping("/cart/remove/{itemId}")
    @ResponseBody
    @Transactional
    public String removeFromCart(@PathVariable("itemId") int itemId) {
        cartService.deleteCartItem(itemId);
        return "success";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> cartService.getCartCount(user.getId()))
                .orElse(0);
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestBody List<Integer> selectedItems, HttpSession session, RedirectAttributes redirect, Model model) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "redirect:/java5/asm/login";

        if (selectedItems.isEmpty()) {
            redirect.addFlashAttribute("error", "Vui lòng chọn sản phẩm để thanh toán");
            return "redirect:/java5/asm/cart";
        }

        List<CartEntity> cartItems = cartService.getCartItemsByUserId(user.getId())
                .stream().filter(item -> selectedItems.contains(item.getId()))
                .collect(Collectors.toList());

        if (cartItems.isEmpty()) {
            redirect.addFlashAttribute("error", "Không tìm thấy sản phẩm đã chọn");
            return "redirect:/java5/asm/cart";
        }

        String stockError = cartItems.stream()
                .filter(item -> item.getQty() > item.getProduct().getQty())
                .findFirst()
                .map(item -> "Sản phẩm " + item.getProduct().getName() + " vượt quá tồn kho (" + item.getProduct().getQty() + " sản phẩm)")
                .orElse(null);
        if (stockError != null) {
            redirect.addFlashAttribute("error", stockError);
            return "redirect:/java5/asm/cart";
        }

        List<CartDetailDTO> checkoutDTOs = cartService.mapToCartDetailDTOs(cartItems);
        double total = cartService.calculateCartTotal(checkoutDTOs);

        session.setAttribute("checkoutItems", checkoutDTOs);
        session.setAttribute("total", total);
        return "redirect:/java5/asm/checkout";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "redirect:/java5/asm/login";

        List<CartDetailDTO> checkoutItems = (List<CartDetailDTO>) session.getAttribute("checkoutItems");
        Double total = (Double) session.getAttribute("total");

        if (checkoutItems == null || total == null) return "redirect:/java5/asm/cart";

        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("total", total);
        model.addAttribute("fullName", user.getFullName() != null ? user.getFullName() : "");
        model.addAttribute("address", user.getAddress() != null ? user.getAddress() : "");
        model.addAttribute("phone", user.getPhone() != null ? user.getPhone() : "");
        return "checkout";
    }

    @PostMapping("/cart/complete")
    @Transactional
    public String completeOrder(HttpSession session, Model model, RedirectAttributes redirect,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("address") String address,
                                @RequestParam("phone") String phone,
                                @RequestParam(value = "note", required = false) String note) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "redirect:/java5/asm/login";

        List<CartDetailDTO> checkoutItems = (List<CartDetailDTO>) session.getAttribute("checkoutItems");
        Double total = (Double) session.getAttribute("total");
        if (checkoutItems == null || total == null) return "redirect:/java5/asm/cart";

        String stockError = cartService.validateCheckoutItems(checkoutItems);
        if (stockError != null) {
            redirect.addFlashAttribute("error", stockError);
            return "redirect:/java5/asm/cart";
        }

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setFullname(fullName);
        order.setAddress(address);
        order.setPhone(phone);
        OrderEntity savedOrder = orderService.save(order);

        cartService.processOrderItems(savedOrder, checkoutItems);

        try {
            cartService.sendOrderConfirmationEmail(user.getEmail(), savedOrder, checkoutItems, total);
            redirect.addFlashAttribute("success", "Đơn hàng đã được đặt thành công! Vui lòng kiểm tra email.");
        } catch (MessagingException e) {
            redirect.addFlashAttribute("error", "Đơn hàng đã được đặt, nhưng không thể gửi email xác nhận.");
        }

        session.removeAttribute("checkoutItems");
        session.removeAttribute("total");
        return "redirect:/java5/asm/users/orders";
    }

    @GetMapping("/users/orders")
    public String viewOrders(Model model, HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    List<OrderDTO> orderDTOs = orderService.findByUser(user).stream()
                            .map(order -> {
                                List<OrderDetailEntity> details = orderDetailService.findByOrder(order);
                                double totalAmount = details.stream()
                                        .mapToDouble(detail -> detail.getPrice() * detail.getQty())
                                        .sum();
                                Date orderDate = details.isEmpty() ? new Date() : details.get(0).getOrderDate();
                                return new OrderDTO(
                                        order.getId(),
                                        order.getFullname(),
                                        order.getAddress(),
                                        order.getPhone(),
                                        totalAmount,
                                        orderDate);
                            })
                            .collect(Collectors.toList());

                    model.addAttribute("orders", orderDTOs);
                    model.addAttribute("totalOrders", orderDTOs.size());
                    model.addAttribute("totalSpent", orderDTOs.stream().mapToDouble(OrderDTO::getTotalAmount).sum());
                    model.addAttribute("dateFormat", new java.text.SimpleDateFormat("dd/MM/yyyy"));
                    return "order";
                })
                .orElse("redirect:/java5/asm/login");
    }
    @GetMapping("/order/detail/{orderId}")
    @ResponseBody
    public OrderDTO getOrderDetail(@PathVariable("orderId") int orderId, HttpSession session) {
        UserEntity user = Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .orElseThrow(() -> new RuntimeException("Người dùng chưa đăng nhập"));

        OrderEntity order = orderService.getOrderById(orderId)
                .filter(o -> o.getUser().getId() == user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc không có quyền truy cập"));

        List<OrderDetailEntity> details = orderDetailService.findByOrder(order);
        List<OrderDetailDTO> detailDTOs = details.stream()
                .map(detail -> new OrderDetailDTO(
                        detail.getId(),
                        detail.getProduct().getName(),
                        detail.getProduct().getImage(),
                        detail.getQty(),
                        detail.getPrice()))
                .collect(Collectors.toList());
        double totalAmount = details.stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQty())
                .sum();
        Date orderDate = details.isEmpty() ? new Date() : details.get(0).getOrderDate();

        OrderDTO orderDTO = new OrderDTO(
                order.getId(),
                order.getFullname(),
                order.getAddress(),
                order.getPhone(),
                totalAmount,
                orderDate);
        orderDTO.setOrderDetails(detailDTOs);
        return orderDTO;
    }
}