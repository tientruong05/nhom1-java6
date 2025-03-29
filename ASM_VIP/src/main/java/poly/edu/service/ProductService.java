package poly.edu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.DTO.ProductDTO;
import poly.edu.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductEntity> getAllProducts();
    Optional<ProductEntity> getProductById(int id);
    ProductEntity saveProduct(ProductEntity product);
    void deleteProduct(int id);
    List<ProductEntity> getProductsByName(String name);
    void addProduct(ProductEntity product);
    void updateProduct(ProductEntity product);
    void removeProduct(int id);
    List<ProductEntity> bestProducts();
    List<ProductEntity> newProducts();
    List<ProductEntity> getSaleProducts();
    Page<CategoryRevenueDTO> getCategoryRevenue(Pageable pageable);
    Page<ProductEntity> getAllProducts(Pageable pageable);
    Page<ProductEntity> searchProducts(String search, Pageable pageable);
    Page<ProductEntity> findSearchAll(String name, Pageable pageable);
    Page<ProductDTO> getFilteredProducts(String search, Integer categoryId, Integer subCategoryId, Integer status, Pageable pageable);
    boolean isFlashSaleActive();
    LocalDateTime getFlashSaleEndTime();

    String saveProductFromForm(String name, Integer categoryId, Integer subCategoryId, String priceStr, Integer qty,
                               String description, String status, MultipartFile imageFile, Integer id, String existingImage);
    String deleteProductById(int id);
}