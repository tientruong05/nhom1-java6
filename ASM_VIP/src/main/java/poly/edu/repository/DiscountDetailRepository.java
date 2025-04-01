package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DiscountDetailEntity;
import poly.edu.entity.DiscountEntity;

import java.util.List;

@Repository
public interface DiscountDetailRepository extends JpaRepository<DiscountDetailEntity, Integer> {
    List<DiscountDetailEntity> findByProductIdAndStatus(int productId, int status);
    List<DiscountDetailEntity> findByCategoryIdAndStatus(int categoryId, int status);
    List<DiscountDetailEntity> findBySubCategoryIdAndStatus(int subCategoryId, int status);
    List<DiscountDetailEntity> findByDiscount(DiscountEntity discount);
    void deleteByDiscountId(int discountId);
}