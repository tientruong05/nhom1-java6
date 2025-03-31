package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DiscountDetailEntity;

import java.util.List;

@Repository
public interface DiscountDetailRepository extends JpaRepository<DiscountDetailEntity, Integer> {
    @Query("SELECT dd.productId FROM discount_details dd WHERE dd.discountId = :discountId AND dd.status = 1 AND dd.productId IS NOT NULL")
    List<Integer> findProductIdsByDiscountId(@Param("discountId") int discountId);
    
    boolean existsByDiscountIdAndProductId(int discountId, int productId);
} 