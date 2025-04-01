package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.entity.ProductEntity;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    List<ProductEntity> findBySubCategoryId(int subCategoryId);

    @Query(value = "SELECT p.* FROM products p ORDER BY p.qty DESC", nativeQuery = true)
    List<ProductEntity> bestProducts(Pageable pageable);

    @Query(value = "SELECT p.* FROM products p ORDER BY p.id DESC", nativeQuery = true)
    List<ProductEntity> newProducts(Pageable pageable);

    @Query(value = "SELECT p.* FROM products p ORDER BY p.discount DESC", nativeQuery = true)
    List<ProductEntity> saleProducts(Pageable pageable);

    @Query(value = "SELECT \r\n"
            + "    c.name AS categoryName, \r\n"
            + "    sc.sub_categories_name AS subCategoryName, \r\n"
            + "    COALESCE(SUM(od.qty * od.price), 0) AS totalRevenue, \r\n"
            + "    COALESCE(SUM(od.qty), 0) AS totalQty, \r\n"
            + "    COALESCE(MAX(od.price), 0) AS maxPrice, \r\n"
            + "    COALESCE(MIN(od.price), 0) AS minPrice, \r\n"
            + "    COALESCE(AVG(od.price), 0) AS avgPrice\r\n"
            + "FROM categories c\r\n"
            + "JOIN sub_categories sc ON c.id = sc.id_categories\r\n"
            + "JOIN products p ON sc.id = p.id_subcategories\r\n"
            + "JOIN order_detail od ON p.id = od.product_id\r\n"
            + "GROUP BY c.name, sc.sub_categories_name\r\n"
            + "ORDER BY totalRevenue DESC",
            nativeQuery = true)
    Page<CategoryRevenueDTO> getCategoryRevenue(Pageable pageable);

    Page<ProductEntity> findByNameContainingIgnoreCaseOrSubCategory_Category_NameContainingIgnoreCaseOrSubCategory_SubCategoriesNameContainingIgnoreCase(String name, String categoryName, String subcategoryName, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.name LIKE CONCAT('%', ?1, '%') AND p.status = 1")
    Page<ProductEntity> findSearchAll(String name, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p " +
           "WHERE (:search IS NULL OR " +
           "       p.name LIKE CONCAT('%', :search, '%') OR " +
           "       p.subCategory.category.name LIKE CONCAT('%', :search, '%') OR " +
           "       p.subCategory.subCategoriesName LIKE CONCAT('%', :search, '%')) " +
           "AND (:categoryId IS NULL OR p.subCategory.category.id = :categoryId) " +
           "AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<ProductEntity> findByFilters(
            @Param("search") String search,
            @Param("categoryId") Integer categoryId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("status") Integer status, // Đổi từ String sang Integer
            Pageable pageable);
    
 // Truy vấn sản phẩm có discount đang hoạt động
    @Query("SELECT p FROM ProductEntity p " +
           "JOIN p.discountDetails dd " +
           "JOIN dd.discount d " +
           "WHERE d.status = 1 AND dd.status = 1 " +
           "AND d.startDate <= CURRENT_DATE AND d.endDate >= CURRENT_DATE")
    List<ProductEntity> findProductsWithActiveDiscounts(Pageable pageable);
    
}