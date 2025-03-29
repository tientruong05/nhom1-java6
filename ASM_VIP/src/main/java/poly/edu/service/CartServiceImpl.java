package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dao.CartDAO;
import poly.edu.entity.CartEntity;
import poly.edu.DTO.CartDetailDTO;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.UserEntity;
import poly.edu.entity.OrderEntity;
import poly.edu.entity.OrderDetailEntity;
import poly.edu.service.ProductService;
import poly.edu.service.OrderDetailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDAO cartDAO;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public Optional<CartEntity> findById(int id) {
        return cartDAO.findById(id);
    }

    @Override
    public Optional<CartEntity> findByUserAndProduct(int userId, int productId) {
        return cartDAO.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<CartEntity> getCartItemsByUserId(int userId) {
        return cartDAO.findByUserId(userId).stream()
                .filter(cart -> cart.getUser() != null && cart.getProduct() != null)
                .collect(Collectors.toList());
    }

    @Override
    public void saveOrUpdate(CartEntity cart) {
        Optional.ofNullable(cart)
                .filter(c -> c.getUser() != null && c.getProduct() != null)
                .ifPresent(c -> {
                    if (c.getId() > 0) {
                        cartDAO.update(c);
                    } else {
                        cartDAO.save(c);
                    }
                });
    }

    @Override
    public void deleteCartItem(int id) {
        cartDAO.findById(id).ifPresent(cart -> cartDAO.delete(id));
    }

    @Override
    public List<CartDetailDTO> getCartDetailsByUserId(int userId) {
        return mapToCartDetailDTOs(getCartItemsByUserId(userId));
    }

    @Override
    public List<CartDetailDTO> mapToCartDetailDTOs(List<CartEntity> cartItems) {
        return cartItems.stream()
                .map(item -> new CartDetailDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImage(),
                        item.getQty(),
                        item.getProduct().getPrice(),
                        item.getProduct().getDiscountedPrice(),
                        item.getProduct().getDiscountPercentage() != null ? 
                                item.getProduct().getDiscountPercentage().intValue() : null,
                        item.getProduct().getQty()))
                .collect(Collectors.toList());
    }

    @Override
    public double calculateCartTotal(List<CartDetailDTO> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getDiscountedPrice() * item.getQty())
                .sum();
    }

    @Override
    public String addItemToCart(UserEntity user, int productId, int quantity) {
        if (user == null) {
            return "redirect:/java5/asm/login";
        }

        ProductEntity product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!validateStock(productId, quantity)) {
            return "error: Số lượng tồn kho không đủ (" + product.getQty() + " sản phẩm)";
        }

        CartEntity cartItem = findByUserAndProduct(user.getId(), productId)
                .map(item -> {
                    int newQty = item.getQty() + quantity;
                    if (newQty > product.getQty()) {
                        throw new RuntimeException("Số lượng vượt quá tồn kho (" + product.getQty() + " sản phẩm)");
                    }
                    item.setQty(newQty);
                    return item;
                })
                .orElseGet(() -> new CartEntity(0, user, product, quantity, product.getPrice()));

        saveOrUpdate(cartItem);
        return "success";
    }

    @Override
    public String updateCartItemQuantity(int itemId, int quantity) {
        CartEntity cartItem = findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục trong giỏ hàng"));

        ProductEntity product = cartItem.getProduct();
        
        if (quantity > product.getQty()) {
            return "error: Số lượng vượt quá tồn kho (" + product.getQty() + " sản phẩm)";
        }

        if (quantity <= 0) {
            deleteCartItem(itemId);
        } else {
            cartItem.setQty(quantity);
            saveOrUpdate(cartItem);
        }
        return "success";
    }

    @Override
    public int getCartCount(int userId) {
        return getCartItemsByUserId(userId)
                .stream()
                .mapToInt(CartEntity::getQty)
                .sum();
    }

    @Override
    public boolean validateStock(int productId, int quantity) {
        return productService.getProductById(productId)
                .map(product -> product.getQty() >= quantity)
                .orElse(false);
    }
    
    @Override
    public String validateCheckoutItems(List<CartDetailDTO> items) {
        return items.stream()
                .map(item -> productService.getProductById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")))
                .filter(product -> items.stream()
                        .filter(dto -> dto.getProductId() == product.getId())
                        .findFirst()
                        .map(dto -> dto.getQty() > product.getQty())
                        .orElse(false))
                .findFirst()
                .map(product -> "Sản phẩm " + product.getName() + " vượt quá tồn kho (" + product.getQty() + " sản phẩm)")
                .orElse(null);
    }
    
    @Override
    @Transactional
    public void processOrderItems(OrderEntity order, List<CartDetailDTO> items) {
        Date orderDate = new Date();
        
        items.forEach(item -> {
            ProductEntity product = productService.getProductById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            int newQty = product.getQty() - item.getQty();
            if (newQty < 0) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho");
            }
            
            // Update product quantity
            product.setQty(newQty);
            productService.saveProduct(product);
            
            // Create order detail
            OrderDetailEntity orderDetail = new OrderDetailEntity();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQty(item.getQty());
            orderDetail.setPrice(item.getDiscountedPrice());
            orderDetail.setOrderDate(orderDate);
            orderDetailService.save(orderDetail);
            
            // Remove item from cart
            deleteCartItem(item.getId());
        });
    }
    
    @Override
    public void sendOrderConfirmationEmail(String toEmail, OrderEntity order, List<CartDetailDTO> items, double total) throws MessagingException {
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

        items.forEach(item -> {
            emailContent.append("<tr>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td>").append(item.getQty()).append("</td>")
                    .append("<td>").append(String.format("%,.0f", item.getDiscountedPrice() * item.getQty())).append(" VNĐ</td>")
                    .append("</tr>");
        });

        emailContent.append("</table>")
                .append("<p><strong>Tổng tiền:</strong> ").append(String.format("%,.0f", total)).append(" VNĐ</p>")
                .append("<p>Địa chỉ giao hàng: ").append(order.getAddress()).append("</p>")
                .append("<p>Chúng tôi sẽ giao hàng sớm nhất có thể. Cảm ơn bạn đã ủng hộ Watch Store!</p>");

        helper.setText(emailContent.toString(), true);
        mailSender.send(message);
    }
}