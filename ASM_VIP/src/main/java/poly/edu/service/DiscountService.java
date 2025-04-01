package poly.edu.service;

import poly.edu.entity.DiscountEntity;
import java.util.List;

public interface DiscountService {
    List<DiscountEntity> findAll();
    DiscountEntity findById(int id);
    void save(DiscountEntity discount);
    void delete(int id);
}