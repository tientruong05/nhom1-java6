package poly.edu.service;

import poly.edu.entity.CartEntity;
import poly.edu.entity.UserEntity;
import poly.edu.DTO.CartDetailDTO;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.OrderEntity;

import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<CartEntity> findById(int id); // Thêm nếu cần
    Optional<CartEntity> findByUserAndProduct(int userId, int productId);
    List<CartEntity> getCartItemsByUserId(int userId);
    void saveOrUpdate(CartEntity cart);
    void deleteCartItem(int id);
    
    // New business logic methods
    List<CartDetailDTO> getCartDetailsByUserId(int userId);
    String addItemToCart(UserEntity user, int productId, int quantity);
    String updateCartItemQuantity(int itemId, int quantity);
    int getCartCount(int userId);
    boolean validateStock(int productId, int quantity);
    List<CartDetailDTO> mapToCartDetailDTOs(List<CartEntity> cartItems);
    double calculateCartTotal(List<CartDetailDTO> cartItems);
    
    // Checkout related methods
    String validateCheckoutItems(List<CartDetailDTO> items);
    void processOrderItems(OrderEntity order, List<CartDetailDTO> items);
    
    // Email functionality
    void sendOrderConfirmationEmail(String email, OrderEntity order, List<CartDetailDTO> items, double total) throws MessagingException;
}