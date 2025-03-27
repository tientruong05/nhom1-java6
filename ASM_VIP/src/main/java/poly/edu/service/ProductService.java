package poly.edu.service;

import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.DTO.ProductDTO;
import poly.edu.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Optional<ProductEntity> getProductById(int id);
    List<ProductEntity> getProductsByName(String name);
    List<ProductEntity> getAllProducts();
    void addProduct(ProductEntity product);
    void updateProduct(ProductEntity product);
    void removeProduct(int id);

    ProductEntity saveProduct(ProductEntity product);
    void deleteProduct(int id);
    List<ProductEntity> newProducts();
    List<ProductEntity> bestProducts();
    List<ProductEntity> getSaleProducts();
    boolean isFlashSaleActive();
    LocalDateTime getFlashSaleEndTime();
    Page<ProductEntity> getAllProducts(Pageable pageable);
    Page<ProductEntity> searchProducts(String search, Pageable pageable);
    Page<CategoryRevenueDTO> getCategoryRevenue(Pageable pageable);
    
    Page<ProductEntity> findSearchAll(String name, Pageable pageable);

    // Sửa kiểu trả về từ Page<ProductEntity> sang Page<ProductDTO>
    Page<ProductDTO> getFilteredProducts(String search, Integer categoryId, Integer subCategoryId, Integer status, Pageable pageable);
}