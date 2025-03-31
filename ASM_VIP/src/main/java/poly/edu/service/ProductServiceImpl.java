package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.DTO.ProductDTO;
import poly.edu.config.FlashSaleManager;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.DiscountDetailEntity;
import poly.edu.entity.DiscountEntity;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.repository.DiscountDetailRepository;
import poly.edu.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SubCategoryService subCategoryService;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

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
        if (product.getId() > 0) {
            Optional<ProductEntity> existingProduct = productRepository.findById(product.getId());
            if (existingProduct.isPresent() && product.getImage() == null) {
                product.setImage(existingProduct.get().getImage());
            }
        }
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
        return productRepository.findSearchAll(name, pageable);
    }

    @Override
    public Page<ProductDTO> getFilteredProducts(String search, Integer categoryId, Integer subCategoryId, Integer status, Pageable pageable) {
        Page<ProductEntity> productPage = productRepository.findByFilters(search, categoryId, subCategoryId, status, pageable);

        return productPage.map(product -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setImage(product.getImage());
            productDTO.setPrice(product.getPrice());
            productDTO.setQty(product.getQty());
            productDTO.setDescription(product.getDescription());
            productDTO.setStatus(product.getStatus());
            productDTO.setSubsetSubCategory(product.getSubCategory().getSubCategoriesName());
            applyDiscountToProduct(productDTO, product.getId(), categoryId, subCategoryId);
            return productDTO;
        });
    }

    private void applyDiscountToProduct(ProductDTO productDTO, int productId, Integer categoryId, Integer subCategoryId) {
        List<DiscountDetailEntity> discountDetails = discountDetailRepository.findByProductIdAndStatus(productId, 1);

        if (discountDetails.isEmpty() && categoryId != null) {
            discountDetails = discountDetailRepository.findByCategoryIdAndStatus(categoryId, 1);
        }

        if (discountDetails.isEmpty() && subCategoryId != null) {
            discountDetails = discountDetailRepository.findBySubCategoryIdAndStatus(subCategoryId, 1);
        }

        for (DiscountDetailEntity detail : discountDetails) {
            DiscountEntity discount = detail.getDiscount();
            if (discount != null && discount.isActive()) {
                float discountValue = discount.getDiscountValue();
                productDTO.setDiscountPercentage(discountValue);
                float originalPrice = productDTO.getPrice();
                float discountedPrice = originalPrice - (originalPrice * discountValue / 100);
                productDTO.setDiscountedPrice(discountedPrice);
                break;
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

    @Override
    public String saveProductFromForm(String name, Integer categoryId, Integer subCategoryId, String priceStr, Integer qty,
                                      String description, String status, MultipartFile imageFile, Integer id, String existingImage) {
        try {
            ProductEntity product;
            if (id == null || id == 0) {
                product = new ProductEntity();
            } else {
                product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
            }

            product.setId(id != null ? id : 0);
            product.setName(name);
            product.setQty(qty);
            product.setDescription(description);

            // Xử lý giá
            float price;
            try {
                price = Float.parseFloat(priceStr);
                if (price < 0) {
                    return "Giá phải lớn hơn hoặc bằng 0";
                }
            } catch (NumberFormatException e) {
                return "Giá không hợp lệ";
            }
            product.setPrice(price);

            // Xử lý Category
            Optional<CategoryEntity> category = Optional.ofNullable(categoryService.getCategoryById(categoryId));
            if (!category.isPresent()) {
                return "Loại hàng không hợp lệ";
            }

            // Xử lý SubCategory
            Optional<SubCategoryEntity> subCategory = Optional.ofNullable(subCategoryService.getSubCategoryById(subCategoryId));
            if (!subCategory.isPresent()) {
                return "Hãng không hợp lệ";
            }
            if (subCategory.get().getCategory().getId() != categoryId) {
                return "Hãng không thuộc loại hàng đã chọn";
            }
            product.setSubCategory(subCategory.get());

            // Xử lý status
            product.setStatus(status != null && status.equals("1") ? 1 : 0);

            // Xử lý upload ảnh
            try {
                Optional.ofNullable(imageFile)
                        .filter(file -> !file.isEmpty())
                        .ifPresent(file -> {
                            String originalFileName = file.getOriginalFilename();
                            if (originalFileName == null || !originalFileName.contains(".")) {
                                throw new RuntimeException("Tên file không hợp lệ.");
                            }
                            Path targetPath = Paths.get("C:/watch-store/photos", originalFileName);
                            try {
                                Files.createDirectories(targetPath.getParent());
                                if (Files.exists(targetPath)) {
                                    try {
                                        Files.deleteIfExists(targetPath);
                                    } catch (IOException e) {
                                        String newFileName = System.currentTimeMillis() + "_" + originalFileName;
                                        targetPath = Paths.get("C:/watch-store/photos", newFileName);
                                    }
                                }
                                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                                product.setImage(targetPath.getFileName().toString());
                            } catch (IOException e) {
                                throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage(), e);
                            }
                        });

                if (product.getImage() == null) {
                    if (id == null || id == 0) {
                        product.setImage("default.png");
                    } else {
                        if (existingImage == null || existingImage.isEmpty()) {
                            return "Ảnh hiện tại không hợp lệ";
                        }
                        product.setImage(existingImage);
                    }
                }
            } catch (RuntimeException e) {
                return e.getMessage();
            }

            // Lưu sản phẩm
            productRepository.save(product);
            return null; // Thành công, không có lỗi
        } catch (Exception e) {
            return "Lỗi khi lưu sản phẩm: " + e.getMessage();
        }
    }

    @Override
    public String deleteProductById(int id) {
        try {
            productRepository.deleteById(id);
            return null; // Thành công, không có lỗi
        } catch (Exception e) {
            return "Lỗi khi xóa sản phẩm: " + e.getMessage();
        }
    }
}