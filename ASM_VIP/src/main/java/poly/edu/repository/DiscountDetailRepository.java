package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.DiscountDetailEntity;
import poly.edu.entity.DiscountEntity;

import java.util.List;

@Repository
public interface DiscountDetailRepository extends JpaRepository<DiscountDetailEntity, Integer> {
    @Query("SELECT dd.product.id FROM DiscountDetailEntity dd WHERE dd.discount.id = :discountId AND dd.status = 1 AND dd.product IS NOT NULL")
    List<Integer> findProductIdsByDiscountId(@Param("discountId") int discountId);
    
    @Query("SELECT CASE WHEN COUNT(dd) > 0 THEN true ELSE false END FROM DiscountDetailEntity dd WHERE dd.discount.id = :discountId AND dd.product.id = :productId")
    boolean existsByDiscountIdAndProductId(@Param("discountId") int discountId, @Param("productId") int productId);
    
    @Query("SELECT dd FROM DiscountDetailEntity dd WHERE dd.product.id = :productId AND dd.status = 1")
    List<DiscountDetailEntity> findByProductIdAndStatus(@Param("productId") int productId, @Param("status") int status);
    
    @Query("SELECT dd FROM DiscountDetailEntity dd WHERE dd.category.id = :categoryId AND dd.status = 1")
    List<DiscountDetailEntity> findByCategoryIdAndStatus(@Param("categoryId") int categoryId, @Param("status") int status);
    
    @Query("SELECT dd FROM DiscountDetailEntity dd WHERE dd.subCategory.id = :subCategoryId AND dd.status = 1")
    List<DiscountDetailEntity> findBySubCategoryIdAndStatus(@Param("subCategoryId") int subCategoryId, @Param("status") int status);

    List<DiscountDetailEntity> findByDiscount(DiscountEntity discount);
    @Transactional
    void deleteByDiscountId(int discountId);
}