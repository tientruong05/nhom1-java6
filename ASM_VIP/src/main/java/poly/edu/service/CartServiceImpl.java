package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.dao.CartDAO;
import poly.edu.entity.CartEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDAO cartDAO;

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
                    Optional<CartEntity> existingCart = findByUserAndProduct(c.getUser().getId(), c.getProduct().getId());
                    existingCart.ifPresentOrElse(
                            existing -> {
                                existing.setQty(existing.getQty() + c.getQty());
                                cartDAO.update(existing);
                            },
                            () -> cartDAO.save(c));
                });
    }

    @Override
    public void deleteCartItem(int id) {
        cartDAO.findById(id).ifPresent(cart -> cartDAO.delete(id));
    }
}