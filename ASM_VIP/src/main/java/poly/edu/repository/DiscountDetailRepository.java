package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DiscountDetailEntity;

import java.util.List;

@Repository
public interface DiscountDetailRepository extends JpaRepository<DiscountDetailEntity, Integer> {
    List<DiscountDetailEntity> findByProductIdAndStatus(int productId, int status);
    List<DiscountDetailEntity> findByCategoryIdAndStatus(int categoryId, int status);
    List<DiscountDetailEntity> findBySubCategoryIdAndStatus(int subCategoryId, int status);
}