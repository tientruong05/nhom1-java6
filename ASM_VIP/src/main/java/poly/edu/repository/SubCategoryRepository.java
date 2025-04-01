package poly.edu.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import poly.edu.entity.SubCategoryEntity;


public interface SubCategoryRepository extends JpaRepository<SubCategoryEntity, Integer> {
	@Query("SELECT s FROM SubCategoryEntity s WHERE s.category.name = :categoryName")
    Page<SubCategoryEntity> findByCategoryName(String categoryName, Pageable pageable);
	
}
