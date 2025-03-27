package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.DTO.ProductDTO;
import poly.edu.config.FlashSaleManager;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.DiscountDetailEntity;
import poly.edu.entity.DiscountEntity;
import poly.edu.entity.ProductEntity;
import poly.edu.repository.DiscountDetailRepository;
import poly.edu.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl1 implements ProductService {
    private final ProductDAO productDAO;

    @Autowired
    public ProductServiceImpl1(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    @Override
    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<ProductEntity> getProductById(int id) {
        return productRepository.findById(id);
    }

    @Override
    public ProductEntity saveProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductEntity> getProductsByName(String name) {
        return productDAO.findByNameContaining(name);
    }

    @Override
    public void addProduct(ProductEntity product) {
        productDAO.save(product);
    }

    @Override
    public void updateProduct(ProductEntity product) {
        productDAO.update(product);
    }

    @Override
    public void removeProduct(int id) {
        productDAO.delete(id);
    }

    @Override
    public List<ProductEntity> bestProducts() {
        Pageable pageable = PageRequest.of(0, 4);
        return productRepository.bestProducts(pageable);
    }

    @Override
    public List<ProductEntity> newProducts() {
        Pageable pageable = PageRequest.of(0, 4);
        return productRepository.newProducts(pageable);
    }

    @Override
    public List<ProductEntity> getSaleProducts() {
        Pageable pageable = PageRequest.of(0, 4);
        return productRepository.findProductsWithActiveDiscounts(pageable);
    }

    @Override
    public Page<CategoryRevenueDTO> getCategoryRevenue(Pageable pageable) {
        return productRepository.getCategoryRevenue(pageable);
    }

    @Override
    public Page<ProductEntity> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<ProductEntity> searchProducts(String search, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrSubCategory_Category_NameContainingIgnoreCaseOrSubCategory_SubCategoriesNameContainingIgnoreCase(search, search, search, pageable);
    }

    @Override
    public Page<ProductEntity> findSearchAll(String name, Pageable pageable) {
        String searchPattern = "%" + name + "%";
        return productRepository.findSearchAll(searchPattern, pageable);
    }

    @Override
    public Page<ProductDTO> getFilteredProducts(String search, Integer categoryId, Integer subCategoryId, Integer status, Pageable pageable) {
        Page<ProductEntity> productPage = productRepository.findByFilters(search, categoryId, subCategoryId, status, pageable);

        // Ánh xạ ProductEntity sang ProductDTO và áp dụng giảm giá
        return productPage.map(product -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());

            // Áp dụng giảm giá
            applyDiscountToProduct(productDTO, product.getId(), categoryId, subCategoryId);

            return productDTO;
        });
    }

    private void applyDiscountToProduct(ProductDTO productDTO, int productId, Integer categoryId, Integer subCategoryId) {
        // Tìm các DiscountDetail áp dụng cho sản phẩm, danh mục, hoặc danh mục con
        List<DiscountDetailEntity> discountDetails = discountDetailRepository.findByProductIdAndStatus(productId, 1);

        if (discountDetails.isEmpty() && categoryId != null) {
            discountDetails = discountDetailRepository.findByCategoryIdAndStatus(categoryId, 1);
        }

        if (discountDetails.isEmpty() && subCategoryId != null) {
            discountDetails = discountDetailRepository.findBySubCategoryIdAndStatus(subCategoryId, 1);
        }

        // Tìm chương trình giảm giá đang hoạt động
        for (DiscountDetailEntity detail : discountDetails) {
            DiscountEntity discount = detail.getDiscount();
            if (discount != null && discount.isActive()) {
                float discountValue = discount.getDiscountValue();
                productDTO.setDiscountPercentage(discountValue);
                float originalPrice = productDTO.getPrice();
                float discountedPrice = originalPrice - (originalPrice * discountValue / 100);
                productDTO.setDiscountedPrice(discountedPrice);
                break; // Chỉ áp dụng chương trình giảm giá đầu tiên
            }
        }
    }

    @Override
    public boolean isFlashSaleActive() {
        return FlashSaleManager.isFlashSaleActive();
    }

    @Override
    public LocalDateTime getFlashSaleEndTime() {
        return FlashSaleManager.getEndTime();
    }
}