package poly.edu.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import poly.edu.entity.DiscountEntity;

public interface DiscountDAO {
    Optional<DiscountEntity> findById(int id);
    List<DiscountEntity> findAll();
    void save(DiscountEntity discount);
    void update(DiscountEntity discount);
    void delete(int id);
    Optional<DiscountEntity> findActiveFlashSale(LocalDateTime now);
} 