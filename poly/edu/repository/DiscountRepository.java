package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.DiscountEntity;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Integer> {
    @Query("SELECT d FROM discounts d WHERE d.status = 1 " +
           "AND CAST(d.startDate AS varchar) <= CAST(:now AS varchar) " +
           "AND CAST(d.endDate AS varchar) >= CAST(:now AS varchar)")
    Optional<DiscountEntity> findActiveFlashSale(@Param("now") LocalDateTime now);
} 