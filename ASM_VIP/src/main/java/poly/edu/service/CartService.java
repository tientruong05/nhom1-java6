package poly.edu.service;

import poly.edu.entity.CartEntity;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<CartEntity> findById(int id); // Thêm nếu cần
    Optional<CartEntity> findByUserAndProduct(int userId, int productId);
    List<CartEntity> getCartItemsByUserId(int userId);
    void saveOrUpdate(CartEntity cart);
    void deleteCartItem(int id);
}