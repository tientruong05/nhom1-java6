package poly.edu.Controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    @Autowired private JavaMailSender mailSender;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        return Optional.ofNullable((UserEntity) session.getAttribute("user"))
                .map(user -> {
                    List<CartEntity> cartItems = cartService.getCartItemsByUserId(user.getId());
                    System.out.println("Cart items: " + cartItems);
                    int cartCount = cartItems.stream().mapToInt(CartEntity::getQty).sum();
                    List<CartDetailDTO> cartItemDTOs = cartItems.stream()
                            .map(item -> {
                                System.out.println("Product Image: " + item.getProduct().getImage()); // Debug giá trị ảnh
                                return new CartDetailDTO(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getProduct().getImage(),
                                        item.getQty(),
                                        item.getProduct().getPrice(),
                                        item.getProduct().getDiscountedPrice(),
                                        item.getProduct().getDiscountPercentage() != null ? item.getProduct().getDiscountPercentage().intValue() : null,
                                        item.getProduct().getQty());
                            })
                            .collect(Collectors.toList());
                    double total = cartItemDTOs.stream()
                            .mapToDouble(item -> item.getDiscountedPrice() * item.getQty())
                            .sum();

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
                .map(user -> cartService.getCartItemsByUserId(user.getId()).stream()
                        .map(item -> new CartDetailDTO(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getImage(),
                                item.getQty(),
                                item.getProduct().getPrice(),
                                item.getProduct().getDiscountedPrice(),
                                item.getProduct().getDiscountPercentage() != null ? item.getProduct().getDiscountPercentage().intValue() : null,
                                item.getProduct().getQty())) // availableQty được ánh xạ từ product.getQty()
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    @PostMapping("/cart/add/{productId}")
    @ResponseBody
    @Transactional
    public String addToCart(@PathVariable("productId") int productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "redirect:/java5/asm/login";

        ProductEntity product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getQty() < quantity) {
            return "error: Số lượng tồn kho không đủ (" + product.getQty() + " sản phẩm)";
        }

        CartEntity cartItem = cartService.findByUserAndProduct(user.getId(), productId)
                .map(item -> {
                    int newQty = item.getQty() + quantity;
                    if (newQty > product.getQty()) {
                        throw new RuntimeException("Số lượng vượt quá tồn kho (" + product.getQty() + " sản phẩm)");
                    }
                    item.setQty(newQty);
                    return item;
                })
                .orElseGet(() -> new CartEntity(0, user, product, quantity, product.getPrice()));

        cartService.saveOrUpdate(cartItem);
        session.setAttribute("cartCount", cartService.getCartItemsByUserId(user.getId())
                .stream().mapToInt(CartEntity::getQty).sum());
        return "success";
    }

    @PostMapping("/cart/update")
    @ResponseBody
    @Transactional
    public String updateCart(@RequestParam("itemId") int itemId,
                             @RequestParam("quantity") int quantity) {
        CartEntity cartItem = cartService.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục trong giỏ hàng"));

        if (quantity > cartItem.getProduct().getQty()) {
            return "error: Số lượng vượt quá tồn kho (" + cartItem.getProduct().getQty() + " sản phẩm)";
        }

        if (quantity <= 0) {
            cartService.deleteCartItem(itemId);
        } else {
            cartItem.setQty(quantity);
            cartService.saveOrUpdate(cartItem);
        }
        return "success";
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
                .map(user -> cartService.getCartItemsByUserId(user.getId())
                        .stream().mapToInt(CartEntity::getQty).sum())
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

        List<CartDetailDTO> checkoutDTOs = cartItems.stream()
                .map(item -> new CartDetailDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImage(),
                        item.getQty(),
                        item.getProduct().getPrice(),
                        item.getProduct().getDiscountedPrice(),
                        item.getProduct().getDiscountPercentage() != null ? item.getProduct().getDiscountPercentage().intValue() : null,
                        item.getProduct().getQty()))
                .collect(Collectors.toList());
        double total = checkoutDTOs.stream()
                .mapToDouble(item -> item.getDiscountedPrice() * item.getQty())
                .sum();

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

        String stockError = checkoutItems.stream()
                .map(item -> productService.getProductById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")))
                .filter(product -> checkoutItems.stream()
                        .filter(dto -> dto.getProductId() == product.getId())
                        .findFirst().get().getQty() > product.getQty())
                .findFirst()
                .map(product -> "Sản phẩm " + product.getName() + " vượt quá tồn kho (" + product.getQty() + " sản phẩm)")
                .orElse(null);
        if (stockError != null) {
            redirect.addFlashAttribute("error", stockError);
            return "redirect:/java5/asm/cart";
        }

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setFullname(fullName);
        order.setAddress(address);
        order.setPhone(phone);
        orderService.save(order);

        Date orderDate = new Date(); // Ngày hiện tại cho tất cả chi tiết đơn hàng
        for (CartDetailDTO item : checkoutItems) {
            ProductEntity product = productService.getProductById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            int newQty = product.getQty() - item.getQty();
            if (newQty < 0) throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho");
            product.setQty(newQty);
            productService.saveProduct(product);

            OrderDetailEntity orderDetail = new OrderDetailEntity();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQty(item.getQty());
            orderDetail.setPrice(item.getDiscountedPrice()); // Giá sau giảm giá
            orderDetail.setOrderDate(orderDate); // Gán ngày hiện tại
            orderDetailService.save(orderDetail);

            cartService.deleteCartItem(item.getId());
        }

        try {
            sendOrderConfirmationEmail(user.getEmail(), order, checkoutItems, total);
            redirect.addFlashAttribute("success", "Đơn hàng đã được đặt thành công! Vui lòng kiểm tra email.");
        } catch (MessagingException e) {
            redirect.addFlashAttribute("error", "Đơn hàng đã được đặt, nhưng không thể gửi email xác nhận.");
        }

        session.removeAttribute("checkoutItems");
        session.removeAttribute("total");
        return "redirect:/java5/asm/users/orders";
    }

    private void sendOrderConfirmationEmail(String toEmail, OrderEntity order, List<CartDetailDTO> items, double total) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Xác nhận đơn hàng #" + order.getId());
        helper.setFrom("your-email@gmail.com");

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h2>Cảm ơn bạn đã đặt hàng!</h2>")
                .append("<p>Đơn hàng của bạn đã được đặt thành công. Dưới đây là chi tiết:</p>")
                .append("<h3>Đơn hàng #" + order.getId() + "</h3>")
                .append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
                .append("<tr><th>Sản phẩm</th><th>Số lượng</th><th>Giá</th></tr>");

        for (CartDetailDTO item : items) {
            emailContent.append("<tr>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td>").append(item.getQty()).append("</td>")
                    .append("<td>").append(String.format("%,.0f", item.getDiscountedPrice() * item.getQty())).append(" VNĐ</td>")
                    .append("</tr>");
        }

        emailContent.append("</table>")
                .append("<p><strong>Tổng tiền:</strong> ").append(String.format("%,.0f", total)).append(" VNĐ</p>")
                .append("<p>Địa chỉ giao hàng: ").append(order.getAddress()).append("</p>")
                .append("<p>Chúng tôi sẽ giao hàng sớm nhất có thể. Cảm ơn bạn đã ủng hộ Watch Store!</p>");

        helper.setText(emailContent.toString(), true);
        mailSender.send(message);
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